## 上周rpc框架作业提要 ##
- my-rpc-client模块为Rpc客户端框架，cglib实现API调用的拦截和远程调用，cglib核心代码是com.geek.ServiceFacade和com.geek.RpcClientCglibProxy。
底层通信采用netty客户端，序列化方式采用protostuff，核心代码为com.geek.RpcClient。目前netty客户端只支持单线程调用，有待改进。
- my-rpc-server模块为Rpc服务端框架。核心代码有com.geek.RpcServer，用于远程服务映射构建和netty服务的启动。远程服务映射是基于@RpcService注解在项目启动时获取到接口限定名和spring管理的业务实现类的映射关系。  
com.geek.RpcHandler作用是通过请求的接口限定名找到业务实现类，然后通过反射调用目标实现类。
- my-rpc-interface模块为客户端应用和服务端应用共享的远程API接口。
- my-rpc-client-app模块为客户端应用，核心代码为com.geek.ClientApplication，用于启动应用和远程api调用测试。
- my-rpc-server-app模块是服务端应用，com.geek.OrderServiceImpl是远程api真实的业务实现，com.geek.ServerApplication是应用启动入口。

## 本周rpc框架改进 ##
### 服务端改进 ###
my-rpc-server模块  
- com.geek.RpcService注解增加group和version属性，以便客户端进行基于group和version的过滤。
- com.geek.ServerExposer实现服务端的服务注册功能，zk中节点格式如/myRpc/com.geek.stub.OrderService/{"ip":"192.168.3.42","version":0,"port":9000,"group":""}。
### 客户端改进 ###
my-rpc-client模块  
- Spring Bean中使用@RpcReference注解注入rpc api的代理对象，无需手动去创建代理对象，是通过com.geek.RpcStubAnnotationPostProcessor在postProcessBeforeInitialization阶段对spring bean成员变量赋值来实现。
- com.geek.Discovery实现订阅zk中服务节点及其子节点的变化事件，捕获NODE_ADDED和NODE_REMOVED事件来同步更新本地服务列表的缓存。
- com.geek.RpcClientFilter实现对本地某个服务的服务列表进行过滤，过滤规则基于服务端注册的服务信息以及@RpcReference指定的group和version。
- com.geek.RoundRobinLoadBalancer实现对过滤后的服务列表进行负载均衡，算法使用轮询。
my-rpc-client-app模块  
- 新增@Service注解的com.geek.TestService，成员变量使用@RpcReference注解注入rpc api的代理对象。该类是为了测试rpc api的代理对象是否注入成功。

## 待改进的地方 ##
- 客户端多个线程无法共享单个channel发送请求，造成资源浪费，需改成tcp长连接。客户端需要增加每次消息间的隔离标识，对应服务端增加隔离标识处理的handler。同时客户端和服务端增加心跳机制的处理。

