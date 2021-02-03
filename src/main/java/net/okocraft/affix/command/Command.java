package net.okocraft.affix.command;

public interface Command {
    public abstract String getName();
    public abstract String getDescription();
    public abstract String getPermissionNode();
    public abstract int getLeastArgsLength();
    public abstract boolean isPlayerCommand();
    public abstract boolean isConsoleCommand();
    public abstract String getUsage();
}
