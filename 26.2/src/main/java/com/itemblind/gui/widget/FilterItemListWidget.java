package com.itemblind.gui.widget;

import com.itemblind.config.ItemBlindConfig;
import com.itemblind.gui.ItemBlindScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FilterItemListWidget extends ContainerObjectSelectionList<FilterItemListWidget.FilterEntry> {
    private final ItemBlindScreen parent;

    public FilterItemListWidget(ItemBlindScreen parent, Minecraft client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        this.parent = parent;
        refreshEntries();
    }

    public void refreshEntries() {
        clearEntries();
        ItemBlindConfig config = ItemBlindConfig.get();

        for (String idStr : config.getBlacklistedItems()) {
            Identifier id = Identifier.tryParse(idStr);
            if (id == null) continue;

            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item == null) continue;

            ItemStack stack = new ItemStack(item);
            addEntry(new FilterEntry(id, stack));
        }
    }

    public class FilterEntry extends ContainerObjectSelectionList.Entry<FilterEntry> {
        private final Identifier identifier;
        private final ItemStack itemStack;
        private final Button removeButton;
        private final List<GuiEventListener> children = new ArrayList<>();

        public FilterEntry(Identifier identifier, ItemStack itemStack) {
            this.identifier = identifier;
            this.itemStack = itemStack;

            this.removeButton = Button.builder(
                    Component.literal("✕").withStyle(ChatFormatting.RED),
                    btn -> {
                        ItemBlindConfig.get().removeItem(identifier);
                        refreshEntries();
                        parent.showStatus(Component.translatable("itemblind.notification.removed", itemStack.getHoverName().getString()).withStyle(ChatFormatting.RED));
                    }
            ).bounds(0, 0, 20, 20).build();

            this.children.add(this.removeButton);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = getX();
            int y = getY();
            int entryWidth = getWidth();
            int entryHeight = 26;
            int centerY = y + (entryHeight - 16) / 2;

            graphics.item(itemStack, x + 4, centerY);

            Component nameText = itemStack.getHoverName();
            graphics.text(minecraft.font, nameText, x + 26, y + 3, 0xFFFFFFFF, true);
            graphics.text(minecraft.font, Component.literal(identifier.toString()).withStyle(ChatFormatting.DARK_GRAY), x + 26, y + 13, 0xFFAAAAAA, false);

            removeButton.setX(x + entryWidth - 26);
            removeButton.setY(y + (entryHeight - 20) / 2);
            removeButton.extractRenderState(graphics, mouseX, mouseY, tickDelta);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return children.stream().filter(c -> c instanceof NarratableEntry).map(c -> (NarratableEntry) c).toList();
        }
    }
}
