package net.okocraft.affix.command.prefix;

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

    AddCommand(PrefixCommand parent) {
        super(parent.getPlugin(), "add", "affix.prefix.add", 2, true, true, "/prefix add <prefix> [--player <name>] [--expire <unixtime>] [--unique <boolean>]");
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        Map<String, String> argsMap = parseArgs(argsList, "--player", "--expire", "--unique");
        
        String prefix = argsList.get(1);

        OfflinePlayer player = targetPlayer(sender, argsMap, "affix.prefix.add.other");
        if (player == null) {
            return false;
        }

        Long expire = null;
        boolean isUnique = false;

        if (argsMap.containsKey("--expire")) {
            if (!sender.hasPermission("affix.prefix.add.expire")) {
                messages.sendNoPermission(sender, "affix.prefix.add.expire");
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
            if (!sender.hasPermission("affix.prefix.add.unique")) {
                messages.sendNoPermission(sender, "affix.prefix.add.unique");
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

        if (!sender.hasPermission("affix.prefix.add.bypassprice") && sender instanceof Player) {
            Price<?> price = config.getPrefixPrice();
            if (!price.has((Player) sender) && !config.getPrefixPrice().pay((Player) sender)) {
                messages.sendYouCannotBuyIt(sender, price);
                return false;
            }
        }

        if (!config.matchRegexPrefixConditions(prefix)) {
            // TODO: 適切なメッセージ。
            messages.sendInvalidPrefixSyntax(sender);
            return false;
        }

        if (ChatColor.stripColor(prefix).length() > config.getMaxPrefixLength()) {
            // TODO: 適切なメッセージ。
            messages.sendInvalidPrefixSyntax(sender);
            return false;
        }

        if (!tables.isAvailablePrefix(prefix, isUnique)) {
            messages.sendPrefixIsInUse(sender);
            return false;
        }

        boolean isAdded;
        if (expire != null) {
            isAdded = tables.addPrefix(player, prefix, expire, isUnique);
        } else {
            isAdded = tables.addPrefix(player, prefix, isUnique);
        }

        if (!isAdded) {
            // TODO; 汎用エラーメッセージを作る。
            // messages.sendError
            return false;
        }

        plugin.getLuckpermsAPI().setPrefix(player, config.formatPrefix(prefix));
        messages.sendPrefixAddSuccess(sender, player, prefix);
        return true;
    }

    /**
     * 基本的に、引数ありのときの補完と引数なしのときの補完を同時に表示すれば良い？
     * 次に、引数なしのときの補完が実行されているときにどのようにして引数ありの補完をやればよいか。
     * 
     * 
     * 
     * 
     * 
     * yukimi usagi soukasi
     * icetak matida? ootaku
     * 
     * 
     * 
     */
    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
        Map<String, String> argsMap = parseArgs(argsList, "--player", "--expire", "--unique");

        if (!sender.hasPermission("affix.prefix.add.other") && argsMap.containsKey("--player")
                || !sender.hasPermission("affix.prefix.add.expire") && argsMap.containsKey("--expire")
                || !sender.hasPermission("affix.prefix.add.unique") && argsMap.containsKey("--unique")) {
            return new ArrayList<>();
        }

        if (args.length >= 2) {
            String previousArg = args[args.length - 2];
            if (previousArg.startsWith("-")) {
                if (previousArg.equalsIgnoreCase("--player")) {
                    return StringUtil.copyPartialMatches(args[args.length - 1], getAllPlayerNameCache(), new ArrayList<>());
                } else if (previousArg.equalsIgnoreCase("--expire")) {
                    return StringUtil.copyPartialMatches(args[args.length - 1],
                    Arrays.asList(String.valueOf(System.currentTimeMillis())), new ArrayList<>());
                } else if (previousArg.equalsIgnoreCase("--unique")) {
                    return StringUtil.copyPartialMatches(args[args.length - 1], Arrays.asList("true", "false"),
                    new ArrayList<>());
                } else {
                    return new ArrayList<>();
                }
            }
        }

        List<String> result = new ArrayList<>();
        if (argsList.size() == 1) {
            result.add("<prefix>");
        }
        if (sender.hasPermission("affix.prefix.add.other") && !argsMap.containsKey("--player")) {
            result.add("--player");
        }
        if (sender.hasPermission("affix.prefix.add.expire") && !argsMap.containsKey("--expire")) {
            result.add("--expire");
        }
        if (sender.hasPermission("affix.prefix.add.unique") && !argsMap.containsKey("--unique")) {
            result.add("--unique");
        }
        return StringUtil.copyPartialMatches(args[args.length - 1], result, new ArrayList<>());
    }
}