package com.itemblind.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itemblind.ItemBlind;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ItemBlindConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("itemblind.json");
    private static ItemBlindConfig INSTANCE;

    private boolean enabled = true;
    private boolean notifyOnDrop = false;
    private boolean soundFeedback = false;
    private Set<String> blacklistedItems = new LinkedHashSet<>();

    private final transient Set<Identifier> cachedIdentifiers = new HashSet<>();

    public static ItemBlindConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static ItemBlindConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                ItemBlindConfig config = GSON.fromJson(reader, ItemBlindConfig.class);
                if (config != null) {
                    config.rebuildCache();
                    return config;
                }
            } catch (Exception e) {
                ItemBlind.LOGGER.error("Failed to load ItemBlind config, backing up and resetting to default", e);
                try {
                    Files.copy(CONFIG_PATH, CONFIG_PATH.resolveSibling("itemblind.json.bak"), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ignored) {
                }
            }
        }
        ItemBlindConfig defaultConfig = new ItemBlindConfig();
        defaultConfig.save();
        return defaultConfig;
    }

    public synchronized void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            ItemBlind.LOGGER.error("Failed to save ItemBlind config", e);
        }
    }

    public void rebuildCache() {
        cachedIdentifiers.clear();
        if (blacklistedItems == null) {
            blacklistedItems = new LinkedHashSet<>();
            return;
        }
        for (String idStr : blacklistedItems) {
            try {
                Identifier id = Identifier.parse(idStr);
                if (id != null) {
                    cachedIdentifiers.add(id);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public boolean isNotifyOnDrop() {
        return notifyOnDrop;
    }

    public void setNotifyOnDrop(boolean notifyOnDrop) {
        this.notifyOnDrop = notifyOnDrop;
        save();
    }

    public boolean isSoundFeedback() {
        return soundFeedback;
    }

    public void setSoundFeedback(boolean soundFeedback) {
        this.soundFeedback = soundFeedback;
        save();
    }

    public Set<String> getBlacklistedItems() {
        return Collections.unmodifiableSet(blacklistedItems);
    }

    public synchronized void setBlacklistedItems(java.util.Collection<String> items) {
        this.blacklistedItems.clear();
        if (items != null) {
            for (String item : items) {
                if (item != null && !item.trim().isEmpty()) {
                    this.blacklistedItems.add(item.trim());
                }
            }
        }
        rebuildCache();
        save();
    }

    public boolean isItemFiltered(Item item) {
        if (!enabled || item == null) {
            return false;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return cachedIdentifiers.contains(id);
    }

    public boolean isItemFiltered(Identifier id) {
        return enabled && id != null && cachedIdentifiers.contains(id);
    }

    public synchronized boolean addItem(Identifier id) {
        if (id == null) return false;
        String key = id.toString();
        if (blacklistedItems.add(key)) {
            cachedIdentifiers.add(id);
            save();
            return true;
        }
        return false;
    }

    public synchronized boolean removeItem(Identifier id) {
        if (id == null) return false;
        String key = id.toString();
        if (blacklistedItems.remove(key)) {
            cachedIdentifiers.remove(id);
            save();
            return true;
        }
        return false;
    }

    public synchronized void clear() {
        blacklistedItems.clear();
        cachedIdentifiers.clear();
        save();
    }
}
