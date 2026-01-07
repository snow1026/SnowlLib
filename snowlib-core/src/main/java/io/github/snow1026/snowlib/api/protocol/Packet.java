package io.github.snow1026.snowlib.api.protocol;

import io.github.snow1026.snowlib.registry.Registrable;

import java.util.List;

public interface Packet<T extends PacketType> extends Registrable {

    T type();

    List<Object> args();
}
