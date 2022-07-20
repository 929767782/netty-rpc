package com.llp.rpc.client;

import com.llp.rpc.client.manager.ClientProxy;
import com.llp.rpc.client.manager.RpcClientManager;
import com.llp.rpc.client.service.HelloService;

/**
 * 客户端启动类
 *
 * @author llp
 */
public class RpcClientMain {
    public static void main(String[] args) {
        RpcClientManager clientManager = new RpcClientManager();
        //创建代理对象
        HelloService service = new ClientProxy(clientManager).getProxyService(HelloService.class);
        System.out.println(service.sayHello());
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(service.sayHello());
    }
}