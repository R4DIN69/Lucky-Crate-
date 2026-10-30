package com.luckycrates.listener;

import com.luckycrates.LuckyCratesPlugin;
import com.luckycrates.gui.CrateEditorGUI;
import com.luckycrates.gui.CratePreviewGUI;
import com.luckycrates.gui.SpinnerGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GUIListener implements Listener {
    private final LuckyCratesPlugin plugin;

    public GUIListener(LuckyCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Block interaction in spinner and view-only preview GUI
        if (event.getInventory().getHolder() instanceof SpinnerGUI ||
            event.getInventory().getHolder() instanceof CratePreviewGUI) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SpinnerGUI ||
            event.getInventory().getHolder() instanceof CratePreviewGUI) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof SpinnerGUI spinner) {
            spinner.handleClose();
        } else if (event.getInventory().getHolder() instanceof CrateEditorGUI editor) {
            editor.handleClose((Player) event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getActiveSpinners().remove(event.getPlayer().getUniqueId());
    }
}
