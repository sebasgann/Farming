package com.rivers.farms.listener;

import com.rivers.farms.config.Settings;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.MoistureChangeEvent;

/**
 * Locks farmland at moisture 7, except when we explicitly reset on harvest.
 */
public class MoistureLockListener implements Listener {

    private final Settings settings;

    public MoistureLockListener(Settings settings) {
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoistureChange(MoistureChangeEvent event) {
        if (!settings.moistureLockAtSeven) return;
        if (!(event.getBlock().getBlockData() instanceof Farmland current)) return;

        BlockState newState = event.getNewState();
        if (newState.getBlockData() instanceof Farmland next) {
            if (current.getMoisture() == 7 && next.getMoisture() < 7) {
                event.setCancelled(true);
            }
        }
    }
}
