package net.okocraft.uniqueprefix.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import net.okocraft.uniqueprefix.config.Config;

public final class ReloadCommand extends BaseCommand {

    ReloadCommand() {
        super(
                "uniqueprefix.reload",
                1,
                true,
                true,
                "/uniqueprefix reload"
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        Config.getInstance().reloadAllConfigs();
        MESSAGES.sendReloadSuccess(sender);
        return false;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
} 