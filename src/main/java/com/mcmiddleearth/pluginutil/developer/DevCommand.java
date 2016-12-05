/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.developer;

import com.mcmiddleearth.pluginutil.developer.DevUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Eriol_Eandur
 */
public class DevCommand implements CommandExecutor{
    
    @Override
    public boolean onCommand(CommandSender cs, Command arg1, String arg2, String[] args) {
        if (cs instanceof Player && !((Player)cs).hasPermission("pluginutil.developer"))
        {
            ((Player)cs).sendMessage("Sorry you don't have permission.");
            return true;
        }
        if(args.length==0) {
            ((Player)cs).sendMessage("Not enough arguments.");
            return false;
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin(args[0]);
        if(plugin == null) {
            ((Player)cs).sendMessage("No plugin with that name found.");
            return true;
        }
        if(!(plugin instanceof Debugable)) {
            ((Player)cs).sendMessage("Incompatible plugin.");
            return true;
        } 
        DevUtil devUtil = ((Debugable)plugin).getDevUtil();
        if(args.length>1 && args[1].equalsIgnoreCase("true")) {
            devUtil.setConsoleOutput(true);
            showDetails(cs, devUtil);
            return true;
        }
        else if(args.length>1 && args[1].equalsIgnoreCase("false")) {
            devUtil.setConsoleOutput(false);
            showDetails(cs, devUtil);
            return true;
        }
        else if(args.length>1) {
            try {
                int level = Integer.parseInt(args[1]);
                devUtil.setLevel(level);
                showDetails(cs, devUtil);
                return true;
            }
            catch(NumberFormatException e){};
        }
        if(cs instanceof Player) {
            Player player = (Player) cs;
            if(args.length>1 && args[1].equalsIgnoreCase("r")) {
                devUtil.remove(player);
                showDetails(cs, devUtil);
                return true;
            }
            devUtil.add(player);
            showDetails(cs, devUtil);
        }
        return false;
    }
    
    private void showDetails(CommandSender cs, DevUtil devUtil) {
        cs.sendMessage("DevUtil: Level - "+devUtil.getLevel()+"; Console - "+devUtil.isConsoleOutput()+"; ");
        cs.sendMessage("         Developer:");
        for(OfflinePlayer player:devUtil.getDeveloper()) {
        cs.sendMessage("                "+player.getName());
        }
    }

}

