package com.itemblind.filter;

import com.itemblind.config.ItemBlindConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ItemFilterManager {
    private static boolean wasCreative = false;

    private ItemFilterManager() {
    }

    public static void checkAndFilterInventory(Minecraft client) {
        if (client.player == null || client.gameMode == null) {
            wasCreative = false;
            return;
        }

        LocalPlayer player = client.player;
        boolean isCreativeOrSpectator = player.isCreative() || player.isSpectator();

        if (isCreativeOrSpectator) {
            if (!wasCreative && ItemBlindConfig.get().isEnabled()) {
                wasCreative = true;
                player.sendSystemMessage(
                        Component.literal("§8[§bItemBlind§8] ")
                                .append(Component.translatable("itemblind.notification.creative_disabled").withStyle(ChatFormatting.GOLD))
                );
            }
            return;
        } else {
            if (wasCreative) {
                wasCreative = false;
                if (ItemBlindConfig.get().isEnabled()) {
                    player.sendSystemMessage(
                            Component.literal("§8[§bItemBlind§8] ")
                                    .append(Component.translatable("itemblind.notification.creative_re_enabled").withStyle(ChatFormatting.GREEN))
                    );
                }
            }
        }

        ItemBlindConfig config = ItemBlindConfig.get();
        if (!config.isEnabled()) {
            return;
        }

        boolean anyDropped = false;
        ItemStack lastDroppedStack = ItemStack.EMPTY;

        for (Slot slot : player.containerMenu.slots) {
            if (slot.container == player.getInventory() && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                if (config.isItemFiltered(stack.getItem())) {
                    lastDroppedStack = stack.copy();
                    client.gameMode.handleContainerInput(
                            player.containerMenu.containerId,
                            slot.index,
                            1,
                            ContainerInput.THROW,
                            player
                    );
                    anyDropped = true;
                }
            }
        }

        ItemStack carried = player.containerMenu.getCarried();
        if (!carried.isEmpty() && config.isItemFiltered(carried.getItem())) {
            lastDroppedStack = carried.copy();
            client.gameMode.handleContainerInput(
                    player.containerMenu.containerId,
                    -999,
                    0,
                    ContainerInput.PICKUP,
                    player
            );
            anyDropped = true;
        }

        if (anyDropped && !lastDroppedStack.isEmpty()) {
            triggerFeedback(client, player, lastDroppedStack, config);
        }
    }

    private static void triggerFeedback(Minecraft client, LocalPlayer player, ItemStack stack, ItemBlindConfig config) {
        if (config.isNotifyOnDrop()) {
            player.sendOverlayMessage(Component.translatable("itemblind.notification.item_dropped", stack.getHoverName()));
        }

        if (config.isSoundFeedback()) {
            client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 0.5F));
        }
    }
}
