/**
 * 
 */
package com.uc.framework.mybatis;

import java.util.ArrayList;

import com.uc.framework.utils.MyMap;

/**
 * @author hotaep
 *
 */
public interface myBatisService 
{
	public MyMap	queryForMap(String query);
	public MyMap	queryForMap(String query, MyMap param);
	public ArrayList<MyMap>	queryForList(String query);
	public ArrayList<MyMap>	queryForList(String query, MyMap param);
}
