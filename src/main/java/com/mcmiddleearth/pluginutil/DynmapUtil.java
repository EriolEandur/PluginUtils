/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

/**
 *
 * @author Eriol_Eandur
 */
public class DynmapUtil {
    
    public static void hide(Player player) {
        JavaPlugin dynmap = getDynmap();
        if(dynmap!=null) {
            ((DynmapAPI)dynmap).assertPlayerInvisibility(player, true, dynmap);
        }
    }
    
    public static void show(Player player) {
        JavaPlugin dynmap = getDynmap();
        if(dynmap!=null) {
            ((DynmapAPI)dynmap).assertPlayerInvisibility(player, false, dynmap);
        }   
    }
    
    private static JavaPlugin getDynmap() {
            Plugin dynmap = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
            if(dynmap==null) {
                Logger.getGlobal().info("Dynmap not found");
                return null;
            }
            else {
                return (JavaPlugin) dynmap;
            }
    }

}
