package io.github.snow1026.snowlib.registry.internal;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.api.protocol.Packet;
import io.github.snow1026.snowlib.api.protocol.ProtocolUtil;
import io.github.snow1026.snowlib.registry.MappedRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 패킷 래퍼와 NMS 패킷 클래스 간의 매핑을 관리하는 특수 레지스트리입니다.
 */
public final class PacketRegistry implements MappedRegistry<Packet<?>> {
    private final Map<Class<?>, Packet<?>> nmsToWrapper = new ConcurrentHashMap<>();
    private final Map<Packet<?>, Class<?>> wrapperToNms = new ConcurrentHashMap<>();
    private final Map<SnowKey, Packet<?>> keyMap = new ConcurrentHashMap<>();

    public PacketRegistry() {
    }

    @Override
    public void register(SnowKey key, Packet<?> wrapper) {
        Class<?> nms = ProtocolUtil.resolveNMSPacket(wrapper);

        nmsToWrapper.put(nms, wrapper);
        wrapperToNms.put(wrapper, nms);

        keyMap.put(key, wrapper);
    }

    @Override
    public void unregister(SnowKey key) {
        Packet<?> wrapper = keyMap.remove(key);
        if (wrapper != null) {
            Class<?> nms = wrapperToNms.remove(wrapper);
            if (nms != null) {
                nmsToWrapper.remove(nms);
            }
        }
    }

    @Override
    public Packet<?> get(SnowKey key) {
        return keyMap.get(key);
    }

    @Override
    public Collection<Packet<?>> getAll() {
        return Collections.unmodifiableCollection(keyMap.values());
    }

    @Override
    public Map<SnowKey, Packet<?>> getEntries() {
        return Collections.unmodifiableMap(keyMap);
    }

    /**
     * NMS 패킷 클래스에 해당하는 래퍼를 찾습니다.
     */
    public Optional<Packet<?>> findWrapper(Class<?> nmsPacket) {
        return Optional.ofNullable(nmsToWrapper.get(nmsPacket));
    }

    /**
     * 래퍼에 해당하는 NMS 클래스를 찾습니다.
     */
    public Optional<Class<?>> findNms(Packet<?> wrapper) {
        return Optional.ofNullable(wrapperToNms.get(wrapper));
    }
}
