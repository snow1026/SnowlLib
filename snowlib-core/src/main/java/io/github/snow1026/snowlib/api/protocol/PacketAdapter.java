package io.github.snow1026.snowlib.api.protocol;

import io.github.snow1026.snowlib.internal.protocol.SnowPacketAdapter;
import org.bukkit.entity.Player;

public interface PacketAdapter {

    static PacketAdapter packetadapter() {
        return new SnowPacketAdapter();
    }

    void send(Player player, Packet<? extends PacketType.ClientBound> packet);
    void send(Packet<? extends PacketType.ServerBound> packet);
}
