package net.okocraft.affix.config;

import java.nio.charset.StandardCharsets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import net.okocraft.affix.affixprice.Price;
import net.okocraft.affix.affixprice.PriceFree;
import net.okocraft.affix.affixprice.PriceItem;
import net.okocraft.affix.affixprice.PriceMoney;
import net.okocraft.affix.affixprice.PriceXP;
import net.okocraft.affix.database.Database.DatabaseType;

public final class Config extends CustomConfig {

    Config(Plugin plugin) {
        super(plugin, "config.yml");
    }

    public boolean matchRegexPrefixConditions(String prefix) {
        for (String condition : get().getStringList("prefix-regex-conditions")) {
            if (!prefix.matches(condition)) {
                return false;
            }
        }
        return true;
    }

    public int getPrefixPriority() {
        return get().getInt("prefix-priority");
    }

    public int getMaxPrefixLength() {
        return get().getInt("prefix-max-length");
    }

    public String formatPrefix(String prefix) {
        return get().getString("prefix-format").replace("%prefix%", prefix);
    }

    public Price<?> getPrefixPrice() {
        try {
            String type = get().getString("prefix-price.type");
            if (type.equalsIgnoreCase("none")) {
                return new PriceFree();
            } else if (type.equalsIgnoreCase("money")) {
                return new PriceMoney(get().getDouble("prefix-price.money", 100D));
            } else if (type.equalsIgnoreCase("xp")) {
                return new PriceXP(get().getInt("prefix-price.xp", 1000));
            } else if (type.equalsIgnoreCase("item")) {
                String item = get().getString("prefix-price.item");
                if (item == null) {
                    return null;
                }
                return new PriceItem(ItemStack.deserializeBytes(item.getBytes(StandardCharsets.UTF_8)));
            }
        } catch (NoSuchMethodError ignored) {
            // if server is not paperclip. 
        }
        return null;
    }

    public void setPrefixPrice(Price<?> price) {
        if (price instanceof PriceXP) {
            get().set("prefix-price", null);
            get().set("prefix-price.type", "xp");
            get().set("prefix-price.xp", price.getPrice());
        
        } else if (price instanceof PriceMoney) {
            get().set("prefix-price", null);
            get().set("prefix-price.type", "money");
            get().set("prefix-price.money", price.getPrice());
            
        } else if (price instanceof PriceItem) {
            get().set("prefix-price", null);
            get().set("prefix-price.type", "item");
            get().set("prefix-price.item", new String(((PriceItem) price).getPrice().serializeAsBytes(), StandardCharsets.UTF_8));
            
        } else if (price instanceof PriceFree) {
            get().set("prefix-price", null);
            get().set("prefix-price.type", "none");

        }
    }

    public boolean matchRegexSuffixConditions(String suffix) {
        for (String condition : get().getStringList("suffix-regex-conditions")) {
            if (!suffix.matches(condition)) {
                return false;
            }
        }
        return true;
    }

    public int getSuffixPriority() {
        return get().getInt("suffix-priority");
    }

    public int getMaxSuffixLength() {
        return get().getInt("suffix-max-length");
    }

    public String formatSuffix(String suffix) {
        return get().getString("suffix-format").replace("%suffix%", suffix);
    }

    public Price<?> getSuffixPrice() {
        try {
            String type = get().getString("suffix-price.type");
            if (type.equalsIgnoreCase("none")) {
                return new PriceFree();
            } else if (type.equalsIgnoreCase("money")) {
                return new PriceMoney(get().getDouble("suffix-price.money", 100D));
            } else if (type.equalsIgnoreCase("xp")) {
                return new PriceXP(get().getInt("suffix-price.xp", 1000));
            } else if (type.equalsIgnoreCase("item")) {
                String item = get().getString("suffix-price.item");
                if (item == null) {
                    return null;
                }
                return new PriceItem(ItemStack.deserializeBytes(item.getBytes(StandardCharsets.UTF_8)));
            }
        } catch (NoSuchMethodError ignored) {
            // if server is not paperclip. 
        }
        return null;
    }

    public void setSuffixPrice(Price<?> price) {
        if (price instanceof PriceXP) {
            get().set("suffix-price", null);
            get().set("suffix-price.type", "xp");
            get().set("suffix-price.xp", price.getPrice());
        
        } else if (price instanceof PriceMoney) {
            get().set("suffix-price", null);
            get().set("suffix-price.type", "money");
            get().set("suffix-price.money", price.getPrice());
            
        } else if (price instanceof PriceItem) {
            get().set("suffix-price", null);
            get().set("suffix-price.type", "item");
            get().set("suffix-price.item", new String(((PriceItem) price).getPrice().serializeAsBytes(), StandardCharsets.UTF_8));
            
        } else if (price instanceof PriceFree) {
            get().set("suffix-price", null);
            get().set("suffix-price.type", "none");

        }
    }

    @Override
    public void reload() {
        Bukkit.getOnlinePlayers().forEach(Player::closeInventory);
        super.reload();
    }

	public String getMysqlHost() {
		return get().getString("mysql-host");
	}
    
	public String getMysqlUser() {
        return get().getString("mysql-user");
	}
    
	public String getMysqlDBName() {
        return get().getString("mysql-pass");
	}
    
	public String getMysqlPass() {
        return get().getString("mysql-db-name");
	}

	public int getMysqlPort() {
		return get().getInt("mysql-port");
	}

	public DatabaseType getDatabaseType() {
        try {
            return DatabaseType.valueOf(get().getString("database-type"));
        } catch (IllegalArgumentException e) {
            return DatabaseType.SQLITE;
        }
	}
}