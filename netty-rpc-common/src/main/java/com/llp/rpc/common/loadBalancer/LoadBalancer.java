package com.llp.rpc.common.loadBalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡算法
 *
 * @author llp
 */
public interface LoadBalancer {

    InetSocketAddress getInstance(List<InetSocketAddress> list);
    InetSocketAddress getInstance(List<InetSocketAddress> list,String local);
}
