package net.okocraft.affix.affixprice;

import org.bukkit.entity.Player;

public class PriceFree extends Price<Object> {

    public PriceFree() {
        super(new Object());
    }

    @Override
    public boolean has(Player player) {
        return true;
    }

    @Override
    public boolean pay(Player player) {
        return true;
    }
    
}
