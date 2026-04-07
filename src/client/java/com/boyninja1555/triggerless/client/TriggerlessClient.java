package com.boyninja1555.triggerless.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class TriggerlessClient implements ClientModInitializer {
    private final Set<String> triggerObjectives = new HashSet<>();
    private int lastLevelId = -1;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(_ -> checkLevelChange());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> new TriggerlessCommands(this).register(dispatcher));
    }

    private void checkLevelChange() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
        int currentLevelId = client.level.hashCode();
        if (currentLevelId != lastLevelId) {
            lastLevelId = currentLevelId;
            loadTriggers();
        }
    }

    public void loadTriggers() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        var commandsDispatcher = ClientCommands.getActiveDispatcher();
        if (commandsDispatcher == null) return;
        var commandsRoot = commandsDispatcher.getRoot();
        triggerObjectives.forEach(name -> commandsRoot.getChildren().removeIf(node -> node.getName().equals(name)));
        triggerObjectives.clear();

        var commandDispatcher = client.player.connection.getCommands();
        var suggestionsProvider = client.player.connection.getSuggestionsProvider();
        CommandContext<ClientSuggestionProvider> context = new CommandContext<>(suggestionsProvider, "trigger ", null, null, commandDispatcher.getRoot(), null, null, null, null, false);
        CompletableFuture<Suggestions> future = suggestionsProvider.customSuggestion(context);
        future.thenAccept(suggestions -> client.execute(() -> {
            suggestions.getList().forEach(s -> {
                String name = s.getText();
                triggerObjectives.add(name);
                commandsDispatcher.register(literal(name)
                        .executes(_ -> {
                            client.player.connection.sendCommand("trigger " + name);
                            return Command.SINGLE_SUCCESS;
                        }).then(ClientCommands.argument("args", StringArgumentType.greedyString()).executes(ctx -> {
                            String args = StringArgumentType.getString(ctx, "args");
                            client.player.connection.sendCommand("trigger " + name + " " + args);
                            return Command.SINGLE_SUCCESS;
                        }))
                );
            });
        }));
    }
}
