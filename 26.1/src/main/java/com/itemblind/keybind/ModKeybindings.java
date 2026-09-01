package com.itemblind.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ModKeybindings {
    public static KeyMapping openScreenKey;
    public static KeyMapping toggleFilterKey;

    private ModKeybindings() {
    }

    public static void register() {
        openScreenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.itemblind.open_screen",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                KeyMapping.Category.MISC
        ));

        toggleFilterKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.itemblind.toggle_filter",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KeyMapping.Category.MISC
        ));
    }
}
