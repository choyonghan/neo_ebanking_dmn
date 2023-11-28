package com.uc.framework.nio.handlers;

import java.nio.channels.SocketChannel;

public interface AcceptorListener 
{
	public void socketConnected(Acceptor acceptor, SocketChannel sc);
	public void socketError(Acceptor acceptor, Exception ex);
}
