package com.quikr.platform.cache;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.quikr.platform.cache.exceptions.QCacheException;
import com.quikr.platform.cache.refresh.CacheEntry;
import com.quikr.platform.cache.refresh.CacheLoader;
import com.quikr.platform.cache.refresh.CacheProvider;
import com.quikr.platform.cache.refresh.LoaderCallable;
import com.quikr.util.Utils;

public class QCache<T>  {
	
	private final CacheLoader<T> cacheLoader;
  private final ICache cacheProvider;
  private final ExecutorService executorService;
  private final ConcurrentMap<String,Boolean> requestsInFlight;
  private static final int EXECUTOR_POOL_SIZE = 10;
  
  public QCache(ICache cacheProvider, CacheLoader<T> cacheLoader){
    this.cacheProvider = cacheProvider;
    this.cacheLoader = cacheLoader;
    executorService = Executors.newFixedThreadPool(EXECUTOR_POOL_SIZE);
    this.requestsInFlight = new ConcurrentHashMap<String, Boolean>();
  }
  
  
	

	public void set(String key, Object value) throws IOException, QCacheException {
		if (value instanceof Serializable){
			CacheEntry<Object> entry = new CacheEntry<Object>(key, value);
			cacheProvider.set(key, entry);
		}else {
			throw new QCacheException("Object must implement Serializable");
		}
	}

	public void set(String key, Object value, Integer TTL) throws IOException {
		CacheEntry<Object> entry = new CacheEntry<Object>(key, value, TTL);
		cacheProvider.set(key, entry);
	}

	public Object get(String key) throws IOException {
		CacheEntry<Object> entry = (CacheEntry<Object>) cacheProvider.get(key);
		if (cacheEntryExpired(entry)){
			//TODO : auto refresh code here.
		}
		
		return entry.getValue();
	}

	public void delete(String key) throws IOException {
		cacheProvider.delete(key);
	}

	public void updateTTL(String key, Integer TTL) throws IOException {
		cacheProvider.updateTTL(key, TTL);
	}

	public Long incr(String key) throws IOException {
		return cacheProvider.incr(key);
	}

	public Map<String, Object> getBulk(ArrayList<String> keys) throws UnsupportedEncodingException {
		return cacheProvider.getBulk(keys);
	}
	
	private boolean cacheEntryExpired(CacheEntry cacheEntry)    {

    if (cacheEntry.getTtl()==0)
        return false;

    long entryTimeStamp = cacheEntry.getEpoch_timestamp();
    long currentTime = Utils.getCurrentTimeInSeconds();
    long ttl = cacheEntry.getTtl();

    /* is the currentTime greater than when the CacheEntry would have expired? */
    if ((entryTimeStamp+ttl) > currentTime)
        return false;
    else
        return true;
	}
	
	private void reloadCacheEntry(CacheEntry cacheEntry) throws ExecutionException, InterruptedException {
    LoaderCallable<T> loaderCallable = new LoaderCallable<T>(this, cacheProvider, cacheLoader, cacheEntry);
    executorService.submit(loaderCallable);
  }
	
	 /**
   * Checks whether request is in flight for refreshing a given CacheEntry
   * @param cacheEntry
   * @return true if the the request is in flight or false if the request isn't in flight.
   */
  private boolean requestInFlight(CacheEntry<T> cacheEntry){
      return requestsInFlight.containsKey(cacheEntry.getKey());
  }

  /**
   * Puts a request in flight for refreshing a given CacheEntry
   * at any given time, no more than one request should be in flight
   * to refresh a CacheEntry
   * @param cacheEntry
   */
  private void putRequestInFlight(CacheEntry<T> cacheEntry){
      requestsInFlight.put(cacheEntry.getKey(),true);
  }

  /**
   * removes a request from being in flight to allow a subsequent request for
   * the same key to be put in flight.
   * @param cacheEntry
   */
  public void removeRequestInFlight(CacheEntry<T> cacheEntry){
      synchronized (requestsInFlight) {
          requestsInFlight.remove(cacheEntry.getKey());
      }

  }

}
