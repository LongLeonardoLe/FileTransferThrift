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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
     * Take source path, destination path, obtain the metadata and send it
     * first, then parse the file for small chunk of 2048 bytes maximum and send
     * them.
     *
     * @param client
     * @param srcPath path to the source file
     * @param desPath path to the destination file
     * @throws TException
     */
    public static void sendFile(FileTransfer.Client client, String srcPath, String desPath) throws TException {
        Adler32 checkSumGen = new Adler32();
        File inputFile = new File(srcPath);
        int offset = 0;
        // Obtain the number of data chunks from the file
        long numberOfChunks = (inputFile.length() / (long) fileTransferConstants.CHUNK_MAX_SIZE) + 1;

        // Try to open and read the file 
        try (FileChannel readChannel = new FileInputStream(inputFile).getChannel()) {
            // Create the metadata and send it
            Metadata fileMeta = new Metadata(srcPath, desPath, 0);
            client.sendMetaData(fileMeta);

            // Parse the file into smaller chunks and send them
            while (offset < numberOfChunks) {
                // Allocate the ByteBuffer to which bytes is transferred to
                ByteBuffer byteChunk;
                if ((readChannel.size() - readChannel.position()) < fileTransferConstants.CHUNK_MAX_SIZE) {
                    byteChunk = ByteBuffer.allocate((int) (readChannel.size() - readChannel.position()));
                } else {
                    byteChunk = ByteBuffer.allocate(fileTransferConstants.CHUNK_MAX_SIZE);
                }
                readChannel.read(byteChunk);

                // Get rid of extra unused bytes if there is any
                if (byteChunk.position() < byteChunk.limit()) {
                    byteChunk.limit(byteChunk.position());
                }
                byteChunk.rewind();

                // Update the checksum
                checkSumGen.update(byteChunk);
                byteChunk.rewind();

                // Create a data chunk and send it
                DataChunk chunk = new DataChunk(srcPath, byteChunk, offset++);
                client.sendDataChunk(chunk);
            }
            client.updateChecksum(srcPath, checkSumGen.getValue());
            readChannel.close();

        } catch (IOException ex) {
            Logger.getLogger(FileTransferClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] argv) throws IOException {
        TTransport transport;
        transport = new TFramedTransport(new TSocket("localhost", 9090));
        TProtocol protocol = new TBinaryProtocol(transport);

        FileTransfer.Client client = new FileTransfer.Client(protocol);
        try {
            transport.open();
            System.out.println("Starting client, sending data...");
            long startTime = System.nanoTime();
            sendFile(client, "/home/cpu10360/Desktop/image.jpg", "/home/cpu10360/Desktop/test.jpg");
            sendFile(client, "/home/cpu10360/Desktop/image1.jpg", "/home/cpu10360/Desktop/test1.jpg");
            sendFile(client, "/home/cpu10360/Desktop/image2.jpg", "/home/cpu10360/Desktop/test2.jpg");
            sendFile(client, "/home/cpu10360/Desktop/image3.jpg", "/home/cpu10360/Desktop/test3.jpg");
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            System.out.println(" [x] Data sent in 0." + duration + " seconds.");
        } catch (TException ex) {
            Logger.getLogger(FileTransferClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        transport.close();
    }
}
