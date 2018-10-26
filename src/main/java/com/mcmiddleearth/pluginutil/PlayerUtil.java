/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Player utility class. Lots of uuid checks.
 * @author Eriol_Eandur
 */
public class PlayerUtil {
    
    public static boolean isSame(OfflinePlayer player1, OfflinePlayer player2) {
        if(player1 == null  || player2 == null) {
            return false;
        }
        return player1.getUniqueId().equals(player2.getUniqueId());
    }
    
    public static Player getOnlinePlayer(OfflinePlayer player) {
        if(player!=null) {
            return Bukkit.getPlayer(player.getUniqueId());
        }
        return null;
    }
    
    public static Player getOnlinePlayer(List<OfflinePlayer> playerList, OfflinePlayer player) {
        return getOnlinePlayer(getOfflinePlayer(playerList, player));
    }
    
    public static OfflinePlayer getOfflinePlayer(List<OfflinePlayer> playerList, OfflinePlayer player) {
        for(OfflinePlayer search : playerList) {
            if(search.getUniqueId().equals(player.getUniqueId())) {
                return search;
            }
        }
        return null;
    }
    
    public static boolean isPlayerInList(List playerList, OfflinePlayer player) {
        for(Object search: playerList) {
            if(isSame((OfflinePlayer)search,player)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean hasOffHand = true;
    
    public static ItemStack getItemInMainHand(Player player) {
        try {
            if(hasOffHand) {
                return player.getInventory().getItemInMainHand();
            } else {
                return player.getInventory().getItemInHand();
            }
        } catch(NoSuchMethodError e) {
            hasOffHand = false;
            return player.getInventory().getItemInHand();
        }
    }
    
}
