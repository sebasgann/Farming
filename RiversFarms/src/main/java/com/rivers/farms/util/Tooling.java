package com.rivers.farms.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Tool-related helpers (e.g., hoe checks and durability damage).
 */
public final class Tooling {

    private Tooling() {}

    public static boolean isHoe(ItemStack stack) {
        if (stack == null) return false;
        String name = stack.getType().name();
        return name.endsWith("_HOE");
    }

    public static void damageHoeOnce(Player player) {
        ItemStack tool = player.getInventory().getItem(EquipmentSlot.HAND);
        if (tool == null || !isHoe(tool)) return;
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable dmg)) return;

        int unb = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
        double chance = 1.0 / (unb + 1);
        if (ThreadLocalRandom.current().nextDouble() > chance) return;

        dmg.setDamage(dmg.getDamage() + 1);
        tool.setItemMeta(dmg);
    }
}
