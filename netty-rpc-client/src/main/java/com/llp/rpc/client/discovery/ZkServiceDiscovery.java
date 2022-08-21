package com.llp.rpc.client.discovery;


import com.llp.rpc.common.config.Config;
import com.llp.rpc.common.loadBalancer.ConsistentHashRule;
import com.llp.rpc.common.loadBalancer.LoadBalancer;
import com.llp.rpc.common.loadBalancer.RoundRobinRule;
import com.llp.rpc.common.zookeeper.CuratorClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * zookeeper实现 服务发现功能
 *
 * @author llp
 */
public class ZkServiceDiscovery implements ServiceDiscovery{

    private CuratorClient curatorClient;

    private final LoadBalancer loadBalancer;

    private List<InetSocketAddress> socketAddressesCache;


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
            if(socketAddressesCache==null || socketAddressesCache.size()==0){
                byte[] data = curatorClient.getData(Config.getZookeeperRegistryPath()+"/"+serviceName);
                curatorClient.watchNode(Config.getZookeeperRegistryPath() + "/" + serviceName, new Watcher() {
                    @Override
                    public void process(WatchedEvent watchedEvent) {
                        String path = watchedEvent.getPath();
                        updateService(path);
                    }
                });
                String allAddress = new String(data);
                socketAddressesCache = new CopyOnWriteArrayList<>();
                for(String address : allAddress.split("#")){
                    String[] hostAndPort = address.split(":");
                    InetSocketAddress socketAddress = new InetSocketAddress(hostAndPort[0],Integer.parseInt(hostAndPort[1]));
                    socketAddressesCache.add(socketAddress);
                }
            }

            if(loadBalancer instanceof ConsistentHashRule){
                return loadBalancer.getInstance(socketAddressesCache, InetAddress.getLocalHost().getHostAddress());
            }else{
                return loadBalancer.getInstance(socketAddressesCache);
            }
        } catch (KeeperException.NoNodeException e){
            throw new RuntimeException("找不到对应服务");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateService(String path){
        try {
            byte[] data = curatorClient.getData(path);
            curatorClient.watchNode(path, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    String path = watchedEvent.getPath();
                    updateService(path);
                }
            });
            String allAddress = new String(data);
            socketAddressesCache = new CopyOnWriteArrayList<>();
            for(String address : allAddress.split("#")){
                String[] hostAndPort = address.split(":");
                InetSocketAddress socketAddress = new InetSocketAddress(hostAndPort[0],Integer.parseInt(hostAndPort[1]));
                socketAddressesCache.add(socketAddress);
            }
        } catch (KeeperException.NoNodeException e){
            throw new RuntimeException("找不到对应服务");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
