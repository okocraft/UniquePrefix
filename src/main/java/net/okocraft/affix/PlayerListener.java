package net.okocraft.affix;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.okocraft.affix.database.Tables;

public class PlayerListener implements Listener {
    
    private final Tables tables;

    PlayerListener(AffixPlugin plugin) {
        this.tables = plugin.getDatabaseTable();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        int idByName = tables.getPlayerIdByName(event.getPlayer().getName());
        int idByUUID = tables.getPlayerIdByUUID(event.getPlayer().getUniqueId());
        
        // 初ログイン
        if (idByUUID == -1 && idByName == -1) {
            tables.registerPlayer(event.getPlayer());
            return;
        }
        
        // 過去いた人が名前を変更 AND 過去いた人の名前を使った別人がログインした
        if (idByUUID == -1 && idByName != -1) {
            OfflinePlayer playerByName = tables.getPlayerById(idByName);
            tables.updatePlayerName(playerByName.getUniqueId(), null);
            tables.registerPlayer(event.getPlayer());
            return;
        }
        
        // 過去いた人が名前を変更 AND 過去いた人の名前を使った別人がすでにログイン済み AND その過去いた人が再度ログインした
        if (idByUUID != -1 && idByName == -1) {
            tables.updatePlayerName(event.getPlayer().getUniqueId(), event.getPlayer().getName());
            return;
        }

        // すでにデータベースに存在しており、名前の変更などもない。
        if (idByUUID != -1 && idByName != -1) {
        }
    }

}
