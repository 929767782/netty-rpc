package factory;


import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 提供服务的工厂  给定接口名，提供具体的调用对象
 *
 * @author llp
 */
@Slf4j
public class ServiceFactory {

    //保存所有有注解@RpcService的集合
    static final Map<String, Object> serviceFactory = new ConcurrentHashMap<>();

    //添加已注解的类进入工厂
    public <T> void addServiceProvider(T service, String serviceName) {
        if (serviceFactory.containsKey(serviceName)) {
            return;
        }
        serviceFactory.put(serviceName, service);
        log.debug("服务类{}添加进工厂",serviceName);
    }

    //远程调用接口的实例从该方法获取
    public Object getServiceProvider(String serviceName) {
        Object service = serviceFactory.get(serviceName);
        if (service == null) {
            throw new RuntimeException("未发现该服务");
        }
        return service;
    }
}