# netty-rpc
### 项目描述
一个轻量级的 RPC 框架。基于 Netty 实现的网络传输，基于 Zookeeper 实现的服务注册中心，采用自定义的应用层通
信协议进行消息的传输，采用多种可选的序列化机制。该框架还包括心跳机制、负载均衡等功能。

### 项目结构
``` lua
netty-rpc
├── netty-rpc-client -- 客户端实现
|          ├── discovery -- 服务发现功能的封装
|          ├── handler -- 客户端的handler。包括发送心跳包，处理响应消息。
|          ├── manager -- 客户端管理器。rpc具体调用细节封装在这里。
|          ├── service -- 远程调用方法的接口
|          └── RpcClientMain -- 客户端启动类（用于测试）
|
├── netty-rpc-server -- 服务端实现
|          ├── factory -- 服务工厂。存放所有标注了注解@RpcService的对象，即暴露服务的对象。
|          ├── handler -- 服务端的handler。包括检测心跳包，处理请求消息。
|          ├── manager -- 服务端管理器。服务端初始化细节（包括启动服务器、扫描暴露服务等）封装在这里。
|          ├── registry -- 服务注册功能的封装
|          └── RpcClientMain -- 服务端启动类
|
└── netty-rpc-common -- 通用模块
           ├── annotation -- 自定义注解。
           ├── config -- 配置相关，读取配置文件信息。
           ├── loadBalancer -- 负载均衡功能。
           ├── message -- rpc传输的消息体封装。包括心跳包消息、请求消息、响应消息。
           ├── protocol -- rpc传输的协议封装。包括编解码器、序列化器、粘包半包处理器。
           ├── utils -- 工具类的封装。
           └── zookeeper -- zookeeper客户端基本操作的封装
```

### rpc基本框架

### 已实现的功能
- [x] **使用 Netty（基于 NIO）替代 BIO 实现网络传输；**
- [x] **使用多种可选的序列化机制；**
- [x] **使用 Zookeeper 管理相关服务地址信息**
- [x] Netty 重用 Channel 避免重复连接服务端
- [x] 使用 Promise 对象包装接收客户端返回结果
- [x] **增加 Netty 心跳机制** : 保证客户端和服务端的连接不被断掉，避免重连。
- [x] **客户端调用远程服务的时候进行负载均衡** ：调用服务的时候，从很多服务地址中根据相应的负载均衡算法选取一个服务地址。
- [x] **自定义注解标注需要暴露的服务，并将其存放到服务工厂中** 。
- [x] **增加可配置比如序列化方式,避免硬编码** ：通过配置文件的方式进行配置
- [x] **客户端与服务端通信协议（数据包结构）重新设计** ，将 `RpcRequestMessage`和 `RpcResponeseMessage` 对象作为消息体，然后增加如下字段作为消息头：
  - **魔数** ： 4 个字节。这个魔数主要是为了筛选来到服务端的数据包，有了这个魔数之后，服务端首先取出前面四个字节进行比对，能够在第一时间识别出这个数据包并非是遵循自定义协议的，也就是无效数据包，为了安全考虑可以直接关闭连接以节省资源。
  - **序列化器编号** ：1个字节。标识序列化的方式，比如是使用 Java 自带的序列化，还是 json，kyro 等序列化方式。
  - **消息体长度** ： 4个字节。用于识别完整的数据包，防止粘包半包现象。
  - **消息体类型** ： 1个字节。用于识别是什么类型的消息。
  - **请求序号** ： 4个字节。为了双工通信，提供异步能力。
  - **版本号** ： 1个字节。可以支持协议的升级。
  
  
### 待实现的功能及思路
- [ ] **集成spring容器管理服务对象** ：通过spring注解注册和消费服务。
- [ ] **处理一个服务接口对应多个实现类的情况** ：在服务接口的注解的value中标注对应的实现类。在协议中多一个字段指定该实现类名。服务端通过指定实现类名调用服务。


### 运行项目
1. 在netty-rpc-common模块的配置文件中，将zookeeper地址改为自己服务器上的zookeeper地址。
2. 运行netty-rpc-server模块中的RpcServerMain，启动服务端。
3. 运行netty-rpc-client模块中的RpcClientMain，启动客户端。
