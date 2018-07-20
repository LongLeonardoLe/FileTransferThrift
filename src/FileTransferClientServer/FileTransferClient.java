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

import java.io.FileInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
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

    /**
     * Take source path, destination path, obtain the metadata and send it first, then parse the
     * file for small chunk of 2048 bytes maximum and send them.
     *
     * @param client
     * @param srcPath path to the source file
     * @param desPath path to the destination file
     * @throws TException
     */
    public static void sendFile(FileTransfer.Client client, String srcPath, String desPath) throws TException {
        File inputFile = new File(srcPath);
        long numberOfChunks = inputFile.length() / fileTransferConstants.CHUNK_MAX_SIZE;
        Metadata header = new Metadata(srcPath, desPath, 0, numberOfChunks, inputFile.length());
        List<List<Long>> checksumList = client.sendMetaData(header);
        if (checksumList == null) {
            sendWholeFile(client, srcPath, desPath);
        } else {
            
        }
    }
    
    public static void sendPartialFile(FileTransfer.Client client, String srcPath, String desPath, List<List<Long>> checksumList) throws TException {
        Adler32 checksumGen = new Adler32();
            try (FileChannel reader = new RandomAccessFile(srcPath, "r").getChannel()) {
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
                    
                    checksumGen.update(byteChunk);
                    if (checksumGen.getValue() != checksumList.get(i).get(1)) {
                        byteChunk.rewind();
                        DataChunk chunk = new DataChunk(srcPath, byteChunk, offset);
                        client.sendDataChunk(chunk);
                    }
                    
                    checksumGen.reset();
                }
            } catch (IOException ex) {
                Logger.getLogger(FileTransferClient.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    public static void sendWholeFile(FileTransfer.Client client, String srcPath, String desPath) throws TException {
        Adler32 checksumGen = new Adler32();

        // Try to open and read the file 
        try (FileChannel reader = new RandomAccessFile(srcPath, "r").getChannel()) {
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
                DataChunk chunk = new DataChunk(srcPath, byteChunk, offset);
                client.sendDataChunk(chunk);
                offset = reader.position();
            } while (offset < reader.size());
            client.updateChecksum(srcPath, checksumGen.getValue());
            reader.close();

        } catch (IOException ex) {
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
    
    public static void main(String[] argv) throws IOException {
        TTransport transport;
        transport = new TFramedTransport(new TSocket("localhost", 9000));
        TProtocol protocol = new TBinaryProtocol(transport);
        FileTransfer.Client client = new FileTransfer.Client(protocol);
        try {
            transport.open();
            sendFile(client, "/home/cpu10360/Desktop/102flowers.tgz", "/home/cpu10360/Desktop/test.tgz");

        } catch (TException ex) {
            Logger.getLogger(FileTransferClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
