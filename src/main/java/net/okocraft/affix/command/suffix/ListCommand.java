package net.okocraft.affix.command.suffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.okocraft.affix.command.BaseCommand;

public final class ListCommand extends BaseCommand {

    public ListCommand(SuffixCommand parent) {
        super(parent.getPlugin(), "list", "affix.suffix.list", 1, true, true, "/suffix list [--player <name>]");
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        Map<String, String> argsMap = parseArgs(new ArrayList<>(Arrays.asList(args)), "--player");
        
        OfflinePlayer player = targetPlayer(sender, argsMap, "affix.suffix.list.other");
        if (player == null) {
            return false;
        }

        messages.sendListHeader(sender, player);
        tables.getSuffixes(player).forEach(suffix -> messages.sendListFormattedLine(sender, suffix));

        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("affix.suffix.list.other")) {
            return new ArrayList<>();
        }

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], Arrays.asList("--player"), new ArrayList<>());
        }

        if (!args[1].equalsIgnoreCase("--player")) {
            return new ArrayList<>();
        }

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], getAllPlayerNameCache(), new ArrayList<>());
        }

        return new ArrayList<>();
    }
}