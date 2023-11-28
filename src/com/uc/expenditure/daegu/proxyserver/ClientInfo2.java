/**
 * 
 */

package com.uc.expenditure.daegu.proxyserver;

import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;

import com.uc.expenditure.daegu.server.ClientState;
import com.uc.framework.nio.handlers.PacketChannel;

/**
 * @author hotaep
 *
 */
public final class ClientInfo2
{
    FileWriter fw = null;
    RandomAccessFile    out = null;

    long    lBlockNo = 0;
    long    lLastSeqNo = 0;
    long    lMaxSeqNo = 100;
    int     lDataSize = 4096;

    public	int	port = 0;
    public InternalProtocol  protocol2 = null; 
    public BankProtocol  protocol = null; 
    public PacketChannel	pc = null;

    // 결번
    LinkedHashMap<Long, Boolean>    lhmMissingSeq = new  LinkedHashMap<Long, Boolean>();
    public ServerState State = null;
    public ClientState cState = null;
   
    public ClientInfo2()
    {
        lBlockNo = 0;
        lLastSeqNo = 0;
        lMaxSeqNo = 100;
        lDataSize = 4096;
        pc = null;
    }
}
