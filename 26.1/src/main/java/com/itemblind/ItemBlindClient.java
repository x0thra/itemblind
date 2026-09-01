package com.itemblind;

import com.itemblind.config.ItemBlindConfig;
import com.itemblind.filter.ItemFilterManager;
import com.itemblind.gui.ItemBlindScreen;
import com.itemblind.keybind.ModKeybindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ItemBlindClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemBlindConfig.get();
        ModKeybindings.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            while (ModKeybindings.openScreenKey.consumeClick()) {
                Minecraft.getInstance().setScreenAndShow(new ItemBlindScreen(null));
            }

            while (ModKeybindings.toggleFilterKey.consumeClick()) {
                ItemBlindConfig config = ItemBlindConfig.get();
                boolean newState = !config.isEnabled();
                config.setEnabled(newState);

                Component msg = newState
                        ? Component.translatable("itemblind.notification.toggled_on")
                        : Component.translatable("itemblind.notification.toggled_off");
                client.player.sendOverlayMessage(msg);
            }

            ItemFilterManager.checkAndFilterInventory(client);
        });

        ItemBlind.LOGGER.info("ItemBlind client initialized successfully.");
    }
}
