package com.quikr.platform.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Redis implements ICache {

	private JedisPool	redisConnectionPool;

	public Redis(String hostAddress) {
		String[] host = hostAddress.split(":");
		
		redisConnectionPool = new JedisPool(host[0], Integer.valueOf(host[1]));
		
	}

	protected Jedis getConnetion() {
		return redisConnectionPool.getResource();
	}

	@Override
	public Long incr(String key) throws IOException {

		Jedis jedis = getConnetion();
		long val = 0;
		try {
			val = jedis.incr(key);
		} finally {
			jedis.close();
		}
		return val;
	}

	/**
	 * update TTL for a particular key after given seconds 
	 * Returns 1 if timeout set otherwise 0.
	 * Reference : http://redis.io/commands/expire 
	 * @throws IOException 
	 **/
	@Override
	public void updateTTL(String key, Integer seconds) throws IOException {
		Jedis jedis = getConnetion();
		long val = 0;
		try {
			val = jedis.expire(key, seconds);
			if (val == 0) //TODO : define proper logical exception
				throw new IOException("Cannot update");
		} finally {
			jedis.close();
		}
	}

	@Override
	public void set(String key, Object value) throws IOException {
		Jedis jedis = getConnetion();
		try {
			byte[] rawData = Serializer.serialize(value);
			jedis.set(key.getBytes(), rawData);
		} finally {
			jedis.close();
		}

	}
	
	/*@Override
	public void set(String key, String value)throws IOException {
		Jedis jedis = getConnetion();
		try {
			jedis.set(key, value);
		} finally {
			jedis.close();
		}
	}*/

	@Override
	public void set(String key, Object value, Integer TTL) throws IOException {
		Jedis jedis = getConnetion();
		try {
			byte[] rawData = Serializer.serialize(value); 
			jedis.setex(key.getBytes(), TTL, rawData);
		} finally {
			jedis.close();
		}

	}
	
/*	@Override
	public void set(String key, String value, Integer TTL)throws IOException {
		Jedis jedis = getConnetion();
		try {
			jedis.setex(key, TTL,value);
		} finally {
			jedis.close();
		}
	}*/

	@Override
	public Object get(String key) throws IOException {
		Jedis jedis = getConnetion();
		Object obj = null;
		try {
			String val = jedis.get(key);
			if (val != null){
				obj = Serializer.deserialize(val.getBytes("ISO-8859-1"));
				/*if (obj == null)
					obj = val;*/ //Case of String. TODO: better way of handling this.
			}
		} finally {
			jedis.close();
		}
		return obj;
	}

	@Override
	public void delete(String key) throws IOException {
		Jedis jedis = getConnetion();
		try {
			jedis.del(key);
		} finally {
			jedis.close();
		}

	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		if (redisConnectionPool != null){
			redisConnectionPool.destroy();
		}
	}

	@Override
	public Map<String, Object> getBulk(ArrayList<String> keys) {
		throw new UnsupportedOperationException("Redis Does not support bulk fetch.");
	}

}
