package com.luckycrates.manager;

import com.luckycrates.LuckyCratesPlugin;
import com.luckycrates.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class KeyManager {
    private final LuckyCratesPlugin plugin;
    private final NamespacedKey keyPdcKey;

    public KeyManager(LuckyCratesPlugin plugin) {
        this.plugin = plugin;
        this.keyPdcKey = new NamespacedKey(plugin, "lucky_crate_key");
    }

    public ItemStack createKey(int amount) {
        String matName = plugin.getConfig().getString("key.material", "TRIPWIRE_HOOK");
        Material material = Material.matchMaterial(matName);
        if (material == null) {
            material = Material.TRIPWIRE_HOOK;
        }

        ItemStack item = new ItemStack(material, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString("key.name", "&6&l★ Lucky Key ★");
            meta.setDisplayName(ColorUtil.color(name));

            List<String> rawLore = plugin.getConfig().getStringList("key.lore");
            List<String> lore = new ArrayList<>();
            for (String line : rawLore) {
                lore.add(ColorUtil.color(line));
            }
            meta.setLore(lore);

            if (plugin.getConfig().getBoolean("key.glow", true)) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            // Secure PersistentDataContainer tag to prevent fake renamed keys
            meta.getPersistentDataContainer().set(keyPdcKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(keyPdcKey, PersistentDataType.BYTE);
    }

    public NamespacedKey getKeyPdcKey() {
        return keyPdcKey;
    }
}
