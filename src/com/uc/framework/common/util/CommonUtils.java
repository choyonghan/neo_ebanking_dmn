package com.uc.framework.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

//import com.ibatis.common.resources.Resources;

import com.uc.framework.common.util.StringProcess;

/**
 * @fileName   : ComboUtils.java
 * @package     : kr.go.sntest.common.util
 */
public class CommonUtils {

	/**
		 * @Method Name         : yyyyCombo
		 * @Method description 	: 넘어온 parameter로 min년도부터 현재+num년도까지 HashMap에 담아서 반환
		 * @return
		 */
	// (yyyy, 2020, 1) 파라미터
	public static List<Map<String, Object>> yyyyCombo(int yyyy, int min, int num) {
		List<Map<String, Object>> mf = new ArrayList<Map<String, Object>>();

		for (int i = min; i < yyyy + num; i++) {
			HashMap<String, Object> list = new HashMap<String, Object>();
			list.put("value", Integer.toString(i));
			mf.add(list);
		}
		return mf;
	}

	/**
	 * @Method Name         : timeCombo
	 * @Method description 	: 24시간 HashMap에 담아서 반환
	 * @return
	 */
	public static List<Map<String, Object>> timeCombo() {
		List<Map<String, Object>> time = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < 24; i++) {
			HashMap<String, Object> list = new HashMap<String, Object>();
			list.put("value", String.format("%02d", i));
			time.add(list);
		}
		return time;
	}

	/**
	 * @Method Name         : getCurrentDate
	 * @Method description 	: 현재 시스템 날자 가져오기 (YYYYMMDD형태로 반환)
	 * @return
	 */
	public static String getCurrentDate() {
		String currentDate = null;

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

		currentDate = df.format(cal.getTime());

		return currentDate;
	}


	/**
	 *  현재 시스템 시간 가져오기 HHMMSSsss
	 */
	public static String getCurrentTime() {

		String currentTime = null;

		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("HHmmssS");

		currentTime = df.format(date);

		return currentTime;
	}


	/**
	 *
	 * @param resName
	 * @param key
	 * @return
	 */
	/*
	public static String getResource(String key) {
		String resource = "properties/ApplicationResource.properties";
		Properties properties = new Properties();
		Reader reader;
		try {
			reader = Resources.getResourceAsReader(resource);
			properties.load(reader);
			return StringProcess.parseString(properties.getProperty(key),"");

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "";
		}
	}
	public static int getResourceInt(String resName, String key) {
		return StringProcess.parseInt(CommonUtils.getResource(key).trim());
	}
	 */

	public static void saveFile(File file, String path, String fileName) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		FileOutputStream outputStream = new FileOutputStream(path + fileName);
		int bytesRead = 0;
		byte[] buffer = new byte[1024];

		while ((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.close();
		inputStream.close();
	}

	public static int renameFile(String ofile, String pathPfile){
		int rtn = 0;

		File cfile = new File(pathPfile);   //pathPfile 은 path plus file 을 의미하지롱요
		File file = new File(ofile);

		if(file.exists()){
			try{
				file.renameTo(cfile);
				rtn = 1;	//파일이름 변경 성공
			}catch(Exception ie){
				rtn = 2;   //파일이름 변경실패!!
			}
		}else{
			rtn = 3;  //파일없음
		}

		return rtn;
	}

	/**
	 * 문자열 왼쪽에 ch를 붙혀서 전체길이를 len 만큼 만듬
	 */
	public static String lPadString(String code, int len, char ch) {
		String tempCode = code.trim();
		int length = strLength(code);

		if (length < len) {
			for (int i = length; i < len; i++) {
				tempCode = ch + tempCode;
			}
			return tempCode;
		} else {
			return tempCode;
		}
	}

	/**
	 * 문자열의 길이를 BYTE 단위로 RETURN
	 */
	public static int strLength(String str) {
		char[] ch = str.toCharArray();
		int length = 0;
		for (int i = 0; i < ch.length; i++) {
			if (Character.UnicodeBlock.of(ch[i]) == Character.UnicodeBlock.HANGUL_SYLLABLES)
				length += 2;
			else
				length++;
		}
		return length;
	}


	/**
	 * 널값 체크해서 반환
	 * @param str
	 * @return
	 */
	public static String nullChk(String str) {
		return str == null || str.equals("null") ? "" : str;
	}

	/**
	 * 널값 체크해서 반환
	 * @param str
	 * @param scode
	 * @param dcode
	 * @return
	 */
	public static String nullChk(String str, String scode, String dcode) {
		if (str == null || str.equals("null")) {
			str = "";
		}

		try {
			str = new String(str.getBytes(scode), dcode);
		} catch (Exception e) {
		}
		return str;
	}

	/**
	 * 널값 체크해서 반환
	 * @param str
	 * @return
	 */
	public static String nullChk(Object str) {
		if (str == null || str.equals("null")){
			str = "";
		}
		return String.valueOf(str);
	}

	/**
	 * 널값 체크해서 반환
	 * @param str
	 * @param result
	 * @return
	 */
	public static String nullChk(String str, String result) {
		if (str == null || str.equals("null") || str.equals("")){
			return result;
		} else {
			return str;
		}
	}

	/**
	 * 널값 체크해서 반환
	 * @param str
	 * @param result
	 * @return
	 */
	public static String nullChk(Object str, String result) {
		String str2 = nullChk(str);
		if (str2 == null || str2.equals("null") || str2.equals("")) {
			return result;
		} else {
			return str2;
		}
	}

	/**
	 * 널값 체크해서 반환
	 * @param str
	 * @param result
	 * @return
	 */
	public static int nullChk(String str, int result) {
		if (str == null || str.equals("null") || str.equals("")) {
			return result;
		} else {
			return Integer.parseInt(str);
		}
	}

	/**
	 * 널값 체크해서 반환
	 * @param str
	 * @param result
	 * @return
	 */
	public static String nullChk(int str, String result) {
		String str_temp = Integer.toString(str);

		if (str_temp.equals("")) {
			return result;
		} else {
			return str_temp;
		}
	}

	/**
	 * 널값 체크해서 반환
	 * @param str
	 * @param result
	 * @return
	 */
	public static int nullChk(Object str, int result) {
		String str2 = nullChk(str);
		if (str2 == null || str2.equals("null") || str2.equals("")) {
			return result;
		} else {
			return Integer.parseInt(str2);
		}
	}


	/**
	 * properties값 출력
	 * @param str
	 * @param result
	 * @return
	 */
	/*
	public static String proVal(String properties, String urlNm) {
		String resource = properties;
		Properties properties1 = new Properties();
		Reader reader;
		@SuppressWarnings("unused")
		String result = "";
		try {
			reader = Resources.getResourceAsReader(resource);
			properties1.load(reader);

			result = StringProcess.parseString(properties1.getProperty(urlNm),"");
		} catch (IOException e1) {

		}
		return result;
	}
*/
}