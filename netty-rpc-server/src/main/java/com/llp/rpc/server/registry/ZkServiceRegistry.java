package com.llp.rpc.server.registry;


import com.llp.rpc.common.config.Config;
import com.llp.rpc.common.zookeeper.CuratorClient;
import org.apache.zookeeper.KeeperException;

import java.net.InetSocketAddress;

/**
 * zookeeper实现服务注册功能
 *
 * @author llp
 */
public class ZkServiceRegistry implements ServiceRegistry{
    private CuratorClient curatorClient;
    public ZkServiceRegistry(String registryAddress) {
        this.curatorClient = new CuratorClient(registryAddress, 5000);
    }

    /**
     * 服务注册
     * @param serviceName
     * @param inetSocketAddress
     */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress){
        String host = inetSocketAddress.getHostName();
        int port = inetSocketAddress.getPort();
        String socketAddress = host + ":" + port;

        String path = Config.getZookeeperRegistryPath() + "/" + serviceName;
        try {
            byte[] data = curatorClient.getData(path);
            String allAddress = new String(data);
            allAddress = allAddress + "#" + socketAddress;
            curatorClient.updatePathData(path,allAddress.getBytes());

        } catch (KeeperException.NoNodeException e){
            try {
                curatorClient.createPathData(path,socketAddress.getBytes());
            } catch (Exception ex) {
                throw new RuntimeException("创建节点失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("服务注册失败");
        }
    }

}
