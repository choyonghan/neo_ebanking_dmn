package com.uc.expenditure.daegu.proxyserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.uc.framework.nio.handlers.Acceptor;
import com.uc.framework.nio.handlers.AcceptorListener;
import com.uc.framework.nio.handlers.Connector;
import com.uc.framework.nio.handlers.ConnectorListener;
import com.uc.framework.nio.handlers.PacketChannel;
import com.uc.framework.nio.handlers.PacketChannelListener;
import com.uc.framework.nio.handlers.TimerListener;
import com.uc.framework.nio.io.EventType;
import com.uc.framework.nio.io.SelectorThread;
import com.uc.framework.nio.io.ServerDataEventAsMap;
import com.uc.framework.parsing.FormatParserAsMap;
import com.uc.framework.utils.MyMap;

public class Socket implements ConnectorListener, AcceptorListener, PacketChannelListener, SocketListener, TimerListener
{
    private List<ServerDataEventAsMap>    queue = null;
    private Logger  logger = null;
    private FormatParserAsMap   fp = null;
    private SelectorThread  st = null;

    public Socket(Logger logger, List<ServerDataEventAsMap> que, FormatParserAsMap fp, SelectorThread st)
    {
        this.logger = logger;
        this.queue = que;
        this.fp = fp;
        this.st = st;
    }
    
    public void process() 
    {    
    }
    
    // ////////////////////////////////////////
    // Implementation of the callbacks from the
    // Acceptor and PacketChannel classes
    // ////////////////////////////////////////
    /**
     * 새로운 클라이언트가 접속
     */
    public void socketConnected(Acceptor acceptor, SocketChannel sc)
    {
        logger.debug("***** socketConnected, PC[" + acceptor + "] *****");

        try {
            // We should reduce the size of the TCP buffers or else we will
            // easily run out of memory when accepting several thousands of
            // connctions
            sc.socket().setReceiveBufferSize(2 * 1024);
            sc.socket().setSendBufferSize(2 * 1024);
            
            // The contructor enables reading automatically.
            ClientInfo ci = new ClientInfo();
            ci.protocol = new InternalProtocol(logger, fp);
            PacketChannel pc = new PacketChannel(logger, sc, st, ci.protocol, this);
            ci.pc = pc;

            int port = sc.socket().getPort();
//            ci.State = ServerState.OFFLINE;
//            lhmClientList.put(port, ci);

            synchronized (queue) {
                queue.add(new ServerDataEventAsMap(EventType.CLIENTACCEPTED, Thread.currentThread(), pc, sc, null, 0, null));
                queue.notify();
            }
//            pc.resumeReading();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void socketError(Acceptor acceptor, Exception ex)
    {
        logger.debug("***** socketError, PC[" + acceptor + "], [" + ex.getLocalizedMessage() + "] *****");

        synchronized (queue) {
            queue.add(new ServerDataEventAsMap(EventType.SOCKETERROR, Thread.currentThread(), null, null, null, 0, null));
            queue.notify();
        }
    }

    public void connectionEstablished(Connector connector, SocketChannel sc) 
    {    
        logger.debug("connectionEstablished, [" + connector + "]");
        
          try {
            // We should reduce the size of the TCP buffers or else we will
            // easily run out of memory when accepting several thousands of
            // connctions
            sc.socket().setReceiveBufferSize(2*1024);
            sc.socket().setSendBufferSize(2*1024);
            // The contructor enables reading automatically.
    
            ClientInfo ci = new ClientInfo();
            ci.protocol = new InternalProtocol(logger, fp);
    
            int port = sc.socket().getPort();
            logger.debug("포트[" + sc.socket().getLocalPort() + "], 포트[" + port + "]");
            
//            ci.State = ServerState.OFFLINE;
//            lhmClientList.put(port, ci);
    
            PacketChannel pc = new PacketChannel(logger, sc, st, ci.protocol, this);
            
            synchronized(queue) {
              queue.add(new ServerDataEventAsMap(EventType.CONNECTED, Thread.currentThread(), pc, sc, null, 0, null));
              queue.notify();
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
    }
    
    public void connectionFailed(Connector connector, Exception ex) 
    {
        logger.debug("connectionFailed, ["+ connector + "], Error: " + ex.getLocalizedMessage());
        
        synchronized(queue) {
          queue.add(new ServerDataEventAsMap(EventType.CONNECTIONFAILED, null, null, null, null, 0, null));
          queue.notify();
        }
    }

    public void packetArrived(PacketChannel pc, ByteBuffer pckt)
    {
        logger.debug("***** packetArrived, PC[" + pc + "] *****");

        MyMap   msg = new MyMap();
        if (fp.disassembleMessageByteAsMap(pckt.array(), pckt.remaining(), msg, "01") == -1) {
            logger.error("paring error");
        }

        synchronized (queue) {
            queue.add(new ServerDataEventAsMap(EventType.PACKETARRIVED, Thread.currentThread(), pc, pc.getSocketChannel(), pckt.array(), pckt.remaining(), msg));
            queue.notify();
        }
    }

    public void socketException(PacketChannel pc, Exception ex)
    {
        logger.debug("***** socketException, PC[" + pc + "] *****");

        if (pc == null)
            logger.debug("소켓오류");

        synchronized (queue) {
            queue.add(new ServerDataEventAsMap(EventType.SOCKETEXCEPTION, Thread.currentThread(), pc, pc.getSocketChannel(), null, 0, null));
            queue.notify();
        }
    }

    public void socketDisconnected(PacketChannel pc)
    {
        logger.debug("***** socketDisconnected, PC[" + pc + "] *****");

        synchronized (queue) {
            queue.add(new ServerDataEventAsMap(EventType.CLIENTDISCONNECTED, Thread.currentThread(), pc, pc.getSocketChannel(), null, 0, null));
            queue.notify();
        }
    }

    public void packetSent(PacketChannel pc, ByteBuffer pckt)
    {
        logger.debug("***** packetSent, PC[" + pc + "] *****");

        synchronized (queue) {
            queue.add(new ServerDataEventAsMap(EventType.PACKETSENT, Thread.currentThread(), pc, pc.getSocketChannel(), null, 0, null));
            queue.notify();
        }
    }

    public void timerExpired(Object handle)
    {
        // TODO Auto-generated method stub
        logger.debug("***** timerExpired, PC[" + handle + "] *****");

        synchronized(queue) {
            queue.add(new ServerDataEventAsMap(EventType.TIMEREXPIRED, null, handle, null, null, 0, null));
            queue.notify();
        }
    }
}
