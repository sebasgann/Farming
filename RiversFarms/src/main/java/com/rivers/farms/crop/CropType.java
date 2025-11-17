package com.rivers.farms.crop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

/**
 * Supported crops and their associated items.
 */
public enum CropType {
    WHEAT(Material.WHEAT, Material.WHEAT, Material.WHEAT_SEEDS),
    BEETROOTS(Material.BEETROOTS, Material.BEETROOT, Material.BEETROOT_SEEDS),
    CARROTS(Material.CARROTS, Material.CARROT, null),
    POTATOES(Material.POTATOES, Material.POTATO, null);

    public final Material blockType;
    public final Material foodItem;
    public final Material seedItem;

    CropType(Material blockType, Material foodItem, Material seedItem) {
        this.blockType = blockType;
        this.foodItem = foodItem;
        this.seedItem = seedItem;
    }

    public static CropType fromBlock(Block b) {
        for (CropType type : values()) if (b.getType() == type.blockType) return type;
        return null;
    }

    public static boolean isMature(Block b) {
        BlockData data = b.getBlockData();
        if (!(data instanceof Ageable ageable)) return false;
        return ageable.getAge() >= ageable.getMaximumAge();
    }

    public static void setLowestAge(Block b) {
        BlockData data = b.getBlockData();
        if (data instanceof Ageable ageable) {
            ageable.setAge(0);
            b.setBlockData(ageable, false);
        }
    }

    public static Block getFarmlandBelow(Block crop) {
        return crop.getRelative(BlockFace.DOWN);
    }
}
