package loadBalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 负载均衡算法
 *
 * @author llp
 */
public interface LoadBalancer {

    Instance getInstance(List<Instance> list);

}
