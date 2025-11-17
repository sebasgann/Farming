package com.rivers.farms.player;

import com.rivers.farms.RiversFarms;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Persistence for player settings stored in plugins/RiversFarms/player-data.yml.
 */
public class PlayerSettingsManager {

    private final RiversFarms plugin;
    private final Map<UUID, PlayerSettings> settingsMap = new HashMap<>();
    private File dataFile;
    private YamlConfiguration dataConfig;

    public PlayerSettingsManager(RiversFarms plugin) {
        this.plugin = plugin;
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "player-data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create player-data.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        settingsMap.clear();

        ConfigurationSection players = dataConfig.getConfigurationSection("players");
        if (players == null) return;

        for (String key : players.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                boolean trampling = players.getBoolean(key + ".tramplingEnabled", true);
                settingsMap.put(uuid, new PlayerSettings(uuid, trampling));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid UUID in player-data.yml: " + key);
            }
        }
    }

    public void save() {
        if (dataConfig == null) dataConfig = new YamlConfiguration();
        dataConfig.set("players", null);
        for (PlayerSettings s : settingsMap.values()) {
            String path = "players." + s.getUuid();
            dataConfig.set(path + ".tramplingEnabled", s.isTramplingEnabled());
        }
        try { dataConfig.save(dataFile); }
        catch (IOException e) {
            plugin.getLogger().severe("Failed to save player-data.yml: " + e.getMessage());
        }
    }

    public PlayerSettings get(UUID id) {
        return settingsMap.computeIfAbsent(id, k -> new PlayerSettings(k, true));
    }
}
