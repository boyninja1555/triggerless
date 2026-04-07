package com.boyninja1555.triggerless.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import static com.boyninja1555.triggerless.Globals.FLOOR_COLOR;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class TriggerlessCommands {
    private final TriggerlessClient mod;

    public TriggerlessCommands(TriggerlessClient mod) {
        this.mod = mod;
    }

    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("reload-triggers").executes(this::reloadTriggersC));
    }

    // Implementations

    private int reloadTriggersC(CommandContext<FabricClientCommandSource> context) {
        mod.loadTriggers();
        context.getSource().sendFeedback(Component.translatable("message.triggerless.triggers-reloaded").withColor(FLOOR_COLOR));
        return Command.SINGLE_SUCCESS;
    }
}
