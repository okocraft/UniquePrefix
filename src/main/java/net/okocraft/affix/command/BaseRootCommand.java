package net.okocraft.affix.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import lombok.Getter;
import net.okocraft.affix.AffixPlugin;
import net.okocraft.affix.config.Messages;

public abstract class BaseRootCommand implements CommandExecutor, TabCompleter, net.okocraft.affix.command.Command {

    @Getter
    protected final AffixPlugin plugin;

    @Getter
    protected final String name;
    @Getter
    protected final String permissionNode;
    @Getter
    protected final int leastArgsLength;
    @Getter
    protected final boolean playerCommand;
    @Getter
    protected final boolean consoleCommand;
    @Getter
    protected final String usage;
 
    protected List<BaseCommand> subCommands = new ArrayList<>();
    
    protected final Messages messages;
    
    public BaseRootCommand(AffixPlugin plugin, String name, String permissionNode, boolean playerCommand, boolean consoleCommand, String usage) {
        this.plugin = plugin;
        this.name = name;
        this.permissionNode = permissionNode;
        this.leastArgsLength = 0;
        this.playerCommand = playerCommand;
        this.consoleCommand = consoleCommand;
        this.usage = usage;

        PluginCommand pluginCommand = Objects.requireNonNull(plugin.getCommand(name),
                "Command " + name + " is not written in plugin.yml");
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);

        this.messages = plugin.getConfigManager().getMessages();
    }

    public BaseCommand getSubCommandByName(String name) {
        for (BaseCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(name)) {
                return subCommand;
            }
        }

        throw new IllegalArgumentException("There is no command with the name " + name);
    }

    public void registerSubCommand(BaseCommand subCommand) {
        subCommands.add(subCommand);
    }

    public String getDescription() {
        return messages.getMessage("command.description." + getName() + ".root-command");
    }

    public List<String> getPermittedSubCommandNames(CommandSender sender) {
        List<String> result = new ArrayList<>();
        for (BaseCommand subCommand : subCommands) {
            if (subCommand.hasPermission(sender)) {
                result.add(subCommand.getName().toLowerCase(Locale.ROOT));
            }
        }
        return result;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            return this.runCommand(sender, args);
        }

        BaseCommand subCommand;
        try {
            subCommand = getSubCommandByName(args[0]);
        } catch (IllegalArgumentException e) {
            plugin.getConfigManager().getMessages().sendInvalidArgument(sender, args[0]);
            this.runCommand(sender, args);
            return false;
        }

        if (!subCommand.isConsoleCommand()
                && (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender)) {
            messages.sendConsoleSenderCannotUse(sender);
            return false;
        }

        if (!subCommand.isPlayerCommand() && sender instanceof Player) {
            messages.sendPlayerCannotUse(sender);
            return false;
        }

        if (!subCommand.hasPermission(sender)) {
            messages.sendNoPermission(sender, subCommand.getPermissionNode());
            return false;
        }

        if (subCommand.getLeastArgsLength() > args.length) {
            messages.sendNotEnoughArguments(sender);
            messages.sendUsage(sender, subCommand.getUsage());
            return false;
        }

        return subCommand.runCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> permittedCommands = getPermittedSubCommandNames(sender);
        permittedCommands.add("help");
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], permittedCommands, new ArrayList<>());
        }

        if (!permittedCommands.contains(args[0].toLowerCase(Locale.ROOT))) {
            return List.of();
        }

        return getSubCommandByName(args[0]).runTabComplete(sender, args);
    }

    /**
     * Show help.
     *
     * @param sender コマンドの実行者
     * @param args   引数
     * @return true
     */
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermissionNode())) {
            messages.sendNoPermission(sender, getPermissionNode());
            return false;
        }
        messages.sendHelpHeader(sender);
        messages.sendHelpLine(sender, this);
        for (BaseCommand subCommand : subCommands) {
            if (sender.hasPermission(subCommand.getPermissionNode())) {
                messages.sendHelpLine(sender, subCommand);
            }
        }
        return true;
    }
}
