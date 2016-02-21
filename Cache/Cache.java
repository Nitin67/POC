package com.quikr.platform.cache;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;



public class Cache {
	
	private CachedEntityType entity;
	
	private ICache cache;
	
	public Cache(CachedEntityType entity) throws IOException{
		
		cache = CacheClientPool.getCache(entity);
		
		this.entity =entity;
		
	}
	
	
	public Object get(String key) throws IOException{
		return cache.get(key);
	}
	
	public void set(String key, Object value) throws IOException{
		
		cache.set(key, value);
	}
	
	public void set(String key, Object value, Integer TTL) throws IOException{
		
		cache.set(key, value , TTL);
	}
	
	public void set(String key, String value) throws IOException{
		
		cache.set(key, value);
	}
	
	public void set(String key, String value, Integer TTL) throws IOException{
		
		cache.set(key, value , TTL);
	}
	
	public void delete(String key) throws IOException{
		
		cache.delete(key);
		
	}
	
	public void updateTTL(String key, Integer TTL) throws IOException{
		
		cache.updateTTL(key, TTL);
	}
	
	public Long incr(String key) throws IOException{
		
		return cache.incr(key);
	}
	
	public Map<String,Object> getBulk(ArrayList<String> keys) throws UnsupportedEncodingException{
		
		return cache.getBulk(keys);
		
	}

	/*
	public void delete(String key){
		memcacheClient.delete(key + KEY_POSTFIX);
	}
	
	public void incrementValueAtkey(String key, int incr){
	  if(-1 == memcacheClient.incr(key, incr)){
	    throw new EntityNotFoundException("no value found with key = " + key);
	  }
	}
*/

}
