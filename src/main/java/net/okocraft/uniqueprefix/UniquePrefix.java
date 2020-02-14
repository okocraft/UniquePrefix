package net.okocraft.uniqueprefix;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import net.okocraft.uniqueprefix.command.UniquePrefixCommand;
import net.okocraft.uniqueprefix.config.Config;

public class UniquePrefix extends JavaPlugin {

	private static UniquePrefix instance;

	@Override
	public void onEnable() {
		Config.getInstance().reloadAllConfigs();
		UniquePrefixCommand.init();
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
	}

	public static UniquePrefix getInstance() {
		if (instance == null) {
			instance = (UniquePrefix) Bukkit.getPluginManager().getPlugin("UniquePrefix");
		}
		return instance;
	}
}
