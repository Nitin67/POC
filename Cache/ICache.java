package com.quikr.platform.cache;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

public interface ICache {
	
	public static final String CACHE_CHARSET_TYPE = "ISO-8859-1";
	
	public void set(String key, Object value) throws IOException;
	
	public void set(String key, Object value, Integer TTL) throws IOException;
	
  //public void set(String key, String value) throws IOException;
	
	//public void set(String key, String value, Integer TTL) throws IOException;
	
	public Object get(String key) throws IOException;
	
	public void delete(String key) throws IOException;
	
	public void updateTTL(String key, Integer TTL) throws IOException;
	
	public Long incr(String key) throws IOException;
	
	public Map<String,Object> getBulk(ArrayList<String> keys) throws UnsupportedEncodingException;

}
