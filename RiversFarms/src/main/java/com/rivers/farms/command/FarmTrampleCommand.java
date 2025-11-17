package com.rivers.farms.command;

import com.rivers.farms.RiversFarms;
import com.rivers.farms.player.PlayerSettings;
import com.rivers.farms.player.PlayerSettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

/**
 * /farmtrample command for players and admins.
 * Players: toggle own trampling.
 * Admins: query or set another player's trampling state.
 */
public class FarmTrampleCommand implements CommandExecutor {

    private final RiversFarms plugin;
    private final PlayerSettingsManager settingsManager;

    public FarmTrampleCommand(RiversFarms plugin, PlayerSettingsManager settingsManager) {
        this.plugin = plugin;
        this.settingsManager = settingsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can toggle their own trampling.");
                return true;
            }
            if (!player.hasPermission("riversfarms.trample.toggle")) {
                player.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }

            PlayerSettings settings = settingsManager.get(player.getUniqueId());
            boolean newValue = !settings.isTramplingEnabled();
            settings.setTramplingEnabled(newValue);

            player.sendMessage(ChatColor.GREEN + "Crop trampling is now " +
                    (newValue ? ChatColor.RED + "ENABLED" : ChatColor.GREEN + "DISABLED") + ChatColor.RESET + ".");
            return true;
        }

        if (!sender.hasPermission("riversfarms.trample.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to manage others' settings.");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        PlayerSettings settings = settingsManager.get(target.getUniqueId());

        if (args.length == 1) {
            sender.sendMessage(ChatColor.YELLOW + target.getName() + "'s crop trampling is " +
                    (settings.isTramplingEnabled() ? ChatColor.RED + "ENABLED" : ChatColor.GREEN + "DISABLED") + ChatColor.RESET + ".");
            return true;
        }

        String value = args[1].toLowerCase();
        boolean enabled;
        if (value.equals("on") || value.equals("true") || value.equals("enable")) enabled = true;
        else if (value.equals("off") || value.equals("false") || value.equals("disable")) enabled = false;
        else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player> <on|off>");
            return true;
        }

        settings.setTramplingEnabled(enabled);
        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s crop trampling to " +
                (enabled ? ChatColor.RED + "ENABLED" : ChatColor.GREEN + "DISABLED") + ChatColor.RESET + ".");
        target.sendMessage(ChatColor.YELLOW + "Your crop trampling has been set to " +
                (enabled ? ChatColor.RED + "ENABLED" : ChatColor.GREEN + "DISABLED") + ChatColor.RESET +
                " by a moderator.");
        return true;
    }
}
