package io.github.magicalbananapie.gravitylib.util;

import io.github.magicalbananapie.gravitylib.EntityGravity;
import net.minecraft.util.math.Vec3d;

public interface EntityAccessor {
    void setGravity(EntityGravity gravity, int length);
    EntityGravity getGravity();
    void setTransitionAngle(float transitionAngle);
    float getTransitionAngle();
    void setEyePosChangeVector(Vec3d eyePosChangeVector);
    Vec3d getEyePosChangeVector();
}
