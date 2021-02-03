package net.okocraft.affix.command.suffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.affix.affixprice.Price;
import net.okocraft.affix.command.BaseCommand;

public final class AddCommand extends BaseCommand {

    AddCommand(SuffixCommand parent) {
        super(parent.getPlugin(), "add", "affix.suffix.add", 2, true, true, "/suffix add <suffix> [--player <name>] [--expire <unixtime>] [--unique <boolean>]");
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        Map<String, String> argsMap = parseArgs(argsList, "--player", "--expire", "--unique");
        
        String suffix = argsList.get(1);

        OfflinePlayer player = targetPlayer(sender, argsMap, "affix.suffix.add.other");
        if (player == null) {
            return false;
        }

        Long expire = null;
        boolean isUnique = false;

        if (argsMap.containsKey("--expire")) {
            if (!sender.hasPermission("affix.suffix.add.expire")) {
                messages.sendNoPermission(sender, "affix.suffix.add.expire");
                return false;
            }
            String expireStr = argsMap.get("--expire");
            try {
                expire = Long.parseLong(expireStr);
            } catch (NumberFormatException e) {
                messages.sendInvalidArgument(sender, expireStr);
                return false;
            }
        }
        
        if (argsMap.containsKey("--unique")) {
            if (!sender.hasPermission("affix.suffix.add.unique")) {
                messages.sendNoPermission(sender, "affix.suffix.add.unique");
                return false;
            }
            String uniqueStr = argsMap.get("--unique");
            try {
                isUnique = Boolean.parseBoolean(uniqueStr);
            } catch (IllegalArgumentException e) {
                messages.sendInvalidArgument(sender, uniqueStr);
                return false;
            }
        }

        if (!sender.hasPermission("affix.suffix.add.bypassprice") && sender instanceof Player) {
            Price<?> price = config.getPrefixPrice();
            if (!price.has((Player) sender) && !config.getPrefixPrice().pay((Player) sender)) {
                messages.sendYouCannotBuyIt(sender, price);
                return false;
            }
        }

        if (!config.matchRegexSuffixConditions(suffix)) {
            // TODO: 適切なメッセージを作る。
            messages.sendInvalidSuffixSyntax(sender);
            return false;
        }
        
        if (ChatColor.stripColor(suffix).length() > config.getMaxSuffixLength()) {
            // TODO; 適切なメッセージを作る。
            messages.sendInvalidSuffixSyntax(sender);
            return false;
        }

        if (!tables.isAvailableSuffix(suffix, isUnique)) {
            messages.sendSuffixIsInUse(sender);
            return false;
        }

        boolean isAdded;
        if (expire != null) {
            isAdded = tables.addSuffix(player, suffix, expire, isUnique);
        } else {
            isAdded = tables.addSuffix(player, suffix, isUnique);
        }

        if (!isAdded) {
            // TODO; 汎用エラーメッセージを作る。
            // messages.sendError
            return false;
        }

        plugin.getLuckpermsAPI().setSuffix(player, config.formatSuffix(suffix));
        messages.sendSuffixAddSuccess(sender, player, suffix);
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
        Map<String, String> argsMap = parseArgs(argsList, "--player", "--expire", "--unique");
        
        if (
            !sender.hasPermission("affix.suffix.add.other") && argsMap.containsKey("--player") || 
            !sender.hasPermission("affix.suffix.add.expire") && argsMap.containsKey("--expire") || 
            !sender.hasPermission("affix.suffix.add.unique") && argsMap.containsKey("--unique")
        ) {
            return new ArrayList<>();
        }

        String previousArg = argsList.get(argsList.size() - 2);
        if (previousArg.startsWith("--")) {
            if (previousArg.equalsIgnoreCase("--player") && !argsMap.containsKey("--player")) {
                return StringUtil.copyPartialMatches(args[args.length - 1], getAllPlayerNameCache(), new ArrayList<>());
            } else if (previousArg.equalsIgnoreCase("--expire") && !argsMap.containsKey("--expire")) {
                return StringUtil.copyPartialMatches(args[args.length - 1], Arrays.asList(String.valueOf(System.currentTimeMillis())), new ArrayList<>());
            } else if (previousArg.equalsIgnoreCase("--unique") && !argsMap.containsKey("--unique")) {
                return StringUtil.copyPartialMatches(args[args.length - 1], Arrays.asList("true", "false"), new ArrayList<>());
            } else {
                return new ArrayList<>();
            }
        } else {
            List<String> result = new ArrayList<>();
            if (argsList.size() == 0) {
                result.add("<suffix>");
            }
            if (sender.hasPermission("affix.suffix.add.other") && !argsMap.containsKey("--player")) {
                result.add("--player");
            }
            if (sender.hasPermission("affix.suffix.add.expire") && !argsMap.containsKey("--expire")) {
                result.add("--expire");
            }
            if (sender.hasPermission("affix.suffix.add.unique") && !argsMap.containsKey("--unique")) {
                result.add("--unique");
            }
            return StringUtil.copyPartialMatches(args[args.length - 1], result, new ArrayList<>());
        }
    }
}