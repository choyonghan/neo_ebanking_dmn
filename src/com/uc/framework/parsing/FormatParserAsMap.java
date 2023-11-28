/**
 *
 */
package com.uc.framework.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

/**
 * @author hotaep
 *
 */
public class FormatParserAsMap
{
	private	Logger	logger;
//	private	LinkedHashMap<String,MessageItem>	msgfmts;
	private	MyMap	msgfmts;

	/**
	 *
	 */
	public FormatParserAsMap(Logger logger)
	{
		// TODO Auto-generated constructor stub
		this.logger = logger;
//		msgfmts = new LinkedHashMap<String,MessageItem>();
		msgfmts = new MyMap();
	}

	public MessageItem getMessageItemAsMap(String msgKind)
	{
		return (MessageItem) msgfmts.getAny(msgKind);
	}

	/**
	 *
	 */
	public int assembleMessageByteHeadAsMap(byte[] bMsg, int length, MyMap val, String msgKind)
	{
		logger.trace("전문 조립을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}

		ArrayList<FieldItem>	head = mi.getHead();//		for (int i = 0; i < bodies.size(); i++) {
		if (head == null) {
			logger.error("해당하는 공통부전문정의가 없습니다.");
			return -1;
		}

		int	pos = 0;

		for (int i = 0; i < head.size(); i++) {
			FieldItem	fi = head.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();

			if (sType.equals("AN")) {
				String	str = Utils.subStringBytes(val.getString(sName), nSize);
	            byte[] value = null;
				try {
					value = str.getBytes(encoding);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//	            byte[]	value = val.getBytes(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					String	sResult = new String(result);
					System.arraycopy(result, 0, bMsg, pos, fi.getSize());
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
				    int l = value.length;
					if (l > nSize) {
                        logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + l + "]");
						System.arraycopy(value, 0, bMsg, pos, nSize);
					} else if (l == nSize) {
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+", 필드값[" +  new String(value) + "]");
						System.arraycopy(value, 0, bMsg, pos, nSize);
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
						System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value)+new String(result) + "]");
					}
				}
			}
			if (sType.equals("B")) {
			    /* hotaep 막음 2012.02.27
				String	str = Utils.subStringBytes(val.getString(sName), nSize);
	            byte[] value = null;
				try {
					value = str.getBytes(encoding);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
   	            byte[]	value = val.getBytes(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					String	sResult = new String(result);
					System.arraycopy(result, 0, bMsg, pos, fi.getSize());
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
				    int l = value.length;
					if (l > nSize) {
                        logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + l + "]");
                        System.arraycopy(value, 0, bMsg, pos, nSize);
                        logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else if (l == nSize) {
                        System.arraycopy(value, 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+", 필드값[" +  new String(value) + "]");
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
                        System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value)+new String(result) + "]");
					}
				}
			}
			if (sType.equals("N")) {
				Long    value = val.getLong(sName);
				if (value == null) {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, 0);
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기="+nSize+", 필드값[" +  new String(result) + "]");
				} else {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, value);
					if (result.length() > nSize) {
						logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + result.length()+"]");
						logger.trace("전문 조립을 종료합니다");
						return -1;
					}
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				}
			}
			pos += fi.getSize();
		}
		length = pos;

		logger.trace("전문 조립을 종료합니다");

		return pos;
	}
	/**
	 *
	 */
	public int assembleMessageByteBodyAsMap(byte[] bMsg, int length, MyMap val, String msgKind, String msgCode)
	{
		logger.trace("전문 조립을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}

		LinkedHashMap<String,ArrayList<FieldItem>>	bodies = mi.getBodies();
		if (bodies == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}
		ArrayList<FieldItem>	body = bodies.get(msgCode);
		if (body == null) {
			logger.trace("해당하는 개별부전문정의가 없습니다.");
			return 0;
		}

		int	pos = 0;

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();
            if (nSize == 0) {
                    //가변길이
			    String	sLink = fi.getLink();
                long    lVal = val.getLong(sLink);
                nSize = (int)lVal;
                logger.trace("[" + sName + "]은 가변필드입니다. 길이[" + nSize + "]");
            }

			if (sType.equals("AN")) {
				if (nSize > 0) {
					String	str = Utils.subStringBytes(val.getString(sName), nSize);
					logger.debug("길이[" + nSize + "], 값[" + str + "], [" + val.getString(sName) + "]");
		            byte[] value = null;
					try {
						value = str.getBytes(encoding);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//              byte[]	value = val.getBytes(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					System.arraycopy(result, 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
                    int l = value.length;
					if (l > nSize) {
						System.arraycopy(value, 0, bMsg, pos, nSize);
                        logger.error("=== 입력값 길이 상이, 필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else if (l == nSize) {
						System.arraycopy(value, 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
						System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" +sName+"], 필드크기["+nSize+"], 필드값[" + value+new String(result) + "]");
					}
				}
				}
			}
			if (sType.equals("B")) {
				if (nSize > 0) {
	                /* hotaep 막음 2012.02.27
					String	str = Utils.subStringBytes(val.getString(sName), nSize);
					logger.debug("길이[" + nSize + "], 값[" + str + "]");
		            byte[] value = null;
					try {
						value = str.getBytes(encoding);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					*/
                byte[]  value = val.getBytes(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					System.arraycopy(result, 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
                    int l = value.length;
					if (l > nSize) {
                        System.arraycopy(value, 0, bMsg, pos, nSize);
                        logger.error("=== 입력값 길이 상이, 필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else if (l == nSize) {
						System.arraycopy(value, 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
						System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" +sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value)+new String(result) + "]");
					}
				}
				}
			}
			if (sType.equals("N")) {
			    Long    value = val.getLong(sName);
				if (value == null) {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, 0);
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, value);
					if (result.length() > nSize) {
						logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + result.length()+"]");
						logger.trace("전문 조립을 종료합니다");
						return -1;
					}
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				}
			}
			pos += fi.getSize();
		}
		length = pos;

		logger.trace("전문 조립을 종료합니다");

		return pos;
	}
	/**
	 *
	 */
	public int assembleMessageByteBodyAsMap(byte[] bMsg, int length, MyMap val, String msgKind, String msgCode, int bodyLength)
	{
		logger.trace("전문 조립을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}

		LinkedHashMap<String,ArrayList<FieldItem>>	bodies = mi.getBodies();
		if (bodies == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}
		ArrayList<FieldItem>	body = bodies.get(msgCode);
		if (body == null) {
			logger.trace("해당하는 개별부전문정의가 없습니다.");
			return 0;
		}

		int	pos = 0;

//		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(0);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = bodyLength;

			if (sType.equals("AN")) {
				String	value = val.getString(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					String	sResult = new String(result);
					System.arraycopy(result, 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
					int l = 0;
					try {
						l = value.getBytes(encoding).length;
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (l > nSize) {
                        try {
							System.arraycopy(value.getBytes(encoding), 0, bMsg, pos, nSize);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + l + "]");
					} else if (l == nSize) {
						try {
						String	newString = new String(value.getBytes(encoding), encoding);
						System.arraycopy(newString.getBytes(encoding), 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" + new String(value) + "]");
						} catch (UnsupportedEncodingException uee) {
						}
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
						try {
							System.arraycopy(value.getBytes(encoding), 0, bMsg, pos, l);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" +sName+"], 필드크기["+nSize+"], 필드값[" + new String(result) + "]");
					}
				}
			}
			if (sType.equals("N")) {
				Long    value = val.getLong(sName);
//				logger.trace(value.toString());
				if (value == null) {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, 0);
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, value);
					if (result.length() > nSize) {
						logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + result.length()+"]");
						logger.trace("전문 조립을 종료합니다");
						return -1;
					}
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				}
			}
//			pos += fi.getSize();
//		}
		length = pos;

		logger.trace("전문 조립을 종료합니다");

		return pos;
	}
	/**
	 *
	 */
	public int assembleMessageByteTailAsMap(byte[] bMsg, int length, MyMap val, String msgKind)
	{
		logger.trace("전문 조립을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}

		ArrayList<FieldItem>	tail = mi.getTail();//		for (int i = 0; i < bodies.size(); i++) {
		if (tail == null) {
			logger.trace("해당하는 꼬리부전문정의가 없습니다.");
			return 0;
		}

		int	pos = 0;

		for (int i = 0; i < tail.size(); i++) {
			FieldItem	fi = tail.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();

			if (sType.equals("AN")) {
                byte[]	value = val.getBytes(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					System.arraycopy(result, 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
				    int l = value.length;
					if (l > nSize) {
						System.arraycopy(value, 0, bMsg, pos, nSize);
                        logger.error("=== 입력값 길이 상이, 필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else if (l == nSize) {
					    System.arraycopy(value, 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else if (l == 0) {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" + new String(result) + "]");
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
                        System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" + new String(result) + "]");
					}
				}
			}
			if (sType.equals("B")) {
                byte[]  value = new byte[nSize];
                value = val.getBytes(sName, encoding);
//				String	value = (String)val.get(sName);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
//					String	sResult = new String(result);
					System.arraycopy(result, 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
//				    int	l = value.getBytes().length;
				    int l = value.length;
					if (l > nSize) {
                        System.arraycopy(value, 0, bMsg, pos, nSize);
                        logger.error("=== 입력값 길이 상이, 필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else if (l == nSize) {
//                      System.arraycopy(value.getBytes(), 0, bMsg, pos, nSize);
					    System.arraycopy(value, 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
//                        System.arraycopy(value.getBytes(), 0, bMsg, pos, l);
                        System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" + new String(result) + "]");
					}
				}
			}
			if (sType.equals("N")) {
				Long    value = val.getLong(sName);
				if (value == null) {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, 0);
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, value);
					if (result.length() > nSize) {
						logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + result.length()+"]");
						logger.trace("전문 조립을 종료합니다");
						return -1;
					}
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				}
			}
			pos += nSize;
		}

		logger.trace("전문 조립을 종료합니다");

		return pos;
	}

	/**
	 *
	 */
	public int assembleMessageByteAsMap(byte[] bMsg, int length, MyMap val, String msgKind)
	{
		logger.trace("전문 조립을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);
		//logger.debug(" mi : " + mi);  // 테스트

		if (mi == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
		}

		ArrayList<FieldItem>	head = mi.getHead();//		for (int i = 0; i < bodies.size(); i++) {
		if (head == null) {
			logger.error("해당하는 공통부전문정의가 없습니다.");
			return -1;
		}

		int	pos = 0;

		for (int i = 0; i < head.size(); i++) {
			FieldItem	fi = head.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();

			if (sType.equals("AN")) {
				String	str = Utils.subStringBytes(val.getString(sName), nSize);
	            byte[] value = null;
				try {
					value = str.getBytes(encoding);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	            //byte[]	value = val.getBytes(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					String	sResult = new String(result);
					System.arraycopy(result, 0, bMsg, pos, fi.getSize());
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
				    int l = value.length;
					if (l > nSize) {
                        System.arraycopy(value, 0, bMsg, pos, nSize);
                        logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + l + "]");
					} else if (l == nSize) {
                        System.arraycopy(value, 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+", 필드값[" +  new String(value) + "]");
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
                        System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value)+new String(result) + "]");
					}
				}
			}
			if (sType.equals("B")) {
				String	str = Utils.subStringBytes(val.getString(sName), nSize);
                /* hotaep 막음 2012.02.27
	            byte[] value = null;
				try {
					value = str.getBytes(encoding);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
	            byte[]  value = val.getBytes(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					String	sResult = new String(result);
					System.arraycopy(result, 0, bMsg, pos, fi.getSize());
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
				    int l = value.length;
					if (l > nSize) {
                        logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + l + "]");
                        System.arraycopy(value, 0, bMsg, pos, nSize);
                        logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else if (l == nSize) {
                        System.arraycopy(value, 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+", 필드값[" +  new String(value) + "]");
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
                        System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value)+new String(result) + "]");
					}
				}
			}
			if (sType.equals("N")) {
				Long    value = val.getLong(sName);
				if (value == null) {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, 0);
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기="+nSize+", 필드값[" +  new String(result) + "]");
				} else {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, value);
					if (result.length() > nSize) {
						logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + result.length()+"]");
						logger.trace("전문 조립을 종료합니다");
						return -1;
					}
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				}
			}
			pos += fi.getSize();
		}
		length = pos;

		LinkedHashMap<String,ArrayList<FieldItem>>	bodies = mi.getBodies();
		if (bodies == null || bodies.size() == 0) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return pos;
		}

        String  keyValue = val.getString(mi.getKeyField());

    	ArrayList<FieldItem>	body = bodies.get(keyValue);
		if (body == null) {
			logger.trace("해당하는 개별부전문정의가 없습니다.");
			return pos;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();

            if (nSize == 0) {
                    //가변길이
			    String	sLink = fi.getLink();
                Long    lVal = val.getLong(sLink);
                nSize = (int)(long)lVal;
                logger.trace("[" + sName + "]은 가변필드입니다. 길이[" + nSize + "]");
            }

			if (sType.equals("AN")) {
				if (nSize > 0) {
					String	str = Utils.subStringBytes(val.getString(sName), nSize);
		            byte[] value = null;
					try {
						value = str.getBytes(encoding);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//              byte[]	value = val.getBytes(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					System.arraycopy(result, 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
                    int l = value.length;
					if (l > nSize) {
                        System.arraycopy(value, 0, bMsg, pos, nSize);
                        logger.error("=== 입력값 길이 상이, 필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else if (l == nSize) {
						System.arraycopy(value, 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
						System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" +sName+"], 필드크기["+nSize+"], 필드값[" + value+new String(result) + "]");
					}
				}
				}
			}
			if (sType.equals("B")) {
				if (nSize > 0) {
	                /* hotaep 막음 2012.02.27
					String	str = Utils.subStringBytes(val.getString(sName), nSize);
		            byte[] value = null;
					try {
						value = str.getBytes(encoding);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
                byte[]  value = val.getBytes(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					System.arraycopy(result, 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
                    int l = value.length;
					if (l > nSize) {
                        System.arraycopy(value, 0, bMsg, pos, nSize);
                        logger.error("=== 입력값 길이 상이, 필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else if (l == nSize) {
						System.arraycopy(value, 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
						System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" +sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value)+new String(result) + "]");
					}
				}
				}
			}
			if (sType.equals("N")) {
				    Long    value = val.getLong(sName);
					if (value == null) {
						String	fmt = String.format("%%0%dd", nSize);
						String	result = String.format(fmt, 0);
						System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
					} else {
						String	fmt = String.format("%%0%dd", nSize);
						String	result = String.format(fmt, value);
						if (result.length() > nSize) {
							logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + result.length()+"]");
							logger.trace("전문 조립을 종료합니다");
							return -1;
						}
						System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
					}
			}
//			pos += fi.getSize();
			pos += nSize;
		}
		length = pos;

		ArrayList<FieldItem>	tail = mi.getTail();//		for (int i = 0; i < bodies.size(); i++) {
		if (tail == null) {
//			logger.error("해당하는 꼬리부전문정의가 없습니다.");
			return pos;
		}

		for (int i = 0; i < tail.size(); i++) {
			FieldItem	fi = tail.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();

			if (sType.equals("AN")) {
				String	str = Utils.subStringBytes(val.getString(sName), nSize);
	            byte[] value = null;
				try {
					value = str.getBytes(encoding);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//              byte[]	value = val.getBytes(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					System.arraycopy(result, 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
				    int l = value.length;
					if (l > nSize) {
                        System.arraycopy(value, 0, bMsg, pos, nSize);
                        logger.error("=== 입력값 길이 상이, 필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else if (l == nSize) {
					    System.arraycopy(value, 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
                        System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" + new String(result) + "]");
					}
				}
			}
			if (sType.equals("B")) {
	            /* hotaep 막음 2012.02.27
				String	str = Utils.subStringBytes(val.getString(sName), nSize);
	            byte[] value = null;
				try {
					value = str.getBytes(encoding);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
                byte[]  value = val.getBytes(sName, encoding);
				if (value == null) {
					byte[]	result = new byte[nSize];
					java.util.Arrays.fill(result, (byte)0x20);
					System.arraycopy(result, 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
				    int l = value.length;
					if (l > nSize) {
                        System.arraycopy(value, 0, bMsg, pos, nSize);
                        logger.error("=== 입력값 길이 상이, 필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else if (l == nSize) {
					    System.arraycopy(value, 0, bMsg, pos, nSize);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(value) + "]");
					} else {
						byte[]	result = new byte[nSize-l];
						java.util.Arrays.fill(result, (byte)0x20);
                        System.arraycopy(value, 0, bMsg, pos, l);
						System.arraycopy(result, 0, bMsg, pos+l, nSize-l);
						logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" + new String(result) + "]");
					}
				}
			}
			if (sType.equals("N")) {
				Long    value = val.getLong(sName);
				if (value == null) {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, 0);
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				} else {
					String	fmt = String.format("%%0%dd", nSize);
					String	result = String.format(fmt, value);
					if (result.length() > nSize) {
						logger.error("입력값이 너무 큽니다. 크기[" + nSize+"] 입력값크기[" + result.length()+"]");
						logger.trace("전문 조립을 종료합니다");
						return -1;
					}
					System.arraycopy(result.getBytes(), 0, bMsg, pos, nSize);
					logger.trace("필드명[" + sName+"], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
				}
			}
			pos += nSize;
		}

		logger.trace("전문 조립을 종료합니다");

		return pos;
	}

	/**
	 *
	 */
	public int disassembleMessageByteHeadAsMap(byte[] bMsg, int length, MyMap val, String msgKind)
	{
		logger.trace("전문 분석을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);

		if (mi == null) {
			logger.error("해당하는 전문이 없습니다.");
			return -1;
		}

		ArrayList<FieldItem>	head = mi.getHead();
		if (head == null) {
			logger.error("해당하는 공통부전문정의가 없습니다.");
			return -1;
		}

		int	totalLength = 0;
		for (int i = 0; i < head.size(); i++) {
			FieldItem	fi = head.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이["+totalLength+"], 전문길이["+length+"]");
			return -1;
		}

		int	pos = 0;

		for (int i = 0; i < head.size(); i++) {
			FieldItem	fi = head.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding).trim() + "]");
					val.setString(sName, new String(result, encoding).trim());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}
			if (sType.equals("B")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding) + "]");
					//val.setBytes(sName, (new String(result, encoding)).getBytes(encoding));
					val.setBytes(sName, result);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}
			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				logger.trace("필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" + Long.parseLong(new String(result).trim()) + "]");
				val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		logger.trace("전문 분석을 종료합니다");

		return 0;
	}
	/**
	 *
	 */
	public int disassembleMessageByteBodyAsMap(byte[] bMsg, int length, MyMap val, String msgKind, String msgCode)
	{
		logger.trace("전문 분석을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문이 없습니다.");
			return -1;
		}

		// 공통부
		ArrayList<FieldItem>	head = mi.getHead();
		if (head == null) {
			logger.error("해당하는 공통부전문정의가 없습니다.");
			return -1;
		}

		int	totalLength = 0;
		for (int i = 0; i < head.size(); i++) {
			FieldItem	fi = head.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이["+totalLength+"], 전문길이["+length+"]");
			return -1;
		}

		int	pos = totalLength;	// 공통부 제외

		LinkedHashMap<String,ArrayList<FieldItem>>	bodies = mi.getBodies();
		if (bodies == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}
		ArrayList<FieldItem>	body = bodies.get(msgCode);
		if (body == null) {
			logger.trace("해당하는 개별부전문정의가 없습니다.");
			return 0;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이["+totalLength+"], 전문길이["+length+"]");
			return -1;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();
            if (nSize == 0) {
                    //가변길이
			    String	sLink = fi.getLink();
                long    lVal = val.getLong(sLink);
                nSize = (int)lVal;
                logger.trace("[" + sName + "]은 가변필드입니다. 길이[" + nSize + "]");
            }

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding).trim() + "]");
					val.setString(sName, new String(result, encoding).trim());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (sType.equals("B")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding) + "]");
					//val.setBytes(sName, (new String(result,encoding)).getBytes(encoding));
					val.setBytes(sName, result);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}

			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				logger.trace("필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" + Long.parseLong(new String(result).trim()) + "]");
				val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		logger.trace("전문 분석을 종료합니다");

		return pos;
	}

	/**
	 *
	 */
	public int disassembleFileByteBodyAsMap(byte[] bMsg, int length, MyMap val, String msgKind, String msgCode)
	{
		logger.trace("전문 분석을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문이 없습니다.");
			return -1;
		}

		// 공통부
		ArrayList<FieldItem>	head = mi.getHead();
		if (head == null) {
			logger.error("해당하는 공통부전문정의가 없습니다.");
			return -1;
		}

		int	pos = 0;
		int	totalLength = 0;

		LinkedHashMap<String,ArrayList<FieldItem>>	bodies = mi.getBodies();
		if (bodies == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}
		ArrayList<FieldItem>	body = bodies.get(msgCode);
		if (body == null) {
			logger.trace("해당하는 개별부전문정의가 없습니다.");
			return 0;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);
			totalLength += fi.getSize();
		}
		logger.debug("입력된 전문길이  길이["+totalLength+"], 전문길이["+length+"]");
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이["+totalLength+"], 전문길이["+length+"]");
			return -1;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();
            if (nSize == 0) {
                //가변길이
			    String	sLink = fi.getLink();
                long    lVal = val.getLong(sLink);
                nSize = (int)lVal;
                logger.trace("[" + sName + "]은 가변필드입니다. 길이[" + nSize + "]");
            }

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				/*  수정일자 : 2022.11.28
				 *  작업자    : 신상훈
				 *  내용       : 전각을 반각으로 처리 시작
				 */
				byte[] tmpResult = new byte[nSize];
				java.util.Arrays.fill(tmpResult, (byte)0);
				try {
					System.arraycopy(bMsg, pos, tmpResult, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.info("전문 분석을 종료합니다");
					return -1;
				}

				String tmpStrBefore = "";
				try {
					tmpStrBefore = new String(tmpResult,encoding);
				} catch (UnsupportedEncodingException e1) {
					logger.error(e1);
				}

				String tmpStrAfter = Utils.convertFullHalf(tmpStrBefore);

				// 미리 공백제거
				//String tmpStrAfter = Utils.convertFullHalf(tmpStrBefore).toString().replaceAll("\\p{Space}", "");
				logger.debug(tmpStrAfter);

				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					result = tmpStrAfter.getBytes("euckr");
				} catch (UnsupportedEncodingException e1) {
					logger.error(e1);
				}
				/*  수정일자 : 2022.11.28
				 *  작업자    : 신상훈
				 *  내용       : 전각을 반각으로 처리 끝
				 */
				try {
					int	k = result.length;
					logger.trace("변환전 길이[" + k + "]");
	            	while (k-- > 0 && (result[k] == 32)) {
	            		//logger.trace("[" + result[k] + "] " + k );
	            	}
					logger.trace("변환후 길이[" + k + "]");
	            	byte[] output = new byte[k+1];
	            	System.arraycopy(result, 0, output, 0, k+1);
	            	logger.debug("result 내용 [" + new String(result, encoding) + "]");
	            	logger.debug("output 내용 [" + new String(output, encoding) + "]");
	            	String	pre = new String(output, encoding);
	            	val.setString(sName, new String(pre.getBytes("utf8"), "utf8"));	// normalize.
					logger.trace("스트링 필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  val.getString(sName) + "]");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}
			if (sType.equals("B")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
//				try {
					logger.trace("바이트배열 필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
					val.setBytes(sName, result);
//				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
//					return -1;
//				}
			}

			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				logger.trace("필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" + Long.parseLong(new String(result).trim()) + "]");
				val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		logger.trace("전문 분석을 종료합니다");

		return pos;
	}

	/**
	 *
	 */
	public int disassembleMessageByteBodyAsMap(byte[] bMsg, int length, MyMap val, String msgKind, String msgCode, long valSize)
	{
		logger.trace("전문 분석을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문이 없습니다.");
			return -1;
		}

		// 공통부
		ArrayList<FieldItem>	head = mi.getHead();
		if (head == null) {
			logger.error("해당하는 공통부전문정의가 없습니다.");
			return -1;
		}

		int	totalLength = 0;
		for (int i = 0; i < head.size(); i++) {
			FieldItem	fi = head.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이["+totalLength+"], 전문길이["+length+"]");
			return -1;
		}

		int	pos = totalLength;	// 공통부 제외

		LinkedHashMap<String,ArrayList<FieldItem>>	bodies = mi.getBodies();
		if (bodies == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}
		ArrayList<FieldItem>	body = bodies.get(msgCode);
		if (body == null) {
			logger.trace("["+ msgCode + "]에 해당하는 개별부전문정의가 없습니다.");
			return 0;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이["+totalLength+"], 전문길이["+length+"]");
			return -1;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
//			int	nSize = valSize;
			int	nSize = fi.getSize();
            if (nSize == 0) {
                //가변길이
		    String	sLink = fi.getLink();
            long    lVal = val.getLong(sLink);
            nSize = (int)lVal;
            logger.trace("[" + sName + "]은 가변필드입니다. 길이[" + nSize + "]");
        }

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding) + "]");
					val.setString(sName, new String(result, encoding).trim());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}
			if (sType.equals("B")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding) + "]");
//					val.setBytes(sName, (new String(result, encoding).getBytes(encoding)));
					val.setBytes(sName, result);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}


			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				logger.debug("필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" +  Long.parseLong(new String(result).trim()) + "]");
				val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		logger.trace("전문 분석을 종료합니다");

		return pos;
	}

	/**
	 *
	 */
	public int disassembleMessageByteTailAsMap(byte[] bMsg, int length, MyMap val, String msgKind)
	{
		logger.trace("전문 분석을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문이 없습니다.");
			return -1;
		}

		ArrayList<FieldItem>	tail = mi.getTail();
		if (tail == null) {
			logger.trace("해당하는 꼬리부부전문정의가 없습니다.");
			return 0;
		}

		int	totalLength = 0;
		for (int i = 0; i < tail.size(); i++) {
			FieldItem	fi = tail.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이["+totalLength+"], 전문길이["+length+"]");
			return -1;
		}

		int	pos = 0;

		for (int i = 0; i < tail.size(); i++) {
			FieldItem	fi = tail.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding).trim() + "]");
					val.setString(sName, new String(result, encoding).trim());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}

			// 속성이 문자열인 경우
			if (sType.equals("B")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding) + "]");
					val.setBytes(sName, (new String(result,encoding)).getBytes(encoding));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				logger.trace("필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" +  Long.parseLong(new String(result).trim()) + "]");
				val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		logger.trace("전문 분석을 종료합니다");

		return 0;
	}

	/**
	 *
	 */
	public int disassembleMessageByteAsMap(byte[] bMsg, int length, MyMap val, String msgKind)
	{
		logger.trace("전문 분석을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문이 없습니다.");
			return -1;
		}

		ArrayList<FieldItem>	head = mi.getHead();
		if (head == null) {
			logger.error("해당하는 공통부전문정의가 없습니다.");
			return -1;
		}

		int	totalLength = 0;
		for (int i = 0; i < head.size(); i++) {
			FieldItem	fi = head.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이["+totalLength+"], 전문길이["+length+"]");
			return -1;
		}

		int	pos = 0;

		for (int i = 0; i < head.size(); i++) {
			FieldItem	fi = head.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding).trim() + "]");
					val.setString(sName, new String(result, encoding).trim());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}

			// 속성이 이진인 경우
			if (sType.equals("B")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding) + "]");

					// 한글이 마지막인 경우 1Byte가 손실되는 경우가 있어서 byte[]를 바로 대입하도록 수정
					// 2014.03.07 -- 신상훈
                    // val.setBytes(sName, new String(result, encoding).getBytes(encoding));
					val.setBytes(sName, result);

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}

			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				logger.trace("필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" +  Long.parseLong(new String(result).trim()) + "]");
				val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		LinkedHashMap<String,ArrayList<FieldItem>>	bodies = mi.getBodies();
		if (bodies == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}

    	String	keyValue = val.getString(mi.getKeyField());

		ArrayList<FieldItem>	body = bodies.get(keyValue);
		if (body == null) {
			logger.trace("[" + keyValue + "]에 해당하는 개별부전문정의가 없습니다.");
			return pos;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이["+totalLength+"], 전문길이["+length+"]");
			return -1;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();
            if (nSize == 0) {
                    //가변길이
			    String	sLink = fi.getLink();
                long    lVal = val.getLong(sLink);
                nSize = (int)lVal;
                logger.trace("[" + sName + "]은 가변필드입니다. 길이[" + nSize + "]");
            }

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("바디 필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding).trim() + "]");
					val.setString(sName, new String(result, encoding).trim());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}

			// 속성이 문자열인 경우
			if (sType.equals("B")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("바디 필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding) + "]");

	                // 한글이 마지막인 경우 1Byte가 손실되는 경우가 있어서 byte[]를 바로 대입하도록 수정
                    // 2014.03.07 -- 신상훈
                    // val.setBytes(sName, new String(result, encoding).getBytes(encoding));
                    val.setBytes(sName, result);

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}

			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
//				logger.trace("바디 필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" +  Long.parseLong(new String(result).trim()) + "]");
				logger.trace("바디 필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" + result + "]");
                val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		ArrayList<FieldItem>	tail = mi.getTail();
		if (tail == null) {
//			logger.error("해당하는 꼬리부부전문정의가 없습니다.");
			return 0;
		}

		for (int i = 0; i < tail.size(); i++) {
			FieldItem	fi = tail.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이["+totalLength+"], 전문길이["+length+"]");
			return -1;
		}

		for (int i = 0; i < tail.size(); i++) {
			FieldItem	fi = tail.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding).trim() + "]");
					val.setString(sName, new String(result, encoding).trim());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}

			// 속성이 문자열인 경우
			if (sType.equals("B")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.trace("바디 필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result, encoding) + "]");

	                // 한글이 마지막인 경우 1Byte가 손실되는 경우가 있어서 byte[]를 바로 대입하도록 수정
                    // 2014.03.07 -- 신상훈
                    // val.setBytes(sName, new String(result, encoding).getBytes(encoding));
                    val.setBytes(sName, result);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}

			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				logger.trace("필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" +  Long.parseLong(new String(result).trim()) + "]");
                val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		logger.trace("전문 분석을 종료합니다");

		return pos;
	}

	/**
	 *
	 */
	public int doParsingAsMap(String mgsFormat)
	{
		// TODO Auto-generated constructor stub
		logger.trace("전문형식 분석을 시작합니다");

		// 전문유형 파일 지정
		InputStream	is;

		if( mgsFormat == null || "".equals(mgsFormat)) {
			mgsFormat = "msgformat";
		}
		// msgformat1 : 100 지급명령
		// msgformat2 : 200 대량예금주조회
		// NeoDMWS6050_msgformat : 400~412, 999 보고서
		// NeoBATCHJOB_msgformat : 청백리( 계좌신규개설 및 계좌거래내역 )
		// msgformat4 : 공통 500~504 회계,부서,사용자,세입목,세입목매핑
		is = this.getClass().getResourceAsStream("/conf/"+mgsFormat+".xml");



		/* old
//		try {
//			is = new FileInputStream("conf/msgformat.xml");
			is = this.getClass().getResourceAsStream("/conf/msgformat.xml");
//		} catch (FileNotFoundException fnf) {
//			logger.error("파일이 없습니다.");
//			logger.trace("전문형식 분석을 종료합니다");
//			return -1;
//		}
 *
 */

		// XML문서 파싱을 위해 객체생성
		DocumentBuilderFactory	dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder	db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pcex) {
			logger.error(pcex.getLocalizedMessage());
			logger.trace("전문형식 분석을 종료합니다");
			return -1;
		}

		// XML을 파싱합니다.
		Document	doc;
		try {
			doc = db.parse(is);
		} catch (IOException ioex) {
			logger.error(ioex.getLocalizedMessage());
			logger.trace("전문형식 분석을 종료합니다");
			return -1;
		} catch (SAXException saxex) {
			logger.error(saxex.getLocalizedMessage());
			logger.trace("전문형식 분석을 종료합니다");
			return -1;
		} catch (IllegalArgumentException iaex) {
			logger.error(iaex.getLocalizedMessage());
			logger.trace("전문형식 분석을 종료합니다");
			return -1;
		}

		// Combine adjacent Text nodes into one single one
		doc.getDocumentElement().normalize();

		// 전문유형을 읽는다.
		NodeList	nodeList = doc.getElementsByTagName("messageFormat");

		// 전문유형별로 처리한다.
		for (int s = 0; s < nodeList.getLength(); s++) {
			Node	fstNode = nodeList.item(s);

			MessageItem	mi = new MessageItem();
			String	msgkey;

			// 노드의 속성을 분석한다.
			if (fstNode.hasAttributes()) {
				NamedNodeMap	nnmap = fstNode.getAttributes();
				Node	msgNumber = nnmap.getNamedItem("msgNumber");
				if (msgNumber == null) {
					logger.error("msgNumber 속성이 필요합니다");
					logger.trace("전문형식 분석을 종료합니다");
					return -1;
				}

				msgkey = msgNumber.getNodeValue();
/*
				logger.trace("<" + fstNode.getNodeName()
						+ " msgNumber=" + msgNumber.getNodeValue()
						+ ">");
*/
				} else {
				logger.error("msgNumber 속성이 필요합니다");
				logger.trace("전문형식 분석을 종료합니다");
				return -1;
			}

			// 공통부를 분석한다.
			NodeList	hl = ((Element)fstNode).getElementsByTagName("head");
			if (hl.getLength() != 1) {
				logger.error("공통부 정의는 1개이어야 합니다");
				logger.trace("전문형식 분석을 종료합니다");
				return -1;
			}

			for (int t = 0; t < hl.getLength(); t++) {
				Node	child = hl.item(t);

				// 공통부에서 상세부를 구분하는 필드이름을 정의한다.
				if (child.hasAttributes()) {
					NamedNodeMap	hmap = child.getAttributes();

					Node	keyname = hmap.getNamedItem("keyname");
					if (keyname == null) {
						logger.error("keyname 속성이 필요합니다");
						logger.trace("전문형식 분석을 종료합니다");
						return -1;
					}
					mi.setKeyField(keyname.getNodeValue());

					Node	sizeName = hmap.getNamedItem("sizeName");
					if (sizeName == null) {
						logger.error("sizeName 속성이 필요합니다");
						logger.trace("전문형식 분석을 종료합니다");
						return -1;
					}
					mi.setSizeField(sizeName.getNodeValue());

					Node	includeHead = hmap.getNamedItem("includeHead");
					if (includeHead == null) {
						logger.error("includeHead 속성이 필요합니다");
						logger.trace("전문형식 분석을 종료합니다");
						return -1;
					}
					if (includeHead.getNodeValue().equals("yes"))
						mi.setIncludeHead(true);
					else if (includeHead.getNodeValue().equals("no"))
						mi.setIncludeHead(false);

/*
					logger.trace("<" + child.getNodeName()
							+ " keyName=" + "\"" + keyname.getNodeValue() + "\""
							+ ">");
*/
					} else {
					logger.error("keyname 속성이 필요합니다");
					logger.trace("전문형식 분석을 종료합니다");
					return -1;
				}

				// 공통부의 필드를 정의한다.
				NodeList	hfl = ((Element)child).getElementsByTagName("field");

				ArrayList<FieldItem>	h = new ArrayList<FieldItem>();

				for (int tt = 0; tt < hfl.getLength(); tt++) {
					Node	gchild = hfl.item(tt);

					// 필드의 이름과 크기등은 속성으로 정의한다.
					if (gchild.hasAttributes()) {
						NamedNodeMap	hfmap = gchild.getAttributes();

						FieldItem	fi = new FieldItem();

						// 구분
						Node	id = hfmap.getNamedItem("id");
						fi.setId(id.getNodeValue());

						// 필드반복 횟수
						Node	repetition = hfmap.getNamedItem("repetition");
						try {
							fi.setRepetition(Integer.parseInt(repetition.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							logger.trace("전문형식 분석을 종료합니다");
							return -1;
						}

						// 필드이름
						Node	name = hfmap.getNamedItem("name");
						fi.setName(name.getNodeValue());

						// 필드 크기
						Node	size = hfmap.getNamedItem("size");
						try {
							fi.setSize(Integer.parseInt(size.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							logger.trace("전문형식 분석을 종료합니다");
							return -1;
						}

						// 필드 위치
/*						Node	position = hfmap.getNamedItem("position");
						try {
							fi.setPosition(Integer.parseInt(position.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							return -1;
						}
*/
						// 필드종류
						Node	type = hfmap.getNamedItem("type");
						fi.setType(type.getNodeValue());

						// 필드형식
						Node	format = hfmap.getNamedItem("format");
						fi.setFormat(format.getNodeValue());

						h.add(fi);
						/*
						logger.trace("\t\t"
								+ "<" + gchild.getNodeName()
								+ " id=" + "\"" + id.getNodeValue() + "\""
								+ " repetition=" + "\"" + repetition.getNodeValue() + "\""
								+ " name=" + "\"" + name.getNodeValue() + "\""
								+ " size=" + "\"" + size.getNodeValue() + "\""
//								+ " position=" + "\"" + position.getNodeValue() + "\""
								+ " type=" + "\"" + type.getNodeValue() + "\""
								+ " format=" + "\"" + format.getNodeValue() + "\""
								+ "/>");
*/
						} else {
						logger.error("id,repetition,name,size,position,type,format 속성이 필요합니다");
						logger.trace("전문형식 분석을 종료합니다");
						return -1;
					}
				}
				mi.setHead(h);
			}

			// 개별부를 분석한다.
			NodeList	bl = ((Element)fstNode).getElementsByTagName("body");
			LinkedHashMap<String,ArrayList<FieldItem>>	bs = new LinkedHashMap<String,ArrayList<FieldItem>>();
			String	key2;

			for (int t = 0; t < bl.getLength(); t++) {
				Node	child = bl.item(t);

				// 상세부를 구분하는 필드값과 실행할 클래스이름을 정의한다.
				if (child.hasAttributes()) {
					NamedNodeMap	hmap = child.getAttributes();

					LinkedHashMap<String,String>	r = new LinkedHashMap<String,String>();

					// 개별부구분하는 값.
					Node	keyvalue = hmap.getNamedItem("keyvalue");
					if (keyvalue == null) {
						logger.error("keyvalue 속성이 필요합니다");
						logger.trace("전문형식 분석을 종료합니다");
						return -1;
					}
					key2 = keyvalue.getNodeValue();

					// 개별부전문을 받은 후 실행할 클래스명.
					Node	className = hmap.getNamedItem("className");
					if (className == null) {
						logger.error("className 속성이 필요합니다");
						logger.trace("전문형식 분석을 종료합니다");
						return -1;
					}

					r.put(key2, className.getNodeValue());
					mi.setRuns(r);
/*
					logger.trace("<" + child.getNodeName()
							+ " keyvalue=" + "\"" + keyvalue.getNodeValue() + "\""
							+ " className=" + "\"" + className.getNodeValue() + "\""
							+ ">");
*/
					} else {
					logger.error("속성이 필요합니다");
					logger.trace("전문형식 분석을 종료합니다");
					return -1;
				}

				// 개별부의 필드를 정의한다.
				NodeList	hfl = ((Element)child).getElementsByTagName("field");

				ArrayList<FieldItem>	b = new ArrayList<FieldItem>();

				for (int tt = 0; tt < hfl.getLength(); tt++) {
					Node	gchild = hfl.item(tt);

					if (gchild.hasAttributes()) {
						NamedNodeMap	hfmap = gchild.getAttributes();

						FieldItem	fi = new FieldItem();

						// 구분
						Node	id = hfmap.getNamedItem("id");
						fi.setId(id.getNodeValue());

						// 필드반복 횟수
						Node	repetition = hfmap.getNamedItem("repetition");
						try {
							fi.setRepetition(Integer.parseInt(repetition.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							logger.trace("전문형식 분석을 종료합니다");
							return -1;
						}

						// 필드이름
						Node	name = hfmap.getNamedItem("name");
						fi.setName(name.getNodeValue());

						// 필드 크기
						Node	size = hfmap.getNamedItem("size");
						try {
							fi.setSize(Integer.parseInt(size.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							logger.trace("전문형식 분석을 종료합니다");
							return -1;
						}

						// 필드 위치
/*
 						Node	position = hfmap.getNamedItem("position");
 						try {
							fi.setPosition(Integer.parseInt(position.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							return -1;
						}
*/
						// 필드종류
						Node	type = hfmap.getNamedItem("type");
						fi.setType(type.getNodeValue());

						// 필드형식
						Node	link = hfmap.getNamedItem("link");
						if (link != null)
						    fi.setLink(link.getNodeValue());

						// 필드형식
						Node	format = hfmap.getNamedItem("format");
						fi.setFormat(format.getNodeValue());

						b.add(fi);
/*
						logger.trace("\t\t"
								+ "<" + gchild.getNodeName()
								+ " id=" + "\"" + id.getNodeValue() + "\""
								+ " repetition=" + "\"" + repetition.getNodeValue() + "\""
								+ " name=" + "\"" + name.getNodeValue() + "\""
								+ " size=" + "\"" + size.getNodeValue() + "\""
//								+ " position=" + "\"" + position.getNodeValue() + "\""
								+ " type=" + "\"" + type.getNodeValue() + "\""
								+ " format=" + "\"" + format.getNodeValue() + "\""
								+ "/>");
*/
						} else {
						logger.error("keyName 속성이 필요합니다");
						logger.trace("전문형식 분석을 종료합니다");
						return -1;
					}
					bs.put(key2, b);
				}
				mi.setBodies(bs);
			}

			// 꼬리부를 분석한다.
			NodeList	tl = ((Element)fstNode).getElementsByTagName("tail");

			for (int t = 0; t < tl.getLength(); t++) {
				Node	child = tl.item(t);

				NodeList	hfl = ((Element)child).getElementsByTagName("field");

				ArrayList<FieldItem>	alt = new ArrayList<FieldItem>();

				for (int tt = 0; tt < hfl.getLength(); tt++) {
					Node	gchild = hfl.item(tt);

					if (gchild.hasAttributes()) {
						NamedNodeMap	hfmap = gchild.getAttributes();

						FieldItem	fi = new FieldItem();

						// 구분
						Node	id = hfmap.getNamedItem("id");
						fi.setId(id.getNodeValue());

						// 필드반복 횟수
						Node	repetition = hfmap.getNamedItem("repetition");
						try {
							fi.setRepetition(Integer.parseInt(repetition.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							return -1;
						}

						// 필드이름
						Node	name = hfmap.getNamedItem("name");
						fi.setName(name.getNodeValue());

						// 필드 크기
						Node	size = hfmap.getNamedItem("size");
						try {
							fi.setSize(Integer.parseInt(size.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							return -1;
						}

						// 필드 위치
						Node	position = hfmap.getNamedItem("position");
						try {
							fi.setPosition(Integer.parseInt(position.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							logger.trace("전문형식 분석을 종료합니다");
							return -1;
						}

						// 필드종류
						Node	type = hfmap.getNamedItem("type");
						fi.setType(type.getNodeValue());

						// 필드형식
						Node	format = hfmap.getNamedItem("format");
						fi.setFormat(format.getNodeValue());

						alt.add(fi);

/*
						logger.trace("\t\t"

								+ "<" + gchild.getNodeName()
								+ " id=" + "\"" + id.getNodeValue() + "\""
								+ " repetition=" + "\"" + repetition.getNodeValue() + "\""
								+ " name=" + "\"" + name.getNodeValue() + "\""
								+ " size=" + "\"" + size.getNodeValue() + "\""
//								+ " position=" + "\"" + format.getNodeValue() + "\""
								+ " type=" + "\"" + type.getNodeValue() + "\""
								+ " format=" + "\"" + format.getNodeValue() + "\""
								+ "/>");
*/
					} else {
						logger.error("keyName 속성이 필요합니다");
						logger.trace("전문형식 분석을 종료합니다");
						return -1;
					}
				}
				mi.setTail(alt);
			}
			msgfmts.setAny(msgkey, mi);
		}
		logger.trace("전문형식 분석을 종료합니다");
		return 0;
	}

	public int disassembleFileByteBodyAsMap6050_5(byte[] bMsg, int length, MyMap val, String msgKind, String msgCode)
	{
		logger.trace("전문 분석을 시작합니다");

		String	encoding = "euckr";

		MessageItem	mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문이 없습니다.");
			return -1;
		}

		// 공통부
		ArrayList<FieldItem>	head = mi.getHead();
		if (head == null) {
			logger.error("해당하는 공통부전문정의가 없습니다.");
			return -1;
		}

		int	pos = 0;
		int	totalLength = 0;

		LinkedHashMap<String,ArrayList<FieldItem>>	bodies = mi.getBodies();
		if (bodies == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}
		ArrayList<FieldItem>	body = bodies.get(msgCode);
		if (body == null) {
			logger.trace("해당하는 개별부전문정의가 없습니다.");
			return 0;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이["+totalLength+"], 전문길이["+length+"]");
			return -1;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);

			String	sName = fi.getName();
			String	sType = fi.getType();
			int	nSize = fi.getSize();
            if (nSize == 0) {
                    //가변길이
			    String	sLink = fi.getLink();
                long    lVal = val.getLong(sLink);
                nSize = (int)lVal;
                logger.trace("[" + sName + "]은 가변필드입니다. 길이[" + nSize + "]");
            }

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					int	k = result.length;
//					logger.trace("변환전 길이[" + k + "]");
	            	while (k-- > 0 && (result[k] == 32 || result[k] == -95)) {
//	            		logger.trace("[" + result[k] + "]");
	            	}
//					logger.trace("변환후 길이[" + k + "]");
	            	byte[] output = new byte[k+1];
	            	System.arraycopy(result, 0, output, 0, k+1);
	            	String	pre = new String(output, encoding);
//	            	val.setString(sName, new String(pre.getBytes("utf8"), "utf8"));	// normalize.
	            	val.setString(sName, new String(result, encoding).trim());//기존걸로 해서안되서 함수자체를 추가함.
					logger.trace("스트링 필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  val.getString(sName) + "]");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}
			if (sType.equals("B")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
//				try {
					logger.trace("바이트배열 필드명[" + sName + "], 필드크기["+nSize+"], 필드값[" +  new String(result) + "]");
					val.setBytes(sName, result);
//				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
//					return -1;
//				}
			}

			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[]	result = new byte[nSize];
				java.util.Arrays.fill(result, (byte)0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				logger.trace("필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" + Long.parseLong(new String(result).trim()) + "]");
				val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		logger.trace("전문 분석을 종료합니다");

		return pos;
	}

}
