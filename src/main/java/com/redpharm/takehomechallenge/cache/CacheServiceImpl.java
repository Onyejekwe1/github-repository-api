package com.redpharm.takehomechallenge.cache;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {
    private final CacheManager cacheManager;

    public CacheServiceImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public <T> T getFromCache(String cacheName, String key, Class<T> type) {
        return cacheManager.getCache(cacheName).get(key, type);
    }

    @Override
    public <T> void putIntoCache(String cacheName, String key, T value) {
        cacheManager.getCache(cacheName).put(key, value);
    }
}

