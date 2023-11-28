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
import org.xml.sax.SAXException;

/**
 * @author hotaep
 *
 */
public class ServiceListParser 
{
	private	Logger	logger;
	private	LinkedHashMap<String,ServiceInfo>	svcinfo = null;
	
	/**
	 * 
	 */
	public ServiceListParser(Logger logger) 
	{
		// TODO Auto-generated constructor stub
		this.logger = logger;
		svcinfo = new LinkedHashMap<String,ServiceInfo>();
	}
	
	public ServiceInfo getServiceInfo(String svcname)
	{
		return svcinfo.get(svcname);
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
			is = this.getClass().getResourceAsStream("/conf/servicelist.xml");
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
		NodeList	bl = doc.getElementsByTagName("service");		
		for (int t = 0; t < bl.getLength(); t++) {
			Node	child = bl.item(t);

			// 상세부를 구분하는 필드값과 실행할 클래스이름을 정의한다.
			if (child.hasAttributes()) {
				NamedNodeMap	hmap = child.getAttributes();

				ServiceInfo	svc = new ServiceInfo();
				
				// 개별부구분하는 값.
				Node	namevalue = hmap.getNamedItem("name");
				if (namevalue == null) {
					logger.error("name 속성이 필요합니다");
					logger.debug("전문형식 분석을 종료합니다");
					return -1;
				}
				svc.setName(namevalue.getNodeValue());
				
				// 개별부구분하는 값.
				Node	modevalue = hmap.getNamedItem("mode");
				if (modevalue == null) {
					logger.error("mode 속성이 필요합니다");
					logger.debug("전문형식 분석을 종료합니다");
					return -1;
				}
				svc.setMode(modevalue.getNodeValue());
				
				// 개별부구분하는 값.
				Node	periodvalue = hmap.getNamedItem("period");
				if (periodvalue == null) {
					logger.error("period 속성이 필요합니다");
					logger.debug("전문형식 분석을 종료합니다");
					return -1;
				}
				svc.setPeriod(periodvalue.getNodeValue());
				
				// 개별부구분하는 값.
				Node	beginvalue = hmap.getNamedItem("begin");
				if (beginvalue == null) {
					logger.error("begin 속성이 필요합니다");
					logger.debug("전문형식 분석을 종료합니다");
					return -1;
				}
				svc.setBegin(beginvalue.getNodeValue());
				
				// 개별부구분하는 값.
				Node	endvalue = hmap.getNamedItem("end");
				if (endvalue == null) {
					logger.error("end 속성이 필요합니다");
					logger.debug("전문형식 분석을 종료합니다");
					return -1;
				}
				svc.setEnd(endvalue.getNodeValue());
				
				// 개별부전문을 받은 후 실행할 클래스명.
				Node	runnable = hmap.getNamedItem("run");
				if (runnable == null) {
					logger.error("run 속성이 필요합니다");
					logger.debug("전문형식 분석을 종료합니다");
					return -1;
				}
				svc.setRunnable(runnable.getNodeValue());

				svcinfo.put(svc.getName(), svc);
			} else {
				logger.error("속성이 필요합니다");
				logger.debug("전문형식 분석을 종료합니다");
				return -1;
			}
		}
		logger.debug("전문형식 분석을 종료합니다");
		return 0;
	}
}
