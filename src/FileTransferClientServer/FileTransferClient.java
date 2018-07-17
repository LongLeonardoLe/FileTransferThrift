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
import java.security.MessageDigest;

import FileTransfer.*;
import org.apache.thrift.transport.TTransportException;

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
     */
    public static void sendFile(FileTransfer.Client client, String srcPath, String desPath) {
        File inputFile = new File(srcPath);
        int offset = 0;
        // Obtain the number of data chunks from the file
        int numberOfChunks = (int) (inputFile.length() / (long) fileTransferConstants.CHUNK_MAX_SIZE) + 1;

        // Try to open and read the file 
        try (FileChannel readChannel = new FileInputStream(inputFile).getChannel()) {
            // Read the whole file
            ByteBuffer buffer = ByteBuffer.allocate((int) inputFile.length());
            readChannel.read(buffer);
            buffer.limit(buffer.position());
            buffer.rewind();

            // Hash the bytes with SHA1, use as checksum
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] mdByteArray = md.digest(buffer.array());
            StringBuilder builder = new StringBuilder("");
            for (int i = 0; i < mdByteArray.length; i++) {
                builder.append(Integer.toString((mdByteArray[i] & 0xff) + 0x100, 16).substring(1));
            }

            // Create the metadata and send it
            Metadata fileMeta = new Metadata(srcPath, desPath, builder.toString());
            client.sendMetaData(fileMeta);

            // Parse the file into smaller chunks and send them
            byte[] bufferByteArray = buffer.array();
            while (offset < numberOfChunks) {
                ByteBuffer byteChunk;
                if ((buffer.limit() - offset * fileTransferConstants.CHUNK_MAX_SIZE) < fileTransferConstants.CHUNK_MAX_SIZE) {
                    int subSize = buffer.limit() - offset * fileTransferConstants.CHUNK_MAX_SIZE;
                    byteChunk = ByteBuffer.allocate(subSize);
                    byteChunk.put(bufferByteArray, offset * fileTransferConstants.CHUNK_MAX_SIZE, subSize);
                } else {
                    byteChunk = ByteBuffer.allocate(fileTransferConstants.CHUNK_MAX_SIZE);
                    byteChunk.put(bufferByteArray, offset * fileTransferConstants.CHUNK_MAX_SIZE, fileTransferConstants.CHUNK_MAX_SIZE);
                }
                
                // Ensure exist no extra unused bytes
                if (byteChunk.position() < byteChunk.limit()) {
                    byteChunk.limit(byteChunk.position());
                }
                byteChunk.rewind();
                
                // Create a data chunk and send it
                DataChunk chunk = new DataChunk(srcPath, byteChunk, offset++);
                client.sendDataChunk(chunk);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] argv) {
        TTransport transport;
        try {
            transport = new TFramedTransport(new TSocket("localhost", 9090));
            TProtocol protocol = new TBinaryProtocol(transport);

            FileTransfer.Client client = new FileTransfer.Client(protocol);
            transport.open();
            sendFile(client, "/home/cpu10360/Desktop/image_LongLe.jpg", "/home/cpu10360/Desktop/test.jpg");
            System.out.println("Started client successfully.");
            transport.close();

        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }
}
