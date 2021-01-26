package io.github.magicalbananapie.gravitylib.mixin;

import io.github.magicalbananapie.gravitylib.EntityGravity;
import io.github.magicalbananapie.gravitylib.GravityLib;
import io.github.magicalbananapie.gravitylib.util.EntityAccessor;
import io.github.magicalbananapie.gravitylib.util.GravityData;
import io.github.magicalbananapie.gravitylib.util.Vec3dHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

import static io.github.magicalbananapie.gravitylib.GravityLib.LOGGER;
import static io.github.magicalbananapie.gravitylib.util.Vec3dHelper.PITCH;
import static io.github.magicalbananapie.gravitylib.util.Vec3dHelper.YAW;
import static io.github.magicalbananapie.gravitylib.GravityLib.config;

/**
 * @author Magicalbananapie
 * @see Entity
 * Mixin changes to entity nessesary for gravity to work
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements EntityAccessor {
    @Shadow @Final protected DataTracker dataTracker;
    @Shadow @Final protected static TrackedData<EntityPose> POSE;
    @Shadow public abstract Vec3d getPos();
    @Shadow private EntityDimensions dimensions;
    @Shadow private float standingEyeHeight;
    @Shadow protected boolean firstUpdate;
    @Shadow public World world;
    @Shadow public abstract EntityPose getPose();
    @Shadow public abstract EntityDimensions getDimensions(EntityPose pose);
    @Shadow protected abstract float getEyeHeight(EntityPose pose, EntityDimensions dimensions);
    @Shadow public abstract void setBoundingBox(Box boundingBox);
    @Shadow @Final public abstract double getX();
    @Shadow @Final public abstract double getY();
    @Shadow @Final public abstract double getZ();
    @Shadow public abstract Box getBoundingBox();
    @Shadow public abstract void move(MovementType type, Vec3d movement);
    @Shadow public float yaw;
    @Shadow public float pitch;
    @Shadow public float prevYaw;
    @Shadow public float prevPitch;
    @Shadow private Entity vehicle;
    @Shadow public abstract float getEyeHeight(EntityPose pose);
    @Shadow public float fallDistance;

    //Could be replaced, and possibly improved, by cardinal components

    private static final TrackedData<EntityGravity> GRAVITY;

    @Environment(EnvType.CLIENT)
    private float transitionAngle = 0;
    @Environment(EnvType.CLIENT)
    public float getTransitionAngle() { return transitionAngle; }
    @Environment(EnvType.CLIENT)
    public void setTransitionAngle(float transitionAngle) { this.transitionAngle = transitionAngle; }

    private Vec3d oldEyePos;
    private Vec3d eyePosChangeVector = Vec3d.ZERO;

    /**
     * Start tracking GRAVITY TrackedData here
     * @param type The type of entity
     * @param world The current world
     * @param ci Mixin CallbackInfo
     */
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;initDataTracker()V", shift = At.Shift.BEFORE))
    public void startTracking(EntityType<?> type, World world, CallbackInfo ci) {
        //TODO: Replace EntityGravity.DOWN with default gravity direction
        EntityGravity gravity = config.defaultGravity;
        gravity.setLength(-1);
        gravity.setPrevious(gravity.ordinal());
        this.dataTracker.startTracking(GRAVITY, gravity);
    }

    /**
     * The conditions in this if statement to make gravity
     * act the way I want confuse me to no end...
     * @author Magical("banana"+"π");
     */
    public void setGravity(EntityGravity gravity, int length) {
        //Here is some code that might be useful for gravity outside
        // of the context of commands: &&!(this.getGravity().isPermanent()&&!gravity.isPermanent())
        gravity.setPrevious(this.getGravity().ordinal());
        gravity.setLength(length);
        if(this.getGravity()!=gravity||((this.getGravity().getLength()!=gravity.getLength()||this.getGravity().getTransition()!=gravity.getTransition()))) {
            this.oldEyePos = this.getPos().add(0, this.getEyeHeight(this.getPose()), 0);
            this.dataTracker.set(GRAVITY, gravity);
            postGravityChange(gravity);
        }
    }

    public EntityGravity getGravity() { return this.dataTracker.get(GRAVITY); }

    private void postGravityChange(EntityGravity gravity) {
        this.transitionAngle = 0;

        if (gravity.getPrevious().getOpposite() == gravity)
            this.fallDistance *= 0.0f; //config.oppositeFallDistanceMultiplier
        else this.fallDistance *= 0.5f; //config.otherFallDistanceMultiplier

        if (this.world.isClient) {
            Vec3d newEyePos = this.getPos().add(0, this.getEyeHeight(this.getPose()), 0);
            Vec3d eyesDiff = newEyePos.subtract(this.oldEyePos);
            setEyePosChangeVector(eyesDiff);
        }
    }

    public Vec3d getEyePosChangeVector() { return this.eyePosChangeVector; }
    public void setEyePosChangeVector(Vec3d eyePosChangeVector) { this.eyePosChangeVector = eyePosChangeVector; }

    /**
     * Recalculate Dimensions if GRAVITY trackedData is set
     * It is possible there is a better alternative to redirect for this mixin
     * @param data TrackedData of an unknown type
     * @param o The type that TrackedData will be set to
     * @return POSE or GRAVITY = data
     */
    @Redirect(method = "onTrackedDataSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/TrackedData;equals(Ljava/lang/Object;)Z"))
    public boolean calculateDimensionsIf(TrackedData<?> data, Object o) {
        return POSE.equals(data) || GRAVITY.equals(data);
    }


    static {
        GRAVITY = DataTracker.registerData(Entity.class, GravityData.ENTITY_GRAVITY);
    }

    /**
     * Makes it such that gravity length decreases each tick
     * @param ci Mixin CallbackInfo
     */
    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tickNetherPortal()V"))
    private void baseTick(CallbackInfo ci) {
        if(this.getGravity().getTransition() > 0) this.getGravity().tickTransition();
        if(this.getGravity().getLength() > 0) this.getGravity().tickLength();
        else if(this.getGravity().getLength() == 0) this.setGravity(config.defaultGravity, -1);
    }

    /**
     * Sets entity gravity to that of it's vehicle when first riding another entity.
     * @param vehicle The provided entity that this entity is attempting to ride
     * @param force True if this should be forced to ride provided entity
     * @param cir Mixin CallbackInfoReturnable
     */
    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPose(Lnet/minecraft/entity/EntityPose;)V"))
    public void setRidingGravity(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        this.setGravity(((EntityAccessor)vehicle).getGravity(), ((EntityAccessor)vehicle).getGravity().getLength());
    }

    //TODO: Add collision checks
    /*protected boolean wouldPoseNotCollide(EntityPose pose) {
        return this.world.isSpaceEmpty(this, this.calculateBoundsForPose(pose).contract(1.0E-7D));
    }*/

    //TODO: AreaHelper methods seem to be important for some reason
    // and likely need to be changed for teleporting to work well

    //NOTICE: EntityDimensions should remain unchanged

    /**
     * Changes how hitboxes are recalculated so that the changes work when gravity changes as well.
     * @author Magicalbananapie
     */
    @Overwrite //TODO: Replace @Overwrite with something better
    public void calculateDimensions() {
        EntityDimensions entityDimensions = this.dimensions;
        EntityPose entityPose = this.getPose();
        EntityDimensions entityDimensions2 = this.getDimensions(entityPose);
        this.dimensions = entityDimensions2;
        this.standingEyeHeight = this.getEyeHeight(entityPose, entityDimensions2);
        if (entityDimensions2.width < entityDimensions.width) {
            //NOTICE: If new bounding box is smaller(in width), recreate the bounding box with respect to position
            // (I have no idea what this portion is here for)
            double d = (double)entityDimensions2.width / 2.0D;
            this.setBoundingBox(new Box(this.getX() - d, this.getY(), this.getZ() - d, this.getX() + d, this.getY() + (double)entityDimensions2.height, this.getZ() + d));
        } else {
            Box box = this.getBoundingBox();
            this.setBoundingBox(new Box(box.minX, box.minY, box.minZ, box.minX + (double)entityDimensions2.width, box.minY + (double)entityDimensions2.height, box.minZ + (double)entityDimensions2.width));
            if (entityDimensions2.width > entityDimensions.width && !this.firstUpdate && !this.world.isClient) {
                //NOTICE: If new bounding box is bigger(in width), center the player on the server side so
                //  that the player appears not to move despite the bounding box size increasing(in width)
                float f = entityDimensions.width - entityDimensions2.width;
                this.move(MovementType.SELF, new Vec3d(f, 0.0D, f));
            }

        }
    }

    /**
     * Changes getEyeHeight to account for gravity
     *
     * @author Magicalbananapie
     * @param pose EntityPose
     * @param dimensions EntityDimensions
     */
    @Inject(method = "getEyeHeight(Lnet/minecraft/entity/EntityPose;Lnet/minecraft/entity/EntityDimensions;)F", at = @At(value = "HEAD"), cancellable = true)
    public void getEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
        switch(getGravity()) {
            case UP: cir.setReturnValue(-dimensions.height * 0.85F); return;
            case DOWN: cir.setReturnValue(dimensions.height * 0.85F); return;
            default: cir.setReturnValue(0F);
        }
    }

    //Would be used to change entity scale, not used here because EntityDimensions makes x and z axis the same
    /*@Inject(at = @At("RETURN"), method = "getDimensions", cancellable = true)
    private void onGetDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> info) { }*/

    //This likely doesn't need to be changed, but whatever calls it needs to account for gravity as well
    /*protected Box calculateBoundsForPose(EntityPose pos) {
        EntityDimensions entityDimensions = this.getDimensions(pos);
        float f = entityDimensions.width / 2.0F;
        Vec3d vec3d = new Vec3d(this.getX() - (double)f, this.getY(), this.getZ() - (double)f);
        Vec3d vec3d2 = new Vec3d(this.getX() + (double)f, this.getY() + (double)entityDimensions.height, this.getZ() + (double)f);
        return new Box(vec3d, vec3d2);
    }*/

    //TODO: Eye height needs to be rotated elsewhere,
    // in whatever places call these methods and more
    /*protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.85F;
    }

    @Environment(EnvType.CLIENT)
    public float getEyeHeight(EntityPose pose) {
        return this.getEyeHeight(pose, this.getDimensions(pose));
    }*/

    /**
     * @author Magicalbananapie
     * I'm theoretically using this to sync mouse and camera
     * Seems to half-break inverted mouse setting :(
     * Changing this to a mixin will aid compat,
     * however rewriting the mod should happen first
     */
    @Environment(EnvType.CLIENT)
    @Overwrite public void changeLookDirection(double yaw, double pitch) {
        final double[] relativePitchYaw = Vec3dHelper.getPrecisePitchAndYawFromVector(
                this.getGravity().getOpposite().adjustVector(Vec3dHelper.getPreciseVectorForRotation(this.pitch, this.yaw)));

        final double changedRelativeYaw = relativePitchYaw[YAW] + (yaw * 0.15d);
        final double changedRelativePitch = relativePitchYaw[PITCH] + (pitch * 0.15d); //ended up removing negative on pitch

        final double maxRelativeYaw = 89.99d;

        double clampedRelativePitch;
        if (changedRelativePitch > maxRelativeYaw) clampedRelativePitch = maxRelativeYaw;
        else clampedRelativePitch = Math.max(changedRelativePitch, -maxRelativeYaw);

        // Directly set pitch and yaw
        final double[] absolutePitchYaw = Vec3dHelper.getPrecisePitchAndYawFromVector(
                this.getGravity().adjustVector(Vec3dHelper.getPreciseVectorForRotation(clampedRelativePitch, changedRelativeYaw)));

        final double changedAbsolutePitch = absolutePitchYaw[PITCH];
        final double changedAbsoluteYaw = (absolutePitchYaw[YAW] % 360);

        // Yaw calculated through yaw change
        final float absoluteYawChange;
        final double effectiveStartingAbsoluteYaw = this.yaw % 360;

        // Limit the change in yaw to 180 degrees each tick
        if (Math.abs(effectiveStartingAbsoluteYaw - changedAbsoluteYaw) > 180) { //(maxRelativeYaw * 2) instead of 180?
            if (effectiveStartingAbsoluteYaw < changedAbsoluteYaw)
                absoluteYawChange = (float)(changedAbsoluteYaw - (effectiveStartingAbsoluteYaw + 360));
            else absoluteYawChange = (float)((changedAbsoluteYaw + 360) - effectiveStartingAbsoluteYaw);
        } else absoluteYawChange = (float)(changedAbsoluteYaw - effectiveStartingAbsoluteYaw);
        float pitchParam = (float)(this.pitch - changedAbsolutePitch);

        this.pitch -= pitchParam; //... also added negative here for consistency
        this.yaw += absoluteYawChange;
        this.pitch = MathHelper.clamp(this.pitch, -90.0F, 90.0F);
        this.prevPitch -= pitchParam; //TODO: Figure out if I should be subtracting here (Or rather if this is causing any issues)
        this.prevYaw += absoluteYawChange;
        this.prevPitch = MathHelper.clamp(this.prevPitch, -90.0F, 90.0F);
        if (this.vehicle != null) this.vehicle.onPassengerLookAround((Entity)(Object)this);
    }

    @Inject(method = "toTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getCustomName()Lnet/minecraft/text/Text;", shift = At.Shift.BEFORE))
    public void toTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        tag.putInt("Gravity", getGravity().ordinal());
        tag.putInt("Length", getGravity().getLength());
    }

    @Inject(method = "fromTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setNoGravity(Z)V"))
    public void fromTag(CompoundTag tag, CallbackInfo ci) {
        this.setGravity(EntityGravity.get(tag.getInt("Gravity")), tag.getInt("Length"));
    }
}