package com.rivers.farms.listener;

import com.rivers.farms.RiversFarms;
import com.rivers.farms.config.Settings;
import com.rivers.farms.crop.CropType;
import com.rivers.farms.util.Cooldowns;
import com.rivers.farms.util.Tooling;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles right-click harvesting using hoes.
 * Resets crop age, spawns particles, plays tilling sound,
 * and drops crop/seed items on the ground at each block.
 */
public class HarvestListener implements Listener {

    private final RiversFarms plugin;
    private final Settings settings;
    private final Cooldowns cooldowns;

    public HarvestListener(RiversFarms plugin, Settings settings, Cooldowns cooldowns) {
        this.plugin = plugin;
        this.settings = settings;
        this.cooldowns = cooldowns;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        CropType type = CropType.fromBlock(clicked);
        if (type == null || !CropType.isMature(clicked)) return;

        Player player = event.getPlayer();

        if (!Tooling.isHoe(player.getInventory().getItem(EquipmentSlot.HAND))) return;
        if (!cooldowns.ready("harvest", player.getUniqueId(), settings.harvestCooldownMs)) return;

        World world = clicked.getWorld();
        int radius = settings.harvestRadius;

        // Sweep particle on main crop
        if (settings.harvestSweep) {
            world.spawnParticle(Particle.SWEEP_ATTACK, clicked.getLocation().add(0.5, 0.5, 0.5), 1);
        }

        Sound harvestSound = Sound.ITEM_HOE_TILL;
        float vol = (float) settings.harvestSoundVolume;

        ItemStack tool = player.getInventory().getItem(EquipmentSlot.HAND);
        int fortune = tool.getEnchantmentLevel(Enchantment.FORTUNE);
        int bonus = settings.harvestFortuneExtra ? computeFortuneBonus(fortune) : 0;

        // Loop affected blocks
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block b = clicked.getRelative(dx, 0, dz);
                if (CropType.fromBlock(b) != type || !CropType.isMature(b)) continue;

                world.spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 0.3, 0.5),
                        10, 0.25, 0.2, 0.25, 0.001, b.getBlockData());

                if (settings.harvestSweep) {
                    world.spawnParticle(Particle.SWEEP_ATTACK, b.getLocation().add(0.5, 0.5, 0.5), 1);
                }

                world.playSound(b.getLocation(), harvestSound, vol, 1.0f);

                // Reset age
                CropType.setLowestAge(b);

                // Moisture 7 → 0 rule
                Block below = b.getRelative(0, -1, 0);
                if (below.getType() == Material.FARMLAND &&
                        below.getBlockData() instanceof Farmland fm &&
                        fm.getMoisture() == 7) {

                    Farmland f2 = (Farmland) fm.clone();
                    f2.setMoisture(0);
                    below.setBlockData(f2, false);
                }

                // DROP ITEMS ON GROUND (instead of adding to inventory)
                world.dropItemNaturally(b.getLocation(), new ItemStack(type.foodItem, 1 + bonus));
                if (type.seedItem != null) {
                    world.dropItemNaturally(b.getLocation(), new ItemStack(type.seedItem, 1));
                }
            }
        }

        Tooling.damageHoeOnce(player);
    }

    private int computeFortuneBonus(int fortune) {
        int bonus = 0;
        for (int i = 0; i < fortune; i++)
            if (ThreadLocalRandom.current().nextDouble() < 0.33)
                bonus++;
        return bonus;
    }
}
