package com.llp.rpc.server.service;


import com.llp.rpc.common.annotation.RpcServer;

@RpcServer
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello() {
        return "hehe";
    }
}
