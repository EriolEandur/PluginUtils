/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.developer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Command to manage developer output to players or console
 * @author Eriol_Eandur
 */
public class DevCommand implements CommandExecutor{
    
    /**
     * Players need permission "pluginutil.developer" to use this command.
     * Arguments array args must contain:
     *    - [0] Name of the plugin to be debugged
     *    - [1] true/false to switch console output or full number to set debug level
     *          without this argument output will be sent to the player issuing the command
     *          with "r" for this argument the player issuing will no longer recieve debug output
     * @param cs
     * @param arg1
     * @param arg2
     * @param args
     * @return 
     */
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

