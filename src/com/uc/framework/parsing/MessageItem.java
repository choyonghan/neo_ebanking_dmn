/**
 * 
 */
package com.uc.framework.parsing;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author hotaep
 *
 */
public class MessageItem 
{
	private ArrayList<FieldItem>	head;	// 공통부
	private	LinkedHashMap<String,ArrayList<FieldItem>>	bodies;	// 개별부
	private ArrayList<FieldItem>	tail;	// 꼬리부
	private	String	keyField;	// 참조필드명
	private	String	sizeField;	// 데이터부크기필드명
	private	boolean	includeHead;	// 데이터부크기필드명
	private	LinkedHashMap<String,String>	runs;	// 실행클래스명
	
	public int getHeadLength()
	{
		int	length = 0;
		if (head == null)
			return -1;
		for (int i = 0; i < head.size(); i++) {
			FieldItem	fi = head.get(i);
			length += fi.getSize();
		}
		return length;
	}
	
	public int getBodyLength(String key)
	{
		int	length = 0;
		ArrayList<FieldItem>	body = bodies.get(key);
		if (body == null) 
			return -1;
		for (int i = 0; i < body.size(); i++) {
			FieldItem	fi = body.get(i);
			length += fi.getSize();
		}
		return length;
	}

	public int getTailLength()
	{
		int	length = 0;
		if (tail == null)
			return -1;
		for (int i = 0; i < tail.size(); i++) {
			FieldItem	fi = tail.get(i);
			length += fi.getSize();
		}
		return length;
	}

	/**
	 * @return the head
	 */
	public ArrayList<FieldItem> getHead() {
		return head;
	}
	/**
	 * @param head the head to set
	 */
	public void setHead(ArrayList<FieldItem> head) {
		this.head = head;
	}
	/**
	 * @return the bodies
	 */
	public LinkedHashMap<String,ArrayList<FieldItem>> getBodies() {
		return bodies;
	}
	/**
	 * @param bodies the bodies to set
	 */
	public void setBodies(LinkedHashMap<String,ArrayList<FieldItem>> bodies) {
		this.bodies = bodies;
	}
	/**
	 * @return the tail
	 */
	public ArrayList<FieldItem> getTail() {
		return tail;
	}
	/**
	 * @param tail the tail to set
	 */
	public void setTail(ArrayList<FieldItem> tail) {
		this.tail = tail;
	}
	/**
	 * @return the keyField
	 */
	public String getKeyField() {
		return keyField;
	}
	/**
	 * @param keyField the keyField to set
	 */
	public void setKeyField(String keyField) {
		this.keyField = keyField;
	}
	/**
	 * @return the runs
	 */
	public LinkedHashMap<String,String> getRuns() {
		return runs;
	}
	/**
	 * @param runs the runs to set
	 */
	public void setRuns(LinkedHashMap<String,String> runs) {
		this.runs = runs;
	}

	/**
	 * @return the sizeField
	 */
	public String getSizeField() {
		return sizeField;
	}

	/**
	 * @param sizeField the sizeField to set
	 */
	public void setSizeField(String sizeField) {
		this.sizeField = sizeField;
	}

	/**
	 * @return the includeHead
	 */
	public boolean isIncludeHead() {
		return includeHead;
	}

	/**
	 * @param includeHead the includeHead to set
	 */
	public void setIncludeHead(boolean includeHead) {
		this.includeHead = includeHead;
	}
}
