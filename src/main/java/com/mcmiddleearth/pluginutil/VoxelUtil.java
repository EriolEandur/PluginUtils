/*
 * Copyright (C) 2016 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.pluginutil;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Eriol_Eandur
 */
public class VoxelUtil {
    
    public static Plugin getVoxelSniperPlugin() {
        return Bukkit.getPluginManager().getPlugin("VoxelSniper");
    }
            
    private static Object getSniperManager() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Plugin plugin = getVoxelSniperPlugin();
        return (plugin!=null?(plugin.getClass().getMethod("getSniperManager")).invoke(plugin):null);
    }
    
    private static Object getBrushManager() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Plugin plugin = getVoxelSniperPlugin();
        return (plugin!=null?(plugin.getClass().getMethod("getBrushManager")).invoke(plugin):null);
    }
    
    private static Object getSniper(Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object sManager = getSniperManager();
        return (sManager!=null?sManager.getClass().getMethod("getSniperForPlayer", Player.class).invoke(sManager, player):null);
    }
    
    private static String getCurrentToolId(Object sniper) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (String) sniper.getClass().getMethod("getCurrentToolId").invoke(sniper);
    }
    private static Object getSnipeData(Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object sniper = getSniper(player);
        String toolId = getCurrentToolId(sniper);
        return sniper.getClass().getMethod("getSnipeData",String.class).invoke(sniper,toolId);
    }
    
    public static int getBrushSize(Player player) {
        try {
            Object snipeData = getSnipeData(player);
            return (int) snipeData.getClass().getMethod("getBrushSize").invoke(snipeData);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            return -1;
        }
    }
    
    public static boolean hasToolInHand(Player player) {
        try {
            Material material = player.getItemInHand().getType();
            Object sniper = getSniper(player);
            Object toolId = getCurrentToolId(sniper);
            Object tool = sniper.getClass().getMethod("getSniperTool",String.class).invoke(sniper,toolId);
            return (boolean) tool.getClass().getMethod("hasToolAssigned",material.getClass()).invoke(tool,material);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            return false;
        }
    }
    
    public static boolean isCurrentBrush(Player player, String brushHandle) {
        try {
            Object sniper = getSniper(player);
            Object bManager = getBrushManager();
            Object brush = sniper.getClass().getMethod("getBrush",String.class).invoke(sniper,(String)null);
            Set<String> handles = (Set<String>) bManager.getClass().getMethod("getSniperBrushHandles", brush.getClass().getClass()).invoke(bManager,brush.getClass());
            for(String str: handles) {
                if(str.equalsIgnoreCase(brushHandle)) {
                    return true;
                }
            }
            return false;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            return false;
        }
    }
    
    public static int getRange(Player player) {
        try {
            Object snipeData = getSnipeData(player);
            if((boolean) snipeData.getClass().getMethod("isRanged").invoke(snipeData)) {
                return (int) snipeData.getClass().getMethod("getRange").invoke(snipeData);
            }
            else {
                return -1;
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            return -1;
        }
    }
}
