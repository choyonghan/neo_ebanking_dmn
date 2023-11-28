package com.uc.expenditure.daegu.server;

import com.uc.framework.nio.io.*;
import com.uc.framework.parsing.FormatParser;
import com.uc.framework.parsing.FormatParserAsMap;
import com.uc.framework.parsing.MessageItem;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

final public class ServerProtocol implements Protocol
{
    Logger  logger = null;

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

  public ServerProtocol(Logger logger, FormatParserAsMap fp)
  {
    // TODO Auto-generated constructor stub
      this.logger = logger;
      this.fp = fp;
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

   	   logger.trace("pos=" + pos +" "+ "len=" + len+" "+ "received=" + received);

       if (pos+len == 0)
           return null;

       if (len > 0) {
           logger.trace("수신버퍼 복사");
           //Utils.dumpAsHex(logger, buffer, "수신데이터1", pos);

           socketBuffer.get(buffer, pos, len);
           pos += len;

           Utils.dumpAsHex(logger, buffer, "수신데이터", pos);
           byte[] tempBuf = new byte[38];

           System.arraycopy(buffer, 0, tempBuf, 0, 38);

           logger.debug("RECV LEN=" + buffer.length + ", MSG==" + new String(tempBuf));
       }
        // 분석에 사용할 전문유형 선택(임시)
        String  msgType = "04";
        MessageItem mi = fp.getMessageItemAsMap(msgType);

        // 전문 1 프레임의 길이
        int totalLength = 0;

        // 전문의 공통부 길이 검사.
        int headLen = mi.getHeadLength();

        logger.trace("totalLength=" + totalLength +" "+ "headLen=" + headLen);

        if (pos < headLen) {    // 받은 전문의 길이가 공통부길이보자 짧은경우
            logger.error("입력받은 전문이 짧습니다. 전문헤더길이["+headLen+"], 받은전문길이["+pos+"]");
            return null;
        }

        // 먼저 헤더부를 분석하여 개별부 정보를 구한다.
        MyMap	head = new MyMap();
//      logger.debug("데이터 : 길이[" + ci.pos + "], 데이터[" + new String(ci.data)+"]");
        headLen = fp.disassembleMessageByteHeadAsMap(buffer, pos, head, msgType);
        if (headLen == -1) {
//          logger.error("전문 공통부 분석중 오류");
            return null;
        }

        // 헤더길이 더한다.
        totalLength = headLen;

    	long	lHead = (Long)head.getMap("Length");

    	logger.trace("pos=" + pos +" "+ "lHead=" + lHead);

    	/*
    	 * 수정일 : 2015. 7. 3
    	 * 내용   : pos 와 lHead 비교시 길이(4) 포함된것과 비교해야됨
    	 */
    	if (pos < lHead + 4) {
    	    return null;
    	}

    	// 개별부 구분하는 필드이름.
        String  keyValue = (String)head.getMap(mi.getKeyField());

        logger.debug("전문구분자 필드명[" +mi.getKeyField()+"], 전문구분자 값[" + keyValue +"]");

        // 전문의개별부 길이 검사.
        long bodyLen = mi.getBodyLength(keyValue);
        logger.debug("전문바디길이["+bodyLen+"]");
        if (bodyLen < 0) {
            logger.error("개별부가 존재하지 않는 전문구조입니다");
            bodyLen = 0;
        } else if (bodyLen == 0) {
            logger.error("개별부의 길이가 가변입니다.");
            long sizeValue = (Long)head.getMap(mi.getSizeField());
            logger.debug("전문크기 필드명[" +mi.getSizeField()+"], 전문구분자 값[" + sizeValue +"]");
            if (mi.isIncludeHead()) {
                bodyLen = sizeValue - mi.getHeadLength();
            } else {
                bodyLen = sizeValue;
            }
        }

        logger.trace("pos=" + pos +" "+ "headLen=" + headLen +" "+ "bodyLen=" + bodyLen);
        if (pos < (headLen+bodyLen)) {
//            logger.error("입력받은 전문이 짧습니다. 전문헤더길이["+(headLen+bodyLen)+"], 길이["+pos+"]");
            return null;
        }
        // 먼저 헤더부를 분석하여 개별부 정보를 구한다.
        MyMap	body = new MyMap();
//      logger.debug("데이터 : 길이[" + ci.pos + "], 데이터[" + new String(ci.data)+"]");
        bodyLen = (long)fp.disassembleMessageByteBodyAsMap(buffer, pos, body, msgType, keyValue, bodyLen);
        if (bodyLen == -1) {
            logger.error("전문 공통부 분석중 오류");
            return null;
        }

        // 바디길이 더한다.
        totalLength += bodyLen;
        logger.trace("pos=" + pos +" "+ "headLen=" + headLen +" "+ "bodyLen=" + bodyLen+" "+ "totalLength=" + totalLength);

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

        logger.trace("pos=" + pos +" "+ "headLen=" + headLen +" "+ "bodyLen=" + bodyLen+" "+ "totalLength=" + totalLength);

        if (pos > 0) {
            byte[]  remain = new byte[pos];
            System.arraycopy(buffer, totalLength, remain, 0, pos);
            java.util.Arrays.fill(buffer, (byte)0x00);
            System.arraycopy(remain, 0, buffer, 0, pos);
        }

        logger.trace("packetBuffer:" + packetBuffer);
        logger.debug("[" + pos + "]바이트의 데이터가 더 있습니다");

        return packetBuffer;
//    }

    // No packet was reassembled. There is not enough data. Wait
    // for more data to arrive.
//    if (socketBuffer.hasArray())
//          System.out.println("[" + new String(socketBuffer.array())+"]");
//    return null;
  }

  public ByteBuffer encode(MyMap shead, Object[] args, int argc) throws IOException
  {
      /**
       * 공통부 조립
       */
      byte[]  bMsg = new byte[10240];

      String	MsgDsc = args[0].toString();
      String	FileName = args[1].toString();

	    shead.setLong("Length", (long)0);
//	    shead.setMap("TrCd", sTrCd);	// 트랜잭션코드
	    shead.setString("JobDsc", "EFT");		// 업무구분코드
	    shead.setString("BankCd", "031");		// 은행코드
	    shead.setString("MsgDsc", MsgDsc);	// 전문종별코드
	    shead.setString("TrxDsc", "S");		// 전송구분코드
	    shead.setString("SRFlag", "C");		// 송수신플래그
	    shead.setString("FileName", FileName);			// 파일번호
	    shead.setString("ResCd", "000");		// 응답코드

      int length = fp.assembleMessageByteAsMap(bMsg, 1024, shead, "04");
      if (length == -1) {
          logger.error("error while assembling message");
      }
      String  tmp = String.format("%04d", length-4);
      logger.debug("전문길이[" + tmp + "]");
      System.arraycopy(tmp.getBytes(), 0, bMsg, 0, 4);

      ByteBuffer buffer = ByteBuffer.allocate(length);
      buffer.put(bMsg, 0, length);
      buffer.limit(buffer.position());
      buffer.flip();

      return buffer;
  }
  /**
   * 전문조립
   */
  public ByteBuffer encode(LinkedHashMap<String, Object> shead, Object[] args, int argc) throws IOException
  {
        /**
         * 공통부 조립
         */
/*
        byte[]  bMsg = new byte[10240];

        String	MsgDsc = args[0].toString();
        String	FileName = args[1].toString();

	    shead.put("Length", (long)0);
//	    shead.put("TrCd", sTrCd.getBytes());	// 트랜잭션코드
	    shead.put("JobDsc", "EFT".getBytes());		// 업무구분코드
	    shead.put("BankCd", "031".getBytes());		// 은행코드
	    shead.put("MsgDsc", MsgDsc.getBytes());	// 전문종별코드
	    shead.put("TrxDsc", "S".getBytes());		// 전송구분코드
	    shead.put("SRFlag", "C".getBytes());		// 송수신플래그
	    shead.put("FileName", FileName.getBytes());			// 파일번호
	    shead.put("ResCd", "000".getBytes());		// 응답코드

        int length = fp.assembleMessageByte(bMsg, 1024, shead, "04");
        if (length == -1) {
            logger.error("error while assembling message");
        }
        String  tmp = String.format("%04d", length-4);
        logger.debug("전문길이[" + tmp + "]");
        System.arraycopy(tmp.getBytes(), 0, bMsg, 0, 4);

        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(bMsg, 0, length);
        buffer.limit(buffer.position());
        buffer.flip();

        return buffer;
*/
		return null;
  }
}
