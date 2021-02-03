package net.okocraft.affix.command.suffix;

import net.okocraft.affix.AffixPlugin;
import net.okocraft.affix.command.BaseRootCommand;

public class SuffixCommand extends BaseRootCommand {
    
    public SuffixCommand(AffixPlugin plugin) {
        super(plugin, "suffix", "suffix.command", true, true, "/suffix <args...>");

        registerSubCommand(new AddCommand(this));
        registerSubCommand(new RemoveCommand(this));
        registerSubCommand(new SetCommand(this));
        registerSubCommand(new UnsetCommand(this));
        registerSubCommand(new ListCommand(this));
    }
}