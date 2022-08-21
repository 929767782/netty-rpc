package com.llp.rpc.client.discovery;


import com.llp.rpc.common.config.Config;
import com.llp.rpc.common.loadBalancer.ConsistentHashRule;
import com.llp.rpc.common.loadBalancer.LoadBalancer;
import com.llp.rpc.common.loadBalancer.RoundRobinRule;
import com.llp.rpc.common.zookeeper.CuratorClient;
import org.apache.zookeeper.KeeperException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * zookeeper实现 服务发现功能
 *
 * @author llp
 */
public class ZkServiceDiscovery implements ServiceDiscovery{

    private CuratorClient curatorClient;

    private final LoadBalancer loadBalancer;


    public ZkServiceDiscovery(String registryAddress, LoadBalancer loadBalancer) {
        this.curatorClient = new CuratorClient(registryAddress,5000);
        this.loadBalancer = loadBalancer == null ? new RoundRobinRule() : loadBalancer;
    }


    /**
     * 根据服务名找到服务地址
     *
     * @param serviceName
     * @return
     */
    @Override
    public InetSocketAddress getService(String serviceName){
        try {
            byte[] data = curatorClient.getData(Config.getZookeeperRegistryPath()+"/"+serviceName);
//            curatorClient.watchNode(Config.getZookeeperRegistryPath() + "/" + serviceName, new Watcher() {
//                @Override
//                public void process(WatchedEvent watchedEvent) {
//
//                }
//            });
            String allAddress = new String(data);
            List<InetSocketAddress> socketAddresses = new ArrayList<>();
            for(String address : allAddress.split("#")){
                String[] hostAndPort = address.split(":");
                InetSocketAddress socketAddress = new InetSocketAddress(hostAndPort[0],Integer.parseInt(hostAndPort[1]));
                socketAddresses.add(socketAddress);
            }
            if(loadBalancer instanceof ConsistentHashRule){
                return loadBalancer.getInstance(socketAddresses, InetAddress.getLocalHost().getHostAddress());
            }else{
                return loadBalancer.getInstance(socketAddresses);
            }
        } catch (KeeperException.NoNodeException e){
            throw new RuntimeException("找不到对应服务");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
