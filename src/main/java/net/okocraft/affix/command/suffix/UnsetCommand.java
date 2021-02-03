package net.okocraft.affix.command.suffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.okocraft.affix.command.BaseCommand;

public final class UnsetCommand extends BaseCommand {

    public UnsetCommand(SuffixCommand parent) {
        super(parent.getPlugin(), "unset", "affix.suffix.unset", 1, true, true, "/suffix unset [--player <name>]");
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        OfflinePlayer player = targetPlayer(sender, parseArgs(argsList, "--player"), "affix.suffix.list.other");
        if (player == null) {
            return false;
        }

        plugin.getLuckpermsAPI().setSuffix(player, null);
        messages.sendSetSuccess(sender, player, "");
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("affix.suffix.unset.other")) {
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