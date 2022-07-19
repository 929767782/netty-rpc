package com.llp.rpc;

import com.llp.rpc.manager.ClientProxy;
import com.llp.rpc.manager.RpcClientManager;
import com.llp.rpc.service.HelloService;
import com.llp.rpc.service.HelloServiceImpl;

/**
 * 客户端测试
 *
 * @author llp
 */
public class RpcClientTest {
    public static void main(String[] args) {
        RpcClientManager clientManager = new RpcClientManager();
        //创建代理对象
        HelloService service = new ClientProxy(clientManager).getProxyService(HelloServiceImpl.class);
        System.out.println(service.sayHello());
    }
}
