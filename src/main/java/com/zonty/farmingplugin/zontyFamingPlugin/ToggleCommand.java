package com.zonty.farmingplugin.zontyFamingPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;


public class ToggleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("togglefarm")) {
                if (args.length != 2) {
                    sender.sendMessage("Usage: /toggle [player] [true/false]");
                    return false;
                }

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }

                boolean value = Boolean.parseBoolean(args[1]);

                NamespacedKey key = new NamespacedKey("farmplugin", "toggled");

                target.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value ? 1: 0);
                sender.sendMessage("Set flag for " + target.getName() + " to " + value + " successfully!");
            }
            else {
                player.sendMessage("Hello " + player.getName() + "! You don't have a persmission to use this command!");
            }
        }

        else {
            sender.sendMessage("You are the almighty. *bows*. But I cba to make sure you're working.");
        }

        return true;
    }
}

