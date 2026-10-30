package com.luckycrates.command;

import com.luckycrates.LuckyCratesPlugin;
import com.luckycrates.manager.CrateManager;
import com.luckycrates.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LuckyCratesCommand implements CommandExecutor, TabCompleter {
    private final LuckyCratesPlugin plugin;

    public LuckyCratesCommand(LuckyCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "place" -> {
                if (sender instanceof Player p) p.performCommand("place crate");
                else sender.sendMessage("§cOnly players can run this command.");
            }
            case "color" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can change crate colors.");
                    return true;
                }
                if (!player.hasPermission("luckycrates.admin")) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission", "&cNo permission.")));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                            "&cUsage: /crate color <color_name> (e.g. red, orange, blue, lime, purple...)"));
                    return true;
                }

                Block target = player.getTargetBlockExact(5);
                if (target == null || !plugin.getCrateManager().isCrate(target.getLocation())) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                            "&cYou must look at a Lucky Crate block within 5 blocks!"));
                    return true;
                }

                String chosenColor = args[1].toLowerCase();
                boolean success = plugin.getCrateManager().setCrateColor(target.getLocation(), chosenColor);
                if (!success) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                            "&cInvalid color! Available: &e" + String.join(", ", plugin.getCrateManager().getAvailableColors())));
                    return true;
                }

                player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1.0f, 1.4f);
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                        "&aCrate color and hologram title updated to &e" + chosenColor.toUpperCase() + "&a!"));
            }
            case "name", "rename" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can name crates.");
                    return true;
                }
                if (!player.hasPermission("luckycrates.admin")) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission", "&cNo permission.")));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                            "&cUsage: /crate " + sub + " <name>"));
                    return true;
                }

                Block target = player.getTargetBlockExact(5);
                if (target == null || !plugin.getCrateManager().isCrate(target.getLocation())) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                            "&cYou must look at a Lucky Crate block within 5 blocks!"));
                    return true;
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]).append(i == args.length - 1 ? "" : " ");
                }
                String newName = sb.toString();

                plugin.getCrateManager().setCrateName(target.getLocation(), newName);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                        "&aCrate name and hologram updated to: &r" + newName));
            }
            case "remove", "delete" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can run this command.");
                    return true;
                }
                if (!player.hasPermission("luckycrates.admin")) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission", "&cNo permission.")));
                    return true;
                }

                Block target = player.getTargetBlockExact(5);
                if (target == null || !plugin.getCrateManager().isCrate(target.getLocation())) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                            "&cLook at a Lucky Crate within 5 blocks to remove it!"));
                    return true;
                }

                Location loc = target.getLocation();
                plugin.getCrateManager().removeCrateStructure(loc);
                player.getWorld().spawnParticle(Particle.POOF, loc.clone().add(0.5, 0.5, 0.5), 25, 0.3, 0.3, 0.3, 0.05);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.8f, 1.2f);
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                        "&aSuccessfully removed Crate, End Rod, and Hologram!"));
            }
            case "key", "givekey" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /crate key <player> [amount]");
                    return true;
                }
                int amount = 1;
                if (args.length >= 3) {
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid amount.");
                        return true;
                    }
                }
                plugin.getGiveKeyCommand().giveKey(sender, args[1], amount);
            }
            case "reload" -> {
                if (!sender.hasPermission("luckycrates.admin")) return true;
                plugin.reloadConfig();
                plugin.getCrateManager().loadCrates();
                sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + "&aConfiguration and crates reloaded!"));
            }
            case "list" -> {
                if (!sender.hasPermission("luckycrates.admin")) return true;
                sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + "&6Registered Lucky Crates:"));
                for (Location loc : plugin.getCrateManager().getCrateLocations()) {
                    String name = plugin.getCrateManager().getCrateName(loc);
                    String color = plugin.getCrateManager().getCrateColor(loc);
                    sender.sendMessage(ColorUtil.color(" §8- &e" + name + " &7(Color: &f" + color + "&7, At: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")"));
                }
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.color("&8&m----------------&r &6&lLuckyCrates Commands &8&m----------------"));
        sender.sendMessage(ColorUtil.color(" &6/place crate &8- &7Place Shulker Box on End Rod pedestal"));
        sender.sendMessage(ColorUtil.color(" &6/crate color <color> &8- &7Change Shulker Box & hologram title color"));
        sender.sendMessage(ColorUtil.color(" &6/crate name <name> &8- &7Name the targeted crate"));
        sender.sendMessage(ColorUtil.color(" &6/crate rename <name> &8- &7Rename the targeted crate"));
        sender.sendMessage(ColorUtil.color(" &6/crate remove &8- &7Remove targeted Crate, End Rod & Hologram"));
        sender.sendMessage(ColorUtil.color(" &6/give <player> luckykey <amount> &8- &7Give Lucky Keys"));
        sender.sendMessage(ColorUtil.color(" &6/crate reload &8- &7Reload config.yml and crates.yml"));
        sender.sendMessage(ColorUtil.color(" &6/crate list &8- &7View list of placed crates"));
        sender.sendMessage(ColorUtil.color("&8&m----------------------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("place", "color", "name", "rename", "remove", "key", "reload", "list", "help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("color")) {
            return new ArrayList<>(plugin.getCrateManager().getAvailableColors());
        }
        return Collections.emptyList();
    }
}
