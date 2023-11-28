/**
 * 
 */
package com.uc.framework.parsing;

import java.util.ArrayList;

/**
 * @author hotaep
 *
 */
public class DaemonInfo 
{
	private String	name;	// 데몬이름
	private String	datapath;
	private String	confpath;
	private String	logpath;
	private	String	msgtype;	// 전문유형
	private	String	port;			// 사용하는 포트
	private	String	desc;			// 기능설명
	private	ArrayList<String>	services;	// 기동할 서비스 목록
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
	 * @return the msgtype
	 */
	public String getMsgtype() {
		return msgtype;
	}
	/**
	 * @param msgtype the msgtype to set
	 */
	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}
	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}
	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}
	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}
	/**
	 * @return the services
	 */
	public ArrayList<String> getServices() {
		return services;
	}
	/**
	 * @param services the services to set
	 */
	public void setServices(ArrayList<String> services) {
		this.services = services;
	}
	/**
	 * @return the datapath
	 */
	public String getDatapath() {
		return datapath;
	}
	/**
	 * @param datapath the datapath to set
	 */
	public void setDatapath(String datapath) {
		this.datapath = datapath;
	}
	/**
	 * @return the confpath
	 */
	public String getConfpath() {
		return confpath;
	}
	/**
	 * @param confpath the confpath to set
	 */
	public void setConfpath(String confpath) {
		this.confpath = confpath;
	}
	/**
	 * @return the logpath
	 */
	public String getLogpath() {
		return logpath;
	}
	/**
	 * @param logpath the logpath to set
	 */
	public void setLogpath(String logpath) {
		this.logpath = logpath;
	}
}
