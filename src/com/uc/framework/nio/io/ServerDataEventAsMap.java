/**
 * 
 */
package com.uc.framework.nio.io;


import java.nio.channels.SocketChannel;
import	java.util.LinkedHashMap;

import com.uc.framework.nio.handlers.PacketChannel;
import com.uc.framework.utils.MyMap;

/**
 * @author hotaep
 *
 */
public class ServerDataEventAsMap 
{
	public EventType		type;
	public Thread			thr;
	public Object			handle;
	public SocketChannel	socket;
	public byte[]			data;
	public int				length;
	public MyMap			msg;
	
	public ServerDataEventAsMap(EventType type, Thread thr, Object handle, SocketChannel socket, byte[] data, int length, MyMap msg)
	{
		this.type = type;
		this.thr = thr;
		this.handle = handle;
		this.socket = socket;
		this.data = data;
		this.length = length;
		this.msg = msg;
	}
}
