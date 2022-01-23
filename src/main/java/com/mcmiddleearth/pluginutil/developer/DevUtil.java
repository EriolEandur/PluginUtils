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
package com.mcmiddleearth.pluginutil.developer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class DevUtil {
    
    private final List<UUID> developer = new ArrayList<UUID>();
    
    private String msgColor = ""+ChatColor.GOLD;

    public void setMsgColor(String msgColor) {
        this.msgColor = msgColor;
    }

    private String plugin = "[Debug] ";

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    private boolean consoleOutput = false;

    public boolean isConsoleOutput() {
        return consoleOutput;
    }

    public void setConsoleOutput(boolean consoleOutput) {
        this.consoleOutput = consoleOutput;
    }

    private int level = 1;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void log(String message) {
        log(1,message);
    }
    public void log(int msglevel, String message) {
        if(level<msglevel) {
            return;
        }
        
        for(UUID uuid:developer) {
            Player player = Bukkit.getPlayer(uuid);
            if(player!=null) {
                player.sendMessage(msgColor+plugin+message);
            }
        }
        if(consoleOutput) {
            Logger.getGlobal().info(plugin+message);
        }
    }
    
    public void add(Player player) {
        for(UUID search: developer) {
            if(search.equals(player.getUniqueId())) {
                return;
            }
        }
        developer.add(player.getUniqueId());
    }
    
    public void remove(Player player) {
        developer.remove(player.getUniqueId());
    }
    
    public List<OfflinePlayer> getDeveloper() {
        List<OfflinePlayer> devs = new ArrayList<OfflinePlayer>();
        for(UUID search: developer) {
            devs.add(Bukkit.getOfflinePlayer(search));
        }
        return devs;
    }

}
