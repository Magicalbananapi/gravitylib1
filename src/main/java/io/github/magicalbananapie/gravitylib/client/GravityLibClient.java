package io.github.magicalbananapie.gravitylib.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * As far as I know, I do not need this,
 * everything should be covered by mixins.
 * This would only be used if I switch to a
 * more modular approach to mixins which
 * requires events.
 */
@Environment(EnvType.CLIENT)
public class GravityLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

    }
}
