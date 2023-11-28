package com.uc.framework.nio.handlers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.*;

import org.apache.log4j.Logger;

import com.uc.framework.nio.io.*;

final public class Acceptor implements AcceptSelectorHandler 
{
	// Used to receive incoming connections
	private ServerSocketChannel ssc; 
	// The selector used by this instance.
	private final SelectorThread ioThread;  
	// Port where to listen for connections.
	private final int listenPort;  
	// Listener to be notified of new connections and of errors.
	private final AcceptorListener listener;
	private Logger  logger = null;

	/**
	 * 생성자
	 * @param listenPort
	 * @param ioThread
	 * @param listener
	 */
	public Acceptor(int listenPort, SelectorThread ioThread, AcceptorListener listener)    
	{ 
		this.ioThread = ioThread;
		this.listenPort = listenPort;
		this.listener = listener;
		this.logger = logger;
	}
  
	public void openServerSocket() throws IOException 
	{
		ssc = ServerSocketChannel.open();
		InetSocketAddress isa = new InetSocketAddress(listenPort);
		ssc.socket().bind(isa, 100);
    
    // This method might be called from any thread. We must use 
    // the xxxLater methods so that the actual register operation
    // is done by the selector's thread. No other thread should access
    // the selector directly.
		ioThread.registerChannelLater(ssc,
				SelectionKey.OP_ACCEPT,
				this,
				new CallbackErrorHandler() {
			public void handleError(Exception ex) {    
				listener.socketError(Acceptor.this, ex);
			}
		});
	}
  
	/**
	 * 문자열로 변경
	 */
	public String toString() 
	{  
		return "ListenPort: " + listenPort;
	}
  
	/**
	 * 클라이언트 접속처리
	 */
	public void handleAccept(Logger logger) 
	{
		SocketChannel sc = null;
		try {
			sc = ssc.accept();
			logger.debug("accept: " + sc);
			Socket s = sc.socket();
			logger.debug("socket: " + s);
			// Reactivate interest to receive the next connection. We
			// can use one of the XXXNow methods since this method is being
			// executed on the selector's thread.
			ioThread.addChannelInterestNow(ssc, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			listener.socketError(this, e);
		}
		if (sc != null) {
			// Connection established
			listener.socketConnected(this, sc);
		}
	}

	/**
	 * 접속종료
	 */
	public void close()  
	{
		try {
			// Must wait for the socket to be closed.
			ioThread.invokeAndWait(new Runnable() {      
				public void run() {
					if (ssc != null) {
						try {
							ssc.close();
						} catch (IOException e) {
							// Ignore
						}
					}        
				}
			});
		} catch (InterruptedException e) {
			// Ignore
		}
	}  
}