package com.quikr.platform.cache;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class CacheClientPool {
	
	private static final String MEMCACHE = "memcache";
	private static final String REDIS = "redis";
	
	private static Map<String, ICache> cacheClient = new ConcurrentHashMap<String, ICache>();
	
	private static Config cacheConfig = ConfigFactory.load("cache.conf");
	private static Config entityMapping = ConfigFactory.load("cacheEntity.conf");
	
	public static ICache getCache(CachedEntityType entity) throws IOException{
		ICache cache = cacheClient.get(entity.getValue());
		if (cache == null){
			List<String> list = entityMapping.getStringList(entity.getValue());
			cache = initCache(list.get(0), cacheConfig.getString(list.get(1)));
			cacheClient.put(entity.getValue(), cache);
		}
		return cache;
	}
	
	private static ICache initCache(String type, String clusterName) throws IOException{
		ICache cache = null;
		switch(type){
			case MEMCACHE:
				cache = new Memcache(clusterName);
				break;
			case REDIS:
				cache = new Redis(clusterName);
				break;
		}
		return cache;
	}


}
