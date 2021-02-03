package net.okocraft.affix.affixprice;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PriceXP extends Price<Integer> {

    public PriceXP(@NotNull Integer price) {
        super(price);
    }

    @Override
    public boolean has(Player player) {
        return player.getExp() > getPrice();
    }

    @Override
    public boolean pay(Player player) {
        if (has(player)) {
            player.setExp(player.getExp() - getPrice());
            return true;
        }
        return false;
    }

    public static int getXPFromLevel(int level) {
        if (level > 30) {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
        if (level > 15) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }
        return level * level + 6 * level;
    }

}
