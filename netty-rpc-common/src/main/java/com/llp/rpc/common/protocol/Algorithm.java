package com.llp.rpc.common.protocol;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.google.gson.*;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum Algorithm implements Serializer{
    Java{
        @Override
        public <T> T deserialize(Class<T> clazz, byte[] bytes) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                return (T) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("反序列化失败", e);
            }
        }

        @Override
        public <T> byte[] serialize(T object) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(object);
                return bos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("序列化失败", e);
            }
        }
    },

    Json{
        @Override
        public <T> T deserialize(Class<T> clazz, byte[] bytes) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Algorithm.ClassCodec()).create();
            String json = new String(bytes, StandardCharsets.UTF_8);
            System.out.println(json);
            return gson.fromJson(json, clazz);
        }

        @Override
        public <T> byte[] serialize(T object) {
            System.out.println(object);
            Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Algorithm.ClassCodec()).create();
            String json = gson.toJson(object);
            System.out.println(json);
            return json.getBytes(StandardCharsets.UTF_8);
        }
    },
    Protostuff{
        private Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

        private Objenesis objenesis = new ObjenesisStd(true);

        @SuppressWarnings("unchecked")
        private <T> Schema<T> getSchema(Class<T> cls) {
            // for thread-safe
            return (Schema<T>) cachedSchema.computeIfAbsent(cls, RuntimeSchema::createFrom);
        }

        @Override
        public <T> byte[] serialize(T obj) {
            Class<T> cls = (Class<T>) obj.getClass();
            LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
            try {
                Schema<T> schema = getSchema(cls);
                return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            } finally {
                buffer.clear();
            }
        }

        @Override
        public <T> T deserialize(Class<T> clazz,byte[] bytes) {
            try {
                T message = (T) objenesis.newInstance(clazz);
                Schema<T> schema = getSchema(clazz);
                ProtostuffIOUtil.mergeFrom(bytes, message, schema);
                return message;
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    };


    class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                String str = json.getAsString();
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            // class -> json
            System.out.println(src);
            System.out.println(src.getName());
            return new JsonPrimitive(src.getName());
        }
    }

}
