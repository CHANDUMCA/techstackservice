package com.sunrize.it.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/redis")
public class RedisController {

    @Autowired
    private RedisService redisService;

    @PostMapping("/set")
    public String setKey(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(required = false) Long ttl) {
        
        log.info("Received POST request to set key: '{}' with value: '{}', TTL: {}", key, value, ttl);
        
        if (ttl != null) {
            redisService.set(key, value, ttl);
            log.debug("Successfully saved key: '{}' with TTL: {}s", key, ttl);
            return "Key '" + key + "' set with value '" + value + "' and TTL " + ttl + " seconds successfully!";
        } else {
            redisService.set(key, value);
            log.debug("Successfully saved key: '{}' without TTL", key);
            return "Key '" + key + "' set with value '" + value + "' successfully!";
        }
    }

    @GetMapping("/get/{key}")
    public Object getKey(@PathVariable String key) {
        log.info("Received GET request to retrieve key: '{}'", key);
        
        Object value = redisService.get(key);
        if (value == null) {
            log.warn("Key '{}' was not found in Redis.", key);
            return "Key '" + key + "' not found in Redis.";
        }
        
        log.debug("Retrieved value for key '{}': '{}'", key, value);
        return value;
    }

    @DeleteMapping("/delete/{key}")
    public String deleteKey(@PathVariable String key) {
        log.info("Received DELETE request for key: '{}'", key);
        
        boolean deleted = redisService.delete(key);
        if (deleted) {
            log.debug("Successfully deleted key: '{}'", key);
            return "Key '" + key + "' deleted successfully.";
        } else {
            log.warn("Failed to delete key '{}'. Key might not exist.", key);
            return "Key '" + key + "' not found or could not be deleted.";
        }
    }
}
