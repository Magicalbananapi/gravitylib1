package io.github.magicalbananapie.gravitylib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.magicalbananapie.gravitylib.EntityGravity;
import io.github.magicalbananapie.gravitylib.util.EntityAccessor;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import static io.github.magicalbananapie.gravitylib.GravityLib.config;

//TODO: Clean up code a bit and merge some methods, remove redundancies, etc. However low priority as it works.
public class GravityCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literal = CommandManager.literal("gravity")
                .requires((serverCommandSource) -> serverCommandSource.hasPermissionLevel(2));

        /* TODO: Add command argument so you can search for things like @e[gravity=down],
         *  AND implement something like this /gravity command structure
         *  /gravity get <optional: (gravitylib.direction||gravitylib.strength), defaults to direction> <optional: (gravitylib.base||gravitylib.length), defaults to base> <optional: @target, defaults to @p>
         *  /gravity set <optional: (gravitylib.direction||gravitylib.strength), defaults to direction> (direction||value) <optional: @target, defaults to @p> <optional: length, defaults to -1 or permanent> [potential tags]
         */

        literal.then(CommandManager.literal("get")
                .executes((ctx) -> execute(ctx, Collections.singleton(ctx.getSource().getPlayer())))
                .then(CommandManager.argument("target", EntityArgumentType.entities())
                        .executes((ctx) -> execute(ctx, EntityArgumentType.getEntities(ctx, "target")))));

        //TODO: Make a command argument for gravity direction
        for (EntityGravity gravity : EntityGravity.values()) {
            literal.then(CommandManager.literal("set").then(CommandManager.literal(gravity.getName())
                    .executes((ctx) -> execute(ctx, Collections.singleton(ctx.getSource().getEntity()), gravity, true))
                    .then(CommandManager.argument("length", IntegerArgumentType.integer(0))
                            .executes((ctx) -> execute(ctx, Collections.singleton(ctx.getSource().getEntity()), gravity, IntegerArgumentType.getInteger(ctx, "length"))))
                    .then(CommandManager.argument("target", EntityArgumentType.entities())
                            .executes((ctx) -> execute(ctx, EntityArgumentType.getEntities(ctx, "target"), gravity, true))
                            .then(CommandManager.argument("permanent", BoolArgumentType.bool())
                                    .executes((ctx) -> execute(ctx, EntityArgumentType.getEntities(ctx, "target"), gravity, BoolArgumentType.getBool(ctx, "permanent"))))
                            .then(CommandManager.argument("length", IntegerArgumentType.integer(0))
                                    .executes((ctx) -> execute(ctx, EntityArgumentType.getEntities(ctx, "target"), gravity, IntegerArgumentType.getInteger(ctx, "length")))))));
        } dispatcher.register(literal);
    }

    /**
     * Prints the gravity direction of the target entities
     * @param context Context of the command
     * @param targets Target Entities of the command
     * @return Number of entities targeted by command
     */
    private static int execute(CommandContext<ServerCommandSource> context, Collection<? extends Entity> targets) {
        int i = 0;
        for (Entity entity : targets) {
            context.getSource().sendFeedback(new LiteralText("Direction: "+((EntityAccessor)entity).getGravity().getName().toUpperCase(Locale.ROOT)), false);
            ++i;
        } return i;
    }

    /**
     * Changes gravity direction of target entities to the provided gravity direction
     * @param context Context of the command
     * @param targets Target Entities of the command
     * @param gravity Direction of gravity set by command
     * @return Number of entities targeted by command
     */
    private static int execute(CommandContext<ServerCommandSource> context, Collection<? extends Entity> targets, EntityGravity gravity, boolean permanent) {
        int i = 0;
        for (Entity entity : targets) {
            ((EntityAccessor)entity).setGravity(gravity, permanent?-1:config.length);

            Text text = gravity.getTranslatableName();
            if(entity instanceof ServerPlayerEntity) {
                if (context.getSource().getEntity() == entity) {
                    context.getSource().sendFeedback(new TranslatableText("commands.gravity.direction.success.self", text), true);
                } else {
                    if (context.getSource().getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                        entity.sendSystemMessage(new TranslatableText("gravity.changed", text), Util.NIL_UUID);
                    } context.getSource().sendFeedback(new TranslatableText("commands.gravity.direction.success.other", entity.getDisplayName(), text), true);
                }
            } ++i;
        } return i;
    }

    /**
     * Changes gravity direction of target entities to the provided gravity direction for the provided tick length
     * @param context Context of the command
     * @param targets Target Entities of the command
     * @param gravity Direction of gravity set by command
     * @param length Length of gravity set by command
     * @return Number of entities targeted by command
     */
    private static int execute(CommandContext<ServerCommandSource> context, Collection<? extends Entity> targets, EntityGravity gravity, int length) {
        int i = 0;
        for (Entity entity : targets) {
            ((EntityAccessor)entity).setGravity(gravity, length);

            Text text = gravity.getTranslatableName();
            if(entity instanceof ServerPlayerEntity) {
                if (context.getSource().getEntity() == entity) {
                    context.getSource().sendFeedback(new TranslatableText("commands.gravity.direction.success.self", text), true);
                } else {
                    if (context.getSource().getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                        entity.sendSystemMessage(new TranslatableText("gravity.changed", text), Util.NIL_UUID);
                    } context.getSource().sendFeedback(new TranslatableText("commands.gravity.direction.success.other", entity.getDisplayName(), text), true);
                }
            } ++i;
        } return i;
    }
}
