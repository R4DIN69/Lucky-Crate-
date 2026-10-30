package com.luckycrates.command;

import com.luckycrates.manager.KeyManager;

import com.luckycrates.LuckyCratesPlugin;
import com.luckycrates.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GiveKeyCommand implements CommandExecutor, TabCompleter, Listener {
    private final LuckyCratesPlugin plugin;

    public GiveKeyCommand(LuckyCratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Intercepts `/give <player> luckykey <amount>` so players and console can use the exact requested command!
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerGiveCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().trim();
        if (msg.startsWith("/")) msg = msg.substring(1);
        String[] parts = msg.split("\\s+");

        if (parts.length >= 3 && parts[0].equalsIgnoreCase("give") && isKeyAlias(parts[2])) {
            event.setCancelled(true);
            Player sender = event.getPlayer();
            if (!sender.hasPermission("luckycrates.admin")) {
                sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                        plugin.getConfig().getString("messages.no-permission", "&cYou do not have permission.")));
                return;
            }
            int amount = 1;
            if (parts.length >= 4) {
                try {
                    amount = Integer.parseInt(parts[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                            plugin.getConfig().getString("messages.invalid-amount", "&cAmount must be a positive integer.")));
                    return;
                }
            }
            giveKey(sender, parts[1], amount);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onServerGiveCommand(ServerCommandEvent event) {
        String msg = event.getCommand().trim();
        if (msg.startsWith("/")) msg = msg.substring(1);
        String[] parts = msg.split("\\s+");

        if (parts.length >= 3 && parts[0].equalsIgnoreCase("give") && isKeyAlias(parts[2])) {
            event.setCancelled(true);
            int amount = 1;
            if (parts.length >= 4) {
                try {
                    amount = Integer.parseInt(parts[3]);
                } catch (NumberFormatException e) {
                    event.getSender().sendMessage("Amount must be a positive integer.");
                    return;
                }
            }
            giveKey(event.getSender(), parts[1], amount);
        }
    }

    private boolean isKeyAlias(String itemArg) {
        return itemArg.equalsIgnoreCase("luckykey") ||
               itemArg.equalsIgnoreCase("lucky_key") ||
               itemArg.equalsIgnoreCase("cratekey");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luckycrates.admin")) {
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.no-permission", "&cYou do not have permission.")));
            return true;
        }

        // Usage: /luckykey give <player> <amount> OR /luckykey <player> <amount>
        String targetName;
        int amount = 1;

        if (args.length >= 2 && args[0].equalsIgnoreCase("give")) {
            targetName = args[1];
            if (args.length >= 3) {
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                            plugin.getConfig().getString("messages.invalid-amount", "&cAmount must be a positive integer.")));
                    return true;
                }
            }
        } else if (args.length >= 1) {
            targetName = args[0];
            if (args.length >= 2) {
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                            plugin.getConfig().getString("messages.invalid-amount", "&cAmount must be a positive integer.")));
                    return true;
                }
            }
        } else {
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                    "&eUsage: &f/give <player> luckykey <amount> &eor &f/luckykey give <player> <amount>"));
            return true;
        }

        giveKey(sender, targetName, amount);
        return true;
    }

    public void giveKey(CommandSender sender, String playerName, int amount) {
        if (amount <= 0) {
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.invalid-amount", "&cAmount must be a positive integer.")));
            return;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null || !target.isOnline()) {
            String notFound = plugin.getConfig().getString("messages.player-not-found", "&cPlayer &e%player% &cwas not found.")
                    .replace("%player%", playerName);
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + notFound));
            return;
        }

        ItemStack key = plugin.getKeyManager().createKey(amount);
        target.getInventory().addItem(key);
        target.playSound(target.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f);

        String keyName = plugin.getConfig().getString("key.name", "&6&l★ Lucky Key ★");
        String senderMsg = plugin.getConfig().getString("messages.key-given", "&aGave &e%amount%x %key% &ato &e%player%&a!")
                .replace("%amount%", String.valueOf(amount))
                .replace("%key%", keyName)
                .replace("%player%", target.getName());
        sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + senderMsg));

        String targetMsg = plugin.getConfig().getString("messages.key-received", "&aYou received &e%amount%x %key%&a!")
                .replace("%amount%", String.valueOf(amount))
                .replace("%key%", keyName);
        target.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + targetMsg));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("give"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
            return list;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
            return list;
        } else if (args.length == 3) {
            return Arrays.asList("1", "5", "10", "64");
        }
        return Collections.emptyList();
    }
}
