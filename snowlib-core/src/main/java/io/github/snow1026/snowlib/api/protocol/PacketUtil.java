package io.github.snow1026.snowlib.api.protocol;

import io.github.snow1026.snowlib.utils.reflect.Reflection;

public final class PacketUtil {

    private PacketUtil() {
        throw new UnsupportedOperationException();
    }

    public static Object conversation(Packet<?> packet) {
        Class<?> nms = ProtocolUtil.resolveNMSPacket(packet);
        if (nms == null) return null;
        return Reflection.newInstance(nms, packet.args().toArray());
    }
}
