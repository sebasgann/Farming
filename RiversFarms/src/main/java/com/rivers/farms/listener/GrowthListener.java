package com.rivers.farms.listener;

import com.rivers.farms.config.Settings;
import com.rivers.farms.crop.CropType;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Natural crop growth adjustments: slow on dry soil, rain→+2 age,
 * and play a valid growth-adjacent sound.
 */
public class GrowthListener implements Listener {

    private final Settings settings;

    public GrowthListener(Settings settings) {
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        if (CropType.fromBlock(block) == null) return;

        Block below = CropType.getFarmlandBelow(block);
        if (settings.moistureDryHalfRate && below.getBlockData() instanceof Farmland farm) {
            if (farm.getMoisture() == 0 && ThreadLocalRandom.current().nextBoolean()) {
                event.setCancelled(true);
                return;
            }
        }

        if (!(block.getBlockData() instanceof Ageable oldAge)) return;
        if (!(event.getNewState().getBlockData() instanceof Ageable newAge)) return;

        World world = block.getWorld();

        if (settings.rainDoubleGrowth) {
            int oldVal = oldAge.getAge();
            int newVal = newAge.getAge();
            if (world.hasStorm() && newVal == oldVal + 1) {
                newAge.setAge(Math.min(newAge.getMaximumAge(), oldVal + 2));
                event.getNewState().setBlockData(newAge);
            }
        }

        if (newAge.getAge() > oldAge.getAge()) {
            world.playSound(block.getLocation(), Sound.ENTITY_VILLAGER_WORK_FARMER, 0.8f, 1.0f);
        }
    }
}
