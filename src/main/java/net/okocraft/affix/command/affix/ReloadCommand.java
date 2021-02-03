package net.okocraft.affix.command.affix;

import java.util.List;

import org.bukkit.command.CommandSender;

import net.okocraft.affix.command.BaseCommand;

public final class ReloadCommand extends BaseCommand {

    public ReloadCommand(AffixCommand parent) {
        super(parent.getPlugin(), "reload", "affix.command.affix.reload", 1, true, true, "/affix reload");
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        plugin.getConfigManager().reloadAllConfigs();
        messages.sendReloadSuccess(sender);
        return false;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}