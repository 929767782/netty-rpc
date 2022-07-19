package com.llp.rpc.protocol;

public interface Serializer {

    //序列化方法
    <T> byte[] serialize(T object);

    //反序列化方法
    <T> T deserialize(Class<T> clazz,byte[] bytes);
}
