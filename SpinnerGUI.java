package com.luckycrates.gui;

import com.luckycrates.LuckyCratesPlugin;
import com.luckycrates.manager.CrateManager;
import com.luckycrates.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SpinnerGUI implements InventoryHolder {
    private final LuckyCratesPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<ItemStack> tape = new ArrayList<>();
    private final int totalShifts = 38;
    private int currentShift = 0;
    private int currentIndex = 0;
    private boolean finished = false;
    private BukkitTask task;
    private ItemStack predeterminedWinner;

    private static final int WIN_SLOT = 13;
    private static final int TOP_POINTER_SLOT = 4;
    private static final int BOTTOM_POINTER_SLOT = 22;

    public SpinnerGUI(LuckyCratesPlugin plugin, Player player) {
        this(plugin, player, "Lucky Crate", "&6");
    }

    public SpinnerGUI(LuckyCratesPlugin plugin, Player player, String crateName, String colorCode) {
        this.plugin = plugin;
        this.player = player;

        String title = ColorUtil.color("&8» " + colorCode + "&l" + crateName + " &8«");
        this.inventory = Bukkit.createInventory(this, 27, title);

        buildWeightedTape();
        setupInventory();
    }

    private void buildWeightedTape() {
        this.predeterminedWinner = plugin.getCrateManager().pickWeightedReward();

        List<ItemStack> allRewards = plugin.getCrateManager().getAllRewardItems();
        if (allRewards.isEmpty()) {
            allRewards.add(new ItemStack(Material.DIRT));
        }

        for (int i = 0; i < totalShifts + 15; i++) {
            ItemStack randomDecoy = allRewards.get(ThreadLocalRandom.current().nextInt(allRewards.size()));
            tape.add(randomDecoy.clone());
        }

        int winningTapeIndex = totalShifts + 4;
        if (winningTapeIndex < tape.size()) {
            tape.set(winningTapeIndex, predeterminedWinner.clone());
        } else {
            tape.add(predeterminedWinner.clone());
        }
    }

    private void setupInventory() {
        Material borderMat = Material.matchMaterial(plugin.getConfig().getString("spinner.border-material", "BLACK_STAINED_GLASS_PANE"));
        if (borderMat == null) borderMat = Material.BLACK_STAINED_GLASS_PANE;

        Material pointerMat = Material.matchMaterial(plugin.getConfig().getString("spinner.pointer-material", "ORANGE_STAINED_GLASS_PANE"));
        if (pointerMat == null) pointerMat = Material.ORANGE_STAINED_GLASS_PANE;

        ItemStack border = createPane(borderMat, " ");
        ItemStack topPointer = createPane(pointerMat, "§6§l▼ PRIZE POINTER ▼");
        ItemStack bottomPointer = createPane(pointerMat, "§6§l▲ PRIZE POINTER ▲");

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(i + 18, border);
        }

        inventory.setItem(TOP_POINTER_SLOT, topPointer);
        inventory.setItem(BOTTOM_POINTER_SLOT, bottomPointer);

        updateRowItems();
    }

    private ItemStack createPane(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void updateRowItems() {
        for (int i = 0; i < 9; i++) {
            int tapeIndex = currentIndex + i;
            if (tapeIndex < tape.size()) {
                inventory.setItem(9 + i, tape.get(tapeIndex));
            }
        }
    }

    public void start() {
        player.openInventory(inventory);
        plugin.getActiveSpinners().add(player.getUniqueId());
        runStep();
    }

    private void runStep() {
        if (finished || !player.isOnline()) return;

        long delay = getDelayForShift(currentShift);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (finished || !player.isOnline()) return;

                currentShift++;
                currentIndex++;
                updateRowItems();
                playTickSound();

                if (currentShift >= totalShifts) {
                    complete();
                } else {
                    runStep();
                }
            }
        }.runTaskLater(plugin, delay);
    }

    private long getDelayForShift(int shift) {
        if (shift < 18) return 1L;
        if (shift < 26) return 2L;
        if (shift < 30) return 3L;
        if (shift < 33) return 5L;
        if (shift < 35) return 7L;
        if (shift < 37) return 10L;
        return 14L;
    }

    private void playTickSound() {
        try {
            String soundName = plugin.getConfig().getString("spinner.sounds.tick", "BLOCK_NOTE_BLOCK_PLING");
            float pitch = (float) plugin.getConfig().getDouble("spinner.sounds.tick-pitch", 1.2);
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 0.7f, pitch);
        } catch (Exception ignored) {}
    }

    private void complete() {
        if (finished) return;
        finished = true;

        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

        ItemStack rawWinner = predeterminedWinner != null ? predeterminedWinner.clone() : tape.get(currentIndex + 4).clone();

        Material winPointerMat = Material.matchMaterial(plugin.getConfig().getString("spinner.win-pointer-material", "LIME_STAINED_GLASS_PANE"));
        if (winPointerMat == null) winPointerMat = Material.LIME_STAINED_GLASS_PANE;

        ItemStack winPointer = createPane(winPointerMat, "§a§l★ WINNER! ★");
        inventory.setItem(TOP_POINTER_SLOT, winPointer);
        inventory.setItem(BOTTOM_POINTER_SLOT, winPointer);

        try {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.0f);
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1.2, 0), 35, 0.5, 0.5, 0.5, 0.15);
            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1.5, 0), 25, 0.3, 0.3, 0.3, 0.1);
        } catch (Exception ignored) {}

        givePrize(rawWinner);

        String rawName = rawWinner.hasItemMeta() && rawWinner.getItemMeta().hasDisplayName()
                ? rawWinner.getItemMeta().getDisplayName()
                : rawWinner.getType().name().replace('_', ' ');

        String cleanName = rawName.replaceAll("(?i)(★[^★]+★\s*)+", "").replaceAll("&[0-9a-fk-or]", "").trim();
        if (cleanName.isEmpty()) cleanName = rawWinner.getType().name().replace('_', ' ');

        // TIGHT TITLE SPACING (letters close together, bold and clean!)
        player.sendTitle(
                ColorUtil.color("&6&l★ &e&lPRIZE WON! &6&l★"),
                ColorUtil.color("&a&l» &f&l" + cleanName + " &a&l«"),
                10, 45, 15
        );

        String winMsg = "&8[&6&lLuckyCrates&8] &a&l★ CONGRATULATIONS! ★ &7You won: &e&l" + cleanName + " &7(x&e" + rawWinner.getAmount() + "&7)!";
        player.sendMessage(ColorUtil.color(winMsg));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getActiveSpinners().remove(player.getUniqueId());
            if (player.getOpenInventory().getTopInventory().getHolder() == this) {
                player.closeInventory();
            }
        }, 40L);
    }

    public void handleClose() {
        if (!finished) {
            finished = true;
            if (task != null && !task.isCancelled()) task.cancel();
            givePrize(predeterminedWinner != null ? predeterminedWinner.clone() : new ItemStack(Material.DIRT));
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                    "&eYour crate opening was saved! Your prize was added to your inventory."));
        }
        plugin.getActiveSpinners().remove(player.getUniqueId());
    }

    private void givePrize(ItemStack item) {
        ItemStack cleanItem = stripRarityLore(item);

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(cleanItem);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix", "") +
                    "&eYour inventory was full, so your prize dropped on the ground!"));
        }
    }

    private ItemStack stripRarityLore(ItemStack original) {
        if (original == null || original.getType() == Material.AIR) return original;
        ItemStack item = original.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());
            lore.removeIf(line -> line.contains("Rarity:") || line.contains("✦") || line.contains("------------------------"));
            meta.setLore(lore.isEmpty() ? null : lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
