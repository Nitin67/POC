package com.quikr.platform.datastore.mysql.response;

import com.quikr.platform.AbstractModel;

public class DBRow extends AbstractModel{
	
	public String getString(String col){
		return (String)getData(col);
	}
	
	public Integer getInt(String col){
		//TODO : use proper exception handling.
		return Integer.parseInt(getString(col));
	}
	
	public Float getFloat(String col){
		return Float.valueOf(getString(col));
	}
	
	public Long getLong(String col){
		return Long.parseLong(getString(col));
	}

}
