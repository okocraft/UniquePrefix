package net.okocraft.affix.command.affix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.affix.affixprice.Price;
import net.okocraft.affix.affixprice.PriceFree;
import net.okocraft.affix.affixprice.PriceItem;
import net.okocraft.affix.affixprice.PriceMoney;
import net.okocraft.affix.affixprice.PriceXP;
import net.okocraft.affix.command.BaseCommand;

public final class SetpriceCommand extends BaseCommand {

    public SetpriceCommand(AffixCommand parent) {
        super(parent.getPlugin(), "setprice", "affix.command.affix.setprice", 3, true, true, "/affix setprice <prefix|suffix> <item|money <double>|xp <integer>|free>");
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        Price<?> price;
        if (args[2].equalsIgnoreCase("item")) {
            if (!(sender instanceof Player)) {
                messages.sendConsoleSenderCannotUse(sender);
                return false;
            }
            price = new PriceItem(((Player) sender).getInventory().getItemInMainHand());
        } else if (args[2].equalsIgnoreCase("money")) {
            if (args.length == 3) {
                messages.sendNotEnoughArguments(sender);
                return false;
            }
            try {
                price = new PriceMoney(Double.parseDouble(args[3]));
            } catch (NumberFormatException e) {
                messages.sendInvalidArgument(sender, args[3]);
                return false;
            }
        } else if (args[2].equalsIgnoreCase("xp")) {
            if (args.length == 3) {
                messages.sendNotEnoughArguments(sender);
                return false;
            }
            try {
                price = new PriceXP(Integer.parseInt(args[3]));
            } catch (NumberFormatException e) {
                messages.sendInvalidArgument(sender, args[3]);
                return false;
            }
        } else if (args[2].equalsIgnoreCase("free")) {
            price = new PriceFree();
        } else {
            // will not reach.
            messages.sendNotEnoughArguments(sender);
            return false;
        }

        if (args[1].equalsIgnoreCase("prefix")) {
            config.setPrefixPrice(price);
            
        } else if (args[1].equalsIgnoreCase("suffix")) {
            config.setSuffixPrice(price);
            
        } else {
            messages.sendInvalidArgument(sender, args[1]);
            return false;
        }

        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> prefixOrSuffix = Arrays.asList("prefix", "suffix");
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], prefixOrSuffix, new ArrayList<>());
        }
        if (!prefixOrSuffix.contains(args[1].toLowerCase(Locale.ROOT))) {
            return new ArrayList<>();
        }

        List<String> priceTypes = new ArrayList<>(Arrays.asList("xp", "money", "item", "free"));
        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], priceTypes, new ArrayList<>());
        }
        priceTypes.remove("item");
        priceTypes.remove("free");
        if (!priceTypes.contains(args[1].toLowerCase(Locale.ROOT))) {
            return new ArrayList<>();
        }

        if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], Arrays.asList((args[2].equalsIgnoreCase("xp") ? "<xp>" : "<money>")), new ArrayList<>());
        }

        return new ArrayList<>();
    }
}