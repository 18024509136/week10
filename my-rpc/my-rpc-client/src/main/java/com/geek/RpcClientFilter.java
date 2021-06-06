package com.geek;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RpcClientFilter {

    @Autowired
    private Discovery discovery;

    public List<RouteInfo> filter(String serviceName) {
        // 查询出所有路由信息
        List<RouteInfo> routeInfos = discovery.getByServiceName(serviceName);
        RpcReference rpcClientConfig = RpcStubAnnotationPostProcessor.getRpcClientConfig(serviceName);
        // 根据分组和版本信息过滤路由信息
        routeInfos = routeInfos.stream().filter(route -> {
            // 客户端要找到分组一致的暴露服务
            if (!rpcClientConfig.group().equals(route.getGroup())) {
                return false;
            }
            // 因为暴露的服务是后向兼容，所有暴露服务的版本大于或等于客户端要求版本即可
            if (rpcClientConfig.version() > route.getVersion()) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
        return routeInfos;
    }
}
