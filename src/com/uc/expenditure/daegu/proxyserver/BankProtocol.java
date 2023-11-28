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
final public class BankProtocol implements Protocol 
{
    Logger  logger = null;
    
  /** Initial byte of a packet. */
  public static final byte STX = 0x02;

  /** Final byte of a packet. */
  public static final byte ETX = 0x03;  
  
  /**
   * Size of the buffer used to reconstrut the packet. Should be big 
   * enough to hold an entire packet.
   */
  private final static int BUFFER_SIZE = 20*1024;
  
  /**
   * Holds a message that is not fully assembled. This buffer is fixed-size.
   * If it is exceed, an Exception is raised by the decode() method. 
   */
  private byte[] buffer = new byte[BUFFER_SIZE];
  
  /** Write position on the previous buffer. */
  private int pos = 0;
  private FormatParserAsMap  fp = null;
  
  public BankProtocol(Logger logger, FormatParserAsMap fp2) 
  {
    // TODO Auto-generated constructor stub
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

//           Utils.dumpAsHex(logger, buffer, "수신데이터", pos);
       }

        // 분석에 사용할 전문유형 선택(임시)
        String  msgType = "02"; 
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
//      logger.debug("데이터 : 길이[" + ci.pos + "], 데이터[" + new String(ci.data)+"]");
        if (fp.disassembleMessageByteHeadAsMap(buffer, pos, head, msgType) == -1) {
//          logger.error("전문 공통부 분석중 오류");
            return null;
        }

        // 개별부 구분하는 필드이름.
        String  keyValue = head.getString(mi.getKeyField());
        logger.debug("전문구분자 필드명[" +mi.getKeyField()+"], 전문구분자 값[" + keyValue +"]");

        // 전문의개별부 길이 검사.
        int bodyLen = mi.getBodyLength(keyValue);
        logger.debug("전문바디길이["+bodyLen+"]");
        if (bodyLen < 0) {
            logger.debug("개별부가 존재하지 않는 전문구조입니다");
            bodyLen = 0;
//            return packetBuffer;
        } else if (bodyLen == 0) {
            logger.debug("개별부의 길이가 가변입니다.");
            long sizeValue = head.getLong(mi.getSizeField());
            logger.debug("전문크기 필드명[" +mi.getSizeField()+"], 전문구분자 값[" + sizeValue +"]");
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
	//      logger.debug("데이터 : 길이[" + ci.pos + "], 데이터[" + new String(ci.data)+"]");
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

        // The current packet is fully reassembled. Return it
        byte[] newBuffer = new byte[totalLength];
        System.arraycopy(buffer, 0, newBuffer, 0, totalLength);
        ByteBuffer packetBuffer = ByteBuffer.wrap(newBuffer);        
//        pos = 0;
        pos -= totalLength;
        if (pos > 0) {
            byte[]  remain = new byte[pos];
            System.arraycopy(buffer, totalLength, remain, 0, pos);
            java.util.Arrays.fill(buffer, (byte)0x00);
            System.arraycopy(remain, 0, buffer, 0, pos);
//            Utils.dumpAsHex(logger, remain, "남은데이터", pos);
        }

        return packetBuffer;
//    }
  
    // No packet was reassembled. There is not enough data. Wait
    // for more data to arrive.
//    if (socketBuffer.hasArray())
//          System.out.println("[" + new String(socketBuffer.array())+"]");
//    return null;
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
/*
        COMMAND : args[0], String
        SRCID   : args[1], String
        DESTID  : args[2], String
        DTSIZE  : args[3], Integer
        DATA	: args[4], Byte[]
 */       		
//        if (args.length < 3) {
//        	return null;
//        }
        
//        MyMap msg = new MyMap();
        
        String	msgdsc = args[0].toString();       
        String	jobdsc = args[1].toString();

		shead.setString("TransactionCode", "DGCITY01");
		shead.setString("EnterpriseNo", "");
		shead.setString("BankCode2", "31");
		shead.setString("MessageType", msgdsc);
		shead.setString("JobType", jobdsc);
		shead.setLong("TransmitCount", (long)1);
		shead.setLong("MessageSerial", (long)1);
		shead.setString("TransmitDate", Utils.getCurrentDate());
		shead.setString("TransmitTime", Utils.getCurrentTime());
//		shead.setString("ResultCode", "0000");
		shead.setString("DiscCode", "");
		shead.setString("SSArea", "");
		shead.setString("EnterpriseArea", "");
//		msg.put("BankArea", "");
        
        int length = fp.assembleMessageByteAsMap(bMsg, 1024, shead, "02");
        if (length == -1) {
            logger.error("error while assembling message");
        }
/*
         String  tmp = String.format("%04d", length);
 
        logger.debug("전문길이[" + tmp + "]");
        System.arraycopy(tmp.getBytes(), 0, bMsg, 0, 4);
*/                
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(bMsg, 0, length);
        buffer.limit(buffer.position());
        buffer.flip();
        
        return buffer;
  }
}
