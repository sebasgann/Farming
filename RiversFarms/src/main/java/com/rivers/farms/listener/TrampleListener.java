package com.rivers.farms.listener;

import com.rivers.farms.player.PlayerSettings;
import com.rivers.farms.player.PlayerSettingsManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 * Per-player control over farmland trampling.
 */
public class TrampleListener implements Listener {

    private final PlayerSettingsManager settingsManager;

    public TrampleListener(PlayerSettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.FARMLAND) return;
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerSettings settings = settingsManager.get(player.getUniqueId());
        if (!settings.isTramplingEnabled()) {
            event.setCancelled(true);
        }
    }
}
