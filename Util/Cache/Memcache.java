package com.quikr.platform.cache;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;



public class Memcache implements ICache{
	
	private MemcachedClient memcacheClient=null;
	
	public Memcache(String serverList) throws IOException{
		
		memcacheClient=new MemcachedClient(new BinaryConnectionFactory(),AddrUtil.getAddresses(serverList));
		
	}

	@Override 
	public Map<String,Object> getBulk(ArrayList<String> keys) throws UnsupportedEncodingException{ 
		Map<String, Object> dataMap =  memcacheClient.getBulk(keys);
		
		Set<Entry<String, Object>> entrySet = dataMap.entrySet();
		Iterator<Entry<String, Object>> it = entrySet.iterator();
		while(it.hasNext()){
			Entry<String, Object> data = it.next();
			Object obj = null;
			byte[] cacheData = (byte[]) data.getValue();
			
			/*if (cacheData != null){
				obj = Serializer.deserialize(cacheData);
				if (obj == null)
					obj = new String(cacheData); //Case of String. TODO: better way of handling this.
			}*/
			if (cacheData != null){
				obj = Serializer.deserialize(new String(cacheData).getBytes(CACHE_CHARSET_TYPE));
			}
			dataMap.put(data.getKey(), obj);
		}
		return dataMap;
		
		
	}
	
	@Override 
	public void delete(String key){
		memcacheClient.delete(key);
	}

	@Override
	public void set(String key, Object value) throws IOException {
		set(key, value, 0);
	}

	@Override
	public void set(String key, Object value, Integer TTL) throws IOException {
		byte[] data = Serializer.serialize(value);
		memcacheClient.set(key,TTL,data);
		
	}

	@Override
	public Object get(String key) throws IOException {
		Object ret = null;
		Object obj = memcacheClient.get(key);
		//if (obj instanceof String){
		//	return obj;
		//}else {
			byte[] data = (byte[]) obj;
			if (data != null){
				ret = Serializer.deserialize(new String(data).getBytes(CACHE_CHARSET_TYPE));
			}
		//}
		return ret;
		
	}

	@Override
	public void updateTTL(String key, Integer TTL) throws IOException {
		memcacheClient.touch(key, TTL);
	}

	@Override
	public Long incr(String key) throws IOException {
		return memcacheClient.incr(key, 1);
	}

	/*@Override
	public void set(String key, String value) throws IOException {
		set(key, value, 0);
	}

	@Override
	public void set(String key, String value, Integer TTL) throws IOException {
		memcacheClient.set(key,TTL,value.getBytes());
	}*/

	
}
