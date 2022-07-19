package com.llp.rpc.server.manager;


import com.llp.rpc.common.annotation.RpcServer;
import com.llp.rpc.common.annotation.RpcServerScan;
import com.llp.rpc.common.config.Config;
import com.llp.rpc.common.protocol.MessageCodec;
import com.llp.rpc.common.protocol.ProtocolFrameDecoder;
import com.llp.rpc.common.utils.PackageScanUtils;
import com.llp.rpc.server.factory.ServiceFactory;
import com.llp.rpc.server.handler.HeartBeatServerHandler;
import com.llp.rpc.server.handler.RpcRequestMessageHandler;
import com.llp.rpc.server.registry.ZkServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * Rpc服务端管理器
 *
 * @author llp
 */
public class RpcServerManager {

    protected String host;
    protected int port;
    protected ZkServiceRegistry serverRegistry;
    protected ServiceFactory serviceFactory;
    NioEventLoopGroup worker = new NioEventLoopGroup();
    NioEventLoopGroup boss = new NioEventLoopGroup();
    ServerBootstrap bootstrap = new ServerBootstrap();

    public RpcServerManager(String host, int port) {
        this.host = host;
        this.port = port;
        serverRegistry = new ZkServiceRegistry(Config.getZookeeperAddress());
        serviceFactory = new ServiceFactory();
        autoRegistry();
    }

    /**
     * 开启服务
     */
    public void start() {
        //日志
        LoggingHandler LOGGING = new LoggingHandler(LogLevel.DEBUG);
        //消息节码器
        MessageCodec MESSAGE_CODEC = new MessageCodec();
        //RPC请求处理器
        RpcRequestMessageHandler RPC_HANDLER = new RpcRequestMessageHandler();
        //心跳处理器
        HeartBeatServerHandler HEATBEAT_SERVER = new HeartBeatServerHandler();

        try {
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 256)//全连接队列的大小
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ProtocolFrameDecoder());//定长解码器
                            pipeline.addLast(MESSAGE_CODEC);
                            pipeline.addLast(LOGGING);
                            pipeline.addLast(new IdleStateHandler(10, 0, 0));
                            pipeline.addLast(HEATBEAT_SERVER);
                            pipeline.addLast(RPC_HANDLER);
                        }
                    });
            //绑定端口
            Channel channel = bootstrap.bind(port).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("启动服务出错");
        }finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    /**
     * 自动扫描@RpcServer注解  注册服务
     */
    public void autoRegistry() {
        String mainClassPath = PackageScanUtils.getStackTrace();
        Class<?> mainClass;
        try {
            mainClass = Class.forName(mainClassPath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("启动类为找到");
        }
        if (!mainClass.isAnnotationPresent(RpcServerScan.class)) {
            throw new RuntimeException("启动类缺少@RpcServerScan 注解");
        }
        String annotationValue = mainClass.getAnnotation(RpcServerScan.class).value();
        //如果注解路径的值是空，则等于main父路径包下
        if ("".equals(annotationValue)) {
            annotationValue = mainClassPath.substring(0, mainClassPath.lastIndexOf("."));
        }
        //获取所有类的set集合
        Set<Class<?>> set = PackageScanUtils.getClasses(annotationValue);

        for (Class<?> c : set) {
            //只有有@RpcServer注解的才注册
            if (c.isAnnotationPresent(RpcServer.class)) {
                String ServerNameValue = c.getAnnotation(RpcServer.class).name();
                Object object;
                try {
                    object = c.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    System.err.println("创建对象" + c + "发生错误");
                    continue;
                }
                //注解的值如果为空，使用类实现的接口名
                if ("".equals(ServerNameValue)) {
                    String[] packageAndInterfaceName = c.getInterfaces()[0].getName().split("\\.");
                    String interfaceName = packageAndInterfaceName[packageAndInterfaceName.length-1];
                    addServer(object,interfaceName);
                } else {
                    addServer(object, ServerNameValue);
                }
            }
        }
    }

    /**
     * 添加对象到工厂和注册到注册中心
     *
     * @param server
     * @param serverName
     * @param <T>
     */
    public <T> void addServer(T server, String serverName) {
        serviceFactory.addServiceProvider(server, serverName);
        serverRegistry.register(serverName, new InetSocketAddress(host, port));
    }

}