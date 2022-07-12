package config;

import protocol.Algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
}
