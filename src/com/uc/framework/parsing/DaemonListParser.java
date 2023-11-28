/**
 * 
 */
package com.uc.framework.parsing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author hotaep
 *
 */
public class DaemonListParser 
{
	private	Logger	logger;
	private	LinkedHashMap<String,DaemonInfo>	dminfo = null;
	
	/**
	 * 
	 */
	public DaemonListParser(Logger logger) 
	{
		// TODO Auto-generated constructor stub
		this.logger = logger;
		dminfo = new LinkedHashMap<String,DaemonInfo>();
	}
	
	/**
	 * 
	 */
	public DaemonInfo getDaemonInfo(String dmname)
	{
		return dminfo.get(dmname);
	}
	/**
	 * 
	 */
	public int doParsing() 
	{
		// TODO Auto-generated constructor stub
		logger.debug("전문형식 분석을 시작합니다");
		
		// 전문유형 파일 지정
		InputStream	is;
//		try {
			is = this.getClass().getResourceAsStream("/conf/daemonlist.xml");
//		} catch (FileNotFoundException fnf) {
//			logger.error("파일이 없습니다.");
//			logger.debug("전문형식 분석을 종료합니다");
//			return -1;
//		}

		// XML문서 파싱을 위해 객체생성 
		DocumentBuilderFactory	dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder	db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pcex) {
			logger.error(pcex.getLocalizedMessage());
			logger.debug("전문형식 분석을 종료합니다");
			return -1;
		}
	
		// XML을 파싱합니다.
		Document	doc;
		try {
			doc = db.parse(is);
		} catch (IOException ioex) {
			logger.error(ioex.getLocalizedMessage());
			logger.debug("전문형식 분석을 종료합니다");
			return -1;
		} catch (SAXException saxex) {
			logger.error(saxex.getLocalizedMessage());
			logger.debug("전문형식 분석을 종료합니다");
			return -1;
		} catch (IllegalArgumentException iaex) {
			logger.error(iaex.getLocalizedMessage());
			logger.debug("전문형식 분석을 종료합니다");
			return -1;
		}

		// Combine adjacent Text nodes into one single one
		doc.getDocumentElement().normalize();
		
			// 개별부를 분석한다.
			NodeList	bl = doc.getElementsByTagName("daemon");
			LinkedHashMap<String,ArrayList<FieldItem>>	bs = new LinkedHashMap<String,ArrayList<FieldItem>>();
			String	key2;
			
			for (int t = 0; t < bl.getLength(); t++) {
				Node	child = bl.item(t);

				DaemonInfo	di = new DaemonInfo();
				
				// 상세부를 구분하는 필드값과 실행할 클래스이름을 정의한다.
				if (child.hasAttributes()) {
					NamedNodeMap	hmap = child.getAttributes();
					
					// 개별부구분하는 값.
					Node	namevalue = hmap.getNamedItem("name");
					if (namevalue == null) {
						logger.error("name 속성이 필요합니다");
						logger.debug("전문형식 분석을 종료합니다");
						return -1;
					}
					di.setName(namevalue.getNodeValue());
					
					// 개별부전문을 받은 후 실행할 클래스명.
					Node	datapath = hmap.getNamedItem("datapath");
					if (datapath == null) {
						logger.error("datapath 속성이 필요합니다");
						logger.debug("전문형식 분석을 종료합니다");
						return -1;
					}
					di.setDatapath(datapath.getNodeValue());

					// 개별부전문을 받은 후 실행할 클래스명.
					Node	logpath = hmap.getNamedItem("logpath");
					if (logpath == null) {
						logger.error("logpath 속성이 필요합니다");
						logger.debug("전문형식 분석을 종료합니다");
						return -1;
					}
					di.setLogpath(logpath.getNodeValue());

					// 개별부전문을 받은 후 실행할 클래스명.
					Node	confpath = hmap.getNamedItem("confpath");
					if (confpath == null) {
						logger.error("confpath 속성이 필요합니다");
						logger.debug("전문형식 분석을 종료합니다");
						return -1;
					}
					di.setConfpath(confpath.getNodeValue());

					// 개별부전문을 받은 후 실행할 클래스명.
					Node	msgtype = hmap.getNamedItem("msgtype");
					if (msgtype == null) {
						logger.error("datapath 속성이 필요합니다");
						logger.debug("전문형식 분석을 종료합니다");
						return -1;
					}
					di.setMsgtype(msgtype.getNodeValue());

					Node	port = hmap.getNamedItem("port");
					if (port == null) {
						logger.error("datapath 속성이 필요합니다");
						logger.debug("전문형식 분석을 종료합니다");
						return -1;
					}
					di.setPort(port.getNodeValue());

				} else {
					logger.error("속성이 필요합니다");
					logger.debug("전문형식 분석을 종료합니다");
					return -1;
				}
				
				// 개별부의 필드를 정의한다.
				NodeList	hfl = ((Element)child).getElementsByTagName("service");

				ArrayList<String>	services = new ArrayList<String>();

				for (int tt = 0; tt < hfl.getLength(); tt++) {
					Node	gchild = hfl.item(tt);

					if (gchild.hasAttributes()) {
						NamedNodeMap	hfmap = gchild.getAttributes();

						// 필드이름
						Node	name = hfmap.getNamedItem("name");
						services.add(name.getNodeValue());						
					} else {
						logger.error("keyName 속성이 필요합니다");
						logger.debug("전문형식 분석을 종료합니다");
						return -1;
					}
				}
				di.setServices(services);
				
				dminfo.put(di.getName(), di);
			}
		logger.debug("전문형식 분석을 종료합니다");
		return 0;
	}
}
