package net.okocraft.affix.config;

import lombok.Getter;
import net.okocraft.affix.AffixPlugin;

public class ConfigManager {
    
    @Getter
    private final Config config;
    
    @Getter
    private final Messages messages;

    public ConfigManager(AffixPlugin plugin) {
        this.config = new Config(plugin);
        this.messages = new Messages(plugin);
    }

    public void reloadAllConfigs() {
        config.reload();
        messages.reload();
    }
}
