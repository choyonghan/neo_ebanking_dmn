/*
  (c) 2004, Nuno Santos, nfsantos@sapo.pt
  relased under terms of the GNU public license 
  http://www.gnu.org/licenses/licenses.html#TOCGPL
*/
package com.uc.expenditure.daegu.proxyserver;


import com.uc.framework.nio.io.*;
import com.uc.framework.parsing.FormatParser;
import com.uc.framework.parsing.FormatParserAsMap;
import com.uc.framework.parsing.MessageItem;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Decoder for an imaginary protocol. The packets of this protocol have the
 * following (very simple) format:
 * 
 * <STX><Data><ETX>
 * 
 * where
 * 
 * - <STX> is ascii 02.
 * - <ETX> is ascii 03.
 * - <asciiData> is any valid character.
 * 
 * @author Nuno Santos
 */
final public class InternalProtocol implements Protocol 
{
    Logger  logger = null;
      
    private final static int BUFFER_SIZE = 2*1024;
    private byte[] buffer = new byte[BUFFER_SIZE];
  
    private int pos = 0;
    private FormatParserAsMap  fp = null;
  
    public InternalProtocol(Logger logger, FormatParserAsMap fp2) 
    {
        // TODO Auto-generated constructor stub
        logger.trace("===== InternalProtocol 생성자 =====");
        this.pos = 0;
        this.logger = logger;
        this.fp = fp2;
    }
  
    public boolean hasRemaining() 
    {
        if (this.pos > 0)
            return true;
        else
            return false;
    }
  
    /**
     * 전문분석
     */
    public ByteBuffer decode(ByteBuffer socketBuffer, boolean received) throws IOException 
    {    
        int   len = 0;
    	if (received) {
    		len = socketBuffer.remaining();
    	}

        if (pos+len == 0)
            return null;

        if (len > 0) {
            logger.trace("수신버퍼 복사");
            socketBuffer.get(buffer, pos, len);
            pos += len;

//            Utils.dumpAsHex(logger, buffer, "수신데이터", pos);
        }
    
        // 분석에 사용할 전문유형 선택(임시)
        String  msgType = "01"; 
        MessageItem mi = fp.getMessageItemAsMap(msgType);
    
        // 전문 1 프레임의 길이
        int totalLength = 0;
    
        // 전문의 공통부 길이 검사.
        int headLen = mi.getHeadLength();
        if (pos < headLen) {    // 받은 전문의 길이가 공통부길이보자 짧은경우
            logger.error("입력받은 전문이 짧습니다. 전문헤더길이["+headLen+"], 받은전문길이["+pos+"]");
            return null;
        }
      
        // 헤더길이 더한다.
        totalLength = headLen;
    
        // 먼저 헤더부를 분석하여 개별부 정보를 구한다.
        MyMap    head = new MyMap();
        // logger.trace("데이터 : 길이[" + ci.pos + "], 데이터[" + new String(ci.data)+"]");
        if (fp.disassembleMessageByteHeadAsMap(buffer, pos, head, msgType) == -1) {
            logger.error("전문 공통부 분석중 오류");
            return null;
        }
    
        // 개별부 구분하는 필드이름.
        String  keyValue = head.getString(mi.getKeyField());
        logger.trace("전문구분자 필드명[" +mi.getKeyField()+"], 전문구분자 값[" + keyValue +"]");
    
        // 전문의개별부 길이 검사.
        int bodyLen = mi.getBodyLength(keyValue);
        logger.trace("전문바디길이["+bodyLen+"]");
        if (bodyLen < 0) {
            logger.trace("개별부 없는 전문");
            bodyLen = 0;
            //            return packetBuffer;
        } else if (bodyLen == 0) {
            long sizeValue = head.getLong(mi.getSizeField());
            logger.trace("전문크기 필드명[" +mi.getSizeField()+"], 전문구분자 값[" + sizeValue +"], 가변길이필드");
            if (mi.isIncludeHead()) {
                bodyLen = (int) (sizeValue - mi.getHeadLength());
            } else {
                bodyLen = (int) sizeValue;                
            }
    
            if (pos < (headLen+bodyLen)) {
                logger.error("입력받은 전문이 짧습니다. 전문헤더길이["+(headLen+bodyLen)+"], 길이["+pos+"]");
                return null;
            }
            // 먼저 헤더부를 분석하여 개별부 정보를 구한다.
            MyMap	body = new MyMap();
            if (fp.disassembleMessageByteBodyAsMap(buffer, pos, head, msgType, keyValue, bodyLen) == -1) {
                logger.error("전문 공통부 분석중 오류");
                return null;
            }
        }
    
        // 바디길이 더한다.
        totalLength += bodyLen;
        
        int tailLen = mi.getTailLength();
        if (tailLen > 0) {
            logger.error("전문테일길이["+tailLen+"]");
            
            if (pos < (headLen + bodyLen + tailLen)) {
                logger.error("입력받은 전문이 짧습니다");
                return null;
            }
            // 테일길이 더한다.
            totalLength += tailLen;
        }
        
        byte[] newBuffer = new byte[totalLength];
        System.arraycopy(buffer, 0, newBuffer, 0, totalLength);
        ByteBuffer packetBuffer = ByteBuffer.wrap(newBuffer);        
        pos -= totalLength;
        
        if (pos > 0) {
            // 처리 후에 남은 데이터가 있음.
            byte[]  remain = new byte[pos];
            System.arraycopy(buffer, totalLength, remain, 0, pos);
            java.util.Arrays.fill(buffer, (byte)0x00);
            System.arraycopy(remain, 0, buffer, 0, pos);
//            Utils.dumpAsHex(logger, remain, "남은데이터", pos);
        }
        return packetBuffer;
    }
    
    /**
     * 전문조립
     */
    public ByteBuffer encode(MyMap shead, Object[] args, int argc) throws IOException
    {
        /**
         * 공통부 조립
         */
        byte[]  bMsg = new byte[10240];                 
        if (args.length < 5) {
            return null;
        }
    
        String	COMMAND = args[0].toString();       
        String	REOPT = args[1].toString();
        String	SRCID = args[2].toString();
        String	DESTID = args[3].toString();
        String	RESCD = args[4].toString();
    
        shead.setString("DESC", "UCNH");
        shead.setString("COMMAND", COMMAND);	// 트랜잭션코드
        shead.setString("RESOPT", REOPT);		// 업무구분코드
        shead.setString("SRCID", SRCID);		// 은행코드
        shead.setString("DESTID", DESTID);	// 전문종별코드
        shead.setString("TRDATE", Utils.getCurrentDate());		// 전송구분코드
        shead.setString("TRTIME", Utils.getCurrentTime());		// 송수신플래그
        shead.setString("RESCD", RESCD);			// 파일번호
        shead.setString("RESERVED", "00000000");			// 파일번호
    
        int length = fp.assembleMessageByteAsMap(bMsg, 1024, shead, "01");
        if (length == -1) {
            logger.error("전문 조립중 오류 발생");
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(bMsg, 0, length);
        buffer.limit(buffer.position());
        buffer.flip();
        
        return buffer;
    }
}
