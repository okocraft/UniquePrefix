package net.okocraft.affix;

import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import net.okocraft.affix.command.affix.AffixCommand;
import net.okocraft.affix.command.prefix.PrefixCommand;
import net.okocraft.affix.command.suffix.SuffixCommand;
import net.okocraft.affix.config.ConfigManager;
import net.okocraft.affix.database.Database;
import net.okocraft.affix.database.Tables;

/**
 * TODO: messagesの足りないところを作る
 * TODO: GUIを実装する。
 */
public class AffixPlugin extends JavaPlugin {

    @Getter
    private ConfigManager configManager;
    private Database database;

    @Getter
    private LuckpermsAPI luckpermsAPI;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        
        try {
            this.database = new Database(this, configManager.getConfig().getDatabaseType());
        } catch (SQLException | IOException e) {
            new IllegalStateException("Could not initialize database.", e);
        }
        luckpermsAPI = new LuckpermsAPI(this);

        new AffixCommand(this);
        new PrefixCommand(this);
        new SuffixCommand(this);

        new PlayerListener(this);
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.dispose();
        }
    }

    public Tables getDatabaseTable() {
        return database.getTables();
    }
}
