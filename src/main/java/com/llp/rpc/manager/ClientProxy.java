package com.llp.rpc.manager;

import com.llp.rpc.message.RpcRequestMessage;
import com.llp.rpc.protocol.SequenceIdGenerator;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;

import java.lang.reflect.Proxy;
/**
 * Rpc客户端代理
 *
 * @author llp
 */
public class ClientProxy {
    private final RpcClientManager RPC_CLIENT;

    public ClientProxy(RpcClientManager client){
        this.RPC_CLIENT=client;
    }

    //JDK动态代理创建代理类
    public  <T> T getProxyService(Class<T> serviceClass) {
        ClassLoader loader = serviceClass.getClassLoader();
        Class<?>[] interfaces = serviceClass.getInterfaces();
        //创建代理对象
        Object o = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {
            // 1. 将方法调用转换为 消息对象
            int sequenceId = SequenceIdGenerator.nextId();
            RpcRequestMessage msg = new RpcRequestMessage(
                    sequenceId,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args
            );

            // 2. 将消息对象发送出去
            Channel channel = RPC_CLIENT.sendRpcRequest(msg);

            // 3. 准备一个空 Promise 对象，来接收结果 存入集合            指定 promise 对象异步接收结果线程
            DefaultPromise<Object> promise = new DefaultPromise<Object>(channel.eventLoop());
            RpcClientManager.PROMISES.put(sequenceId, promise);

            // 4. 等待 promise 结果
            promise.await();
            if(promise.isSuccess()) {
                // 调用正常
                return promise.getNow();
            } else {
                // 调用失败
                throw new RuntimeException(promise.cause());
            }
        });
        return (T) o;
    }
}
