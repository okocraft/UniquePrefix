package net.okocraft.affix.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import net.okocraft.affix.AffixPlugin;
import net.okocraft.affix.config.Config;
import net.okocraft.affix.config.Messages;
import net.okocraft.affix.database.Tables;

public abstract class BaseCommand implements Command {

    @Getter
    private final String name;
    @Getter
    private final String permissionNode;
    @Getter
    private final int leastArgsLength;
    @Getter
    private final boolean playerCommand;
    @Getter
    private final boolean consoleCommand;
    @Getter
    private final String usage;

    protected final AffixPlugin plugin;
    protected final Config config;
    protected final Messages messages;
    protected final Tables tables;

    protected final List<String> offlinePlayers = new ArrayList<>();


    public BaseCommand(AffixPlugin plugin, String name, String permissionNode, int leastArgsLength, boolean playerCommand, boolean consoleCommand, String usage) {
        this.name = name;
        this.permissionNode = permissionNode;
        this.leastArgsLength = leastArgsLength;
        this.playerCommand = playerCommand;
        this.consoleCommand = consoleCommand;
        this.usage = usage;

        this.plugin = plugin;
        this.config = plugin.getConfigManager().getConfig();
        this.messages = plugin.getConfigManager().getMessages();
        this.tables = plugin.getDatabaseTable();

        for (OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
            offlinePlayers.add(offlinePlayer.getName());
        }
    }
    
    public String getDescription() {
        String packageName = getClass().getPackageName();
        packageName = packageName.substring(packageName.lastIndexOf("."), packageName.length());
        return messages.getMessage("command.description." + packageName + "." + getName());
    }

    /**
     * 各コマンドの処理
     *
     * @param sender コマンドの実行者
     * @param args   引数
     * @return コマンドが成功したらtrue
     */
    public boolean runCommand(CommandSender sender, String[] args) {
        return false;
    }

    /**
     * 各コマンドのタブ補完の処理
     *
     * @param sender コマンドの実行者
     * @param args   引数
     * @return その時のタブ補完のリスト
     */
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    /**
     * このコマンドを使う権限があるか調べる。
     * 
     * @param sender 権限があるか調べる対象
     * @return 権限があればtrue なければfalse
     * @see CommandSender#hasPermission(String)
     */
    public boolean hasPermission(CommandSender sender) {
        if (permissionNode == null || permissionNode.isEmpty()) {
            return true;
        }

        return sender.hasPermission(getPermissionNode());
    }

    /**
     * 引数を解析して解析結果をマップにして返す。
     * 
     * @param args 解析する引数のリスト。解析されたキーとその値はこのリストから削除される。
     * @param keys 解析するキー
     * 
     * @return 解析結果のマップ。キーは {@code keys} のキー、値は解析された引数値。
     */
    protected Map<String, String> parseArgs(List<String> args, String ... keys) {
        List<String> keysList = Arrays.asList(keys);
        Map<String, String> argsMap = new HashMap<>();

        Iterator<String> argsIt = args.iterator();
        while (argsIt.hasNext()) {
            String arg = argsIt.next();
            if (arg.startsWith("-") && keysList.contains(arg.toLowerCase(Locale.ROOT))) {
                argsIt.remove();
                while (argsIt.hasNext()) {
                    String nextArg = argsIt.next();
                    if (nextArg.startsWith("-") && keysList.contains(arg.toLowerCase(Locale.ROOT))) {
                        argsIt.remove();
                        arg = nextArg;
                    } else {
                        argsMap.put(arg, nextArg);
                        argsIt.remove();
                        break;
                    }
                }
            }
        }

        return argsMap;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    protected OfflinePlayer targetPlayer(CommandSender sender, Map<String, String> argsMap, String otherPlayerPermission) {
        OfflinePlayer result;
        if (argsMap.containsKey("--player")) {
            if (!sender.hasPermission(otherPlayerPermission)) {
                messages.sendNoPermission(sender, otherPlayerPermission);
                return null;
            }
            result = plugin.getServer().getOfflinePlayer(argsMap.get("--player"));
        } else if (sender instanceof Player) {
            result = (Player) sender;
        } else {
            messages.sendNotEnoughArguments(sender);
            return null;
        }

        if (tables.getPlayerIdByUUID(result.getUniqueId()) == -1) {
            messages.sendNoPlayerFound(sender, result.getName());
            return null;
        }

        return result;
    }

    
    private List<String> allPlayerNameCache;
    
    public List<String> getAllPlayerNameCache() {
        if (allPlayerNameCache == null) {
            allPlayerNameCache = new ArrayList<>(tables.getAllPlayerName());
        }
        return new ArrayList<>(allPlayerNameCache);
    }
}