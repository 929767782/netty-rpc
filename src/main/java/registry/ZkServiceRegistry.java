package registry;


import config.Config;

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
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            String host = inetSocketAddress.getHostName();
            int port = inetSocketAddress.getPort();
            String socketAddress = host + ":" + port;

            String path = Config.getZookeeperRegistryPath() + "/" + serviceName;
            byte[] data = curatorClient.getData(path);
            if(data.length>0){
                String allAddress = new String(data);
                allAddress = allAddress + "#" + socketAddress;
                curatorClient.updatePathData(path,allAddress.getBytes());
            }else{
                curatorClient.createPathData(path,socketAddress.getBytes());
            }
        } catch (Exception e) {
            throw new RuntimeException("服务注册失败");
        }
    }

}
