/**
 *  주시스템명 : 유채널 프레임웍
 *  업  무  명 : TMAX 연계
 *  기  능  명 : Tmax Field
 *  클래스  ID : TmaxField
 *  변경  이력 :
 * ------------------------------------------------------------------------
 *  작성자     소속  		 일자      Tag   내용
 * ------------------------------------------------------------------------
 *  김대완    유채널       2008.12.05  %01% 신규작성
 *
 */

package com.uc.framework.common.util.uccomm.net;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Field
{
    protected Log log = LogFactory.getLog(this.getClass());

    /**
     * Field 명
     */
    private String fieldName;

    /**
     * FIeld 크기
     */
    private float fieldSize;

    /**
     * Field 형식
     */
    private String fieldType;
    
    /**
     * CODE ID(공통코드명 조회용)
     */
    private String codeID;

    /**
     * 코드명을 담을 Name 필드명
     */
    private String nameField;

    /**
     * 생성자
     * @param fieldName
     * @param fieldSize
     * @param fieldType
     * @param code
     */
    public Field(String fieldName, float fieldSize, String fieldType, String code)
    {
    	setTmaxField(fieldName, fieldSize, fieldType, code, null);
    }
    
    /**
     * 
     * @param fieldName
     * @param fieldSize
     * @param fieldType
     * @param code
     * @param nameField
     */
    public Field(String fieldName, float fieldSize, String fieldType, String code, String nameField)
    {
    	setTmaxField(fieldName, fieldSize, fieldType, code, nameField);
    }
    
    
    /**
     * 생성자
     * @param name
     * @param size
     * @param type
     */
    public Field(String fieldName, float fieldSize, String fieldType)
    {
        super();

        setTmaxField(fieldName, fieldSize, fieldType, null, null);
        
    }

    /**
     * 필드 SET
     * @param fieldName
     * @param fieldSize
     * @param fieldType
     * @param code
     */
    private void setTmaxField(String fieldName, float fieldSize, String fieldType, String code, String nameField)
    {
    	this.fieldName = fieldName;
    	this.fieldSize   = fieldSize;
    	this.fieldType  = fieldType;
    	this.codeID     = code;
    	this.nameField = nameField;
	}

	/**
     * @return Returns the fieldName.
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @return Returns the fieldSize.
     */
    public float getFieldSize()
    {
        return fieldSize;
    }

    /**
     * @param fieldSize The fieldSize to set.
     */
    public void setFieldSize(float fieldSize)
    {
        this.fieldSize = fieldSize;
    }

    /**
     * @return Returns the fieldType.
     */
    public String getFieldType()
    {
        return fieldType;
    }

    /**
     * @param fieldType The fieldType to set.
     */
    public void setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
    }

	/**
	 * @return codeID
	 */
	public String getCodeID() {
		return codeID;
	}

	/**
	 * @param codeID 설정하려는 codeID
	 */
	public void setCodeID(String codeID) {
		this.codeID = codeID;
	}

	/**
	 * @return nameField
	 */
	public String getNameField() {
		return nameField;
	}

	/**
	 * @param nameField 설정하려는 nameField
	 */
	public void setNameField(String nameField) {
		this.nameField = nameField;
	}
}
