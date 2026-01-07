package io.github.snow1026.snowlib.api.protocol;

public sealed interface PacketType permits PacketType.ClientBound, PacketType.ServerBound {

    PacketDirection direction();
    PacketState state();

    non-sealed interface ClientBound extends PacketType {
        default PacketDirection direction() {
            return PacketDirection.CLIENTBOUND;
        }
    }

    non-sealed interface ServerBound extends PacketType {
        default PacketDirection direction() {
            return PacketDirection.SERVERBOUND;
        }
    }
}
