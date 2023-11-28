package com.uc.framework.nio.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.uc.framework.nio.io.*;
import com.uc.framework.utils.Utils;

final public class PacketChannel implements ReadWriteSelectorHandler 
{
	/** The associated selector. */
	protected final SelectorThread selector;
	/** The socket where read and write operations are performed. */
	private final SocketChannel sc;
	/** Used for reading from the socket. */
	private ByteBuffer inBuffer;
	/**
	 * The buffer with the packet currently being sent. 
	 * This class can only send one packet at a time, there are no
	 * queueing mechanisms.
	 */
	private ByteBuffer outBuffer = null;
  
	/**
	 * Used to convert raw bytes into packets. 
	 * (Strategy design pattern)
	 */
	private final Protocol protocol;

	/**
	 * Object interested in the events generated by this class.
	 * It is notified whenever an error occurs or a packet is read. 
	 */
	private final PacketChannelListener listener;

	private Logger	logger = null;

    /**
	 * Creates and initializes a new instance. Read interest is enabled
	 * by the constructor, so callers should be ready to star receiving
	 * packets.
	 * 
	 * @param socketChannel Socket to be wrapped.
	 * @param selector Selector to be used for managing IO events.
	 * @param listener Object to receive the callbacks.
	 * @param protocolDecoder Decoder for reassembling the packets.
	 * @throws IOException
	 */
	public PacketChannel(Logger logger, SocketChannel socketChannel, SelectorThread selector, Protocol protocol,
                       PacketChannelListener listener) throws IOException 
    {        
		this.logger = logger;
		
		this.selector = selector;
		this.protocol = protocol;    
		this.sc = socketChannel;
		this.listener = listener;
    
		// Creates the reading buffer
		// The size is the same as the size of the TCP sockets receive buffer.
		// We will never read more than that at a time.
		inBuffer = ByteBuffer.allocateDirect(sc.socket().getReceiveBufferSize());
    
		// Quick and dirty hack. When a buffer is created by the first time
		// it is empty, with 
		inBuffer.position(inBuffer.capacity());
    
//	    inBuffer.flip();
    
		// Registers with read interest.
		selector.registerChannelNow(sc, 0,  this);
    }
  
	public void resumeReading() throws IOException 
	{
		logger.trace("resumeReading in [" + Thread.currentThread().getName() + "]");
//		inBuffer.clear();
//       	inBuffer.position(inBuffer.capacity());
		processInBuffer(false);
//		reactivateReading();
		logger.trace("resumeReading done");
	}
  
	public void close() 
	{
		logger.trace("close in [" + Thread.currentThread().getName() + "]");
		try {
			sc.close();
		} catch (IOException e) {
			// Ignore
		}
		logger.trace("close done");
	}
  
	public void handleRead() 
	{
		logger.trace("handleRead in [" + Thread.currentThread().getName() + "], 소켓[" + sc.socket().toString() + "]");
		try {      
			// Reads from the socket      
			// Returns -1 if it has reached end-of-stream
			int readBytes = sc.read(inBuffer);      
	        logger.trace(">>>>>>>>>>>>> read byte[" + readBytes + "]");
			// End of stream???
			if (readBytes == -1) {
				// End of stream. Closing channel...
				//////        close();
				listener.socketDisconnected(this);
		        logger.trace("handleRead done");
				return;
			}     
			// Nothing else to be read?
			if (readBytes == 0) {        
				// There was nothing to read. Shouldn't happen often, but 
				// it is not an error, we can deal with it. Ignore this event
				// and reactivate reading.
				reactivateReading();
		        logger.trace("handleRead done");
				return;
			}

			// There is some data in the buffer. Processes it.
			inBuffer.flip();
			processInBuffer(true);
		} catch (IOException ex) {
			// Serious error. Close socket.
			listener.socketException(this, ex);
			close();
		}
		logger.trace("handleRead done");
	}
  
	private void processInBuffer(boolean first) throws IOException 
	{    
		logger.trace("processInBuffer start");
	  
		ByteBuffer packet = protocol.decode(inBuffer, first);
		// A packet may or may not have been fully assembled, depending
		// on the data available in the buffer
		if (packet == null) {      
			// Partial packet received. Must wait for more data. All the contents
			// of inBuffer were processed by the protocol decoder. We can
			// delete it and prepare for more data.
			first = false;
			inBuffer.clear();
			reactivateReading();
		} else {      
			// A packet was reassembled.
			first = false;
			listener.packetArrived(this, packet);
			// The inBuffer might still have some data left. Perhaps
			// the beginning of another packet. So don't clear it. Next
			// time reading is activated, we start by processing the inBuffer
			// again.
		}
		logger.trace("processInBuffer done");
	}

	public void disableReading() throws IOException 
	{
		logger.trace("disableReading in [" + Thread.currentThread().getName() + "]");
		selector.removeChannelInterestNow(sc, SelectionKey.OP_READ);    
		logger.trace("disableReading done");
	}
  
	private void reactivateReading() throws IOException 
	{
		logger.trace("reactivateReading in [" + Thread.currentThread().getName() + "]");
//		selector.addChannelInterestNow(sc, SelectionKey.OP_READ);
		selector.addChannelInterestLater(sc, SelectionKey.OP_READ, null);
		logger.trace("reactivateReading done");
	}
  
	public void sendPacket(ByteBuffer packet) 
	{
		logger.trace("sendPacket start");
/*
         synchronized (selector.queue) {
             selector.queue.add(packet);
            selector.queue.notify();
        }
        try {
            requestWrite();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		logger.info("sendPacket done");
*/
        // keeps a reference to the packet. In production code this should copy
		// the contents of the buffer.
		outBuffer = packet;
		handleWrite();
		logger.trace("sendPacket done");
	}

	private void requestWrite() throws IOException 
	{
		logger.trace("requestWrite in [" + Thread.currentThread().getName() + "]");
//		selector.addChannelInterestNow(sc, SelectionKey.OP_WRITE);
		selector.addChannelInterestLater(sc, SelectionKey.OP_WRITE, null);
		logger.trace("requestWrite done");
	}
  
	public void handleWrite() 
	{    
		logger.trace("handleWrite in [" + Thread.currentThread().getName() + "], 소켓[" + sc.socket().toString() + "]");

//		Utils.dumpAsHex(logger, outBuffer.array(), "송신데이터", outBuffer.capacity());
		
		try {
			// Writes to the socket as much as possible. Since this is a 
			// non-blocking operation, we don't know in advance how many
			// bytes will actually be written.
			int written = sc.write(outBuffer);
			// Check if there are more to be written. 
			if (outBuffer.hasRemaining()) {
				// There is. Reactivate interest in writing. We will try again 
				// when the socket is ready.
				requestWrite();
			} else {
				// outBuffer was completly written. Notifies listener
				ByteBuffer sentPacket = outBuffer;
				outBuffer = null;
				listener.packetSent(this, sentPacket);
			}
		} catch (IOException ioe) {
			close();
			listener.socketException(this, ioe);
		}
		logger.trace("handleWrite done");
	}
  
	public SocketChannel getSocketChannel() 
	{
		return sc;
	}
  
	public String toString() 
	{  
		return Integer.toString(sc.socket().getLocalPort());
	}
}
