/**
 * 
 */
package com.uc.expenditure.daegu.server;

import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;

/**
 * @author hotaep
 *
 */
public final class ClientInfo
{
    FileWriter fw = null;
    public String	sFileName = null;
    public RandomAccessFile    out = null;

    public long    lBlockNo = 0;
    public long    lLastSeqNo = 0;
    public long    lMaxSeqNo = 100;
    public int     lDataSize = 4096;
    
    public ServerProtocol  protocol = null; 

    // 결번
    public LinkedHashMap<Long, Boolean>    lhmMissingSeq = new  LinkedHashMap<Long, Boolean>();
    public ServerState State = null;
   
    public ClientInfo()
    {
        lBlockNo = 0;
        lLastSeqNo = 0;
        lMaxSeqNo = 100;
        lDataSize = 4096;
        sFileName = null;
    }
}
