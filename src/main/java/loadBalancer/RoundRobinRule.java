package loadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡算法
 *
 * @author llp
 */
public class RoundRobinRule implements LoadBalancer {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * 防止Integer越界 超过Integer最大值
     *
     * @return
     */
    private final int getAndIncrement() {
        int current;
        int next;
        do {
            current = this.atomicInteger.get();
            next = current >= 2147483647 ? 0 : current + 1;
        } while (!this.atomicInteger.compareAndSet(current, next));
        return next;
    }


    /**
     * 轮询获取实例
     * @param list
     * @return
     */
    @Override
    public InetSocketAddress getInstance(List<InetSocketAddress> list) {
        int index = getAndIncrement() % list.size();
        return list.get(index);
    }
}
