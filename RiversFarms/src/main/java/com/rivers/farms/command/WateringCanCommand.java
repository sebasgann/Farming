package com.rivers.farms.command;

import com.rivers.farms.RiversFarms;
import com.rivers.farms.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

/**
 * /wateringcan command for ops to give the custom item.
 */
public class WateringCanCommand implements CommandExecutor {

    private final RiversFarms plugin;

    public WateringCanCommand(RiversFarms plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("riversfarms.wateringcan.give")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        Player target;
        if (args.length >= 1) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " [player]");
            return true;
        }

        target.getInventory().addItem(Items.createWateringCan(plugin));
        sender.sendMessage(ChatColor.GREEN + "Gave a Watering Can to " + target.getName());
        return true;
    }
}
