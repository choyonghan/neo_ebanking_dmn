/**
 * 주시스템명        : 유채널 프레임웍
 * 업  무  명        : CommonModel
 * 기  능  명        : 사용자 로그인 후 기본정보를 Session에 담아둘 객체
 * 클래스 ID         : CommonModel
 * 변경이력                :
 *
 * -------------------------------------------------------------------------------
 *  작성자        소속           일  자          Tag              내용
 * -------------------------------------------------------------------------------
 *  김대완      유채널(주)     2009.04.03        %01%          신규작성
 *
 */

package com.uc.framework.common.util.uccomm;

import java.util.HashMap;

import com.uc.framework.common.util.StringUtil;

public class CommonModel extends HashMap<String, Object>
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public CommonModel()
    {


    }

	/**
	 *
	 * @param key	:: SET 할 필드명
	 * @param field   :: FIELD 값
	 */
	public void setField(String key, Object field) {

		put(key, field);

	}


	/**
	 *
	 * @param key   :: FIELD의 키값
	 * @return         :: FIELD값
	 */
	public Object getField(String key){

		return get(key);
	}


	/**
	 * @return Returns the current_date.
	 */
	public String getCurrent_date()
	{
		return StringUtil.reConvertDate((String) getField("open_date"));
	}


    /**
     * 코드를 받아 코드명을 가져옴
     * @param strArrName
     * @param strCode
     * @return 코드명
     */

	@SuppressWarnings("unchecked")
	public String getCommName(String strArrName, String strCode )
    {
    	// return commonCodeSearch.getCommName(strArrName, strCode);

    	HashMap<String, Object> list = (HashMap<String, Object>) this.get(strArrName);

    	return (String) list.get(strCode);

    }

    /**
     *  List명과 코드를 받아서 코드명을 가져옴;
     * @param strArrName
     * @param strCode
     * @return
     */
	@SuppressWarnings("unchecked")
	public String getNameByCode(String strArrName, String strCode)
	{

    	HashMap<String, Object> map = (HashMap<String, Object>) this.get(strArrName);

    	return (String) map.get(strCode);
	}

    /**
     *  List명과 코드명을 받아서 코드를 가져옴;
     * @param strArrName
     * @param strCode
     * @return
     */
	@SuppressWarnings("unchecked")
	public String getCodeByName(String strArrName, String strName)
	{

    	HashMap<String, Object> map = (HashMap<String, Object>) this.get(strArrName);

		return (String) map.get(strName);
	}

    /**
     * 코드명을 받아 코드를 가져옴
     * @param strArrName
     * @param strName
     * @return 코드
     */
    @SuppressWarnings("unchecked")
	public String getCommCode(String strArrName, String strName )
    {
    	HashMap<String, Object> map = (HashMap<String, Object>) this.get(strArrName);

		return (String) map.get(strName);
    }


}

