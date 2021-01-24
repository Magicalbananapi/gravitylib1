package io.github.magicalbananapie.gravitylib.mixin.client;

import io.github.magicalbananapie.gravitylib.EntityGravity;
import io.github.magicalbananapie.gravitylib.util.EntityAccessor;
import io.github.magicalbananapie.gravitylib.util.Vec3dHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.magicalbananapie.gravitylib.util.Vec3dHelper.PITCH;
import static io.github.magicalbananapie.gravitylib.util.Vec3dHelper.YAW;
import static io.github.magicalbananapie.gravitylib.GravityLib.config;

//FIXME: Camera rotations are both broken, and don't work with the down direction

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Camera camera;

    //TODO: Move this into an event (Probably after it is working), example in CameraOverhaul
    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V", shift = At.Shift.BEFORE))
    private void PostCameraUpdate(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity cameraEntity = client.getCameraEntity();
        //Works in spectator mode and with custom entity types now as long as they extend livingEntity,
        // however there are likely crashes in odd cases, and TODO: CAMERA ROTATIONS DON'T WORK IN SPECTATOR
        if (cameraEntity instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity)cameraEntity;
            EntityGravity gravity = ((EntityAccessor)entity).getGravity();

            double interpolatedPitch = camera.getPitch() % 360;
            double interpolatedYaw = (camera.getYaw() + 180.0f) % 360;

            double[] relativePitchYaw = Vec3dHelper.getPrecisePitchAndYawFromVector(
                    gravity.adjustVector(Vec3dHelper.getPreciseVectorForRotation(interpolatedPitch, interpolatedYaw)));

            double relativeInterpolatedPitch = relativePitchYaw[PITCH] % 360;
            double relativeInterpolatedYaw = relativePitchYaw[YAW] % 360;

            //NOTICE: This is ALL math for transitions until the actual rotations begin
            double xTranslation = 0;
            double yTranslation = 0;
            double zTranslation = 0;
            float transitionRollAmount = 0;
            if(config.transition) {
                float effectiveTimeoutTicks = gravity.getLength() - (1 * client.getTickDelta());

                if (gravity.getLength() > 0 && effectiveTimeoutTicks > ((float)config.tickLength)/3) {
                    double rotationAngle;

                    // We don't want to run all this code every render tick, so we store the angle to rotate by over the transition
                    if (((EntityAccessor)entity).getTransitionAngle()==0) {

                        // Get the absolute look vector
                        Vec3d absoluteLookVec = Vec3dHelper.getPreciseVectorForRotation(entity.pitch, entity.yaw);

                        // Get the relative look vector for the current gravity direction
                        Vec3d relativeCurrentLookVector = gravity.getOpposite().adjustVector(absoluteLookVec);
                        // Get the pitch and yaw from the relative look vector
                        double[] pitchAndYawRelativeCurrentLook = Vec3dHelper.getPrecisePitchAndYawFromVector(relativeCurrentLookVector);
                        // Pitch - 90, -90 changes it from the forwards direction to the upwards direction
                        double relativeCurrentPitch = pitchAndYawRelativeCurrentLook[Vec3dHelper.PITCH] - 90;
                        // Yaw
                        double relativeCurrentYaw = pitchAndYawRelativeCurrentLook[Vec3dHelper.YAW];
                        // Get the relative upwards vector
                        Vec3d relativeCurrentUpVector = Vec3dHelper.getPreciseVectorForRotation(
                                relativeCurrentPitch, relativeCurrentYaw);
                        // Get the absolute vector for the relative upwards vector
                        Vec3d absoluteCurrentUpVector = gravity.adjustVector(relativeCurrentUpVector);

                        // Get the relative look vector for the previous gravity direction
                        Vec3d relativePrevLookVector = ((EntityAccessor)entity).getPreviousGravity().getOpposite().adjustVector(absoluteLookVec);
                        // Get the pitch and yaw from the relative look vector
                        double[] pitchAndYawRelativePrevLook = Vec3dHelper.getPrecisePitchAndYawFromVector(relativePrevLookVector);
                        // Pitch - 90, -90 changes it from the forwards direction to the upwards direction
                        double relativePrevPitch = pitchAndYawRelativePrevLook[Vec3dHelper.PITCH] - 90;
                        // Yaw
                        double relativePrevYaw = pitchAndYawRelativePrevLook[Vec3dHelper.YAW];
                        // Get the relative upwards vector
                        Vec3d relativePrevUpVector = Vec3dHelper.getPreciseVectorForRotation(
                                relativePrevPitch, relativePrevYaw);
                        // Get the absolute vector for the relative upwards vector
                        Vec3d absolutePrevUpVector = ((EntityAccessor)entity).getPreviousGravity().adjustVector(relativePrevUpVector);

                        //See http://stackoverflow.com/a/33920320 for the maths
                        rotationAngle = (180d / Math.PI) * Math.atan2(
                                absoluteCurrentUpVector.crossProduct(absolutePrevUpVector).dotProduct(absoluteLookVec),
                                absoluteCurrentUpVector.dotProduct(absolutePrevUpVector));

                        ((EntityAccessor)entity).setTransitionAngle((float) rotationAngle);
                    } else rotationAngle = ((EntityAccessor)entity).getTransitionAngle();

                    double multiplier = 1 - ((((float)config.tickLength) - effectiveTimeoutTicks) / ((float)config.tickLength)*2/3); // multiplierOneToZero = 1 - multiplierZeroToOne // and multiplierZeroToOne = numerator / denominator

                    transitionRollAmount = (float) (rotationAngle * multiplier);
                    Vec3d eyePosChangeVector = ((EntityAccessor)entity).getEyePosChangeVector();
                    xTranslation = eyePosChangeVector.x * multiplier;
                    yTranslation = eyePosChangeVector.y * multiplier;
                    zTranslation = eyePosChangeVector.z * multiplier;
                    client.worldRenderer.scheduleTerrainUpdate();
                }
            }

            // 1: Undo the absolute pitch and yaw rotation of the player (in that order);
            // THIS IS VERY IMPORTANT AND EXACTLY WHAT IT SAYS IT IS, this is just inverting
            // the 2 matrix rotations just before this is called
            matrix.multiply(Vector3f.NEGATIVE_Y.getDegreesQuaternion((float)interpolatedYaw));
            matrix.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion((float)interpolatedPitch));

            // 2: Rotate by the relative player's yaw and pitch (in that order),
            // this, combined with the camera transformation sets the correct camera yaw and pitch
            matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion((float)relativeInterpolatedPitch));
            matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float)relativeInterpolatedYaw));

            // 3: Now that our look direction is effectively 0 yaw and 0 pitch, perform the rotation specific for this gravity
            Vec3i vars = gravity.getOpposite().getCameraTransformVars();
            int x = vars.getX(); if (x != 0) matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(x));
            int y = vars.getY(); if (y != 0) matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(y));
            int z = vars.getZ(); if (z != 0) matrix.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(z));

            //NOTICE: Changes I made to try to fix NESW being inverted (Just in case the hitbox rotation would have fixed them)
            // (Seems to be incorrect due to extreme angle change, but IDK how to fix it otherwise, I tried)
            // 1. called for inverse adjustment to direction on gravity specific rotation,
            // 2. removed inverse adjustment to direction on relativePitchYaw
            // This inverts the camera movement axis such that it works as intended, however it also messes up the look vector
            // after the transition, such that there is no way it is accurate, also it should be noted that because this is an
            // inverse adjustment, it does not address the concerns with reverse gravity's movement b
            // I SUSPECT that there will be no issues undoing these changes once the hitbox is rotated (especially the eyes).

            //FIXME: transitionRollAmount, alongside rotationAngle and transitionAngle,
            // are 0 at all times, figure out why and fix it as this should NOT happen.
            // However it seems as if these are only used for transitions, so for now: [Low Priority]
            //NOTICE: The cause of the above problem is the old getDefaultGravity() instead of getPrevious()
            //TODO: Make transitionEnabled able to be changed mid-game
            if(config.transition) {
                matrix.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(transitionRollAmount));

                matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
                matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw()));
                matrix.translate(xTranslation, yTranslation, zTranslation);
                matrix.multiply(Vector3f.NEGATIVE_Y.getDegreesQuaternion(camera.getYaw()));
                matrix.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion(camera.getPitch()));
            }//
        }
    }
}
