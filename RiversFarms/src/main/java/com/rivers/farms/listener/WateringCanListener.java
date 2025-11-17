package com.rivers.farms.listener;

import com.rivers.farms.RiversFarms;
import com.rivers.farms.config.Settings;
import com.rivers.farms.crop.CropType;
import com.rivers.farms.util.Items;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles all interactions with the custom Watering Can.
 * This includes crop hydration, age growth, and prevention
 * of block placement using the SOUL_LANTERN base item.
 */
public class WateringCanListener implements Listener {

    private final RiversFarms plugin;
    private final Settings settings;

    public WateringCanListener(RiversFarms plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    /**
     * Prevents the Watering Can (SOUL_LANTERN with PDC tag)
     * from being placed as a block under any circumstances.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWateringCanPlace(BlockPlaceEvent event) {
        ItemStack inHand = event.getItemInHand();
        if (Items.isWateringCan(inHand)) {
            event.setCancelled(true);
        }
    }

    /**
     * Primary Watering Can handler for right-click interactions.
     * Produces water particles, rehydrates farmland, and grows
     * crops by +1 age with a per-block cooldown.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUse(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!Items.isWateringCan(item)) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        World world = block.getWorld();

        // 5-tick FALLING_WATER animation
        for (int t = 0; t < settings.wateringParticleTicks; t++) {
            int d = t;
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    world.spawnParticle(
                            Particle.FALLING_WATER,
                            block.getLocation().add(0.5, 1.0, 0.5),
                            4,
                            0.25, 0.05, 0.25,
                            0.01
                    ), d);
        }

        // Growth sound for watering effects
        Sound growSound = Sound.ITEM_BONE_MEAL_USE;

        Block below = block.getRelative(0, -1, 0);

        // Rehydrate farmland
        if (below.getType() == Material.FARMLAND &&
                below.getBlockData() instanceof Farmland farm) {

            if (farm.getMoisture() < 7) {
                Farmland f2 = (Farmland) farm.clone();
                f2.setMoisture(7);
                below.setBlockData(f2, false);
            }
        }

        // Grow crop +1 if eligible and not on cooldown
        if (CropType.fromBlock(block) != null &&
                block.getBlockData() instanceof Ageable age) {

            NamespacedKey key = new NamespacedKey(plugin,
                    "watering_" + block.getX() + "_" + block.getY() + "_" + block.getZ());

            long now = System.currentTimeMillis();
            long until = block.getChunk().getPersistentDataContainer()
                    .getOrDefault(key, PersistentDataType.LONG, 0L);

            if (now >= until && age.getAge() < age.getMaximumAge()) {

                Ageable grown = (Ageable) age.clone();
                grown.setAge(age.getAge() + 1);
                block.setBlockData(grown, false);
                world.playSound(block.getLocation(), growSound, 0.8f, 1.0f);

                block.getChunk().getPersistentDataContainer()
                        .set(key, PersistentDataType.LONG, now + settings.wateringPerCropMs);
            }
        }
    }
}
