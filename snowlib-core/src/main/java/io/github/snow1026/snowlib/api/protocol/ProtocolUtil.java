package io.github.snow1026.snowlib.api.protocol;

import io.github.snow1026.snowlib.utils.reflect.Reflection;

public final class ProtocolUtil {

    private ProtocolUtil() {
        throw new UnsupportedOperationException();
    }

    public static Class<?> resolveNMSPacket(Packet<?> wrapper) {
        return resolveNMSPacket(wrapper.getClass());
    }

    public static Class<?> resolveNMSPacket(Class<?> wrapper) {
        String name = wrapper.getSimpleName();
        boolean client = name.startsWith("C");
        boolean server = name.startsWith("S");

        if (!client && !server) {
            throw new IllegalStateException("Packet name must start with C or S: " + name);
        }

        String dir = client ? "Clientbound" : "Serverbound";
        String base = name.substring(1);
        String state = ((PacketState) Reflection.on(wrapper).call("type").get()).name().toLowerCase();

        return Reflection.getClass("{nms}.network.protocol." + state + "." + dir + base + "Packet");
    }
}
