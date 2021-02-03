package net.okocraft.affix.command.prefix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.okocraft.affix.command.BaseCommand;

public final class UnsetCommand extends BaseCommand {

    public UnsetCommand(PrefixCommand parent) {
        super(parent.getPlugin(), "unset", "affix.prefix.unset", 1, true, true, "/prefix unset [--player <name>]");
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        OfflinePlayer player = targetPlayer(sender, parseArgs(argsList, "--player"), "affix.prefix.list.other");
        if (player == null) {
            return false;
        }

        plugin.getLuckpermsAPI().setPrefix(player, null);
        messages.sendSetSuccess(sender, player, "");
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("affix.prefix.unset.other")) {
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