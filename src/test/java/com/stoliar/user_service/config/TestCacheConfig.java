package com.stoliar.user_service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@TestConfiguration
public class TestCacheConfig {

    @Bean
    @Primary
    public CacheManager testCacheManager() {
        return new MockCacheManager();
    }

    static class MockCacheManager implements CacheManager {
        private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>();

        @Override
        public Cache getCache(String name) {
            return caches.computeIfAbsent(name, MockCache::new);
        }

        @Override
        public Collection<String> getCacheNames() {
            return Collections.unmodifiableSet(caches.keySet());
        }
    }

    static class MockCache implements Cache {
        private final String name;

        MockCache(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return null;
        }

        @Override
        public ValueWrapper get(Object key) {
            return null;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            return null;
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void put(Object key, Object value) {
        }

        @Override
        public void evict(Object key) {
        }

        @Override
        public void clear() {
        }
    }
}