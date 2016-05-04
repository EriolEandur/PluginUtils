/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutils;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Eriol_Eandur
 */
public class DynmapUtil {
    
    public static void hide(Player player) {
        JavaPlugin dynmap = getDynmap();
        if(dynmap!=null) {
                getDynmap().getCommand("dynmap").execute(player, "dynmap", new String[]{"hide"});
        }
    }
    
    public static void show(Player player) {
        JavaPlugin dynmap = getDynmap();
        if(dynmap!=null) {
            getDynmap().getCommand("dynmap").execute(player, "dynmap", new String[]{"show"});
        }   
    }
    
    private static JavaPlugin getDynmap() {
            //Plugin dynmap = MiniGamesPlugin.getPluginInstance().getServer().getPluginManager().getPlugin("dynmap");
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
