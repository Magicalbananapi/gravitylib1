package io.github.magicalbananapie.gravitylib.util;

import io.github.magicalbananapie.gravitylib.EntityGravity;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.network.PacketByteBuf;


/**
 * @author Magicalbananapie
 * @see net.minecraft.entity.data.TrackedData
 * - A collection of useful data structures for gravity
 */
public class GravityData {
    public static final TrackedDataHandler<EntityGravity> ENTITY_GRAVITY = new TrackedDataHandler<EntityGravity>() {
        public void write(PacketByteBuf packetByteBuf, EntityGravity entityGravity) {
            packetByteBuf.writeEnumConstant(entityGravity);
        }

        public EntityGravity read(PacketByteBuf packetByteBuf) {
            return packetByteBuf.readEnumConstant(EntityGravity.class);
        }

        public EntityGravity copy(EntityGravity entityGravity) {
            return entityGravity;
        }
    };
}