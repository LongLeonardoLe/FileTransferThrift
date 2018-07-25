/*
 * The MIT License
 *
 * Copyright 2018 Long Le <longlnt@vng.com.vn>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package FileTransferClientServer;

import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;

import FileTransfer.*;
import java.io.IOException;
import org.apache.thrift.TException;

/**
 *
 * @author Long Le <longlnt@vng.com.vn>
 */
public class FileTransferClient {

    // The bandwidth limit for I/O and network transport
    private static int bwlimit = 10485760;

    /**
     * Take source path, destination path, obtain the metadata and send it first, then parse the
     * file to small data chunks
     *
     * @param client
     * @param srcPath path to the source file
     * @param desPath path to the destination file
     * @param isBwlimitSet
     * @throws TException
     */
    public static void sendFile(FileTransfer.Client client, String srcPath, String desPath, boolean isBwlimitSet) throws TException {
        File srcFile = new File(srcPath);
        File desFile = new File(desPath);
        // Check if the destination file already exists
        if (desFile.exists()) {
            // Case YES
            // check if the destination and source files have the same size
            // If so, compare checksum of chunks in both files
            long numberOfChunks = srcFile.length() / fileTransferConstants.CHUNK_MAX_SIZE + 1;
            Metadata header = new Metadata(srcPath, desPath, 0, numberOfChunks, srcFile.length());
            List<List<Long>> checksumList = client.sendMetaData(header);
            if (desFile.length() == srcFile.length()) {
                if (checksumList.isEmpty()) {
                    sendWholeFile(client, header, isBwlimitSet);
                } else {
                    sendPartialFile(client, header, checksumList, isBwlimitSet);
                }
            } else {
                sendWholeFile(client, header, isBwlimitSet);
            }
        } else {
            // Case NO, send the whole file
            long numberOfChunks = srcFile.length() / fileTransferConstants.CHUNK_MAX_SIZE + 1;
            Metadata header = new Metadata(srcPath, desPath, 0, numberOfChunks, srcFile.length());
            client.sendMetaData(header);
            sendWholeFile(client, header, isBwlimitSet);
        }
    }

    public static void sendPartialFile(FileTransfer.Client client, Metadata header, List<List<Long>> checksumList, boolean isBwlimitSet) throws TException {
        Adler32 checksumGen = new Adler32();
        Adler32 totalChecksum = new Adler32();
        long count = 0;
        int outputBufferSize = 0;
        long time = System.currentTimeMillis();

        try (FileChannel reader = new RandomAccessFile(header.srcPath, "r").getChannel()) {
            long numberOfChunks = reader.size() / fileTransferConstants.CHUNK_MAX_SIZE + 1;
            for (int i = 0; i < checksumList.size(); ++i) {
                long offset = checksumList.get(i).get(0);
                reader.position(offset);
                ByteBuffer byteChunk = ByteBuffer.allocate(fileTransferConstants.CHUNK_MAX_SIZE);
                reader.read(byteChunk);

                // Get rid of unused bytes
                if (byteChunk.position() < byteChunk.limit()) {
                    byteChunk.limit(byteChunk.position());
                }
                byteChunk.rewind();

                // Calculate the checksum of the chunk, if different, send the chunk, otherwise skip
                checksumGen.update(byteChunk);
                byteChunk.rewind();
                totalChecksum.update(byteChunk);
                if (checksumGen.getValue() != checksumList.get(i).get(1)) {
                    byteChunk.rewind();
                    DataChunk chunk = new DataChunk(header.srcPath, byteChunk, offset);
                    client.sendDataChunk(chunk);
                    count++;
                }

                // Check whether it exceeds the limit of bytes in 1 s
                if (isBwlimitSet) {
                    outputBufferSize += byteChunk.array().length;
                    if (outputBufferSize >= bwlimit) {
                        long timespan = System.currentTimeMillis() - time;
                        if (timespan < 1000) {
                            outputBufferSize = 0;
                            Thread.sleep(1000 - timespan);
                        }
                        time = System.currentTimeMillis();
                    }
                }

                checksumGen.reset();
            }
            client.updateChecksum(header.srcPath, totalChecksum.getValue());
            System.out.println("Sent " + count + "/" + numberOfChunks + " chunks.");
            reader.close();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(FileTransferClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void sendWholeFile(FileTransfer.Client client, Metadata header, boolean isBwlimitSet) throws TException {
        long time = System.currentTimeMillis();
        int outputBufferSize = 0;
        Adler32 checksumGen = new Adler32();

        // Try to open and read the file 
        try (FileChannel reader = new RandomAccessFile(header.srcPath, "r").getChannel()) {
            long offset = 0;
            // Parse the file into smaller chunks and send them
            do {
                // Allocate the ByteBuffer to which bytes will be transferred to
                ByteBuffer byteChunk = ByteBuffer.allocate(fileTransferConstants.CHUNK_MAX_SIZE);
                reader.read(byteChunk);

                // Get rid of unused bytes
                if (byteChunk.position() < byteChunk.limit()) {
                    byteChunk.limit(byteChunk.position());
                }
                byteChunk.rewind();

                // Update the checksum
                checksumGen.update(byteChunk);
                byteChunk.rewind();

                // Create a data chunk and send it
                DataChunk chunk = new DataChunk(header.srcPath, byteChunk, offset);
                client.sendDataChunk(chunk);

                // Check whether it exceeds the limit of bytes in 1 s
                if (isBwlimitSet) {
                    outputBufferSize += byteChunk.array().length;
                    if (outputBufferSize >= bwlimit) {
                        long timespan = System.currentTimeMillis() - time;
                        if (timespan < 1000) {
                            outputBufferSize = 0;
                            Thread.sleep(1000 - timespan);
                        }
                        time = System.currentTimeMillis();
                    }
                }

                offset = reader.position();
            } while (offset < reader.size());
            client.updateChecksum(header.srcPath, checksumGen.getValue());
            reader.close();

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(FileTransferClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // To work with multi-threaded, uncomment the code block
    /*public static Thread createThread(int port, String dirPath, File[] paths) {
        return new Thread(() -> {
            TTransport transport;
            transport = new TFramedTransport(new TSocket("localhost", port));
            TProtocol protocol = new TBinaryProtocol(transport);
            FileTransfer.Client client = new FileTransfer.Client(protocol);
            try {
                transport.open();
                for (int i = 0; i < paths.length; ++i) {
                    String srcPath = new StringBuilder().append(dirPath).append('/').append(paths[i].getName()).toString();
                    String desPath = new StringBuilder().append("/home/cpu10360/Desktop/").append("des/").append(paths[i].getName()).toString();
                    sendFile(client, srcPath, desPath);
                }
            } catch (TException ex) {
                Logger.getLogger(FileTransferClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }*/
    public static void main(String[] args) throws IOException {
        TTransport transport;
        transport = new TFramedTransport(new TSocket("localhost", 9000));
        TProtocol protocol = new TBinaryProtocol(transport);
        FileTransfer.Client client = new FileTransfer.Client(protocol);
        boolean isBwlimitSet = false;
        for (int i = 0; i < args.length; ++i) {
            if (args[i].contains("--bwlimit=")) {
                int limit = Integer.parseInt(args[i].substring(args[i].indexOf("=")+1));
                if (limit == 0) {
                    break;
                }
                isBwlimitSet = true;
                bwlimit = limit * 1024;
            }
        }
        try {
            transport.open();
            sendFile(client, "/home/cpu10360/Desktop/102flowers.tgz", "/home/cpu10360/Desktop/test.tgz", isBwlimitSet);

        } catch (TException ex) {
            Logger.getLogger(FileTransferClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
