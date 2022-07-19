package com.llp.rpc;

import com.llp.rpc.annotation.RpcServerScan;
import com.llp.rpc.manager.RpcServerManager;
@RpcServerScan
public class RpcServerTest {
    public static void main(String[] args) {
        new RpcServerManager("127.0.0.1",8888).start();
    }
}