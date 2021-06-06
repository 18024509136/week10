package com.geek;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Rpc客户端核心类
 *
 * @author huangxiaodi
 * @since 2021-05-26 09:34
 */
@Component
@ChannelHandler.Sharable
public class NettyClient extends SimpleChannelInboundHandler<Object> {

    private Object lock = new Object();

    private RpcResponse rpcResponse;

    @Autowired
    private RpcClientFilter filter;

    @Autowired
    private RoundRobinLoadBalancer loadBalancer;

    /**
     * 使用netty客户端发送请求
     *
     * @param rpcRequest
     * @return
     * @throws Exception
     */
    public RpcResponse send(RpcRequest rpcRequest) throws Exception {
        NioEventLoopGroup workGroup = new NioEventLoopGroup(1);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    // out1
                    pipeline.addLast(new RpcClientEncoder());
                    // in1
                    pipeline.addLast(new RpcClientDecoder());
                    // in2
                    pipeline.addLast(NettyClient.this);
                }
            });
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

            // 获取并过滤路由信息
            List<RouteInfo> routeInfos = this.filter.filter(rpcRequest.getInterfaceName());
            // 对过滤后路由信息进行负载均衡
            RouteInfo routeInfo = loadBalancer.choose(rpcRequest.getInterfaceName(), routeInfos);

            ChannelFuture channelFuture = bootstrap.connect(routeInfo.getIp(), routeInfo.getPort()).sync();
            channelFuture.channel().writeAndFlush(rpcRequest);

            // 等待响应
            synchronized (lock) {
                lock.wait();
            }

            if (rpcResponse != null) {
                channelFuture.channel().closeFuture().sync();
            }
            return rpcResponse;

        } finally {
            workGroup.shutdownGracefully().sync();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        this.rpcResponse = (RpcResponse) msg;

        synchronized (lock) {
            // 唤醒等待响应的线程
            lock.notifyAll();
        }
        ctx.close();
    }
}
