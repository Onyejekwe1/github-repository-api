package com.redpharm.takehomechallenge.cache;

public interface CacheService {
    <T> T getFromCache(String cacheName, String key, Class<T> type);
    <T> void putIntoCache(String cacheName, String key, T value);
}

