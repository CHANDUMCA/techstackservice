package com.sunrize.it.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        
        if (ttl != null) {
            redisService.set(key, value, ttl);
            return "Key '" + key + "' set with value '" + value + "' and TTL " + ttl + " seconds successfully!";
        } else {
            redisService.set(key, value);
            return "Key '" + key + "' set with value '" + value + "' successfully!";
        }
    }

    @GetMapping("/get/{key}")
    public Object getKey(@PathVariable String key) {
        Object value = redisService.get(key);
        if (value == null) {
            return "Key '" + key + "' not found in Redis.";
        }
        return value;
    }

    @DeleteMapping("/delete/{key}")
    public String deleteKey(@PathVariable String key) {
        boolean deleted = redisService.delete(key);
        if (deleted) {
            return "Key '" + key + "' deleted successfully.";
        } else {
            return "Key '" + key + "' not found or could not be deleted.";
        }
    }
}
