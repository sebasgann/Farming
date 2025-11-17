package com.rivers.farms.listener;

import com.rivers.farms.RiversFarms;
import com.rivers.farms.config.Settings;
import com.rivers.farms.crop.CropType;
import com.rivers.farms.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Composter behavior and Compost AoE growth.
 */
public class CompostListener implements Listener {

    private final RiversFarms plugin;
    private final Settings settings;

    public CompostListener(RiversFarms plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onComposterInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getType() == Material.COMPOSTER &&
            block.getBlockData() instanceof Levelled lvl &&
            lvl.getLevel() == lvl.getMaximumLevel()) {

            event.setCancelled(true);

            Levelled reset = (Levelled) lvl.clone();
            reset.setLevel(0);
            block.setBlockData(reset, false);

            Player player = event.getPlayer();
            ItemStack compost = Items.createCompost(plugin, settings);

            var leftover = player.getInventory().addItem(compost);
            leftover.values().forEach(it -> player.getWorld().dropItemNaturally(player.getLocation(), it));
            return;
        }

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItem(EquipmentSlot.HAND);
        if (!Items.isCompost(hand)) return;

        CropType center = CropType.fromBlock(block);
        if (center == null) {
            event.setCancelled(true);
            return;
        }

        hand.setAmount(hand.getAmount() - 1);
        player.getInventory().setItem(EquipmentSlot.HAND, hand.getAmount() > 0 ? hand : null);

        World world = block.getWorld();
        int radius = settings.compostRadius;
        int ticks = settings.compostParticleTicks;
        double chance = settings.compostChance;

        Sound growSound = Sound.ITEM_BONE_MEAL_USE;
        Particle particle = Particle.valueOf(settings.compostParticle);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block b = block.getRelative(dx, 0, dz);
                CropType t = CropType.fromBlock(b);
                if (t == null) continue;
                if (!(b.getBlockData() instanceof Ageable age)) continue;
                if (age.getAge() >= age.getMaximumAge()) continue;

                if (ThreadLocalRandom.current().nextDouble() <= chance) {
                    Ageable clone = (Ageable) age.clone();
                    clone.setAge(age.getAge() + 1);
                    b.setBlockData(clone, false);

                    world.playSound(b.getLocation(), growSound, 0.8f, 1.0f);

                    for (int tTick = 0; tTick < ticks; tTick++) {
                        int delay = tTick;
                        Bukkit.getScheduler().runTaskLater(plugin, () ->
                                world.spawnParticle(particle,
                                        b.getLocation().add(0.5, 0.4, 0.5),
                                        3, 0.2, 0.2, 0.2, 0.01), delay);
                    }
                }
            }
        }
    }
}
