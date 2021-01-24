package io.github.magicalbananapie.gravitylib.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * A collection of math helpers for theoretically faster trig
 * Partially from Mysteryem's Similar class providing the majority and partially from
 * https://stackoverflow.com/questions/523531/fast-transcendent-trigonometric-functions-for-java
 * which included the trig tables and faster arcTan methods
 * I'm going to be honest to whoever happens to be reading this, I have no idea why this is here or if
 * this even matters or not, and if I implemented it incorrectly might be actively decreasing performance...
 * If this thing had to rerun the asin which its intended to avoid repeatedly due to me somehow deciding to
 * recreate the helper every frame... we have a problem
 **/

public class Vec3dHelper {
    private static final double PI_OVER_180 = Math.PI / 180d;
    private static final double ONE_HUNDRED_EIGHTY_OVER_PI = 180d / Math.PI;
    private static final float ONE_HUNDRED_EIGHTY_OVER_PI_FLOAT = (float)(180d / Math.PI);
    public static final int PITCH = 0;
    public static final int YAW = 1;

    public static double getAbsolutePitchPrecise(Vec3d lookVec) {
        return -(Math.asin(lookVec.y) * (180D / Math.PI));
    }

    public static double getAbsoluteYawPrecise(Vec3d lookVec) {
        return (Math.atan2(-lookVec.x, lookVec.z) * (180D / Math.PI));
    }

    public static double invSqrt(double x) {
        double xhalf = 0.5 * x;
        long i = Double.doubleToRawLongBits(x);
        i = 0x5FE6EB50C7B537AAL - (i>>1);
        x = Double.longBitsToDouble(i);
        x = x * (1.5 - xhalf*x*x);
        return x;
    }

    private static final double ONE_SIXTH = 1.0 / 6.0;
    private static final int FRAC_EXP = 8; // LUT precision == 2 ** -8 == 1/256
    private static final int LUT_SIZE = (1 << FRAC_EXP) + 1;
    private static final double FRAC_BIAS =
            Double.longBitsToDouble((0x433L - FRAC_EXP) << 52);
    private static final double[] ASIN_TAB = new double[LUT_SIZE];
    private static final double[] COS_TAB = new double[LUT_SIZE];

    static {
        /* Populate trig tables */
        for (int ind = 0; ind < LUT_SIZE; ++ ind)
        {
            double v = ind / (double) (1 << FRAC_EXP);
            double asinv = Math.asin(v);
            COS_TAB[ind] = Math.cos(asinv);
            ASIN_TAB[ind] = asinv;
        }
    }


    public static double[] getFastPitchAndYawFromVector(Vec3d xyz) {
        double pitch = -fastASin(xyz.y) * ONE_HUNDRED_EIGHTY_OVER_PI;
        double yaw = MathHelper.atan2(-xyz.x, xyz.z) * ONE_HUNDRED_EIGHTY_OVER_PI;
        return new double[]{pitch, yaw};
    }

    private static double fastASin(double input) {
        boolean negateResult = false;
        if (input < 0.0d) {
            input = -input;
            negateResult = true;
        }
        if (Double.isNaN(input) || input > 1d) {
            return Double.NaN;
        }

        int i = (int)Double.doubleToRawLongBits(FRAC_BIAS + input);
        return negateResult ? -ASIN_TAB[i] : ASIN_TAB[i];
    }

    public static Vec3d getFastUpwardsVector(float pitch, float yaw) { return getFastVectorForRotation(pitch + 90f, yaw); }

    public static Vec3d getFastVectorForRotation(float pitch, float yaw) { return Vec3d.fromPolar(pitch, yaw); }

    public static float getPitch(Vec3d lookVec) {
        return (float)-(Math.asin(lookVec.y) * (180D / Math.PI));
    }

    public static double[] getPrecisePitchAndYawFromVector(Vec3d xyz) {
        double pitch = -(Math.asin(xyz.y) * ONE_HUNDRED_EIGHTY_OVER_PI);
        double yaw = Math.atan2(-xyz.x, xyz.z) * ONE_HUNDRED_EIGHTY_OVER_PI;
        return new double[]{pitch, yaw};
    }

    public static Vec3d getPreciseVectorForRotation(double pitch, double yaw) {
        double f = Math.cos(-yaw * PI_OVER_180 - Math.PI);
        double f1 = Math.sin(-yaw * PI_OVER_180 - Math.PI);
        double f2 = -Math.cos(-pitch * PI_OVER_180);
        double f3 = Math.sin(-pitch * PI_OVER_180);
        return new Vec3d((f1 * f2), f3, (f * f2));
    }

    public static float getYaw(Vec3d lookVec) {
        return (float)(Math.atan2(-lookVec.x, lookVec.z) * (180D / Math.PI));
    }
}
