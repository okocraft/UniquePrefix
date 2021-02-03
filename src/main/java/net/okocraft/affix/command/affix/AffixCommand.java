package net.okocraft.affix.command.affix;

import net.okocraft.affix.AffixPlugin;
import net.okocraft.affix.command.BaseRootCommand;

public class AffixCommand extends BaseRootCommand {

    public AffixCommand(AffixPlugin plugin) {
        super(plugin, "affix", "affix.command.affix", true, true, "/affix <args...>");

        registerSubCommand(new ReloadCommand(this));
    }

}