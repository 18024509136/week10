package com.geek;

import cn.hutool.json.JSONUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * 服务暴露类
 */
@Component
public class ServerExposer {

    private static CuratorFramework client;

    static {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .connectionTimeoutMs(3000)
                .sessionTimeoutMs(5000)
                .namespace("myRpc")
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }

    /**
     * @param interfaceName
     * @param rpcService
     * @throws Exception
     */
    public void register(String interfaceName, RpcService rpcService, int port) throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        String localIp = addr.getHostAddress();

        RegisterInfo registerInfo = new RegisterInfo(localIp, port, rpcService.group(), rpcService.version());
        String json = JSONUtil.toJsonStr(registerInfo);

        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/" + interfaceName + "/" + json);
    }


}
