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
import java.util.zip.Adler32;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    /**
     * Override the method, handle the sent metadata
     *
     * @param header
     * @throws TException
     */
    @Override
    public void sendMetaData(Metadata header) throws TException {
        System.out.println("Received header of src file: " + header.srcPath);

        // Add the metadata to the list
        if (!this.headerList.containsKey(header.srcPath)) {
            this.headerList.put(header.srcPath, header);
        }

        // If the metadata comes before data chunks, initial the list for future data chunks.
        if (!this.dataStore.containsKey(header.srcPath)) {
            this.dataStore.put(header.srcPath, new ArrayList());
        }
    }

    /**
     * Override sendDataChunk for the Handler
     *
     * @param chunk the DataChunk
     * @throws TException
     */
    @Override
    public void sendDataChunk(DataChunk chunk) throws TException {
        // Screw the metadata, the data chunk is added anyway.
        // However, these chunks won't integrate without the metadata
        if (!this.headerList.containsKey(chunk.srcPath)) {
            if (!this.dataStore.containsKey(chunk.srcPath)) {
                ArrayList<DataChunk> array = new ArrayList();
                array.add(chunk);
                this.dataStore.put(chunk.srcPath, array);
                return;
            } else {
                this.dataStore.get(chunk.srcPath).add(chunk);
                return;
            }
        }
        // Add the new chunk to the list
        this.dataStore.get(chunk.srcPath).add(chunk);
    }

    /**
     * Override the method, handle updating the checksum of the file and then
     * write to file
     *
     * @param srcPath
     * @param checkSum
     * @throws TException
     */
    @Override
    public void updateChecksum(String srcPath, long checkSum) throws TException {
        if (!this.headerList.containsKey(srcPath)) {
            return;
        }
        this.headerList.get(srcPath).checkSum = checkSum;
        boolean append = false;
        ByteBuffer buffer = ByteBuffer.allocate(this.dataStore.get(srcPath).size() * fileTransferConstants.CHUNK_MAX_SIZE);
        try {
            if (this.checkSum(srcPath, buffer)) {
                this.writeToFile(srcPath, buffer, append);

            }
        } catch (NoSuchAlgorithmException | IOException ex) {
            Logger.getLogger(FileTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write the buffer to file
     *
     * @param srcPath the origin of being-written file
     * @param buffer bytes to be written
     * @param append appending mode
     * @throws IOException
     */
    public void writeToFile(String srcPath, ByteBuffer buffer, boolean append) throws IOException {
        File file = new File(this.headerList.get(srcPath).desPath);
        FileChannel writeChannel = new FileOutputStream(file, append).getChannel();
        writeChannel.write(buffer);
        System.out.println(" [x] Wrote to file: " + this.headerList.get(srcPath).desPath);
        this.headerList.remove(srcPath);
        this.dataStore.remove(srcPath);
    }

    /**
     * Check sum of the file given a new data chunk
     *
     * @param srcPath file source path
     * @param buffer ByteBuffer of the file
     * @return boolean: true if match the checksum in the metadata, otherwise,
     * false
     * @throws NoSuchAlgorithmException
     */
    public boolean checkSum(String srcPath, ByteBuffer buffer) throws NoSuchAlgorithmException {
        Adler32 checkSumGen = new Adler32();
        ArrayList<DataChunk> chunkArray = this.dataStore.get(srcPath);

        // Transfer bytes from sent data chunks into the ByteBuffer
        for (int i = 0; i < chunkArray.size(); ++i) {
            int chunkBufferSize = chunkArray.get(i).buffer.limit() - chunkArray.get(i).buffer.position();
            buffer.position(fileTransferConstants.CHUNK_MAX_SIZE * chunkArray.get(i).offset);
            buffer.put(chunkArray.get(i).buffer.array(), chunkArray.get(i).buffer.position(), chunkBufferSize);
            // Update the checksum
            checkSumGen.update(chunkArray.get(i).buffer.array(), chunkArray.get(i).buffer.position(), chunkBufferSize);
        }

        if (buffer.position() < buffer.limit()) {
            buffer.limit(buffer.position());
        }
        buffer.rewind();

        return checkSumGen.getValue() == this.headerList.get(srcPath).checkSum;
    }
}
