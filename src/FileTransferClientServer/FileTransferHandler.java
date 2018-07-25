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

import FileTransfer.DataChunk;
import FileTransfer.Metadata;
import FileTransfer.fileTransferConstants;
import FileTransfer.FileTransfer;

import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Adler32;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
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
     * @return
     * @throws TException
     */
    @Override
    public List<List<Long>> sendMetaData(Metadata header) throws TException {
        System.out.println("Received header of src file: " + header.srcPath);
        // Add the metadata to the list
        if (!this.headerList.containsKey(header.srcPath)) {
            this.headerList.put(header.srcPath, header);
            this.countRecords.put(header.srcPath, 0);
        }
        File desFile = new File(header.desPath);
        List<List<Long>> checksumList = new ArrayList();

        // Check if the destination file already exists
        if (!desFile.exists()) {
            // If not, create a new file
            try {
                RandomAccessFile writer = new RandomAccessFile(header.desPath, "rw");
                writer.setLength(header.size);
            } catch (IOException ex) {
                Logger.getLogger(FileTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return checksumList;
        } else {
            // If exist, return the HashMap contains checksums of chunks obtained by splitting the destination file
            // list[i][0]: offset where the i-th chunk starts
            // list[i][1]: checksum of bytes in the i-th chunk
            try {
                Adler32 checksumGen = new Adler32();
                FileChannel reader = new RandomAccessFile(desFile, "r").getChannel();
                long offset = 0;
                while (offset < reader.size()) {
                    // Read bytes of the chunk into the buffer
                    ByteBuffer byteChunk = ByteBuffer.allocate(fileTransferConstants.CHUNK_MAX_SIZE);
                    reader.read(byteChunk);

                    // Get rid of unused bytes
                    if (byteChunk.position() < byteChunk.limit()) {
                        byteChunk.limit(byteChunk.position());
                    }
                    byteChunk.rewind();

                    // Generate the checksum for the chunk
                    checksumGen.update(byteChunk);

                    // Add offset and checksum to the list
                    ArrayList<Long> checksumChunk = new ArrayList();
                    checksumChunk.add(0, offset);
                    checksumChunk.add(1, checksumGen.getValue());
                    checksumList.add(checksumChunk);
                    
                    checksumGen.reset();
                    offset = reader.position();
                }
            } catch (IOException ex) {
                Logger.getLogger(FileTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return checksumList;
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
     * Override the method, handle updating the checksum of the file and then write to file
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
        
        // Check whether all chunks have been received
        if (this.countRecords.get(srcPath) == this.headerList.get(srcPath).numOfChunks) {
            try {
                // Calculate checksum for the destination file 
                if (this.checkSum(srcPath)) {
                    // Checksum SUCCEEDED, keep the file, remove the metadata from memory
                    System.out.println(" [x] Write to file: " + this.headerList.get(srcPath).desPath);
                    this.headerList.remove(srcPath);
                } else {
                    // Checksum FAILED, delete the file, error comes out
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
     * Write the buffer to file
     *
     * @param srcPath
     * @param buffer bytes to be written
     * @param offset
     * @throws IOException
     */
    public void writeToFile(String srcPath, ByteBuffer buffer, long offset) throws IOException {
        // Write output to file
        try (RandomAccessFile writer = new RandomAccessFile(this.headerList.get(srcPath).desPath, "rw")) {
            writer.seek(offset);
            writer.write(buffer.array(), buffer.position(), buffer.limit() - buffer.position());
            writer.close();
        }
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
            writer.write(buffer.array(), buffer.position(), buffer.limit() - buffer.position());
	}
    }

    /**
     * Check sum of the file given a new data chunk
     *
     * @param srcPath file source path
     * @return boolean: true if match the checksum in the metadata, otherwise, false
     * @throws java.io.IOException
     */
    public boolean checkSum(String srcPath) throws IOException {
        Adler32 checkSumGen = new Adler32();
        File desFile = new File(this.headerList.get(srcPath).desPath);

        // Update the checksum chunk by chunk to avoid the out of memory situation
        try (FileChannel readChannel = new RandomAccessFile(desFile, "r").getChannel()) {
            long offset = 0;
            while (offset < this.headerList.get(srcPath).size) {
                ByteBuffer byteChunk = ByteBuffer.allocate(fileTransferConstants.CHUNK_MAX_SIZE);
                readChannel.read(byteChunk);

                // Get rid of unused bytes
                if (byteChunk.position() < byteChunk.limit()) {
                    byteChunk.limit(byteChunk.position());
                }
                byteChunk.rewind();

                // Update the checksum
                checkSumGen.update(byteChunk);

                // Update the offset
                offset = readChannel.position();
            }
        }

        return checkSumGen.getValue() == this.headerList.get(srcPath).checkSum;
    }
}
