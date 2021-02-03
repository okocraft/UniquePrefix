package net.okocraft.affix.affixprice;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public abstract class Price<T> {

    @Getter
    @Setter
    @NotNull
    protected T price;
    
    public abstract boolean has(Player player);

    public abstract boolean pay(Player player);

}
