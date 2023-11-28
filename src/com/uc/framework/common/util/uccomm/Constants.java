/**
 *  주 시스템명 : UC프레임웍
 *  업  무  명 : 공통 프레임워크
 *  기  능  명 : 공통 상수 정의
 *  클래스  ID : Constants
 *  변경  이력  :
 * ------------------------------------------------------------------------
 *  작성자	소속      일자		Tag 	내용      
 * ------------------------------------------------------------------------
 *  김대완  유채널   2008.12.05 %01%   최초작성
 * 
 */

package com.uc.framework.common.util.uccomm;


public class Constants
{

	
	//--------------------------------------------------------
	//	DATA 형태 정의
	//--------------------------------------------------------	
	public static final int	DATA_ORIG			=	 0;	//원본 그대로
	public static final int	DATA_DATE			=	 1;	//20050101 -> 2005-01-01
	public static final int	DATA_CASH			=	 2;	//20000 -> 20,000
	public static final int	DATA_CASH_ZERO		=	21;	//20000 -> 20,000 "" -> 0
	public static final int	DATA_INT			=	 3;	//000012345 -> 12345
	public static final int	DATA_LONG			=	 9;	//000012345 -> 12345
	public static final int	DATA_INT_CASH		=	 6;	//000012345 -> 12,345
	public static final int	DATA_LONG_CASH		=	 7;	//000012345 -> 12,345
	public static final int	DATA_TEXT			=	 4;	//"      123  " -> "123"
	public static final int	DATA_TIME			=	 5;	//120101 -> 12:01:01
	public static final int	DATA_KYENO			=	 8;	//00069025000890 -> 000690-25-000890
	public static final int	DATA_DATETIME		=	10;	//20081107111111 -> 2008-11-07 11:11:11
	

	public static final String MESSAGE_KEY = "Message";

	
	public static final String SUCCESS     = "success";
	public static final String LIST        = "list";
	public static final String RESULT      = "result";
	public static final String RESEARCH    = "research";
	public static final String EXCEL       = "excel";
	public static final String ERROR       = "error";
	public static final String EXCEPTION   = "exceptions";
	public static final String WEBTPOOL    = "kumgoPool";
	public static final String LOGIN 	   = "login";
	
	public static int SERVICE_MODE = 1;
	
	public static int TYPEHEAD     = 0;
	public static int TYPENOHEAD   = 1;
	public static int TYPEFIELDLEN = 2;
 	
	public static int SYNC     = 0;
	public static int ASYNC   = 1;
}
