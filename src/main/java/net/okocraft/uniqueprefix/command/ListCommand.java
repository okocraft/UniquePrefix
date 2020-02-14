package net.okocraft.uniqueprefix.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class ListCommand extends BaseCommand {

    ListCommand() {
        super("uniqueprefix.list", 1, true, true, "/uniqueprefix list");
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        OfflinePlayer player;

        if (args.length > 1 && sender.hasPermission("uniqueprefix.other")) {
            player = getOfflinePlayer(args[1]);
            if (!player.hasPlayedBefore() && player.getName() == null) {
                MESSAGES.sendNoPlayerFound(sender, args[1]);
                return false;
            }
        } else if (sender instanceof Player) {
            player = (OfflinePlayer) sender;
        } else {
            MESSAGES.sendNotEnoughArguments(sender);
            return false;
        }

        MESSAGES.sendListHeader(sender, player);
        PREFIX_DATA.getPrefixes(player)
                .forEach(prefix -> MESSAGES.sendListFormattedLine(sender, prefix));

        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2 && sender.hasPermission("uniqueprefix.other")) {
            return StringUtil.copyPartialMatches(args[1], offlinePlayers, new ArrayList<>());
        }

        return List.of();
    }
}