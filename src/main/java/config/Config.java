package config;

import protocol.Algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置类，加载配置文件中的序列化方式和服务器端口号等重要属性
 */
public abstract class Config {
    static Properties properties;
    static {
        try(InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public static int getServerPort(){
        String port = properties.getProperty("server.port");
        if(port==null){
            return 8080;
        }else {
            return Integer.parseInt(port);
        }
    }

    public static Algorithm getSerializerAlgorithm(){
        String algorithm = properties.getProperty("serializer.algorithm");
        if(algorithm==null){
            return Algorithm.Java;
        }else{
            return Algorithm.valueOf(algorithm);
        }
    }

    public static String getZookeeperAddress(){
        String address = properties.getProperty("zookeeper.address");
        return address;
    }

    public static String getZookeeperSessionTimeout(){
        return properties.getProperty("zookeeper.session_timeout");
    }
    public static String getZookeeperRegistryPath(){
        return properties.getProperty("zookeeper.registry_path");
    }
    public static String getZookeeperNameSpace(){
        return properties.getProperty("zookeeper.name_space");
    }
}
