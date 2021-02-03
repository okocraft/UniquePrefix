package net.okocraft.affix.command.prefix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.okocraft.affix.command.BaseCommand;

public final class RemoveCommand extends BaseCommand {

    public RemoveCommand(PrefixCommand parent) {
        super(parent.getPlugin(), "remove", "affix.prefix.remove", 2, true, true, "/prefix remove <prefix|prefixid> [--player <name>]");
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        OfflinePlayer player = targetPlayer(sender, parseArgs(argsList, "--player"), "affix.prefix.list.other");
        if (player == null) {
            return false;
        }

        String prefix = argsList.get(1);

        if (tables.getPrefixes(player).contains(prefix)) {
            if (tables.removePrefix(player, prefix)) {
                messages.sendDoNotHavePrefix(sender);
                return false;
            }
        } else {
            try {
                int prefixId = Integer.parseInt(prefix);
                prefix = tables.getAffixById(prefixId);
                if (tables.removePrefix(player, prefix)) {
                    messages.sendDoNotHavePrefix(sender);
                    return false;
                }
            } catch (NumberFormatException e) {
                messages.sendDoNotHavePrefix(sender);
                return false;
            }
        }

        prefix = config.formatPrefix(prefix);
        plugin.getLuckpermsAPI().setPrefix(player, null);
        messages.sendRemoveSuccess(sender, player, prefix);
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
        Map<String, String> argsMap = parseArgs(argsList, "--player");
        
        if (!sender.hasPermission("affix.prefix.add.other") && argsMap.containsKey("--player")) {
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
                result.addAll(tables.getPrefixes(targetPlayer(sender, argsMap, "affix.prefix.add.other")));
            }
            if (sender.hasPermission("affix.prefix.add.other") && !argsMap.containsKey("--player")) {
                result.add("--player");
            }
            return StringUtil.copyPartialMatches(args[args.length - 1], result, new ArrayList<>());
        }
    }
}