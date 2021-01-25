package io.github.magicalbananapie.gravitylib;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import static io.github.magicalbananapie.gravitylib.GravityLib.config;

/**
 * @author Magicalbananapie
 * @see net.minecraft.entity.EntityPose
 * Enum that stores gravity directions
 */
public enum EntityGravity {
    DOWN("down", new Vec3i(0, 0, 0))  {
        @Override public EntityGravity getOpposite() { return DOWN; }
        @Override public double[] adjustXYZ(double x, double y, double z) {
            return new double[]{x, y, z};
        }
    }, UP("up", new Vec3i(0, 0, 180)) {
        @Override public EntityGravity getOpposite() { return UP; }
        @Override public double[] adjustXYZ(double x, double y, double z) { return new double[]{-x, -y, z}; }
    }, NORTH("north", new Vec3i(90, 0, 0)) {
        @Override public EntityGravity getOpposite() { return SOUTH; }
        @Override public double[] adjustXYZ(double x, double y, double z) { return new double[]{x, -z, y}; }
    }, SOUTH("south", new Vec3i(-90, 0, 0)) {
        @Override public EntityGravity getOpposite() { return NORTH; }
        @Override public double[] adjustXYZ(double x, double y, double z) { return new double[]{x, z, -y}; }
    }, EAST("east",  new Vec3i(0, 0, 90)) {
        @Override public EntityGravity getOpposite() { return WEST; }
        @Override public double[] adjustXYZ(double x, double y, double z) { return new double[]{-y, x, z}; }
    }, WEST("west",  new Vec3i(0, 0, -90)) {
        @Override public EntityGravity getOpposite() { return EAST; }
        @Override public double[] adjustXYZ(double x, double y, double z) { return new double[]{y, -x, z}; }
    };

    EntityGravity(String name, Vec3i cameraTransformVars) {
        this.name = name;
        this.cameraTransformVars = cameraTransformVars;
    }

    public abstract EntityGravity getOpposite();
    public abstract double[] adjustXYZ(double x, double y, double z);

    private final String name;
    private final Vec3i cameraTransformVars;
    private int length, transitionLength; //length is used for preventing gravity changes & transitionLength is used for transitions
    private boolean permanent = false;

    public String getName() { return this.name; }
    public Text getTranslatableName() { return new TranslatableText("gravity." + this.name); }

    public Vec3i getCameraTransformVars() { return cameraTransformVars; }

    public int getTransitionLength() { return this.transitionLength; }
    public void setTransitionLength( int transitionLength ) { this.transitionLength = transitionLength; }
    public int getLength() { return this.length; }
    public void setLength(int length) { this.length = length; }
    public void tickLength() { --this.length; }

    public boolean isPermanent() { return permanent; }
    public void setPermanent() { this.permanent = true; }

    public Vec3d adjustVector(Vec3d input) {
        double[] d = this.adjustXYZ(input.x, input.y, input.z);
        return new Vec3d(d[0], d[1], d[2]);
    }

    public static EntityGravity get(int ordinal) {
        if     (ordinal==1) return EntityGravity.UP;
        else if(ordinal==2) return EntityGravity.NORTH;
        else if(ordinal==3) return EntityGravity.SOUTH;
        else if(ordinal==4) return EntityGravity.EAST;
        else if(ordinal==5) return EntityGravity.WEST;
        else  /*ordinal==0*/return EntityGravity.DOWN; //Defaults to Down
    }
}