package io.github.magicalbananapie.gravitylib.mixin;

import io.github.magicalbananapie.gravitylib.EntityGravity;
import io.github.magicalbananapie.gravitylib.util.EntityAccessor;
import io.github.magicalbananapie.gravitylib.util.GravityData;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Magicalbananapie
 * @see Entity
 * Mixin changes to entity nessesary for gravity to work
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements EntityAccessor {
    @Shadow @Final protected DataTracker dataTracker;
    @Shadow @Final protected static TrackedData<EntityPose> POSE;

    //Could be replaced, and possibly improved, by cardinal components
    private static final TrackedData<EntityGravity> GRAVITY;

    /**
     * Start tracking GRAVITY TrackedData here
     * @param type The type of entity
     * @param world The current world
     * @param ci Mixin CallbackInfo
     */
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;initDataTracker()V", shift = At.Shift.BEFORE))
    public void startTracking(EntityType<?> type, World world, CallbackInfo ci) {
        //TODO: Replace EntityGravity.DOWN with default gravity direction
        this.dataTracker.startTracking(GRAVITY, EntityGravity.DOWN);
    }

    public void setGravity(EntityGravity gravity) {
        this.dataTracker.set(GRAVITY, gravity);
    }

    public EntityGravity getGravity() {
        return this.dataTracker.get(GRAVITY);
    }

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
     * Sets entity gravity to that of it's vehicle when first riding another entity.
     * @param vehicle The provided entity that this entity is attempting to ride
     * @param force True if this should be forced to ride provided entity
     * @param cir Mixin CallbackInfoReturnable
     */
    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPose(Lnet/minecraft/entity/EntityPose;)V"))
    public void setRidingGravity(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        this.setGravity(((EntityAccessor)vehicle).getGravity());
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
            case UP: cir.setReturnValue(-dimensions.height * 0.85F);
            case DOWN: cir.setReturnValue(dimensions.height * 0.85F);
            default: cir.setReturnValue(0F);
        }
    }

    //Would be used to change entity scale, not used here because EntityDimensions makes x and z axis the same
    /*@Inject(at = @At("RETURN"), method = "getDimensions", cancellable = true)
    private void onGetDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> info) { }*/

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


}