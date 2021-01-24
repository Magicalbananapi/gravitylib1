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
    public int tickLength = 15;

    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean transition = true;

    @ConfigEntry.Gui.Tooltip(count = 3)
    public EntityGravity defaultGravity = EntityGravity.DOWN;
}
