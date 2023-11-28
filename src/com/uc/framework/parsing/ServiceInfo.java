/**
 * 
 */
package com.uc.framework.parsing;

import java.util.ArrayList;

/**
 * @author hotaep
 *
 */
public class ServiceInfo 
{
	private	String	name;	// 데몬이름
	private	String	mode;	// 실행모드
	private	String	period;	// 실행주기
	private	String	begin;	// 실행시작시각
	private	String	end;		// 실행종료시각
	private	String	runnable;	// 전문유형
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the runnable
	 */
	public String getRunnable() {
		return runnable;
	}
	/**
	 * @param runnable the runnable to set
	 */
	public void setRunnable(String runnable) {
		this.runnable = runnable;
	}
	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}
	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}
	/**
	 * @return the period
	 */
	public String getPeriod() {
		return period;
	}
	/**
	 * @param period the period to set
	 */
	public void setPeriod(String period) {
		this.period = period;
	}
	/**
	 * @return the begin
	 */
	public String getBegin() {
		return begin;
	}
	/**
	 * @param begin the begin to set
	 */
	public void setBegin(String begin) {
		this.begin = begin;
	}
	/**
	 * @return the end
	 */
	public String getEnd() {
		return end;
	}
	/**
	 * @param end the end to set
	 */
	public void setEnd(String end) {
		this.end = end;
	}
}
