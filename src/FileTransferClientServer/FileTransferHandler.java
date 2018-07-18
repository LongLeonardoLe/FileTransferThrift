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
    private final HashMap<String, ArrayList<String>> fileNameList;

    public FileTransferHandler() {
        this.headerList = new HashMap();
        this.fileNameList = new HashMap();
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
    }

    /**
     * Override sendDataChunk for the Handler, save the chunk to disk
     *
     * @param chunk the DataChunk
     * @throws TException
     */
    @Override
    public void sendDataChunk(DataChunk chunk) throws TException {
        // Initial the fileNameList
        if (!this.fileNameList.containsKey(chunk.srcPath)) {
            this.fileNameList.put(chunk.srcPath, new ArrayList());
        }

        try {
            // Make the directory: ./tempData/<srcPath.hashCode>/
            String folderName = new StringBuilder().append("./tempData/").append(chunk.srcPath.hashCode()).append('/').toString();
            File folder = new File(folderName);
            folder.mkdir();
            
            // Write the file with format: <srcPath.hashCode>_<offset>.dat
            String fileName = new StringBuilder()
                    .append(chunk.srcPath.hashCode())
                    .append("_")
                    .append(chunk.offset)
                    .append(".dat")
                    .toString();
            File file = new File(new StringBuilder()
                    .append(folderName)
                    .append(fileName)
                    .toString());
            FileChannel writeChannel = new FileOutputStream(file, false).getChannel();
            writeChannel.write(chunk.buffer);
            
            // Add the file name to fileNameList for accurate checksum update
            if (chunk.offset > this.fileNameList.get(chunk.srcPath).size()) {
                this.fileNameList.get(chunk.srcPath).add(fileName);
            } else {
                this.fileNameList.get(chunk.srcPath).add(chunk.offset, fileName);
            }
            writeChannel.close();
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
        // Disable the append mode
        boolean append = false;

        // Get the list of File for integration
        String folderName = new StringBuilder().append("./tempData/").append(Integer.toString(srcPath.hashCode())).append('/').toString();
        File[] files = new File[this.fileNameList.get(srcPath).size()];
        for (int i = 0; i < files.length; ++i) {
            String fileName = new StringBuilder().append(folderName).append(this.fileNameList.get(srcPath).get(i)).toString();
            files[i] = new File(fileName);
        }
        
        // Allocate the buffer and check for integrity
        ByteBuffer buffer = ByteBuffer.allocate(files.length * fileTransferConstants.CHUNK_MAX_SIZE);
        try {
            if (this.checkSum(srcPath, files, buffer)) {
                this.writeToFile(srcPath, buffer, append);
            }
        } catch (IOException ex) {
            Logger.getLogger(FileTransferHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
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
        // Write output to file
        File outputFile = new File(this.headerList.get(srcPath).desPath);
        FileChannel writeChannel = new FileOutputStream(outputFile, append).getChannel();
        writeChannel.write(buffer);
        System.out.println(" [x] Write to file: " + this.headerList.get(srcPath).desPath);
        writeChannel.close();
        
        // Delete the data related to the file on disk and memory
        this.headerList.remove(srcPath);
        this.fileNameList.remove(srcPath);
        File folder = new File(new StringBuilder().append("./tempData/").append(Integer.toString(srcPath.hashCode())).append('/').toString());
        File[] files = folder.listFiles();
        for (File file : files) {
            file.delete();
        }
        folder.delete();
    }

    /**
     * Check sum of the file given a new data chunk
     *
     * @param srcPath file source path
     * @param files
     * @param buffer ByteBuffer of the file
     * @return boolean: true if match the checksum in the metadata, otherwise,
     * false
     * @throws java.io.IOException
     * @throws NoSuchAlgorithmException
     */
    public boolean checkSum(String srcPath, File[] files, ByteBuffer buffer) throws IOException {
        Adler32 checkSumGen = new Adler32();

        // Transfer bytes from sent data chunks into the ByteBuffer
        for (File chunkFile : files) {
            // Read the file into a byte array
            FileInputStream readChannel = new FileInputStream(chunkFile);
            byte[] chunkBuffer = new byte[(int) chunkFile.length()];
            readChannel.read(chunkBuffer);
            
            // Parse the file name to get the offset
            String fileName = chunkFile.getName();
            String offStr = fileName.substring(Integer.toString(srcPath.hashCode()).length() + 1, fileName.length() - ".dat".length());
            int offset = new Integer(offStr);
            
            // Transfer the byte array to the buffer
            buffer.position(fileTransferConstants.CHUNK_MAX_SIZE * offset);
            buffer.put(chunkBuffer);
            
            // Update the checksum
            checkSumGen.update(chunkBuffer);
        }

        // Get rid of unused bytes
        if (buffer.position() < buffer.limit()) {
            buffer.limit(buffer.position());
        }
        buffer.rewind();

        return checkSumGen.getValue() == this.headerList.get(srcPath).checkSum;
    }
}
