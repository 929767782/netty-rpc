package loadBalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡算法
 *
 * @author llp
 */
public interface LoadBalancer {

    InetSocketAddress getInstance(List<InetSocketAddress> list);

}
