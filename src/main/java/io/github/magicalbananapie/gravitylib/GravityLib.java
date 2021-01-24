package io.github.magicalbananapie.gravitylib;

import io.github.magicalbananapie.gravitylib.command.GravityCommand;
import io.github.magicalbananapie.gravitylib.util.GravityData;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author Magicalbananapie
 * - This mod stores and manages essential functionallity of gravity
 * - It is currently embeded inside ArcaneSpace but will be seperated later
 */
public class GravityLib implements ModInitializer {
    public static final String MOD_ID = "gravitylib";
    public static final String MOD_NAME = "GravityLib";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME, StringFormatterMessageFactory.INSTANCE);

    public static GravityConfig config;
    public static double scale;

    public static Identifier id(String name) {
        return new Identifier(MOD_ID, name);
    }

    public static void log(String message){
        LOGGER.log(Level.INFO, message);
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(GravityConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(GravityConfig.class).getConfig();
        TrackedDataHandlerRegistry.register(GravityData.ENTITY_GRAVITY);
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            GravityCommand.register(dispatcher);
        });
        String[] num = config.scale.split("/");
        scale = (double)(Integer.getInteger(num[0]))/(Integer.getInteger(num[1]));
    }
}
