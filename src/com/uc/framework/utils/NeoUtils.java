/**
 *
 */
package com.uc.framework.utils;

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

import com.uc.framework.parsing.FieldItem;
import com.uc.framework.parsing.MessageItem;

/**
 * @author hotaep
 *
 */
public class NeoUtils {
	private static String FLAG_Y = "Y";
	private static String FLAG_N = "N";
	private static String FLAG_ALL = "A";
	private Logger logger;
	private MyMap msgfmts;

	public static String getGbnCode(MyMap ht) throws Exception {
		String trnxNo = "";

		System.out.println("##### ANE_LIM_ACC_YN[" + ht.getString("ANE_LIM_ACC_YN") + "]");
		System.out.println("##### DPST_ANE_LIM_ACC_YN[" + ht.getString("DPST_ANE_LIM_ACC_YN") + "]");

		// 지급원장이 한도계좌이고 입금명세가 한도계좌일경우
		if (FLAG_Y.equals(ht.getString("ANE_LIM_ACC_YN")) && FLAG_Y.equals(ht.getString("DPST_ANE_LIM_ACC_YN"))) {
			trnxNo = FLAG_Y; // 금고 전송만 해당
		} else if (FLAG_N.equals(ht.getString("ANE_LIM_ACC_YN"))
				&& FLAG_Y.equals(ht.getString("DPST_ANE_LIM_ACC_YN"))) {
			trnxNo = FLAG_Y; // 금고 전송만 해당
		} else if (FLAG_Y.equals(ht.getString("ANE_LIM_ACC_YN"))
				&& FLAG_N.equals(ht.getString("DPST_ANE_LIM_ACC_YN"))) {
			trnxNo = FLAG_ALL; // 지급과 금고 전송 모두 해당
		} else {
			trnxNo = FLAG_N; // 지급만 해당
		}
		return trnxNo;
	}

	public void fileToPrint(String mgsFormat, byte[] bMsg, int length, MyMap val, String msgKind, String msgCode,
			Logger logger, String gbn) {
		if (doParsingAsMap(mgsFormat, logger) < 0) {
			logger.info("전문포맷 분석오류");
			return;
		} else {
			if ("B".equals(gbn)) {
				fileBytePrint(bMsg, length, val, msgKind, msgCode, logger);
			} else if ("H".equals(gbn)) {
				fileBytePrintHead(bMsg, length, val, msgKind, logger);
			} else {
				fileBytePrintTail(bMsg, length, val, msgKind, logger);
			}

		}
	}

	public int doParsingAsMap(String mgsFormat, Logger logger) {
		this.logger = logger;
		msgfmts = new MyMap();

		// TODO Auto-generated constructor stub
		logger.info("전문형식 분석을 시작합니다");

		if (mgsFormat == null || "".equals(mgsFormat)) {
			mgsFormat = "msgformat";
		}
		// msgformat1 : 100 지급명령
		// msgformat2 : 200 대량예금주조회
		// NeoDMWS6050_msgformat : 400~412, 999 보고서
		// NeoBATCHJOB_msgformat : 청백리( 계좌신규개설 및 계좌거래내역 )
		// msgformat4 : 공통 500~504 회계,부서,사용자,세입목,세입목매핑
		// 전문유형 파일 지정
		InputStream is = this.getClass().getResourceAsStream("/conf/" + mgsFormat + ".xml");

		/*
		 * old // try { // is = new FileInputStream("conf/msgformat.xml"); is =
		 * this.getClass().getResourceAsStream("/conf/msgformat.xml"); // } catch
		 * (FileNotFoundException fnf) { // logger.error("파일이 없습니다."); //
		 * logger.trace("전문형식 분석을 종료합니다"); // return -1; // }
		 *
		 */

		// XML문서 파싱을 위해 객체생성
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pcex) {
			logger.error(pcex.getLocalizedMessage());
			logger.error("전문형식 분석을 종료합니다");
			return -1;
		}

		// XML을 파싱합니다.
		Document doc;
		try {
			doc = db.parse(is);
		} catch (IOException ioex) {
			logger.error(ioex.getLocalizedMessage());
			logger.error("전문형식 분석을 종료합니다");
			return -1;
		} catch (SAXException saxex) {
			logger.error(saxex.getLocalizedMessage());
			logger.error("전문형식 분석을 종료합니다");
			return -1;
		} catch (IllegalArgumentException iaex) {
			logger.error(iaex.getLocalizedMessage());
			logger.error("전문형식 분석을 종료합니다");
			return -1;
		}

		// Combine adjacent Text nodes into one single one
		doc.getDocumentElement().normalize();

		// 전문유형을 읽는다.
		NodeList nodeList = doc.getElementsByTagName("messageFormat");

		// 전문유형별로 처리한다.
		for (int s = 0; s < nodeList.getLength(); s++) {
			Node fstNode = nodeList.item(s);

			MessageItem mi = new MessageItem();
			String msgkey;

			// 노드의 속성을 분석한다.
			if (fstNode.hasAttributes()) {
				NamedNodeMap nnmap = fstNode.getAttributes();
				Node msgNumber = nnmap.getNamedItem("msgNumber");
				if (msgNumber == null) {
					logger.error("msgNumber 속성이 필요합니다");
					logger.error("전문형식 분석을 종료합니다");
					return -1;
				}

				msgkey = msgNumber.getNodeValue();
				/*
				 * logger.trace("<" + fstNode.getNodeName() + " msgNumber=" +
				 * msgNumber.getNodeValue() + ">");
				 */
			} else {
				logger.error("msgNumber 속성이 필요합니다");
				logger.error("전문형식 분석을 종료합니다");
				return -1;
			}

			// 공통부를 분석한다.
			NodeList hl = ((Element) fstNode).getElementsByTagName("head");
			if (hl.getLength() != 1) {
				logger.error("공통부 정의는 1개이어야 합니다");
				logger.error("전문형식 분석을 종료합니다");
				return -1;
			}

			for (int t = 0; t < hl.getLength(); t++) {
				Node child = hl.item(t);

				// 공통부에서 상세부를 구분하는 필드이름을 정의한다.
				if (child.hasAttributes()) {
					NamedNodeMap hmap = child.getAttributes();

					Node keyname = hmap.getNamedItem("keyname");
					if (keyname == null) {
						logger.error("keyname 속성이 필요합니다");
						logger.error("전문형식 분석을 종료합니다");
						return -1;
					}
					mi.setKeyField(keyname.getNodeValue());

					Node sizeName = hmap.getNamedItem("sizeName");
					if (sizeName == null) {
						logger.error("sizeName 속성이 필요합니다");
						logger.error("전문형식 분석을 종료합니다");
						return -1;
					}
					mi.setSizeField(sizeName.getNodeValue());

					Node includeHead = hmap.getNamedItem("includeHead");
					if (includeHead == null) {
						logger.error("includeHead 속성이 필요합니다");
						logger.error("전문형식 분석을 종료합니다");
						return -1;
					}
					if (includeHead.getNodeValue().equals("yes"))
						mi.setIncludeHead(true);
					else if (includeHead.getNodeValue().equals("no"))
						mi.setIncludeHead(false);

					logger.trace("<" + child.getNodeName() + " keyName=" + "\"" + keyname.getNodeValue() + "\"" + ">");

				} else {
					logger.error("keyname 속성이 필요합니다");
					logger.error("전문형식 분석을 종료합니다");
					return -1;
				}

				// 공통부의 필드를 정의한다.
				NodeList hfl = ((Element) child).getElementsByTagName("field");

				ArrayList<FieldItem> h = new ArrayList<FieldItem>();

				for (int tt = 0; tt < hfl.getLength(); tt++) {
					Node gchild = hfl.item(tt);

					// 필드의 이름과 크기등은 속성으로 정의한다.
					if (gchild.hasAttributes()) {
						NamedNodeMap hfmap = gchild.getAttributes();

						FieldItem fi = new FieldItem();

						// 구분
						Node id = hfmap.getNamedItem("id");
						fi.setId(id.getNodeValue());

						// 필드반복 횟수
						Node repetition = hfmap.getNamedItem("repetition");
						try {
							fi.setRepetition(Integer.parseInt(repetition.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							logger.error("전문형식 분석을 종료합니다");
							return -1;
						}

						// 필드이름
						Node name = hfmap.getNamedItem("name");
						fi.setName(name.getNodeValue());

						// 필드 크기
						Node size = hfmap.getNamedItem("size");
						try {
							fi.setSize(Integer.parseInt(size.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							logger.error("전문형식 분석을 종료합니다");
							return -1;
						}

						// 필드 위치
						/*
						 * Node position = hfmap.getNamedItem("position"); try {
						 * fi.setPosition(Integer.parseInt(position.getNodeValue())); } catch
						 * (NumberFormatException nfex) { logger.error(nfex.getLocalizedMessage());
						 * return -1; }
						 */
						// 필드종류
						Node type = hfmap.getNamedItem("type");
						fi.setType(type.getNodeValue());

						// 필드형식
						Node format = hfmap.getNamedItem("format");
						fi.setFormat(format.getNodeValue());

						h.add(fi);

//						logger.info("\t\t"
//								+ "<" + gchild.getNodeName()
//								+ " id=" + "\"" + id.getNodeValue() + "\""
//								+ " repetition=" + "\"" + repetition.getNodeValue() + "\""
//								+ " name=" + "\"" + name.getNodeValue() + "\""
//								+ " size=" + "\"" + size.getNodeValue() + "\""
////								+ " position=" + "\"" + position.getNodeValue() + "\""
//								+ " type=" + "\"" + type.getNodeValue() + "\""
//								+ " format=" + "\"" + format.getNodeValue() + "\""
//								+ "/>");

					} else {
						logger.error("id,repetition,name,size,position,type,format 속성이 필요합니다");
						logger.error("전문형식 분석을 종료합니다");
						return -1;
					}
				}
				mi.setHead(h);
			}

			// 개별부를 분석한다.
			NodeList bl = ((Element) fstNode).getElementsByTagName("body");
			LinkedHashMap<String, ArrayList<FieldItem>> bs = new LinkedHashMap<String, ArrayList<FieldItem>>();
			String key2;

			for (int t = 0; t < bl.getLength(); t++) {
				Node child = bl.item(t);

				// 상세부를 구분하는 필드값과 실행할 클래스이름을 정의한다.
				if (child.hasAttributes()) {
					NamedNodeMap hmap = child.getAttributes();

					LinkedHashMap<String, String> r = new LinkedHashMap<String, String>();

					// 개별부구분하는 값.
					Node keyvalue = hmap.getNamedItem("keyvalue");
					if (keyvalue == null) {
						logger.error("keyvalue 속성이 필요합니다");
						logger.error("전문형식 분석을 종료합니다");
						return -1;
					}
					key2 = keyvalue.getNodeValue();

					// 개별부전문을 받은 후 실행할 클래스명.
					Node className = hmap.getNamedItem("className");
					if (className == null) {
						logger.error("className 속성이 필요합니다");
						logger.error("전문형식 분석을 종료합니다");
						return -1;
					}

					r.put(key2, className.getNodeValue());
					mi.setRuns(r);

//					logger.info("<" + child.getNodeName()
//							+ " keyvalue=" + "\"" + keyvalue.getNodeValue() + "\""
//							+ " className=" + "\"" + className.getNodeValue() + "\""
//							+ ">");

				} else {
					logger.error("속성이 필요합니다");
					logger.error("전문형식 분석을 종료합니다");
					return -1;
				}

				// 개별부의 필드를 정의한다.
				NodeList hfl = ((Element) child).getElementsByTagName("field");

				ArrayList<FieldItem> b = new ArrayList<FieldItem>();

				for (int tt = 0; tt < hfl.getLength(); tt++) {
					Node gchild = hfl.item(tt);

					if (gchild.hasAttributes()) {
						NamedNodeMap hfmap = gchild.getAttributes();

						FieldItem fi = new FieldItem();

						// 구분
						Node id = hfmap.getNamedItem("id");
						fi.setId(id.getNodeValue());

						// 필드반복 횟수
						Node repetition = hfmap.getNamedItem("repetition");
						try {
							fi.setRepetition(Integer.parseInt(repetition.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							logger.error("전문형식 분석을 종료합니다");
							return -1;
						}

						// 필드이름
						Node name = hfmap.getNamedItem("name");
						fi.setName(name.getNodeValue());

						// 필드 크기
						Node size = hfmap.getNamedItem("size");
						try {
							fi.setSize(Integer.parseInt(size.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							logger.error("전문형식 분석을 종료합니다");
							return -1;
						}

						// 필드 위치
						/*
						 * Node position = hfmap.getNamedItem("position"); try {
						 * fi.setPosition(Integer.parseInt(position.getNodeValue())); } catch
						 * (NumberFormatException nfex) { logger.error(nfex.getLocalizedMessage());
						 * return -1; }
						 */
						// 필드종류
						Node type = hfmap.getNamedItem("type");
						fi.setType(type.getNodeValue());

						// 필드형식
						Node link = hfmap.getNamedItem("link");
						if (link != null)
							fi.setLink(link.getNodeValue());

						// 필드형식
						Node format = hfmap.getNamedItem("format");
						fi.setFormat(format.getNodeValue());

						b.add(fi);

//						logger.info("\t\t"
//								+ "<" + gchild.getNodeName()
//								+ " id=" + "\"" + id.getNodeValue() + "\""
//								+ " repetition=" + "\"" + repetition.getNodeValue() + "\""
//								+ " name=" + "\"" + name.getNodeValue() + "\""
//								+ " size=" + "\"" + size.getNodeValue() + "\""
////								+ " position=" + "\"" + position.getNodeValue() + "\""
//								+ " type=" + "\"" + type.getNodeValue() + "\""
//								+ " format=" + "\"" + format.getNodeValue() + "\""
//								+ "/>");

					} else {
						logger.error("keyName 속성이 필요합니다");
						logger.error("전문형식 분석을 종료합니다");
						return -1;
					}
					bs.put(key2, b);
				}
				mi.setBodies(bs);
			}

			// 꼬리부를 분석한다.
			NodeList tl = ((Element) fstNode).getElementsByTagName("tail");

			for (int t = 0; t < tl.getLength(); t++) {
				Node child = tl.item(t);

				NodeList hfl = ((Element) child).getElementsByTagName("field");

				ArrayList<FieldItem> alt = new ArrayList<FieldItem>();

				for (int tt = 0; tt < hfl.getLength(); tt++) {
					Node gchild = hfl.item(tt);

					if (gchild.hasAttributes()) {
						NamedNodeMap hfmap = gchild.getAttributes();

						FieldItem fi = new FieldItem();

						// 구분
						Node id = hfmap.getNamedItem("id");
						fi.setId(id.getNodeValue());

						// 필드반복 횟수
						Node repetition = hfmap.getNamedItem("repetition");
						try {
							fi.setRepetition(Integer.parseInt(repetition.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							return -1;
						}

						// 필드이름
						Node name = hfmap.getNamedItem("name");
						fi.setName(name.getNodeValue());

						// 필드 크기
						Node size = hfmap.getNamedItem("size");
						try {
							fi.setSize(Integer.parseInt(size.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							return -1;
						}

						// 필드 위치
						Node position = hfmap.getNamedItem("position");
						try {
							fi.setPosition(Integer.parseInt(position.getNodeValue()));
						} catch (NumberFormatException nfex) {
							logger.error(nfex.getLocalizedMessage());
							logger.error("전문형식 분석을 종료합니다");
							return -1;
						}

						// 필드종류
						Node type = hfmap.getNamedItem("type");
						fi.setType(type.getNodeValue());

						// 필드형식
						Node format = hfmap.getNamedItem("format");
						fi.setFormat(format.getNodeValue());

						alt.add(fi);

//						logger.info("\t\t"
//
//								+ "<" + gchild.getNodeName()
//								+ " id=" + "\"" + id.getNodeValue() + "\""
//								+ " repetition=" + "\"" + repetition.getNodeValue() + "\""
//								+ " name=" + "\"" + name.getNodeValue() + "\""
//								+ " size=" + "\"" + size.getNodeValue() + "\""
////								+ " position=" + "\"" + format.getNodeValue() + "\""
//								+ " type=" + "\"" + type.getNodeValue() + "\""
//								+ " format=" + "\"" + format.getNodeValue() + "\""
//								+ "/>");

					} else {
						logger.error("keyName 속성이 필요합니다");
						logger.error("전문형식 분석을 종료합니다");
						return -1;
					}
				}
				mi.setTail(alt);
			}
//			logger.info("OKOKOKOK###############################");
//			logger.info("OKOKOKOK["+msgkey+"]");
//			logger.info("OKOKOKOK111["+mi.getHeadLength()+"]");
//			logger.info("OKOKOKOK222["+mi.getBodyLength("100")+"]");
//			logger.info("OKOKOKOK333["+mi.getTailLength()+"]");
			msgfmts.setAny(msgkey, mi);
		}
		logger.info("전문형식 분석을 종료합니다");
		return 0;
	}

	public int fileBytePrint(byte[] bMsg, int length, MyMap val, String msgKind, String msgCode, Logger logger) {
		logger.info("전문 분석을 시작합니다");

		String encoding = "euckr";

		MessageItem mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문이 없습니다.");
			return -1;
		}

		// 공통부
		ArrayList<FieldItem> head = mi.getHead();
		if (head == null) {
			logger.error("해당하는 공통부전문정의가 없습니다.");
			return -1;
		}

		int pos = 0;
		int totalLength = 0;

		LinkedHashMap<String, ArrayList<FieldItem>> bodies = mi.getBodies();
		if (bodies == null) {
			logger.error("해당하는 전문 유형이 없습니다.");
			return -1;
		}
		ArrayList<FieldItem> body = bodies.get(msgCode);
//		if (body == null) {
//			logger.trace("해당하는 개별부전문정의가 없습니다.");
//			return 0;
//		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem fi = body.get(i);
			totalLength += fi.getSize();
		}
		logger.info("입력된 전문길이  길이[" + totalLength + "], 전문길이[" + length + "]");
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이[" + totalLength + "], 전문길이[" + length + "]");
			return -1;
		}

		for (int i = 0; i < body.size(); i++) {
			FieldItem fi = body.get(i);

			String sName = fi.getName();
			String sType = fi.getType();
			int nSize = fi.getSize();
			if (nSize == 0) {
				// 가변길이
				String sLink = fi.getLink();
				long lVal = val.getLong(sLink);
				nSize = (int) lVal;
				logger.info("[" + sName + "]은 가변필드입니다. 길이[" + nSize + "]");
			}

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				byte[] result = new byte[nSize];
				java.util.Arrays.fill(result, (byte) 0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.error("전문 분석을 종료합니다");
					return -1;
				}
				try {
					int k = result.length;
					// logger.debug("변환전 길이[" + k + "]");
					while (k-- > 0 && (result[k] == 32 || result[k] == -95)) {
						// logger.debug("[" + result[k] + "] " + k );
					}
					// logger.debug("변환후 길이[" + k + "]");
					byte[] output = new byte[k + 1];
					System.arraycopy(result, 0, output, 0, k + 1);
					// logger.debug("result 내용 [" + new String(result, encoding) + "]");
					// logger.debug("output 내용 [" + new String(output, encoding) + "]");
					String pre = new String(output, encoding);
					logger.info("필드명[" + LPAD(sName, 30, "-") + "]필드크기[" + LPAD(String.valueOf(nSize), 5, "-") + "]필드값[" + new String(pre.getBytes("utf8"), "utf8") + "]");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}
			if (sType.equals("B")) {
				byte[] result = new byte[nSize];
				java.util.Arrays.fill(result, (byte) 0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.error("전문 분석을 종료합니다");
					return -1;
				}
//				try {
				logger.info("필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" + new String(result) + "]");
//					val.setBytes(sName, result);
//				} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
//					e.printStackTrace();
//					return -1;
//				}
			}

			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[] result = new byte[nSize];
				java.util.Arrays.fill(result, (byte) 0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.error("전문 분석을 종료합니다");
					return -1;
				}
				logger.info("필드명[" + LPAD(sName, 30, "-") + "]필드크기[" +LPAD(String.valueOf(nSize),5,"-") + "]필드값[" + Long.parseLong(new String(result).trim())
						+ "]");
//				val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		logger.info("전문 분석을 종료합니다");

		return pos;
	}

	public int fileBytePrintHead(byte[] bMsg, int length, MyMap val, String msgKind, Logger logger) {
		logger.info("전문 분석을 시작합니다");

		String encoding = "euckr";

		MessageItem mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문이 없습니다.");
			return -1;
		}

		ArrayList<FieldItem> head = mi.getHead();
		if (head == null) {
			logger.error("해당하는 공통부전문정의가 없습니다.");
			return -1;
		}

		int totalLength = 0;
		for (int i = 0; i < head.size(); i++) {
			FieldItem fi = head.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이[" + totalLength + "], 전문길이[" + length + "]");
			return -1;
		}

		int pos = 0;

		for (int i = 0; i < head.size(); i++) {
			FieldItem fi = head.get(i);

			String sName = fi.getName();
			String sType = fi.getType();
			int nSize = fi.getSize();

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				byte[] result = new byte[nSize];
				java.util.Arrays.fill(result, (byte) 0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.info("필드명[" + LPAD(sName, 30, "-") + "]필드크기[" + LPAD(String.valueOf(nSize),5,"-") + "]필드값[" + new String(result, encoding).trim()
							+ "]");
//					val.setString(sName, new String(result, encoding).trim());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}
			if (sType.equals("B")) {
				byte[] result = new byte[nSize];
				java.util.Arrays.fill(result, (byte) 0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.info("필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" + new String(result, encoding) + "]");
					// val.setBytes(sName, (new String(result, encoding)).getBytes(encoding));
//					val.setBytes(sName, result);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}
			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[] result = new byte[nSize];
				java.util.Arrays.fill(result, (byte) 0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				logger.info("필드명[" + LPAD(sName, 30, "-") + "]필드크기[" + LPAD(String.valueOf(nSize),5,"-") + "]필드값[" + Long.parseLong(new String(result).trim())
						+ "]");
//				val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		logger.trace("전문 분석을 종료합니다");

		return 0;
	}

	public int fileBytePrintTail(byte[] bMsg, int length, MyMap val, String msgKind, Logger logger) {
		logger.info("전문 분석을 시작합니다");

		String encoding = "euckr";

		MessageItem mi = (MessageItem) msgfmts.getAny(msgKind);
		if (mi == null) {
			logger.error("해당하는 전문이 없습니다.");
			return -1;
		}

		ArrayList<FieldItem> tail = mi.getTail();
		if (tail == null) {
			logger.trace("해당하는 꼬리부부전문정의가 없습니다.");
			return 0;
		}

		int totalLength = 0;
		for (int i = 0; i < tail.size(); i++) {
			FieldItem fi = tail.get(i);
			totalLength += fi.getSize();
		}
		if (totalLength > length) {
			logger.error("입력된 전문길이가 짧습니다. 길이[" + totalLength + "], 전문길이[" + length + "]");
			return -1;
		}

		int pos = 0;

		for (int i = 0; i < tail.size(); i++) {
			FieldItem fi = tail.get(i);

			String sName = fi.getName();
			String sType = fi.getType();
			int nSize = fi.getSize();

			// 속성이 문자열인 경우
			if (sType.equals("AN")) {
				byte[] result = new byte[nSize];
				java.util.Arrays.fill(result, (byte) 0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.info("필드명[" + LPAD(sName, 30, "-") + "]필드크기[" + LPAD(String.valueOf(nSize),5,"-") + "]필드값[" + new String(result, encoding).trim()
							+ "]");
//					val.setString(sName, new String(result, encoding).trim());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return -1;
				}
			}

			// 속성이 문자열인 경우
			if (sType.equals("B")) {
				byte[] result = new byte[nSize];
				java.util.Arrays.fill(result, (byte) 0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				try {
					logger.info("필드명[" + sName + "], 필드크기[" + nSize + "], 필드값[" + new String(result, encoding) + "]");
//					val.setBytes(sName, (new String(result,encoding)).getBytes(encoding));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// 속성이 숫자인경우
			if (sType.equals("N")) {
				byte[] result = new byte[nSize];
				java.util.Arrays.fill(result, (byte) 0);
				try {
					System.arraycopy(bMsg, pos, result, 0, nSize);
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					logger.trace("전문 분석을 종료합니다");
					return -1;
				}
				logger.info("필드명[" + LPAD(sName, 30, "-") + "]필드크기[" + LPAD(String.valueOf(nSize),5,"-") + "]필드값[" + Long.parseLong(new String(result).trim())
						+ "]");
//				val.setLong(sName, Long.parseLong(new String(result).trim()));
			}
			pos += nSize;
		}

		logger.trace("전문 분석을 종료합니다");

		return 0;
	}

	public static String LPAD(String stringData, int lengthData, String charData) {
		// [리턴 반환 변수 선언 실시]
		String returnData = "";

		// [인풋 데이터 조건 체크 수행 실시 : 원본 문자열 길이 보다 인풋 값 길이가 더크고, char 문자가 1글자 인 경우]
		try {
			if (stringData != null && stringData.length() < lengthData && charData != null && charData.length() == 1) {

				// [반복문을 수행 횟수]
		        int count = 0;
		        for (int i = 0, len = stringData.length(); i < len; i++) {
		            char ch = stringData.charAt(i);

		            if (ch <= 0x7F) {
		                count++;
		            } else {
		                count += 2;
		            }
		        }

//				int countValue = lengthData - stringData.length();
				int countValue = lengthData - count;

				// [반복문 수행 실시]
				for (int i = 0; i < countValue; i++) {

					// [문자 추가]
					returnData = returnData + charData;
				}

				// [원본 추가]
				returnData = returnData + stringData;
			} else { // [조건 만족 안함]

				// [리턴 결과 반환 수행 실시]
				returnData = stringData;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// [리턴 결과 반환 실시]
		return returnData;
	}

	public static String RPAD(String stringData, int lengthData, String charData) {
		// [리턴 반환 변수 선언 실시]
		String returnData = "";

		// [인풋 데이터 조건 체크 수행 실시 : 원본 문자열 길이 보다 인풋 값 길이가 더크고, char 문자가 1글자 인 경우]
		try {
			if (stringData != null && stringData.length() < lengthData && charData != null && charData.length() == 1) {

				// [반복문을 수행 횟수]
				int countValue = lengthData - stringData.length();

				// [원본 추가]
				returnData = returnData + stringData;

				// [반복문 수행 실시]
				for (int i = 0; i < countValue; i++) {

					// [문자 추가]
					returnData = returnData + charData;
				}
			} else { // [조건 만족 안함]

				// [리턴 결과 반환 수행 실시]
				returnData = stringData;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// [리턴 결과 반환 실시]
		return returnData;
	}
}
