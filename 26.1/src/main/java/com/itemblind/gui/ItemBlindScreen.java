package com.itemblind.gui;

import com.itemblind.config.ItemBlindConfig;
import com.itemblind.gui.widget.FilterItemListWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemBlindScreen extends Screen {
    private final Screen parent;
    private final ItemBlindConfig config;

    private FilterItemListWidget listWidget;
    private EditBox searchField;
    private Button toggleFilterButton;
    private Button toggleNotifyButton;
    private Button toggleSoundButton;
    private Component statusMessage = Component.empty();
    private int statusMessageTimer = 0;

    private final List<SuggestionItem> currentSuggestions = new ArrayList<>();
    private static final int MAX_SUGGESTIONS = 6;
    private static final int SUGGESTION_ROW_HEIGHT = 22;

    public record SuggestionItem(Identifier identifier, ItemStack itemStack, String displayName) {
    }

    public ItemBlindScreen(Screen parent) {
        super(Component.translatable("itemblind.title"));
        this.parent = parent;
        this.config = ItemBlindConfig.get();
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(width - 32, 400);
        int left = (width - contentWidth) / 2;

        // 1. Üst Ayar Butonları Satırı
        int btnWidth = (contentWidth - 8) / 3;

        toggleFilterButton = Button.builder(getFilterToggleText(), btn -> {
            config.setEnabled(!config.isEnabled());
            btn.setMessage(getFilterToggleText());
        }).bounds(left, 28, btnWidth, 20).build();
        addRenderableWidget(toggleFilterButton);

        toggleNotifyButton = Button.builder(getNotifyToggleText(), btn -> {
            config.setNotifyOnDrop(!config.isNotifyOnDrop());
            btn.setMessage(getNotifyToggleText());
        }).bounds(left + btnWidth + 4, 28, btnWidth, 20).build();
        addRenderableWidget(toggleNotifyButton);

        toggleSoundButton = Button.builder(getSoundToggleText(), btn -> {
            config.setSoundFeedback(!config.isSoundFeedback());
            btn.setMessage(getSoundToggleText());
        }).bounds(left + (btnWidth + 4) * 2, 28, btnWidth, 20).build();
        addRenderableWidget(toggleSoundButton);

        // 2. Arama ve Ekleme Satırı
        int searchWidth = contentWidth - 165;
        searchField = new EditBox(font, left, 54, searchWidth, 20, Component.translatable("itemblind.search.placeholder"));
        searchField.setHint(Component.translatable("itemblind.search.placeholder").withStyle(ChatFormatting.DARK_GRAY));
        searchField.setResponder(this::onSearchQueryChanged);
        addRenderableWidget(searchField);

        Button addButton = Button.builder(Component.translatable("itemblind.button.add"), btn -> {
            addItemFromInput();
        }).bounds(left + searchWidth + 4, 54, 55, 20).build();
        addRenderableWidget(addButton);

        Button addHeldButton = Button.builder(Component.translatable("itemblind.button.add_hand"), btn -> {
            addHeldItem();
        }).bounds(left + searchWidth + 63, 54, 102, 20).build();
        addRenderableWidget(addHeldButton);

        // 3. Filtre Listesi
        int listTop = 80;
        int listBottom = height - 36;
        listWidget = new FilterItemListWidget(this, Minecraft.getInstance(), width, listBottom - listTop, listTop, 26);
        addRenderableWidget(listWidget);

        // 4. Alt Butonlar
        Button clearButton = Button.builder(Component.translatable("itemblind.button.clear").withStyle(ChatFormatting.RED), btn -> {
            config.clear();
            listWidget.refreshEntries();
            showStatus(Component.translatable("itemblind.notification.cleared").withStyle(ChatFormatting.YELLOW));
        }).bounds(left, height - 28, 100, 20).build();
        addRenderableWidget(clearButton);

        Button doneButton = Button.builder(CommonComponents.GUI_DONE, btn -> {
            onClose();
        }).bounds(left + contentWidth - 100, height - 28, 100, 20).build();
        addRenderableWidget(doneButton);
    }

    private Component getFilterToggleText() {
        return config.isEnabled()
                ? Component.translatable("itemblind.filter.enabled").withStyle(ChatFormatting.GREEN)
                : Component.translatable("itemblind.filter.disabled").withStyle(ChatFormatting.RED);
    }

    private Component getNotifyToggleText() {
        return config.isNotifyOnDrop()
                ? Component.translatable("itemblind.config.notify_on").withStyle(ChatFormatting.AQUA)
                : Component.translatable("itemblind.config.notify_off").withStyle(ChatFormatting.GRAY);
    }

    private Component getSoundToggleText() {
        return config.isSoundFeedback()
                ? Component.translatable("itemblind.config.sound_on").withStyle(ChatFormatting.LIGHT_PURPLE)
                : Component.translatable("itemblind.config.sound_off").withStyle(ChatFormatting.GRAY);
    }

    private void onSearchQueryChanged(String query) {
        currentSuggestions.clear();
        String trimmed = query.trim().toLowerCase(Locale.ROOT);

        if (trimmed.length() >= 1) {
            int count = 0;
            for (Identifier id : BuiltInRegistries.ITEM.keySet()) {
                Item item = BuiltInRegistries.ITEM.getValue(id);
                if (item == null) continue;

                ItemStack stack = new ItemStack(item);
                String displayName = stack.getHoverName().getString();
                String lowerDisplay = displayName.toLowerCase(Locale.ROOT);
                String idStr = id.toString().toLowerCase(Locale.ROOT);
                String pathStr = id.getPath().toLowerCase(Locale.ROOT);

                if (lowerDisplay.contains(trimmed) || idStr.contains(trimmed) || pathStr.contains(trimmed)) {
                    currentSuggestions.add(new SuggestionItem(id, stack, displayName));
                    count++;
                    if (count >= MAX_SUGGESTIONS) {
                        break;
                    }
                }
            }
        }
    }

    private void addItemFromInput() {
        String input = searchField.getValue().trim();
        if (input.isEmpty()) return;

        Identifier id = Identifier.tryParse(input.contains(":") ? input : "minecraft:" + input.toLowerCase(Locale.ROOT));
        if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            ItemStack stack = new ItemStack(item);
            if (config.addItem(id)) {
                showStatus(Component.translatable("itemblind.notification.added", stack.getHoverName().getString()).withStyle(ChatFormatting.GREEN));
            } else {
                showStatus(Component.translatable("itemblind.notification.already_exists", stack.getHoverName().getString()).withStyle(ChatFormatting.YELLOW));
            }
            searchField.setValue("");
            currentSuggestions.clear();
            listWidget.refreshEntries();
        } else {
            showStatus(Component.translatable("itemblind.error.invalid_item", input).withStyle(ChatFormatting.RED));
        }
    }

    private void addHeldItem() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack held = player.getMainHandItem();
            if (!held.isEmpty()) {
                Identifier id = BuiltInRegistries.ITEM.getKey(held.getItem());
                if (id != null) {
                    if (config.addItem(id)) {
                        showStatus(Component.translatable("itemblind.notification.added", held.getHoverName().getString()).withStyle(ChatFormatting.GREEN));
                    } else {
                        showStatus(Component.translatable("itemblind.notification.already_exists", held.getHoverName().getString()).withStyle(ChatFormatting.YELLOW));
                    }
                    listWidget.refreshEntries();
                }
            } else {
                showStatus(Component.translatable("itemblind.notification.no_held_item").withStyle(ChatFormatting.RED));
            }
        }
    }

    public void showStatus(Component msg) {
        this.statusMessage = msg;
        this.statusMessageTimer = 80;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean isDouble) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (!currentSuggestions.isEmpty() && searchField.isFocused()) {
            int suggestionLeft = searchField.getX();
            int suggestionTop = searchField.getY() + searchField.getHeight() + 2;
            int suggestionWidth = searchField.getWidth();
            int suggestionHeight = currentSuggestions.size() * SUGGESTION_ROW_HEIGHT;

            if (mouseX >= suggestionLeft && mouseX <= suggestionLeft + suggestionWidth &&
                    mouseY >= suggestionTop && mouseY <= suggestionTop + suggestionHeight) {
                int index = (int) ((mouseY - suggestionTop) / SUGGESTION_ROW_HEIGHT);
                if (index >= 0 && index < currentSuggestions.size()) {
                    SuggestionItem item = currentSuggestions.get(index);
                    if (config.addItem(item.identifier())) {
                        showStatus(Component.translatable("itemblind.notification.added", item.displayName()).withStyle(ChatFormatting.GREEN));
                    } else {
                        showStatus(Component.translatable("itemblind.notification.already_exists", item.displayName()).withStyle(ChatFormatting.YELLOW));
                    }
                    searchField.setValue("");
                    currentSuggestions.clear();
                    listWidget.refreshEntries();
                    return true;
                }
            }
        }

        return super.mouseClicked(event, isDouble);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
        super.extractRenderState(graphics, mouseX, mouseY, tickDelta);

        graphics.text(font, title, (width - font.width(title)) / 2, 10, 0xFFFFFFFF, true);

        if (statusMessageTimer > 0) {
            statusMessageTimer--;
            graphics.text(font, statusMessage, (width - font.width(statusMessage)) / 2, height - 48, 0xFFFFFFFF, true);
        }

        if (config.getBlacklistedItems().isEmpty() && (searchField == null || searchField.getValue().isEmpty())) {
            Component emptyText = Component.translatable("itemblind.empty_list").withStyle(ChatFormatting.GRAY);
            graphics.text(font, emptyText, (width - font.width(emptyText)) / 2, height / 2, 0xFFAAAAAA, true);
        }

        // Öneri Açılır Listesini Render Et
        if (!currentSuggestions.isEmpty() && searchField.isFocused()) {
            int suggestionLeft = searchField.getX();
            int suggestionTop = searchField.getY() + searchField.getHeight() + 2;
            int suggestionWidth = searchField.getWidth();
            int totalHeight = currentSuggestions.size() * SUGGESTION_ROW_HEIGHT;

            // Arka plan ve kenarlık (ARGB)
            graphics.fill(suggestionLeft - 1, suggestionTop - 1, suggestionLeft + suggestionWidth + 1, suggestionTop + totalHeight + 1, 0xFF000000);
            graphics.fill(suggestionLeft, suggestionTop, suggestionLeft + suggestionWidth, suggestionTop + totalHeight, 0xF0181818);

            for (int i = 0; i < currentSuggestions.size(); i++) {
                SuggestionItem item = currentSuggestions.get(i);
                int rowY = suggestionTop + i * SUGGESTION_ROW_HEIGHT;
                boolean hovered = mouseX >= suggestionLeft && mouseX <= suggestionLeft + suggestionWidth &&
                        mouseY >= rowY && mouseY < rowY + SUGGESTION_ROW_HEIGHT;

                if (hovered) {
                    graphics.fill(suggestionLeft, rowY, suggestionLeft + suggestionWidth, rowY + SUGGESTION_ROW_HEIGHT, 0x80555555);
                }

                graphics.item(item.itemStack(), suggestionLeft + 2, rowY + 3);

                Component nameComp = Component.literal(item.displayName());
                graphics.text(font, nameComp, suggestionLeft + 22, rowY + 2, 0xFFFFFFFF, true);

                Component idComp = Component.literal(item.identifier().toString()).withStyle(ChatFormatting.DARK_GRAY);
                graphics.text(font, idComp, suggestionLeft + 22, rowY + 12, 0xFFAAAAAA, false);
            }
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
