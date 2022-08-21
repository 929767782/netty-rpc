package com.llp.rpc.common.loadBalancer;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConsistentHashRule implements LoadBalancer {

    private final static String VIRTUAL_NODE_SPLIT = "#";
    private final ConcurrentHashMap<String, ConsistentHashRing> selectorCache = new ConcurrentHashMap<>();

    @Override
    public InetSocketAddress getInstance(List<InetSocketAddress> list) {
        return null;
    }

    @Override
    public InetSocketAddress getInstance(List<InetSocketAddress> list,String local) {
        int identityHashCode = computeIdentityHashCode(list);
        ConsistentHashRing selector = selectorCache.get(local);

        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectorCache.put(local, new ConsistentHashRing(identityHashCode, list, 160));
            selector = selectorCache.get(local);
        }
        return selector.selectForKey(local);
    }

    static class ConsistentHashRing {
        private final TreeMap<Integer, InetSocketAddress> hashRing;
        private final int identityHashCode;

        ConsistentHashRing(int identityHashCode, List<InetSocketAddress> serviceList, int virtualNodeNum) {
            this.hashRing = new TreeMap<>();
            this.identityHashCode = identityHashCode;
            for (InetSocketAddress instance : serviceList) {
                for (int i = 0; i < virtualNodeNum / 4; i++) {
                    byte[] digest = md5(String.join(VIRTUAL_NODE_SPLIT, instance.getAddress().toString(), String.valueOf(instance.getPort())) + i);
                    for (int h = 0; h < 4; h++) {
                        int key = hash(digest, h);
                        hashRing.put(key, instance);
                    }
                }
            }
        }
        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return md.digest();
        }

        static int hash(byte[] digest, int idx) {
            return ((digest[3 + idx * 4] & 255) << 24 | (digest[2 + idx * 4] & 255) << 16 | (digest[1 + idx * 4] & 255) << 8 | (digest[idx * 4] & 255));
        }

        public InetSocketAddress selectForKey(String serviceName) {
            byte[] digest = md5(serviceName);
            Map.Entry<Integer, InetSocketAddress> entry = hashRing.tailMap(hash(digest, 0), true).firstEntry();
            if (entry == null) {
                entry = hashRing.firstEntry();
            }
            return entry.getValue();
        }
    }

    private int computeIdentityHashCode(List<InetSocketAddress> list) {
        long identityHashCode = 0L;
        for (InetSocketAddress isa : list) {
            identityHashCode += (long) isa.hashCode();
        }
        return (int)((identityHashCode & 0xFFFFFFFFL) + (identityHashCode & 0xFFFFFFFF00000000L));
    }
}
