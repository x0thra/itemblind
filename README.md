# ItemBlind 👁️🚫

A Minecraft: Java Edition mod built with Fabric to filter inventory.

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2%20%7C%2026.1.x-blue.svg)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Loader%200.19.3+-green.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**ItemBlind** is a client-side Fabric mod for Minecraft that prevents unwanted items from cluttering your inventory. When you pick up an item on your filter list, ItemBlind automatically and safely drops it back to the ground with zero tick lag and zero inventory desync.

---

## 📋 Requirements
- **Minecraft Version:** 26.1.x, 26.2
- **Fabric Loader:** 0.19.3+
- **Dependencies:** None (Completely standalone!)

---

## ✨ Features

- **⚡ Zero Tick Lag & Instant Response:** Filters run with $O(1)$ `HashSet` lookups and zero client-server desync using native container network packets (`ContainerInput.THROW`).
- **🔍 Live 3D Item Suggestions:** Dynamic autocomplete search box showing real-time 3D item icons, translated item names, and resource identifiers as you type.
- **✋ One-Click Add:** Quickly blacklist whatever item you're holding with the "Add Held Item" button.
- **💾 Real-Time Auto-Save:** Instant persistent saving to JSON upon every interaction. No manual save button needed.
- **🛡️ Creative Mode Dupe Protection:** Automatically detects Creative & Spectator modes and pauses dropping to prevent infinite item duplication loops, accompanied by a helpful chat notification.
- **⌨️ Customizable Keybinds:** Open the menu anytime using **`X`** (rebindable in Controls) or configure via **ModMenu**.
- **🌐 Localization:** Full support for English (`en_us`) and Turkish (`tr_tr`).
- **🪶 Zero External Dependencies:** Completely standalone client mod—no heavy config libraries required.

---

## 🎮 How to Use

1. Press **`X`** (default) in-game to open the **ItemBlind** filter screen.
2. Type an item name (e.g. `dirt`, `cobblestone`, `wheat_seeds`) into the search bar:
   - Click any item in the dropdown suggestions to add it instantly.
   - Or click **"+ Add"** to add the typed ID.
   - Or click **"Add Held Item"** to blacklist the item currently in your main hand.
3. Remove items anytime with the **`✕`** button next to each entry.
4. Toggle notifications, sound effects, or the entire filter on/off directly from the top bar.

---

## 🏗️ Multi-Version Architecture

This project is built using a clean multi-project Gradle setup, keeping version-specific code decoupled:

```
itemblind/
├── 26.2/       # Minecraft 26.2 module (Fabric API 0.158.0+26.2)
├── 26.1/       # Minecraft 26.1.x module (Fabric API 0.155.2+26.1.2)
└── ...
```

---

## 🛠️ Building from Source

### Prerequisites
- JDK 25 (e.g., Eclipse Adoptium Temurin 25)

### Build All Versions
```bash
./gradlew build
```

The compiled jars will be placed in each subproject's build directory:
- `26.2/build/libs/itemblind-mc26.2-1.0.0.jar`
- `26.1/build/libs/itemblind-mc26.1-1.0.0.jar`

### Build a Specific Version
```bash
# For Minecraft 26.2
./gradlew :26.2:build

# For Minecraft 26.1.x
./gradlew :26.1:build
```

---

## 📄 License

This mod is available under the [MIT License](LICENSE).
