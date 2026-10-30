package com.boyninja1555.triggerless.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.HashSet;
import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class TriggersManager {
    private static final Set<String> objectives = new HashSet<>();

    public static void load() {
        var client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        var commandsDispatcher = ClientCommands.getActiveDispatcher();
        if (commandsDispatcher == null) return;
        var commandsRoot = commandsDispatcher.getRoot();
        objectives.forEach(name -> commandsRoot.getChildren().removeIf(node -> node.getName().equals(name)));
        objectives.clear();

        var commandDispatcher = client.player.connection.getCommands();
        var suggestionsProvider = client.player.connection.getSuggestionsProvider();
        CommandContext<ClientSuggestionProvider> context = new CommandContext<>(suggestionsProvider, "trigger ", null, null, commandDispatcher.getRoot(), null, null, null, null, false);
        suggestionsProvider.customSuggestion(context).thenAccept(suggestions -> client.execute(() -> suggestions.getList().forEach(s -> {
            String name = s.getText();
            objectives.add(name);
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
        })));
    }
}
