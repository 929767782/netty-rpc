import manager.RpcClientManager;
import service.HelloService;

/**
 * 客户端测试
 *
 * @author chenlei
 */
public class RpcClient {
    public static void main(String[] args) {
        HelloService service = RpcClientManager.getProxyService(HelloService.class);
        service.sayHello();
    }
}
