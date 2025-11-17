package com.rivers.farms.util;

import com.rivers.farms.RiversFarms;
import com.rivers.farms.config.Settings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.stream.Collectors;

/**
 * Responsible for constructing all custom plugin items,
 * including the Watering Can and Compost. Items created here
 * are tagged with PDC values to ensure they are uniquely
 * identifiable throughout the plugin's logic.
 */
public final class Items {

    public static NamespacedKey WATERING_CAN_KEY;
    public static NamespacedKey COMPOST_KEY;

    private Items() {}

    public static void initKeys(RiversFarms plugin) {
        WATERING_CAN_KEY = new NamespacedKey(plugin, "watering_can");
        COMPOST_KEY = new NamespacedKey(plugin, "compost_item");
    }

    /**
     * Creates the custom Watering Can item.
     * Uses SOUL_LANTERN base item and marks it with PDC metadata.
     */
    public static ItemStack createWateringCan(RiversFarms plugin) {
        ItemStack stack = new ItemStack(Material.SOUL_LANTERN);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Watering Can");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(WATERING_CAN_KEY, PersistentDataType.BYTE, (byte)1);
        stack.setItemMeta(meta);
        return stack;
    }

    /** Checks if the given ItemStack is the plugin's Watering Can. */
    public static boolean isWateringCan(ItemStack stack) {
        if (stack == null || stack.getType() != Material.SOUL_LANTERN) return false;
        if (!stack.hasItemMeta()) return false;
        return stack.getItemMeta().getPersistentDataContainer().has(WATERING_CAN_KEY, PersistentDataType.BYTE);
    }

    /**
     * Creates the Compost item with display name and lore
     * taken from config values.
     */
    public static ItemStack createCompost(RiversFarms plugin, Settings settings) {
        ItemStack stack = new ItemStack(Material.BROWN_DYE);
        ItemMeta meta = stack.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', settings.compostName));
        meta.setLore(settings.compostLore.stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(COMPOST_KEY, PersistentDataType.BYTE, (byte)1);

        stack.setItemMeta(meta);
        return stack;
    }

    /** Checks if an item is the custom Compost item. */
    public static boolean isCompost(ItemStack stack) {
        if (stack == null || stack.getType() != Material.BROWN_DYE) return false;
        if (!stack.hasItemMeta()) return false;
        return stack.getItemMeta().getPersistentDataContainer().has(COMPOST_KEY, PersistentDataType.BYTE);
    }
}
