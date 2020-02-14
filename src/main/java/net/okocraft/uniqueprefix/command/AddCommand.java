package net.okocraft.uniqueprefix.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.uniqueprefix.config.Config;

public final class AddCommand extends BaseCommand {

    AddCommand() {
        super(
                "uniqueprefix.add",
                2,
                true,
                true,
                "/uniqueprefix add <prefix>"
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        OfflinePlayer player;
        String prefix;

        // /uniqueprefix add player prefix
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

        if (!prefix.matches(Config.getInstance().getPrefixFormat())) {
            MESSAGES.sendInvalidPrefixSyntax(sender);
            return false;
        }
        
        if (player == sender && !player.getPlayer().getInventory().removeItem(Config.getInstance().getLegendaryTicket()).isEmpty()) {
            MESSAGES.sendNoLegendaryTicket(sender);
            return false;
        }

        if (!PREFIX_DATA.addPrefix(player, prefix, false)) {
            MESSAGES.sendPrefixIsInUse(sender);
            return false;
        }

        prefix = "&7[" + prefix + "&7]";
        String prefixCommand = Config.getInstance().getPrefixSetCommand().replace("%player%", player.getUniqueId().toString())
                .replace("%prefix%", prefix);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), prefixCommand);
        MESSAGES.sendPrefixAddSuccess(sender, player, prefix);
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        if (sender.hasPermission("uniqueprefix.other")) {
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1], offlinePlayers, new ArrayList<>());
            }
            
            if (args.length == 3) {
                return StringUtil.copyPartialMatches(args[2], List.of("&fあ", "&9怒"), new ArrayList<>());
            }
        } else {            
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[2], List.of("&fあ", "&9怒"), new ArrayList<>());
            }
        }

        return List.of();
    }
}