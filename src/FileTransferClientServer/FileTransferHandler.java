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

import FileTransfer.*;

import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.HashMap;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Long Le <longlnt@vng.com.vn>
 */
public class FileTransferHandler implements FileTransfer.Iface {

    private final HashMap<String, ArrayList<DataChunk>> dataStore;
    private final HashMap<String, Metadata> headerList;

    public FileTransferHandler() {
        this.dataStore = new HashMap();
        this.headerList = new HashMap();
    }

    @Override
    public void sendMetaData(Metadata header) throws TException {
        System.out.println("Received header of src file: " + header.srcPath);
        this.headerList.put(header.srcPath, header);
        this.dataStore.put(header.srcPath, new ArrayList());
    }

    /**
     * Override sendDataChunk for the Handler
     * @param chunk the DataChunk
     * @throws TException 
     */
    @Override
    public void sendDataChunk(DataChunk chunk) throws TException {
        // Check whether the data chunk is sent before the metadata
        if (!this.dataStore.containsKey(chunk.srcPath)) {
            System.err.println("Metadata hasn't arrived");
        }

        boolean append = false; // Disable appending mode of FileOutputStream
        try {
            System.out.println("Received the chunk with offset: " + chunk.offset);
            this.dataStore.get(chunk.srcPath).add(chunk);

            // Get the buffer size of avaiable data of the file
            System.out.println("Begin check sum");
            int bufferSize = 0;
            for (int i = 0; i < this.dataStore.get(chunk.srcPath).size(); ++i) {
                ByteBuffer tmpBuffer = this.dataStore.get(chunk.srcPath).get(i).buffer;
                bufferSize += tmpBuffer.limit() - tmpBuffer.position();
            }
            
            // Check sum of the file
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            if (this.checkSum(chunk, buffer)) {
                // save to file
                System.out.println("Sum checked");
                File file = new File(this.headerList.get(chunk.srcPath).desPath);
                try (FileChannel writeChannel = new FileOutputStream(file, append).getChannel()) {
                    System.out.println(buffer.toString());
                    writeChannel.write(buffer);
                    System.out.println("Wrote to file: " + this.headerList.get(chunk.srcPath).desPath);
                }
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check sum of the file given a new data chunk
     * @param chunk new arriving data chunk
     * @param buffer ByteBuffer of the file
     * @return boolean: true if match the checksum in the metadata, otherwise, false
     * @throws NoSuchAlgorithmException 
     */
    public boolean checkSum(DataChunk chunk, ByteBuffer buffer) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        ArrayList<DataChunk> chunkArray = this.dataStore.get(chunk.srcPath);

        // Transfer bytes from sent data chunks into the ByteBuffer
        for (int i = 0; i < chunkArray.size(); ++i) {
            // Initialize the byte array to concatenation
            int chunkBufferSize = chunkArray.get(i).buffer.limit() - chunkArray.get(i).buffer.position();
            byte[] byteArray = new byte[chunkBufferSize];
            chunkArray.get(i).buffer.get(byteArray, 0, chunkBufferSize);
            
            // Transfer bytes to the result buffer
            buffer.position(fileTransferConstants.CHUNK_MAX_SIZE * chunkArray.get(i).offset);
            buffer.put(byteArray, 0, chunkBufferSize);
            
            // Reset the position of the ith chunk's ByteBuffer 
            chunkArray.get(i).buffer.position(chunkArray.get(i).buffer.position() - chunkBufferSize);
        }

        if (buffer.position() < buffer.limit()) {
            buffer.limit(buffer.position());
        }
        buffer.rewind();

        // Convert to String
        byte[] mdByteArray = md.digest(buffer.array());
        StringBuilder builder = new StringBuilder("");
        for (int i = 0; i < mdByteArray.length; i++) {
            builder.append(Integer.toString((mdByteArray[i] & 0xff) + 0x100, 16).substring(1));
        }
        
        return builder.toString().equals(this.headerList.get(chunk.srcPath).checkSum);
    }
}
