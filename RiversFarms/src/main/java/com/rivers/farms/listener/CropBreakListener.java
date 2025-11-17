package com.rivers.farms.listener;

import com.rivers.farms.crop.CropType;
import com.rivers.farms.util.Tooling;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Enforces hoe vs non-hoe behaviors on breaking crops.
 */
public class CropBreakListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (CropType.fromBlock(block) == null) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack inHand = player.getInventory().getItem(EquipmentSlot.HAND);
        boolean hoe = Tooling.isHoe(inHand);

        if (hoe) {
            event.setCancelled(true);
        } else {
            event.setDropItems(false);
        }
    }
}
