package net.okocraft.affix.affixprice;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PriceItem extends Price<ItemStack> {

    public PriceItem(@NotNull ItemStack price) {
        super(price);
    }

    @Override
    public boolean has(Player player) {
        ItemStack price = getPrice();
        int amount = price.getAmount();
        price.setAmount(1);
        return player.getInventory().containsAtLeast(price, amount);
    }

    @Override
    public boolean pay(Player player) {
        ItemStack price = getPrice();
        return !player.getInventory().removeItem(price).isEmpty();
    }

    @Override
    public ItemStack getPrice() {
        return super.getPrice().clone();
    }
}
