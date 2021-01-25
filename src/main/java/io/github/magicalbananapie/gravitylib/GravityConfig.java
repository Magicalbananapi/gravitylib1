package io.github.magicalbananapie.gravitylib;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

/**
 * @author Magicalbananapie
 * - Config options for GravityLib
 */
@Config(name = GravityLib.MOD_ID)
public class GravityConfig implements ConfigData {
    /**
     * [15] by Default.
     * Determines tick length of gravity.
     * [-1] will make gravity permanent.
     */
    @ConfigEntry.Gui.Tooltip(count = 3)
    @ConfigEntry.BoundedDiscrete(min = -1, max = 100)
    public int length = 15;

    /**
     * [2/3] by Default.
     * Provided value must be two integers
     * or doubles separated by a slash,
     * such as "1/2" or "0.5/2.5",
     * otherwise it will default to [2/3].
     * [0] will disable transitions.
     */
    @ConfigEntry.Gui.Tooltip(count = 6)
    public String scale = "2/3";

    /**
     * [DOWN] by Default.
     * Determines the default direction
     * of gravity unless overridden.
     */
    @ConfigEntry.Gui.Tooltip(count = 3)
    public EntityGravity defaultGravity = EntityGravity.DOWN;
}
