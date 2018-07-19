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
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Long Le <longlnt@vng.com.vn>
 */
public class FileTransferHandler implements FileTransfer.Iface {

    // The storage for files' metadata
    private final HashMap<String, Metadata> headerList;

    // The storage for path names of data chunks on disk
    private final HashMap<String, Integer> countRecords;

    public FileTransferHandler() {
        this.headerList = new HashMap();
        this.countRecords = new HashMap();
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
            try {
                this.headerList.put(header.srcPath, header);
                this.countRecords.put(header.srcPath, 0);
                RandomAccessFile writer = new RandomAccessFile(header.desPath, "rw");
                writer.setLength(header.size);
            } catch (IOException ex) {
                Logger.getLogger(FileTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Override sendDataChunk for the Handler, save the chunk to disk
     *
     * @param chunk the DataChunk
     * @throws TException
     */
    @Override
    public void sendDataChunk(DataChunk chunk) throws TException {
        if (!this.headerList.containsKey(chunk.srcPath)) {
            return;
        }

        try {
            this.countRecords.put(chunk.srcPath, this.countRecords.get(chunk.srcPath) + 1);
            this.writeToFile(chunk.srcPath, chunk.buffer, chunk.offset);
        } catch (IOException ex) {
            Logger.getLogger(FileTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    }

    /**
     * Write the buffer to file
     *
     * @param srcPath
     * @param buffer bytes to be written
     * @param offset
     * @throws IOException
     */
    public void writeToFile(String srcPath, ByteBuffer buffer, int offset) throws IOException {
        // Write output to file
        try (RandomAccessFile writer = new RandomAccessFile(this.headerList.get(srcPath).desPath, "rw")) {
            writer.seek(offset * fileTransferConstants.CHUNK_MAX_SIZE);
            System.out.println(writer.getFilePointer());
            writer.write(buffer.array());
        }

        // Delete the data related to the file on disk and memory if completed
        if (this.countRecords.get(srcPath) == this.headerList.get(srcPath).numOfChunks) {
            try {
                if (this.checkSum(srcPath)) {
                    System.out.println(" [x] Write to file: " + this.headerList.get(srcPath).desPath);
                    this.headerList.remove(srcPath);
                } else {
                    File file = new File(this.headerList.get(srcPath).desPath);
                    file.delete();
                    System.err.println(" [ERROR] Writing to " + this.headerList.get(srcPath).desPath + " failed.");
                }
            } catch (IOException ex) {
                Logger.getLogger(FileTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Check sum of the file given a new data chunk
     *
     * @param srcPath file source path
     * @return boolean: true if match the checksum in the metadata, otherwise,
     * false
     * @throws java.io.IOException
     */
    public boolean checkSum(String srcPath) throws IOException {
        Adler32 checkSumGen = new Adler32();
        int numOfChunks = this.headerList.get(srcPath).numOfChunks;
        File desFile = new File(this.headerList.get(srcPath).desPath);

        try (FileChannel readChannel = new FileInputStream(desFile).getChannel()) {
            for (int i = 0; i < numOfChunks; ++i) {
                ByteBuffer byteChunk;
                if ((readChannel.size() - readChannel.position()) < fileTransferConstants.CHUNK_MAX_SIZE) {
                    byteChunk = ByteBuffer.allocate((int) (readChannel.size() - readChannel.position()));
                } else {
                    byteChunk = ByteBuffer.allocate(fileTransferConstants.CHUNK_MAX_SIZE);
                }
                readChannel.read(byteChunk);

                // Get rid of unused bytes
                if (byteChunk.position() < byteChunk.limit()) {
                    byteChunk.limit(byteChunk.position());
                }
                byteChunk.rewind();

                // Update the checksum
                checkSumGen.update(byteChunk);
                byteChunk.rewind();
            }
        }

        return checkSumGen.getValue() == this.headerList.get(srcPath).checkSum;
    }
}
