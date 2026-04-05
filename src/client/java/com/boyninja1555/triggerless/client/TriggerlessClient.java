package com.boyninja1555.triggerless.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Minecraft;

public class TriggerlessClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientSendMessageEvents.ALLOW_COMMAND.register(this::triggersHandle);
    }

    private boolean triggersHandle(String commandLine) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return true;

        String[] parts = commandLine.split(" ", 2);
        String rootCommand = parts[0];
        if (rootCommand.equalsIgnoreCase("trigger")) return true;
        var scoreboard = client.level.getScoreboard();
        var objective = scoreboard.getObjective(rootCommand);
        var dispatcher = client.player.connection.getCommands();
        var triggerNode = dispatcher.getRoot().getChild("trigger");
        boolean existsInScoreboard = objective != null;
        boolean existsInCommandTree = (triggerNode != null && triggerNode.getChild(rootCommand) != null);
        if (existsInScoreboard || existsInCommandTree) {
            client.player.connection.sendCommand("trigger " + commandLine);
            System.out.println("trigger " + commandLine);
            return false;
        }

        return true;
    }
}
