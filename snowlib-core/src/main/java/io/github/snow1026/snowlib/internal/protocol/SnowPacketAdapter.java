package io.github.snow1026.snowlib.internal.protocol;

import io.github.snow1026.snowlib.api.protocol.Packet;
import io.github.snow1026.snowlib.api.protocol.PacketAdapter;
import io.github.snow1026.snowlib.api.protocol.PacketType;
import io.github.snow1026.snowlib.api.protocol.PacketUtil;
import io.github.snow1026.snowlib.utils.reflect.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SnowPacketAdapter implements PacketAdapter {

    @Override
    public void send(Player player, Packet<? extends PacketType.ClientBound> packet) {
        Reflection.on(connection(player)).call("send", PacketUtil.conversation(packet));
    }

    @Override
    public void send(Packet<? extends PacketType.ServerBound> packet) {
        Reflection.on(connection()).call("send", PacketUtil.conversation(packet));
    }

    private static Object nmsPlayer(Player player) {
        return Reflection.on(player).call("getHandle").get();
    }

    private static Object connection(Player player) {
        return Reflection.on(nmsPlayer(player)).field("connection").value();
    }

    private static Object nmsServer() {
        return Reflection.on(Bukkit.getServer()).call("getServer").get();
    }

    private static Object connection() {
        return Reflection.on(nmsServer()).field("connection").value();
    }
}
