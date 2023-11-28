package com.uc.expenditure.daegu.server;

import java.io.IOException;
import com.uc.egtob.net.ClientMessageService;

/**
 * @author Administrator
 *
 */
public class ClientConnService extends ClientMessageService{

    private String       hostAddr ;
    private int          connPort;
    
    public ClientConnService(String addr, int port) {
        
        hostAddr  = addr;
        connPort  = port;
    }
    
    /**
     * 전북은행에 연결
     * @param buffer  :: 송신 버퍼
     * @param svcId   :: 서비스 ID
     * @throws IOException
     * @throws Exception
     */
    public void JConnect() throws IOException, Exception {
        Connect(hostAddr, connPort);
    }
    
    /**
     * 전북은행에 데이터 송신
     * @param buffer  :: 송신 버퍼
     * @param svcId   :: 서비스 ID
     * @throws IOException
     * @throws Exception
     */
    public void JSend (byte[] buffer) throws IOException, Exception {
        sendData(buffer);
    }
    
    /**
     * 데이터 수신
     * @param timeOut : 수신시 대기시간 
     * @return
     * @throws Exception
     */
    public byte[] JRecv(int timeOut) throws Exception {
        
        byte[] recvBuff = recv(timeOut);
        return recvBuff;
    }
    
    /**
     * 소켓연결 종료
     * @throws IOException 
     */
    public void JClose() throws IOException {
        Disconnect();
    }
}