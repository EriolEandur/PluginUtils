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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class TitleUtil {

    public static void _invalid_showTitle(Player player, String color, String title, String subtitle) {
        player.sendTitle(color+title, subtitle);
    }
    
    public static void showTitle(Player player, String title, String subtitle) {
        showTitle(player, title, subtitle, 20,80,20);
    }
    
    public static void showTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            //throw new NumberFormatException();
            sendTitle(player, fadeIn, stay, fadeOut, title, subtitle);
        } catch(Error | Exception e) {
            Logger.getLogger(TitleUtil.class.getName()).log(Level.WARNING, "Error in Minigames plugin while accessing NMS class. This plugin version was not made for your server. Please look for an update. Plugin will use Bukkit.dispatchCommand to send '/title ...' instead of directly sending title packets.");
            //title = ChatColor.translateAlternateColorCodes('&', title);
            //subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
            setTimes_Bukkit(player,fadeIn, stay, fadeOut);
            if(title!=null) 
                setTitle_Bukkit(player, title);
            if(subtitle!=null) 
                setSubtitle_Bukkit(player, subtitle);
        }
    }
    
    private static void setTimes_Bukkit(Player player, int fadeIn, int stay, int fadeOut) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title "+player.getName()+" times +"+fadeIn+" "+stay+" "+fadeOut);
    }
    
    private static void setTitle_Bukkit(Player player, String title) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title "+player.getName()+" title "+"{\"text\":\""+title+"\"}");
    }
    
    private static void setSubtitle_Bukkit(Player player, String subtitle) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title "+player.getName()+" subtitle "+"{\"text\":\""+subtitle+"\"}");
    }
    
    public static void showTitleAll(List<Player> playerList, List<Player> except, String title, String subtitle) {
        for(Player player: playerList) {
            if(!PlayerUtil.isPlayerInList(except, player)) {
                showTitle(player,title, subtitle);
            }
        }
    }
    
    public static void showTitleAll(List<Player> playerList, Player except, String title, String subtitle) {
        for(Player player: playerList) {
            if(!PlayerUtil.isSame(player, except)) {
                showTitle(player,title, subtitle);
            }
        }
    }

    public static void showTitleAll(List<Player> playerList, Player except,
                                    String title, String subtitle, 
                                    int fadeIn, int stay, int fadeOut) {
        for(Player player: playerList) {
            if(!PlayerUtil.isSame(player, except)) {
                showTitle(player,title, subtitle,fadeIn, stay, fadeOut);
            }
        }
    }

    private static void setTitleAll_Bukkit(List<Player> playerList, Player except, String title) {
        for(Player player: playerList) {
            if(!PlayerUtil.isSame(player, except)) {
                setTitle_Bukkit(player,title);
            }
        }
    }

   private static void setSubtitleAll_Bukkit(List<Player> playerList, Player except, String subtitle) {
        for(Player player: playerList) {
            if(!PlayerUtil.isSame(player, except)) {
                setSubtitle_Bukkit(player,subtitle);
            }
        }
    }

    private static void setTimesAll_Bukkit(List<Player> playerList, Player except, int fadeIn, int stay, int fadeOut) {
        for(Player player: playerList) {
            if(!PlayerUtil.isSame(player, except)) {
                setTimes_Bukkit(player,fadeIn, stay, fadeOut);
            }
        }
    }

    private static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        if (title != null) {
            title = ChatColor.translateAlternateColorCodes('&', title);
            title = title.replaceAll("%player%", player.getDisplayName());
            /*Object enumTitle = NMSUtil.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
            Object chatTitle = NMSUtil.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
            Constructor<?> titleConstructor = NMSUtil.getNMSClass("PacketPlayOutTitle").getConstructor(NMSUtil.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], NMSUtil.getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            Object titlePacket = titleConstructor.newInstance(enumTitle, chatTitle, fadeIn, stay, fadeOut);
            NMSUtil.sendPacket(player, titlePacket);*/
        }

        if (subtitle != null) {
            subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
            subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
            /*Object enumSubtitle = NMSUtil.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
            Object chatSubtitle = NMSUtil.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + subtitle + "\"}");
            Constructor<?> subtitleConstructor = NMSUtil.getNMSClass("PacketPlayOutTitle").getConstructor(NMSUtil.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], NMSUtil.getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            Object subtitlePacket = subtitleConstructor.newInstance(enumSubtitle, chatSubtitle, fadeIn, stay, fadeOut);
            NMSUtil.sendPacket(player, subtitlePacket);*/
        }
        /*Object enumTimes = NMSUtil.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
        Constructor<?> titleConstructor = NMSUtil.getNMSClass("PacketPlayOutTitle").getConstructor(NMSUtil.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], NMSUtil.getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
        Object titlePacket = titleConstructor.newInstance(enumTimes, null, fadeIn, stay, fadeOut);
        NMSUtil.sendPacket(player, titlePacket);*/
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
    


}
