package net.okocraft.uniqueprefix.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.uniqueprefix.config.Config;
import net.okocraft.uniqueprefix.config.PrefixData;

public final class RemoveCommand extends BaseCommand {

    RemoveCommand() {
        super(
                "uniqueprefix.remove",
                2,
                true,
                true,
                "/uniqueprefix remove <prefix>"
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        OfflinePlayer player;
        String prefix;

        if (args.length > 2 && sender.hasPermission("uniqueprefix.other")) {
            player = getOfflinePlayer(args[1]);
            if (!player.hasPlayedBefore() && player.getName() == null) {
                MESSAGES.sendNoPlayerFound(sender, args[1]);
                return false;
            }
            prefix = args[2];
        } else if (sender instanceof Player) {
            player = (OfflinePlayer) sender;
            prefix = args[1];
        } else {
            MESSAGES.sendNotEnoughArguments(sender);
            return false;
        }

        if (!PREFIX_DATA.removePrefix(player, prefix)) {
            MESSAGES.sendDoNotHavePrefix(sender);
            return false;
        }

        prefix = "&7[" + prefix + "&7]";
        String prefixCommand = Config.getInstance().getPrefixRemoveCommand().replace("%player%", player.getUniqueId().toString())
                .replace("%prefix%", prefix);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), prefixCommand);
        MESSAGES.sendRemoveSuccess(sender, player, prefix);
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        if (sender.hasPermission("uniqueprefix.other")) {
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1], offlinePlayers, new ArrayList<>());
            }

            OfflinePlayer offlinePlayer = getOfflinePlayer(args[1]);
            if (offlinePlayer.getName() == null) {
                return List.of();
            }
            
            if (args.length == 3) {
                return StringUtil.copyPartialMatches(args[2], PrefixData.getInstance().getPrefixes(offlinePlayer), new ArrayList<>());
            }
        } else {            
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[2], PrefixData.getInstance().getPrefixes((OfflinePlayer) sender), new ArrayList<>());
            }
        }

        return List.of();
    }
}