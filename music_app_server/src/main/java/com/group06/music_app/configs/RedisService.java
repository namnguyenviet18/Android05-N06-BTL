package com.group06.music_app.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveDate(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public Object getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    public void deleteKeysContainingEmail(String email) {
        Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions().match("*" + email + "*").build());

        while (cursor.hasNext()) {
            String key = cursor.next();
            redisTemplate.delete(key);
        }
    }
}
