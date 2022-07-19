package com.llp.rpc.server;

import com.llp.rpc.common.annotation.RpcServerScan;
import com.llp.rpc.server.manager.RpcServerManager;

/**
 * 服务端启动类
 *
 * @author llp
 */
@RpcServerScan
public class RpcServerMain {
    public static void main(String[] args) {
        new RpcServerManager("127.0.0.1",8888).start();
    }
}