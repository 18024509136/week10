package com.geek;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * robin负载均衡类
 */
@Component
public class RoundRobinLoadBalancer {

    private Map<String, RoundRobin> balanceMap = new ConcurrentHashMap<>(10);

    public RouteInfo choose(String serviceName, List<RouteInfo> routeInfos) {
        if (routeInfos == null || routeInfos.isEmpty()) {
            return null;
        }

        RoundRobin roundRobin = balanceMap.get(serviceName);
        // 双重检测，保证高并发下不会重复赋值
        if (roundRobin == null) {
            synchronized (RoundRobinLoadBalancer.class) {
                if (roundRobin == null) {
                    roundRobin = new RoundRobin(new AtomicLong(0));
                    this.balanceMap.putIfAbsent(serviceName, roundRobin);
                }
            }
        }

        int nextIndex = roundRobin.getNextIndex(routeInfos.size());
        return routeInfos.get(nextIndex);
    }

    @AllArgsConstructor
    private static class RoundRobin {

        private AtomicLong current = new AtomicLong(0);

        private int getNextIndex(int total) {
            long newCurrent = current.getAndIncrement();
            return (int) (newCurrent % total);
        }
    }
}
