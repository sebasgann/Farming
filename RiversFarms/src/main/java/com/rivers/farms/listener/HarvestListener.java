package com.rivers.farms.listener;

import com.rivers.farms.RiversFarms;
import com.rivers.farms.config.Settings;
import com.rivers.farms.crop.CropType;
import com.rivers.farms.util.Cooldowns;
import com.rivers.farms.util.Tooling;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Farmland;
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
 * Handles right-click harvesting using hoes:
 * - Gated by cooldown and maturity checks
 * - AOE scan in a square radius
 * - Base crop count determined by hoe tier (0/1/2/3)
 * - Fortune then adds extra crops on top of the tier base
 * - Always 1 seed minimum for crops that actually have seeds (wheat/beetroot)
 * - Drops items on the ground at the crop location
 * - Resets crop to age 0 and applies moisture rule (7 -> 0)
 * - Damages the hoe exactly once per swing (Unbreaking-aware)
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

    /**
     * Returns the base number of crops dropped for a given hoe tier.
     * Non-hoe: 0
     * Wood/Stone: 1
     * Iron/Gold:  2
     * Diamond/Netherite: 3
     */
    private int tierBaseCrops(ItemStack tool) {
        if (tool == null) return 0;
        switch (tool.getType()) {
            case WOODEN_HOE:
            case STONE_HOE:
                return 1;
            case IRON_HOE:
            case GOLDEN_HOE:
                return 2;
            case DIAMOND_HOE:
            case NETHERITE_HOE:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Computes fortune bonus crops for ONE harvested block.
     * Each fortune level has a 33% chance to add +1 crop.
     * Example: Fortune III yields +0..3 crops, each level rolled independently.
     */
    private int computeFortuneExtraPerBlock(int fortuneLevel) {
        if (fortuneLevel <= 0) return 0;
        int extra = 0;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < fortuneLevel; i++) {
            if (rng.nextDouble() < 0.33) extra++;
        }
        return extra;
    }

    /** Main AOE harvest handler. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        CropType type = CropType.fromBlock(clicked);
        if (type == null || !CropType.isMature(clicked)) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack tool = player.getInventory().getItem(EquipmentSlot.HAND);
        if (!com.rivers.farms.util.Tooling.isHoe(tool)) return;

        if (!cooldowns.ready("harvest", player.getUniqueId(), settings.harvestCooldownMs)) return;

        World world = clicked.getWorld();
        int radius = settings.harvestRadius;

        if (settings.harvestSweep) {
            world.spawnParticle(Particle.SWEEP_ATTACK, clicked.getLocation().add(0.5, 0.5, 0.5), 1, 0, 0, 0, 0);
        }

        Sound harvestSound = Sound.ITEM_HOE_TILL;
        float vol = (float) settings.harvestSoundVolume;

        // Base from hoe tier
        int baseCrops = tierBaseCrops(tool);
        // Seeds: 1 minimum only if the crop has a seed item defined
        int baseSeeds = (baseCrops > 0 && type.seedItem != null) ? 1 : 0;

        // Read fortune level once from the tool
        int fortuneLevel = (tool != null) ? tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.FORTUNE) : 0;

        // Scan square AOE
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block b = clicked.getRelative(dx, 0, dz);
                if (CropType.fromBlock(b) != type || !CropType.isMature(b)) continue;

                world.spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 0.3, 0.5),
                        10, 0.25, 0.2, 0.25, 0.001, b.getBlockData());

                if (settings.harvestSweep) {
                    world.spawnParticle(Particle.SWEEP_ATTACK, b.getLocation().add(0.5, 0.5, 0.5),
                            1, 0, 0, 0, 0);
                }

                world.playSound(b.getLocation(), harvestSound, vol, 1.0f);

                // Reset to age 0 (replant)
                CropType.setLowestAge(b);

                // Moisture 7 -> 0 under harvested crop
                Block below = b.getRelative(0, -1, 0);
                if (below.getType() == Material.FARMLAND &&
                        below.getBlockData() instanceof Farmland fm &&
                        fm.getMoisture() == 7) {
                    Farmland nf = (Farmland) fm.clone();
                    nf.setMoisture(0);
                    below.setBlockData(nf, false);
                }

                // Fortune extra ON TOP OF tier base, per block
                int fortuneExtra = settings.harvestFortuneExtra ? computeFortuneExtraPerBlock(fortuneLevel) : 0;
                int totalCrops = Math.max(0, baseCrops + fortuneExtra);

                // Drop crops
                if (totalCrops > 0) {
                    world.dropItemNaturally(b.getLocation(), new ItemStack(type.foodItem, totalCrops));
                }

                // Drop seeds (minimum 1 if the crop actually has seeds)
                if (baseSeeds > 0) {
                    world.dropItemNaturally(b.getLocation(), new ItemStack(type.seedItem, baseSeeds));
                }
            }
        }

        // Damage hoe exactly once per swing (not per block)
        Tooling.damageHoeOnce(player);
    }
}
