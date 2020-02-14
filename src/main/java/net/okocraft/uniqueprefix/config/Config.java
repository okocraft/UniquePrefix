package net.okocraft.uniqueprefix.config;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class Config extends CustomConfig {

    private static final Config INSTANCE = new Config();

    private Config() {
        super("config.yml");
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public String getPrefixFormat() {
        return get().getString("prefix-format", "&([0-9]|[a-f])(\\p{InHiragana}|\\p{InKatakana}|\\p{InCjkUnifiedIdeographs})");
    }

    public String getPrefixSetCommand() {
        return get().getString("prefix-set-command", "lp user %player% meta setprefix 1 %prefix%");
    }

    public String getPrefixRemoveCommand() {
        return get().getString("prefix-remove-command", "lp user %player% meta removeprefix 1 %prefix%");
    }

    public ItemStack getLegendaryTicket() {
        String name = get().getString("legendary-ticket.display-name", "§6§lレジェンダリーチケット");
        List<String> lore = get().getStringList("legendary-ticket.lore");
        return new ItemStack(Material.PAPER) {
            {
                ItemMeta meta = getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(lore);
                setItemMeta(meta);
            }
        };
    }

    /**
     * Reload config. If this method used before {@code JailConfig.save()}, the data
     * on memory will be lost.
     */
    @Override
    public void reload() {
        Bukkit.getOnlinePlayers().forEach(Player::closeInventory);
        super.reload();
    }

    public void reloadAllConfigs() {
        reload();
        Messages.getInstance().reload();
        PrefixData.getInstance().reload();
    }
}