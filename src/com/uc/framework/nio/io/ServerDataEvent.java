/**
 * 
 */
package com.uc.framework.nio.io;


import java.nio.channels.SocketChannel;
import	java.util.LinkedHashMap;

import com.uc.framework.nio.handlers.PacketChannel;

/**
 * @author hotaep
 *
 */
public class ServerDataEvent 
{
	public EventType		type;
	public Thread			thr;
	public PacketChannel	pc;
	public SocketChannel	socket;
	public byte[]			data;
	public int				length;
	public LinkedHashMap<String,Object>	msg;
	
	public ServerDataEvent(EventType type, Thread thr, PacketChannel pc, SocketChannel socket, byte[] data, int length, LinkedHashMap<String,Object>msg)
	{
		this.type = type;
		this.thr = thr;
		this.pc = pc;
		this.socket = socket;
		this.data = data;
		this.length = length;
		this.msg = msg;
	}
}
