package net.okocraft.affix.command.prefix;

import net.okocraft.affix.AffixPlugin;
import net.okocraft.affix.command.BaseRootCommand;

public class PrefixCommand extends BaseRootCommand {
    
    public PrefixCommand(AffixPlugin plugin) {
        super(plugin, "prefix", "prefix.command", true, true, "/prefix <args...>");

        registerSubCommand(new AddCommand(this));
        registerSubCommand(new RemoveCommand(this));
        registerSubCommand(new SetCommand(this));
        registerSubCommand(new UnsetCommand(this));
        registerSubCommand(new ListCommand(this));
    }
}