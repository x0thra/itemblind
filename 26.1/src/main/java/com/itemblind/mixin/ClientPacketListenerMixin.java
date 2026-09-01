package com.itemblind.mixin;

import com.itemblind.filter.ItemFilterManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleTakeItemEntity", at = @At("TAIL"))
    private void itemblind$onItemPickup(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && packet.getPlayerId() == client.player.getId()) {
            client.execute(() -> ItemFilterManager.checkAndFilterInventory(client));
        }
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    private void itemblind$onSlotUpdate(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.execute(() -> ItemFilterManager.checkAndFilterInventory(client));
        }
    }
}
