package net.okocraft.affix;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PrefixNode;

public class LuckpermsAPI {
    
    private final AffixPlugin plugin;

    private final LuckPerms luckperms;

    LuckpermsAPI(AffixPlugin plugin) {
        this.plugin = plugin;
        luckperms = LuckPermsProvider.get();
    }

    @Nullable
    private User getFrom(OfflinePlayer player) {
        if (player == null) {
            return null;
        }
        return luckperms.getUserManager().getUser(player.getUniqueId());
    }

    public boolean setPrefix(OfflinePlayer player, @Nullable String prefix) {        
        User user = getFrom(player);
        if (user == null) {
            return false;
        }

        int prefixPriority = plugin.getConfigManager().getConfig().getPrefixPriority();

        if (prefix == null) {
            String currentPrefix = user.getCachedData().getMetaData().getPrefixes().get(prefixPriority);
            if (currentPrefix == null) {
                return false;
            }
            return user.data().remove(Node.builder("prefix." + prefixPriority + "." + currentPrefix).build()) == DataMutateResult.SUCCESS;
        }

        return user.data().add(PrefixNode.builder(prefix, prefixPriority).build()) == DataMutateResult.SUCCESS;
    }

    public String getCurrentPrefix(OfflinePlayer player) {
        User user = getFrom(player);
        if (user != null) {
            return user.getCachedData().getMetaData().getPrefix();
        } else {
            return null;
        }
    }

    public String getCurrentSuffix(OfflinePlayer player) {
        User user = getFrom(player);
        if (user != null) {
            return user.getCachedData().getMetaData().getSuffix();
        } else {
            return null;
        }
    }

    public boolean setSuffix(OfflinePlayer player, @Nullable String suffix) {
        User user = getFrom(player);
        if (user == null) {
            return false;
        }

        int prefixPriority = plugin.getConfigManager().getConfig().getPrefixPriority();

        if (suffix == null) {
            String currentPrefix = user.getCachedData().getMetaData().getPrefixes().get(prefixPriority);
            if (currentPrefix == null) {
                return false;
            }
            return user.data().remove(Node.builder("prefix." + prefixPriority + "." + currentPrefix).build()) == DataMutateResult.SUCCESS;
        }

        return user.data().add(PrefixNode.builder(suffix, prefixPriority).build()) == DataMutateResult.SUCCESS;
    }

}
