package com.llp.rpc.client.discovery;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 *
 * @author llp
 */
public interface ServiceDiscovery {

    /**
     * 根据服务名找到InetSocketAddress
     */
    InetSocketAddress getService(String serviceName);

}
