package com.geek;

import cn.hutool.json.JSONUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 服务发现类
 */
@Component
public class Discovery {

    private static CuratorFramework client;

    private static final Map<String, List<RouteInfo>> servers = new ConcurrentHashMap<>(10);

    static {
        // 初始化zookeeper客户端
        initZkClient();
    }

    private static void initZkClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .connectionTimeoutMs(3000)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }


    public void init() throws Exception {
        TreeCache treeCache = new TreeCache(client, "/myRpc");
        treeCache.start();
        // 监听事件的路径格式如：/myRpc/com.geek.stub.OrderService/{"ip":"192.168.3.42","version":0,"port":9000,"group":""}
        treeCache.getListenable().addListener((cf, event) -> {
            TreeCacheEvent.Type eventType = event.getType();
            switch (eventType) {
                case NODE_ADDED:
                    // 新增路由信息
                    addRoutes(event.getData().getPath());
                    break;
                case NODE_REMOVED:
                    // 更新路由信息
                    updateRoutes(event.getData().getPath());
                    break;
                case CONNECTION_LOST:
                    // 重新注册客户端和监听
                    reconnect();
                    break;
                default:
                    break;
            }
        });
    }

    private void reconnect() throws Exception {
        if (client != null) {
            client.close();
        }
        initZkClient();
        this.init();
    }

    private void addRoutes(String path) throws Exception {
        String[] splitPath = path.substring(1).split("/");
        if (splitPath.length == 2) {
            String servicePath = splitPath[1];
            List<RouteInfo> routes = client.getChildren().forPath(path).stream()
                    .map(routeString -> JSONUtil.toBean(routeString, RouteInfo.class)).collect(Collectors.toList());
            servers.put(servicePath, routes);
        }
    }

    private void updateRoutes(String path) throws Exception {
        String[] splitPath = path.substring(1).split("/");
        String servicePath = splitPath[1];

        String fullServicePath = path.substring(0, path.lastIndexOf("/"));
        List<RouteInfo> routes = client.getChildren().forPath(fullServicePath).stream()
                .map(routeString -> JSONUtil.toBean(routeString, RouteInfo.class)).collect(Collectors.toList());
        servers.put(servicePath, routes);
    }

    public List<RouteInfo> getByServiceName(String serviceName) {
        return servers.get(serviceName);
    }
}
