package com.llp.rpc.service;

import com.llp.rpc.annotation.RpcServer;

@RpcServer
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello() {
        return "hehe";
    }
}
