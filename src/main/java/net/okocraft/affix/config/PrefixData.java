package net.okocraft.affix.config;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.okocraft.affix.AffixPlugin;

/**
 * @deprecated UniquePrefixからの移行のためだけに残しているクラス。それ以外の目的で使われるべきではない。
 */
@Deprecated
public final class PrefixData extends CustomConfig {

    private static final PrefixData INSTANCE = new PrefixData();

    private PrefixData() {
        super(AffixPlugin.getPlugin(AffixPlugin.class), "data.yml");
    }

    public static PrefixData getInstance() {
        return INSTANCE;
    }

    public boolean addPrefix(OfflinePlayer player, String prefix, boolean force) {
        if (isUsed(prefix) && !force) {
            return false;
        }

        List<String> prefixes = getPrefixes(player);
        if (prefixes.contains(prefix)) {
            return false;
        }
        prefixes.add(prefix);
        setPrefixes(player, prefixes, force);
        return true;
    }

    public boolean removePrefix(OfflinePlayer player, String prefix) {
        List<String> prefixes = getPrefixes(player);
        if (!prefixes.remove(prefix)) {
            return false;
        }
        setPrefixes(player, prefixes, true);
        return true;
    }

    public void setPrefixes(OfflinePlayer player, List<String> prefixes, boolean force) {
        if (!force) {
            Set<String> all = getAllPrefix();
            all.removeAll(getPrefixes(player));
            prefixes.removeAll(all);
        }

        get().set(player.getUniqueId().toString(), prefixes);
        save();
    }

    public OfflinePlayer getPlayerOwningPrefix(String prefix) {
        for (OfflinePlayer player : getPlayerOwningPrefix()) {
            if (getPrefixes(player).contains(prefix)) {
                return player;
            }
        }

        return null;
    }

    public Set<OfflinePlayer> getPlayerOwningPrefix() {
        return get().getKeys(false).stream().map(uuid -> {
            try {
                return UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }).filter(Objects::nonNull).map(Bukkit::getOfflinePlayer).filter(OfflinePlayer::hasPlayedBefore)
                .collect(Collectors.toSet());
    }

    public List<String> getPrefixes(OfflinePlayer player) {
        return get().getStringList(player.getUniqueId().toString());
    }

    public boolean isUsed(String prefix) {
        return getAllPrefix().contains(prefix);
    }

    public Set<String> getAllPrefix() {
        return getPlayerOwningPrefix().stream().flatMap(player -> getPrefixes(player).stream())
                .collect(Collectors.toSet());
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
}