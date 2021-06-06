package com.geek;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rpc上下文
 */
public class RpcContext {

    private static final ThreadLocal<Map<String, Object>> contextLocal = new ThreadLocal<>();

    public static void set(String key, Object value) {
        Map<String, Object> paramsMap = contextLocal.get();

        if (paramsMap == null) {
            paramsMap = new ConcurrentHashMap<>(1);
            contextLocal.set(paramsMap);
        }

        paramsMap.put(key, value);
    }

    public static Map<String, Object> getContext() {
        return contextLocal.get();
    }

    public static Object get(String key) {
        Map<String, Object> paramsMap = contextLocal.get();
        if (paramsMap != null) {
            return paramsMap.get(key);
        }
        return null;
    }

    public static void clear() {
        contextLocal.remove();
    }
}
