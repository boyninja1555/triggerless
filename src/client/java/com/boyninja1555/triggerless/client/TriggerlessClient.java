package com.boyninja1555.triggerless.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class TriggerlessClient implements ClientModInitializer {
    private int lastLevelId = -1;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(_ -> onLevelChange());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> TriggerlessCommands.register(dispatcher));
    }

    private void onLevelChange() {
        var client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
        int currentLevelId = client.level.hashCode();
        if (currentLevelId != lastLevelId) {
            lastLevelId = currentLevelId;
            TriggersManager.load();
        }
    }
}
