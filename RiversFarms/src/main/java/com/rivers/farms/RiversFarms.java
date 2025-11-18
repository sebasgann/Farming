package com.rivers.farms;

import com.rivers.farms.command.FarmTrampleCommand;
import com.rivers.farms.command.WateringCanCommand;
import com.rivers.farms.config.Settings;
import com.rivers.farms.listener.*;
import com.rivers.farms.player.PlayerSettingsManager;
import com.rivers.farms.util.Cooldowns;
import com.rivers.farms.util.Items;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Core plugin class for RiversFarms.
 * Loads configuration, registers commands, listeners,
 * items, and custom crafting recipes.
 */
public class RiversFarms extends JavaPlugin {

    private Settings settings;
    private Cooldowns cooldowns;
    private PlayerSettingsManager playerSettingsManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        this.settings = new Settings(getConfig());
        this.cooldowns = new Cooldowns();
        Items.initKeys(this);

        this.playerSettingsManager = new PlayerSettingsManager(this);
        playerSettingsManager.load();

        registerRecipe();
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        playerSettingsManager.save();
    }

    /** Registers all commands for the plugin. */
    private void registerCommands() {
        getCommand("farmtrample").setExecutor(new FarmTrampleCommand(this, playerSettingsManager));
        getCommand("wateringcan").setExecutor(new WateringCanCommand(this));
    }

    /** Registers all event listeners for farming interactions. */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CropBreakListener(), this);
        getServer().getPluginManager().registerEvents(new HarvestListener(this, settings, cooldowns), this);
        getServer().getPluginManager().registerEvents(new GrowthListener(settings), this);
        getServer().getPluginManager().registerEvents(new MoistureLockListener(settings), this);
        getServer().getPluginManager().registerEvents(new WateringCanListener(this, settings), this);
        getServer().getPluginManager().registerEvents(new CompostListener(this, settings), this);
        getServer().getPluginManager().registerEvents(new TrampleListener(playerSettingsManager), this);
    }

    /**
     * Registers the Watering Can crafting recipe:
     *
     *     N N N
     *     N W N
     *     N N N
     *
     * Water Bucket center, Iron Nuggets surrounding.
     */
    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(this, "watering_can");

        ItemStack wateringCan = Items.createWateringCan(this);

        ShapedRecipe recipe = new ShapedRecipe(key, wateringCan);
        recipe.shape("NNN", "NWN", "NNN");
        recipe.setIngredient('N', Material.IRON_NUGGET);
        recipe.setIngredient('W', Material.WATER_BUCKET);

        getServer().addRecipe(recipe);
    }

    public Settings getSettings() { return settings; }
    public Cooldowns getCooldowns() { return cooldowns; }
    public PlayerSettingsManager getPlayerSettingsManager() { return playerSettingsManager; }
}
