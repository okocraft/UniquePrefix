package net.okocraft.affix.command.suffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.okocraft.affix.command.BaseCommand;

public final class RemoveCommand extends BaseCommand {

    public RemoveCommand(SuffixCommand parent) {
        super(parent.getPlugin(), "remove", "affix.suffix.remove", 2, true, true, "/suffix remove <suffix|suffixid> [--player <name>]");
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        OfflinePlayer player = targetPlayer(sender, parseArgs(argsList, "--player"), "affix.suffix.list.other");
        if (player == null) {
            return false;
        }

        String suffix = argsList.get(1);

        if (tables.getSuffixes(player).contains(suffix)) {
            if (tables.removeSuffix(player, suffix)) {
                messages.sendDoNotHaveSuffix(sender);
                return false;
            }
        } else {
            try {
                int suffixId = Integer.parseInt(suffix);
                suffix = tables.getAffixById(suffixId);
                if (tables.removeSuffix(player, suffix)) {
                    messages.sendDoNotHaveSuffix(sender);
                    return false;
                }
            } catch (NumberFormatException e) {
                messages.sendDoNotHaveSuffix(sender);
                return false;
            }
        }

        suffix = config.formatSuffix(suffix);
        plugin.getLuckpermsAPI().setSuffix(player, null);
        messages.sendRemoveSuccess(sender, player, suffix);
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
        Map<String, String> argsMap = parseArgs(argsList, "--player");
        
        if (!sender.hasPermission("affix.suffix.add.other") && argsMap.containsKey("--player")) {
            return new ArrayList<>();
        }

        String previousArg = argsList.get(argsList.size() - 2);
        if (previousArg.startsWith("--")) {
            if (previousArg.equalsIgnoreCase("--player") && !argsMap.containsKey("--player")) {
                return StringUtil.copyPartialMatches(args[args.length - 1], getAllPlayerNameCache(), new ArrayList<>());
            } else {
                return new ArrayList<>();
            }
        } else {
            List<String> result = new ArrayList<>();
            if (argsList.size() == 0) {
                result.addAll(tables.getSuffixes(targetPlayer(sender, argsMap, "affix.suffix.add.other")));
            }
            if (sender.hasPermission("affix.suffix.add.other") && !argsMap.containsKey("--player")) {
                result.add("--player");
            }
            return StringUtil.copyPartialMatches(args[args.length - 1], result, new ArrayList<>());
        }
    }
}