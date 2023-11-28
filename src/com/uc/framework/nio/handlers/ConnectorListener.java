package com.uc.framework.nio.handlers;

import java.nio.channels.SocketChannel;

public interface ConnectorListener 
{
	public void connectionEstablished(Connector connector, SocketChannel sc);
	public void connectionFailed(Connector connector, Exception cause);
}
