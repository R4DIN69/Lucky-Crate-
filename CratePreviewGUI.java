package com.luckycrates.gui;

import com.luckycrates.manager.CrateManager;

import com.luckycrates.LuckyCratesPlugin;
import com.luckycrates.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CratePreviewGUI implements InventoryHolder {
    private final Inventory inventory;

    public CratePreviewGUI(LuckyCratesPlugin plugin, String crateName) {
        // Normal players see just the crate name at the top
        String title = ColorUtil.color(crateName);
        this.inventory = Bukkit.createInventory(this, 54, title);

        Map<Integer, ItemStack> rewards = plugin.getCrateManager().getSlotRewards();
        for (Map.Entry<Integer, ItemStack> entry : rewards.entrySet()) {
            if (entry.getKey() < 54 && entry.getValue() != null) {
                inventory.setItem(entry.getKey(), entry.getValue().clone());
            }
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 0.8f, 1.2f);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
