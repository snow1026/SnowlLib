package io.github.snow1026.snowlib.api.protocol.game;

import io.github.snow1026.snowlib.api.protocol.Packet;
import io.github.snow1026.snowlib.api.protocol.PacketState;
import io.github.snow1026.snowlib.api.protocol.PacketType;
import io.github.snow1026.snowlib.utils.reflect.Reflection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CAddEntityPacket implements Packet<PacketType.ClientBound> {
    private final List<Object> args = new ArrayList<>();

    public CAddEntityPacket(Entity entity) {
        this(entity, 0);
    }

    public CAddEntityPacket(Entity entity, int data) {
        this(entity.getEntityId(), entity.getUniqueId(), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch(), entity.getType(), data, entity.getVelocity(), Reflection.on(Reflection.on(entity).call("getHandle").get()).call("getYHeadRot").get());
    }

    public CAddEntityPacket(int id, UUID uuid, double x, double y, double z, float xRot, float yRot, EntityType type, int data, Vector deltaMovement, double yHeadRot) {
        args.add(id);
        args.add(uuid);
        args.add(x);
        args.add(y);
        args.add(z);
        args.add(xRot);
        args.add(yRot);
        args.add(Reflection.invokeStaticMethod(Reflection.getClass("{obc}.entity.CraftEntityType"), "bukkitToMinecraft", type));
        args.add(data);
        args.add(Reflection.invokeStaticMethod(Reflection.getClass("{obc}.util.CraftVector"), "toVec3", deltaMovement));
        args.add(yHeadRot);
    }

    @Override
    public PacketType.ClientBound type() {
        return () -> PacketState.GAME;
    }

    @Override
    public List<Object> args() {
        return args;
    }
}
