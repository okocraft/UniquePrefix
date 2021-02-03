package net.okocraft.affix.affixprice;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import net.okocraft.affix.AffixPlugin;

public class PriceMoney extends Price<Double> {

    private static final Economy ECONOMY;
    static {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            ECONOMY = null;
            AffixPlugin.getPlugin(AffixPlugin.class).getLogger().warning("Vault is not installed.");
        } else {
            ECONOMY = rsp.getProvider();
        }
    }

    public PriceMoney(@NotNull Double price) {
        super(price);
    }

    @Override
    public boolean has(Player player) {
        if (ECONOMY != null) {
            return ECONOMY.has(player, getPrice());
        } else {
            return false;
        }
    }

    @Override
    public boolean pay(Player player) {
        if (ECONOMY != null) {
            return ECONOMY.withdrawPlayer(player, getPrice()).type == ResponseType.SUCCESS;
        } else {
            return false;
        }
    }
    
}
