package com.uc.framework.nio.handlers;

import java.nio.ByteBuffer;

public interface PacketChannelListener 
{
	public void packetArrived(PacketChannel pc, ByteBuffer pckt);
	public void packetSent(PacketChannel pc, ByteBuffer pckt);
	public void socketException(PacketChannel pc, Exception ex);
	public void socketDisconnected(PacketChannel pc);
}
