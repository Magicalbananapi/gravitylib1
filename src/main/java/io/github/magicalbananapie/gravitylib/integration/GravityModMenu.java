package io.github.magicalbananapie.gravitylib.integration;

import io.github.magicalbananapie.gravitylib.GravityConfig;
import io.github.magicalbananapie.gravitylib.GravityLib;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;

/**
 * @author Magicalbananapie
 * - Allows the mod to independently show its own config screen.
 * - Currently unused as ArcaneSpace calls the config class.
 */
public class GravityModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // Return the screen here with the one you created from Cloth Config Builder
        return (screen) -> AutoConfig.getConfigScreen(GravityConfig.class, screen).get();
    }

    @Override
    public String getModId() {
        return GravityLib.MOD_ID;
    }
}