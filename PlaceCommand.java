package com.luckycrates.command;

import com.luckycrates.manager.CrateManager;

import com.luckycrates.LuckyCratesPlugin;
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

import java.util.Collections;
import java.util.List;

public class PlaceCommand implements CommandExecutor, TabCompleter {
    private final LuckyCratesPlugin plugin;

    public PlaceCommand(LuckyCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can place crates.");
            return true;
        }

        if (!player.hasPermission("luckycrates.admin")) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission", "&cNo permission.")));
            return true;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType().isAir()) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                    "&cYou must look at a solid ground block within 5 blocks!"));
            return true;
        }

        // Place End Rod on ground with Shulker Box on top
        Location crateLoc = plugin.getCrateManager().placeCrateStructure(targetBlock, "&6&lLucky Crate");

        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, crateLoc.clone().add(0.5, 0.8, 0.5), 25, 0.3, 0.3, 0.3, 0.05);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);

        player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                "&aSuccessfully placed Lucky Crate (Shulker Box on End Rod) at &e" +
                crateLoc.getBlockX() + ", " + crateLoc.getBlockY() + ", " + crateLoc.getBlockZ() + "&a!"));
        player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                "&7Use &e/crate name <name> &7to set a custom name for it!"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("crate");
        }
        return Collections.emptyList();
    }
}
