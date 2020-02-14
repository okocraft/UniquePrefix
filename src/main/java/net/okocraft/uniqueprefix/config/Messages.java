package net.okocraft.uniqueprefix.config;

import java.util.Map;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import net.okocraft.uniqueprefix.command.BaseCommand;

public final class Messages extends CustomConfig {

    private static final Messages INSTANCE = new Messages();

    private Messages() {
        super("messages.yml");
    }

    public static Messages getInstance() {
        return INSTANCE;
    }

    /**
     * Send message to player.
     *
     * @param sender       メッセージを送る対象
     * @param addPrefix    プラグインのプレフィックスをメッセージの先頭に追加するかどうか
     * @param path         メッセージのパス
     * @param placeholders メッセージ中のプレースホルダーの値
     */
    public void sendMessage(CommandSender sender, boolean addPrefix, String path, Map<String, Object> placeholders) {
        String prefix = addPrefix ? get().getString("plugin-prefix", "&8[&6PlotGUI&8]&r") + " " : "";
        String message = prefix + getMessage(path);
        for (Map.Entry<String, Object> placeholder : placeholders.entrySet()) {
            message = message.replace(placeholder.getKey(), placeholder.getValue().toString());
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    /**
     * Send message to player.
     *
     * @param sender       メッセージを送る対象
     * @param path         メッセージのパス
     * @param placeholders メッセージ中のプレースホルダーの値
     */
    public void sendMessage(CommandSender sender, String path, Map<String, Object> placeholders) {
        sendMessage(sender, true, path, placeholders);
    }

    /**
     * Send message to player.
     *
     * @param sender メッセージを送る対象
     * @param path   メッセージのパス
     */
    public void sendMessage(CommandSender sender, String path) {
        sendMessage(sender, path, Map.of());
    }

    /**
     * Send message to player.
     *
     * @param sender    メッセージを送る対象
     * @param addPrefix プラグインのプレフィックスをメッセージの先頭に追加するかどうか
     * @param path      メッセージのパス
     */
    public void sendMessage(CommandSender sender, boolean addPrefix, String path) {
        sendMessage(sender, addPrefix, path, Map.of());
    }

    /**
     * Gets message from key. Returned messages will not translated its color code.
     *
     * @param path メッセージのパス
     * @return カラーコードを置換されていないメッセージ
     */
    public String getMessage(String path) {
        return get().getString(path, path);
    }

    public void sendInvalidArgument(CommandSender sender, String invalid) {
        sendMessage(sender, "command.error.invalid-argument", Map.of("%argument%", invalid));
    }

    public void sendNoPermission(CommandSender sender, String permission) {
        sendMessage(sender, "command.error.no-permission", Map.of("%permission%", permission));
    }

    public void sendConsoleSenderCannotUse(CommandSender sender) {
        sendMessage(sender, "command.error.cannot-use-from-console");
    }

    public void sendPlayerCannotUse(CommandSender sender) {
        sendMessage(sender, "command.error.player-cannot-use");
    }

    public void sendNotEnoughArguments(CommandSender sender) {
        sendMessage(sender, "command.error.not-enough-arguments");
    }

    public void sendUsage(CommandSender sender, String usage) {
        sendMessage(sender, "command.info.usage", Map.of("%usage%", usage));
    }

    public void sendNoPlayerFound(CommandSender sender, String player) {
        sendMessage(sender, "command.error.no-player-found", Map.of("%player%", player));
    }

    public void sendInvalidPrefixSyntax(CommandSender sender) {
        sendMessage(sender, "command.error.invalid-syntax");
    }

    public void sendNoLegendaryTicket(CommandSender sender) {
        sendMessage(sender, "command.error.no-legendary-ticket");
    }

    public void sendPrefixIsInUse(CommandSender sender) {
        sendMessage(sender, "command.error.prefix-is-already-in-use");
    }

    public void sendPrefixAddSuccess(CommandSender sender, OfflinePlayer player, String prefix) {
        sendMessage(sender, "command.info.add-success",
                Map.of("%player%", Optional.ofNullable(player.getName()).orElse("null"), "%prefix%",
                        ChatColor.translateAlternateColorCodes('&', prefix)));
    }

    public void sendListHeader(CommandSender sender, OfflinePlayer player) {
        sendMessage(sender, "command.info.list-header",
                Map.of("%player%", Optional.ofNullable(player.getName()).orElse("null")));
    }

    public void sendListFormattedLine(CommandSender sender, String prefix) {
        sendMessage(sender, false, "command.info.list-line", Map.of("%prefix%", prefix));
    }

    public void sendReloadSuccess(CommandSender sender) {
        sendMessage(sender, "command.info.reload");
    }

    public void sendDoNotHavePrefix(CommandSender sender) {
        sendMessage(sender, "command.error.player-do-not-have-the-prefix");
    }

    public void sendRemoveSuccess(CommandSender sender, OfflinePlayer player, String prefix) {
        sendMessage(sender, "command.info.remove-success",
                Map.of("%player%", Optional.ofNullable(player.getName()).orElse("null"), "%prefix%",
                        ChatColor.translateAlternateColorCodes('&', prefix)));
    }

    public void sendSetSuccess(CommandSender sender, OfflinePlayer player, String prefix) {
        sendMessage(sender, "command.info.set-success",
                Map.of("%player%", player.getName(), "%prefix%", ChatColor.translateAlternateColorCodes('&', prefix)));
    }

    public void sendHelpHeader(CommandSender sender) {
        sendMessage(sender, "command.info.help-header");
    }

    public void sendHelpLine(CommandSender sender, BaseCommand subCommand) {
        sendMessage(sender, false, "command.info.help-line",
                Map.of("%usage%", subCommand.getUsage(), "%description%", subCommand.getDescription()));

    }
}