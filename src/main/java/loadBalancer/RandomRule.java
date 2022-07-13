package loadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

/**
 * 负载均衡随机算法
 *
 * @author llp
 */
public class RandomRule implements LoadBalancer{
    private final Random random=new Random();

    /**
     * 随机获取实例
     * @param list
     * @return
     */
    @Override
    public InetSocketAddress getInstance(List<InetSocketAddress> list) {
        return list.get(random.nextInt(list.size()));
    }
}