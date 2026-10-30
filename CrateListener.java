package com.luckycrates.listener;

import com.luckycrates.LuckyCratesPlugin;
import com.luckycrates.gui.CrateEditorGUI;
import com.luckycrates.gui.CratePreviewGUI;
import com.luckycrates.gui.SpinnerGUI;
import com.luckycrates.manager.CrateManager;
import com.luckycrates.manager.KeyManager;
import com.luckycrates.util.ColorUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class CrateListener implements Listener {
    private final LuckyCratesPlugin plugin;

    public CrateListener(LuckyCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation();
        if (!plugin.getCrateManager().isCrate(loc)) return;

        Player player = event.getPlayer();
        String crateName = plugin.getCrateManager().getCrateName(loc);
        String crateColor = plugin.getCrateManager().getCrateColor(loc);
        String colorCode = plugin.getCrateManager().getColorCode(crateColor);

        // 1. LEFT CLICK:
        // Creative Mode -> Open Editor
        // Normal Player -> Open Crate Preview
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("luckycrates.admin")) {
                new CrateEditorGUI(plugin, crateName, crateColor).open(player);
            } else {
                new CratePreviewGUI(plugin, colorCode + "&l" + crateName).open(player);
            }
            return;
        }

        // 2. RIGHT CLICK: Open Crate Spinner with Lucky Key
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getHand() != EquipmentSlot.HAND) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true);

            if (!player.hasPermission("luckycrates.use")) return;

            if (plugin.getActiveSpinners().contains(player.getUniqueId())) {
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + "&cYou are already opening a crate!"));
                return;
            }

            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();

            boolean hasKeyInMain = plugin.getKeyManager().isKey(mainHand);
            boolean hasKeyInOff = !hasKeyInMain && plugin.getKeyManager().isKey(offHand);

            if (!hasKeyInMain && !hasKeyInOff) {
                String keyName = plugin.getConfig().getString("key.name", "&6&l★ Lucky Key ★");
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + "&cYou need a " + keyName + " &cto open " + crateName + "&c!"));
                try {
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.8f, 0.8f);
                } catch (Exception ignored) {}
                return;
            }

            // Consume one key
            if (hasKeyInMain) {
                mainHand.setAmount(mainHand.getAmount() - 1);
            } else {
                offHand.setAmount(offHand.getAmount() - 1);
            }

            player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc.clone().add(0.5, 1.0, 0.5), 25, 0.3, 0.3, 0.3, 0.1);

            new SpinnerGUI(plugin, player, crateName, colorCode).start();
        }
    }

    // -------------------------------------------------------------
    // KEY PROTECTIONS: Cannot drop, place, or move to containers
    // -------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getKeyManager().isKey(event.getItemInHand())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + "&cLucky Keys cannot be placed!"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (plugin.getKeyManager().isKey(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + "&cLucky Keys cannot be dropped!"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (plugin.getKeyManager().isKey(item)) {
            if (event.getRightClicked() instanceof ItemFrame || event.getRightClicked() instanceof ArmorStand) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + "&cLucky Keys cannot be placed on entities!"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMove(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean isKeyMoving = plugin.getKeyManager().isKey(current) || plugin.getKeyManager().isKey(cursor);
        if (!isKeyMoving) return;

        if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP || event.getSlotType() == InventoryType.SlotType.OUTSIDE) {
            event.setCancelled(true);
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + "&cLucky Keys cannot be dropped!"));
            return;
        }

        InventoryType topType = event.getView().getTopInventory().getType();
        if (topType != InventoryType.CRAFTING && topType != InventoryType.PLAYER) {
            if (event.getClickedInventory() != null && event.getClickedInventory() == event.getView().getTopInventory()) {
                event.setCancelled(true);
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + "&cLucky Keys cannot be stored in external containers!"));
                return;
            }
            if (event.isShiftClick()) {
                event.setCancelled(true);
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") + "&cLucky Keys cannot be stored in external containers!"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Iterator<ItemStack> iter = event.getDrops().iterator();
        while (iter.hasNext()) {
            ItemStack drop = iter.next();
            if (plugin.getKeyManager().isKey(drop)) {
                iter.remove();
                event.getItemsToKeep().add(drop);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!plugin.getCrateManager().isCrate(block.getLocation())) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                "&cThis is a protected Lucky Crate! Use &e/crate remove &cto remove it."));
    }
}
