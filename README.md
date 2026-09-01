# ItemBlind 

A Minecraft: Java Edition mod built with Fabric to filter inventory.

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2%20%7C%2026.1.x-blue.svg)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Loader%200.19.3+-green.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**ItemBlind** is a client-side Fabric mod for Minecraft that prevents unwanted items from cluttering your inventory. When you pick up an item on your filter list, ItemBlind automatically drops it back to the ground for you.

---

## 📋 Requirements
- **Minecraft Version:** 26.1.x, 26.2
- **Fabric Loader:** 0.19.3+
- **Dependencies:** None 

---

## 🎮 How to Use

1. Press **`X`** (default) in-game to open the **ItemBlind** filter screen.
2. Type an item name (e.g. `dirt`, `cobblestone`, `wheat_seeds`) into the search bar:
   - Click any item in the dropdown suggestions to add it instantly.
   - Or click **"+ Add"** to add the typed ID.
   - Or click **"Add Held Item"** to blacklist the item currently in your main hand.
3. Remove items anytime with the **`✕`** button next to each entry.

---

## 🛠️ Building from Source

### Prerequisites
- JDK 25

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
