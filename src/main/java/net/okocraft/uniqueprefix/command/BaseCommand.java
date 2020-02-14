package net.okocraft.uniqueprefix.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import net.okocraft.uniqueprefix.UniquePrefix;
import net.okocraft.uniqueprefix.config.Messages;
import net.okocraft.uniqueprefix.config.PrefixData;

public abstract class BaseCommand {

    static final UniquePrefix PLUGIN = UniquePrefix.getInstance();
    static final Messages MESSAGES = Messages.getInstance();
    static final PrefixData PREFIX_DATA = PrefixData.getInstance();

    // For tab complete
    final List<String> offlinePlayers = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;

        {
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.getName() == null) {
                    continue;
                }

                add(player.getName());
            }
        }
    };

    private final String permissionNode;
    private final int leastArgLength;
    private final boolean playerCommand;
    private final boolean consoleCommand;
    private final String usage;

    BaseCommand(String permissionNode, int leastArgLength, boolean playerCommand, boolean consoleCommand, String usage) {
        this.permissionNode = permissionNode;
        this.leastArgLength = leastArgLength;
        this.playerCommand = playerCommand;
        this.consoleCommand = consoleCommand;
        this.usage = usage;
    }

    /**
     * 各コマンドの処理
     *
     * @param sender コマンドの実行者
     * @param args   引数
     * @return コマンドが成功したらtrue
     */
    public abstract boolean runCommand(CommandSender sender, String[] args);

    /**
     * 各コマンドのタブ補完の処理
     *
     * @param sender コマンドの実行者
     * @param args   引数
     * @return その時のタブ補完のリスト
     */
    public abstract List<String> runTabComplete(CommandSender sender, String[] args);

    /**
     * コマンドの名前を取得する。
     *
     * @return コマンドの名前
     */
    public String getName() {
        String className = this.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        return className.substring(0, className.length() - "command".length());
    }

    /**
     * このコマンドの権限を取得する。
     *
     * @return 権限
     */
    public String getPermissionNode() {
        return permissionNode;
    }

    /**
     * プレイヤーが使用可能なコマンドかどうかを取得する
     *
     * @return プレイヤーが使用可能なコマンドだったらtrue, さもなくばfalse
     */
    public boolean isPlayerCommand() {
        return playerCommand;
    }

    /**
     * コンソールが使用可能なコマンドかどうかを取得する
     *
     * @return コンソールが使用可能なコマンドだったらtrue, さもなくばfalse
     */
    public boolean isConsoleCommand() {
        return consoleCommand;
    }

    /**
     * 最低限必要な引数の長さを取得する。
     *
     * @return 最低限の引数の長さ
     */
    public int getLeastArgLength() {
        return leastArgLength;
    }

    /**
     * コマンドの引数の内容を取得する。例: "/box autostoreList [page]"
     *
     * @return 引数の内容
     */
    public String getUsage() {
        return usage;
    }

    /**
     * コマンドの説明を取得する。例: "アイテムの自動収納の設定をリストにして表示する。"
     *
     * @return コマンドの説明
     */
    public String getDescription() {
        return MESSAGES.getMessage("command.description." + getName());
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

    protected OfflinePlayer getOfflinePlayer(String name) {
        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        return offlinePlayer;
    }
}