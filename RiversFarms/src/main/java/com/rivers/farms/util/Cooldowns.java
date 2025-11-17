package com.rivers.farms.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple per-key, per-UUID cooldown tracker using wall-clock time.
 */
public class Cooldowns {
    private final Map<String, Map<UUID, Long>> buckets = new HashMap<>();

    public boolean ready(String key, UUID id, long cooldownMs) {
        long now = System.currentTimeMillis();
        Map<UUID, Long> map = buckets.computeIfAbsent(key, k -> new HashMap<>());
        long last = map.getOrDefault(id, 0L);
        if (now - last >= cooldownMs) {
            map.put(id, now);
            return true;
        }
        return false;
    }
}
