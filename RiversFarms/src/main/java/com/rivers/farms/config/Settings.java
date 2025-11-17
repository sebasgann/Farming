package com.rivers.farms.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Centralized configuration access.
 * Provides typed fields for all tunables used by the plugin.
 */
public class Settings {

    public final int harvestRadius;
    public final long harvestCooldownMs;
    public final double harvestSoundVolume;
    public final boolean harvestSweep;
    public final boolean harvestFortuneExtra;

    public final boolean moistureLockAtSeven;
    public final boolean moistureDryHalfRate;
    public final boolean rainDoubleGrowth;

    public final int wateringRadius;
    public final int wateringParticleTicks;
    public final long wateringPerCropMs;

    public final String compostName;
    public final java.util.List<String> compostLore;
    public final String compostParticle;
    public final int compostParticleTicks;
    public final int compostRadius;
    public final double compostChance;

    public Settings(FileConfiguration cfg) {
        this.harvestRadius = cfg.getInt("harvest.radius", 2);
        this.harvestCooldownMs = cfg.getLong("cooldowns.harvest_seconds", 1L) * 1000L;
        this.harvestSoundVolume = cfg.getDouble("harvest.sound_volume", 0.5D);
        this.harvestSweep = cfg.getBoolean("harvest.sweep_particle", true);
        this.harvestFortuneExtra = cfg.getBoolean("harvest.fortune_extra", true);

        this.moistureLockAtSeven = cfg.getBoolean("moisture.lock_at_seven", true);
        this.moistureDryHalfRate = cfg.getBoolean("moisture.dry_half_rate", true);
        this.rainDoubleGrowth = cfg.getBoolean("moisture.rain_double_growth", true);

        this.wateringRadius = cfg.getInt("watering.radius", 0);
        this.wateringParticleTicks = cfg.getInt("watering.particle_ticks", 5);
        this.wateringPerCropMs = cfg.getLong("cooldowns.watering_per_crop_hours", 1L) * 60L * 60L * 1000L;

        this.compostName = cfg.getString("compost.item_name", "&6Compost");
        this.compostLore = cfg.getStringList("compost.item_lore");
        this.compostParticle = cfg.getString("compost.particle", "HAPPY_VILLAGER");
        this.compostParticleTicks = cfg.getInt("compost.particle_ticks", 10);
        this.compostRadius = cfg.getInt("compost.growth_radius", 1);
        this.compostChance = cfg.getDouble("compost.growth_chance", 0.5D);
    }
}
