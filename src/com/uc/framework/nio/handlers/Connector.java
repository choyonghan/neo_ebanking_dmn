package com.uc.framework.nio.handlers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import	org.apache.log4j.Logger;

import com.uc.framework.nio.io.*;
final public class Connector implements ConnectorSelectorHandler 
{
	// The socket being connected.
	private SocketChannel sc;  
	// The address of the remote endpoint.
	private final InetSocketAddress remoteAddress;
  	// The selector used for receiving events.
	private final SelectorThread selectorThread;
	// The listener for the callback events.
	private final ConnectorListener listener;
	private Logger	logger = null;
  
	public Connector(Logger logger, SelectorThread selector, InetSocketAddress remoteAddress, ConnectorListener listener)
	{
		this.logger = logger;
		this.selectorThread = selector;
		this.remoteAddress = remoteAddress;
		this.listener = listener;
	}
	
	public void connect() throws IOException 
	{
		logger.trace("connect started");
		sc = SocketChannel.open();  
		// Very important. Set to non-blocking. Otherwise a calllocalhost
		// to connect will block until the connection attempt fails 
		// or succeeds.
		sc.configureBlocking(false);
		sc.connect(remoteAddress);
		// Registers itself to receive the connect event.
		selectorThread.registerChannelLater(
				sc,
				SelectionKey.OP_CONNECT, 
				this,
				new CallbackErrorHandler() {
					public void handleError(Exception ex) {    
						listener.connectionFailed(Connector.this, ex);
					}
				});
		logger.trace("connect done, sc[" + sc.toString() + "]");
	}
    
	public void handleConnect() 
	{
		logger.trace("handleConnect started");
		try {
			if (!sc.finishConnect()) {
				// Connection failed
				listener.connectionFailed(this, null);
				return;
			}
			// Connection succeeded
			listener.connectionEstablished(this, sc);
		} catch (IOException ex) {      
			// Could not connect.
			listener.connectionFailed(this, ex);
		}
		logger.trace("handleConnect done");
	}
  
	public String toString() 
	{   
		return "Remote endpoint: " + 
    		remoteAddress.getAddress().getHostAddress() + ":" + remoteAddress.getPort();
	}
}
