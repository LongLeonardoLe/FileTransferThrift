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

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.transport.TTransportException;

/**
 *
 * @author Long Le <longlnt@vng.com.vn>
 */
public class FileTransferServer {

    public static FileTransferHandler handler;

    public static FileTransfer.Processor processor;

    public static void main(String[] argv) {

        try {
            handler = new FileTransferHandler();
            processor = new FileTransfer.Processor(handler);

            // Starting a nonblocking server
            TNonblockingServerSocket socket = new TNonblockingServerSocket(9090);
            TNonblockingServer.Args args = new TNonblockingServer.Args(socket);
            args.protocolFactory(new TBinaryProtocol.Factory());
            args.transportFactory(new TFramedTransport.Factory());
            args.processorFactory(new TProcessorFactory(processor));

            TServer server = new TNonblockingServer(args);
            System.out.println("Starting a nonblocking server...");
            server.serve();

        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }
}
