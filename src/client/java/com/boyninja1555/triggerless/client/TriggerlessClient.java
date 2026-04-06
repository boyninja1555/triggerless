package com.boyninja1555.triggerless.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.boyninja1555.triggerless.Globals.FLOOR_COLOR;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class TriggerlessClient implements ClientModInitializer {
    private final Set<String> triggerObjectives = new HashSet<>();
    private boolean loaded = false;
    private int lastLevelId = -1;

    @Override
    public void onInitializeClient() {
        ClientSendMessageEvents.ALLOW_COMMAND.register(this::triggersHandle);
        ClientTickEvents.END_CLIENT_TICK.register(_ -> checkLevelChange());

        // Commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> {
            dispatcher.register(literal("reload-triggers").executes(this::reloadTriggersCommand));
        });
    }

    // Commands

    private int reloadTriggersCommand(CommandContext<FabricClientCommandSource> context) {
        loadTriggers();
        context.getSource().getPlayer().sendSystemMessage(Component.literal("Triggers reloaded! You should be able to access any new trigger commands in shorthand form.").withColor(FLOOR_COLOR));
        return Command.SINGLE_SUCCESS;
    }

    // Etc

    private void checkLevelChange() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
        int currentLevelId = client.level.hashCode();
        if (currentLevelId != lastLevelId) {
            lastLevelId = currentLevelId;
            loadTriggers();
        }
    }

    private void loadTriggers() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        triggerObjectives.clear();
        loaded = false;

        var commandDispatcher = client.player.connection.getCommands();
        var suggestionsProvider = client.player.connection.getSuggestionsProvider();
        CommandContext<ClientSuggestionProvider> context = new CommandContext<>(suggestionsProvider, "trigger ", null, null, commandDispatcher.getRoot(), null, null, null, null, false);
        CompletableFuture<Suggestions> future = suggestionsProvider.customSuggestion(context);
        future.thenAccept(suggestions -> {
            suggestions.getList().forEach(s -> {
                triggerObjectives.add(s.getText());
                System.out.println("Registered trigger -> " + s.getText());
            });
            loaded = true;
        });
    }

    private boolean triggersHandle(String commandLine) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return true;
        if (!loaded) return true;

        String[] parts = commandLine.split(" ", 2);
        String rootCommand = parts[0];
        if (rootCommand.equalsIgnoreCase("trigger")) return true;
        if (triggerObjectives.contains(rootCommand)) {
            client.player.connection.sendCommand("trigger " + commandLine);
            return false;
        }

        return true;
    }
}
