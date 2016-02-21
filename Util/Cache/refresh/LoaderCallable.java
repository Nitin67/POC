/**
 * Copyright 2015 Quikr India, pvt ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quikr.platform.cache.refresh;


import com.quikr.platform.cache.ICache;
import com.quikr.platform.cache.QCache;
import com.quikr.platform.cache.refresh.CacheLoader;
import com.quikr.platform.cache.refresh.CacheProvider;
import com.quikr.platform.cache.refresh.CacheEntry;

import java.util.concurrent.Callable;

/**
 * The callable that is used by {@link Kelly} to reload the {@link CacheProvider} with a {@link CacheEntry}
 * using the provided {@link CacheLoader}
 * @param <T>
 */
public class LoaderCallable<T> implements Callable {

    private final CacheLoader<T> cacheLoader;
    private final ICache cacheProvider;
    private final CacheEntry<T> cacheEntry;
    private final QCache<T> cache;

    public LoaderCallable(QCache<T> kelly, ICache cacheProvider, CacheLoader<T> cacheLoader, CacheEntry<T> cacheEntry){
        this.cacheLoader = cacheLoader;
        this.cacheProvider = cacheProvider;
        this.cacheEntry = cacheEntry;
        this.cache = kelly;
    }

    @Override
    public Object call() throws Exception {
        CacheEntry<T> cacheEntryTemp = cacheLoader.reload(cacheEntry.getKey(), cacheEntry.getValue());
        cacheProvider.set(cacheEntryTemp.getKey(), cacheEntryTemp);
        cache.removeRequestInFlight(cacheEntry);
        return true;
    }
}    