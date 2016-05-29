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
package com.mcmiddleearth.pluginutil.message;

import com.mcmiddleearth.pluginutil.FileUtil;
import com.mcmiddleearth.pluginutil.NMSUtil;
import com.mcmiddleearth.pluginutil.NumericUtil;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Ivanpl, Eriol_Eandur
 */

public class MessageUtil {
    
    
    public final ChatColor INFO = ChatColor.AQUA;
    public final ChatColor ERROR = ChatColor.RED;
    public final ChatColor ERROR_STRESSED = ChatColor.DARK_RED;
    public final ChatColor STRESSED = ChatColor.GREEN;
    public final ChatColor HIGHLIGHT = ChatColor.GOLD;
    public final ChatColor HIGHLIGHT_STRESSED = ChatColor.YELLOW;
    
    
    private String PREFIX   = "[Plugin] ";
    
    private String INDENTED = "    ";
    
    private static final int PAGE_LENGTH = 13;
    
    private static final int LENGTH_OF_HOVER_LINE = 40;

    public String getPREFIX() {
        return PREFIX;
    }
    
    public String getNOPREFIX() {
        return INDENTED;
    }
    
    public void setPluginName(String pluginName) {
        PREFIX = "["+pluginName+"] ";
        INDENTED = "";
        for(int i = 0;i<pluginName.length()+2;i++) {
            INDENTED = INDENTED.concat(" ");
        }
    }
    
    public String infoPrefix() {
        return INFO+PREFIX;
    }
    
    public String errorPrefix() {
        return ERROR+PREFIX;
    }
    
    public String highlightPrefix() {
        return HIGHLIGHT+PREFIX;
    }
    
    public String infoNoPrefix() {
        return INFO+INDENTED;
    }
    
    public String errorNoPrefix() {
        return ERROR+INDENTED;
    }
        
    public void sendErrorMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            sender.sendMessage(ERROR + PREFIX + message);
        } else {
            sender.sendMessage(PREFIX + message);
        }
    }
    
    public void sendInfoMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            sender.sendMessage(INFO + PREFIX + message);
        } else {
            sender.sendMessage(PREFIX + message);
        }
    }
    
    public void sendIndentedInfoMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            sender.sendMessage(INFO + INDENTED + message);
        } else {
            sender.sendMessage(INDENTED + message);
        }
    }
    
    public void sendNoPrefixInfoMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            sender.sendMessage(INFO + message);
        } else {
            sender.sendMessage(message);
        }
    }
    
    public void sendBroadcastMessage(String string) {
        Bukkit.getServer().broadcastMessage(INFO + PREFIX + string);
    }
    
    public static void sendBroadcastMessage(FancyMessage message) {
        for(Player player: Bukkit.getOnlinePlayers()) {
            message.send(player);
        }
    }

    public static void sendRawMessage(Player sender, String message) {
        try {
            Object chatBaseComponent = NMSUtil.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, message);
            Constructor<?> titleConstructor = NMSUtil.getNMSClass("PacketPlayOutChat").getConstructor(NMSUtil.getNMSClass("IChatBaseComponent"));
            Object chatPacket = titleConstructor.newInstance(chatBaseComponent);
            NMSUtil.sendPacket(sender, chatPacket);
            /*((CraftPlayer) sender).getHandle()
                                  .playerConnection
                                  .sendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer
                                                                                      .a(message)));*/
        } catch(Error | Exception e ) {
            Logger.getLogger(MessageUtil.class.getName()).log(Level.WARNING, "Error in Minigames plugin while accessing NMS class. This plugin version was not made for your server. Please look for an update. Plugin will use Bukkit.dispatchCommand to send '/tellraw ...' instead of directly sending message packets.");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName()+ " " + message);
Logger.getGlobal().info(message);
        }    
    }
        
    public void sendFancyFileListMessage(Player recipient, FancyMessage header,
                                                    File baseDir, FileFilter filter, String[] args,
                                                    String listCommand, String selectCommand, boolean showSubDir) {
        // args may be length 0 or include: [relative Dir] [#page]
        // list command must be like: /listCommand [relativeDirectory] [#page]
        // select command must be like: /selectCommand <filename>
        int page=1;
        String relativeDir = "";
        if(args.length>0) {
            try {
                page = Integer.parseInt(args[args.length-1]);
            } catch (NumberFormatException ex) {
                relativeDir = argsToDir(args[0]);
            }
        }
        if(args.length>1) {
            relativeDir = argsToDir(args[0]);
        }
        File dir = new File(baseDir+"/"+relativeDir);
        if(!dir.exists()) {
            sendErrorMessage(recipient, "Directory not found.");
            return;
        }
        if(!baseDir.exists()) {
            sendErrorMessage(recipient, "Base Directory not found.");
            return;
        }            
        File[] files = dir.listFiles(filter);
        List<FancyMessage> list = new ArrayList<>();
        header.addSimple(" "+ relativeDir);
        if(!dir.equals(baseDir)) {
            String parentDir = new File(relativeDir).getParent();
            if(parentDir == null) {
                parentDir = "";
            }
            list.add(new FancyMessage(MessageType.INFO_NO_PREFIX, this)
                    .addClickable(ChatColor.BLUE+".. parent directory", listCommand+" "+parentDir)
                    .setRunDirect());
        }
        if(showSubDir) {
            File[] dirs = dir.listFiles(FileUtil.getDirFilter());
            for(File subDir:dirs) {
                list.add(new FancyMessage(MessageType.INFO_NO_PREFIX, this)
                        .addClickable(ChatColor.BLUE+"<"+subDir.getName()+">", listCommand+" "
                                      +(relativeDir.length()>0?relativeDir+"/":"")+subDir.getName())
                        .setRunDirect());
            }
        }      
        for(File file:files) {
            String filename = file.getName().substring(0,file.getName().lastIndexOf('.'));
            list.add(new FancyMessage(MessageType.WHITE, this)
                    .addClickable(ChatColor.DARK_AQUA+filename,selectCommand+" "+(relativeDir.length()>0?relativeDir+"/":"")+filename)
                    .addSimple(ChatColor.WHITE+" "+getDescription(file)));
        }
        sendFancyListMessage(recipient, header, list, listCommand+" "+relativeDir, page);
    }

    private static String argsToDir(String args) {
        String relativeDir;
        if(args.startsWith("/")|| args.startsWith("\\")) {
            relativeDir = args.substring(1);
        } else {
            relativeDir = args;
        }
        return relativeDir;
    }
    
    /*public static void sendClickableListMessage(Player recipient, LinkedHashMap<String,String> header,
                                                List<LinkedHashMap<String,String>> list, 
                                                String listCommand, int page) {
        // list command must be like: /listCommand [#page]
        LinkedHashMap<String,String[]> fancyHeader = new LinkedHashMap<>();
        for(String key:header.keySet()) {
            fancyHeader.put(key, new String[]{header.get(key),null});
        }
        List<LinkedHashMap<String,String[]>> fancyList = new ArrayList<>();
        for(LinkedHashMap<String,String> entry: list) {
            LinkedHashMap<String,String[]> fancyEntry = new LinkedHashMap<>();
            for(String key: entry.keySet()) {
                fancyEntry.put(key, new String[]{entry.get(key),null});
            }
            fancyList.add(fancyEntry);
        }
        sendFancyListMessage(recipient, fancyHeader, fancyList, listCommand, page);
    }*/
    
    public void sendFancyListMessage(Player recipient, FancyMessage header,
                                                List<FancyMessage> list, 
                                                String listCommand, int page) {
        // list command must be like: /listCommand [#page]
        int maxPage=Math.max((int) Math.ceil(list.size()/((float)PAGE_LENGTH)),1);
        if(page>maxPage) {
            page = maxPage;
        }
        header.addSimple(" [page " +page+"/"+maxPage+"]").
               send(recipient);
        if(page>1) {
            new FancyMessage(MessageType.INFO_INDENTED, this)
                .addClickable(ChatColor.BLUE+"---^ page up ^---", listCommand+" "+(page-1))
                .setRunDirect()
                .send(recipient);
        }
        for(int i = (page-1)*PAGE_LENGTH; i < list.size() && i < page*PAGE_LENGTH; i++) {
            list.get(i).send(recipient);
        }
        if(page<maxPage) {
            new FancyMessage(MessageType.INFO_INDENTED, this)
                .addClickable(ChatColor.BLUE+"---v page down v--", listCommand+" "+(page+1))
                .setRunDirect()
                .send(recipient);
        }
    }

    private static String getDescription(File file) {
        if(file.getName().endsWith("json")) {
            try {
                String input;
                try (Scanner reader = new Scanner(file)) {
                    input = "";
                    while(reader.hasNext()){
                        input = input+reader.nextLine();
                    }
                }
                JSONObject jInput = (JSONObject) new JSONParser().parse(input);
                return (String) jInput.get("description");
            } catch (FileNotFoundException | ParseException ex) {}
        }
        return "";
    }
    
    public static ChatColor randomColor() {
        List<ChatColor> list = new ArrayList<>();
        for (ChatColor color : ChatColor.values()) {
            if (!(color.equals(ChatColor.BOLD)
                    || color.equals(ChatColor.COLOR_CHAR)
                    || color.equals(ChatColor.MAGIC)
                    || color.equals(ChatColor.RESET)
                    || color.equals(ChatColor.STRIKETHROUGH)
                    || color.equals(ChatColor.UNDERLINE))) {
                list.add(color);
            }
        }
        return list.get(NumericUtil.getRandom(0, list.size() - 1));
    }
    
    public String hoverFormat(String hoverMessage,String headerSeparator, boolean header) {
        class MyScanner {
            private final Scanner scanner;
            public String currentToken=null;
            public MyScanner(String string) {
                scanner = new Scanner(string);
                scanner.useDelimiter(" ");
                if(scanner.hasNext()) {
                    currentToken = scanner.next();
                }
            }
            public String next() {
                if(scanner.hasNext()) {
                    currentToken = scanner.next();
                } else {
                    currentToken = null;
                }
                return currentToken;
            }
            public boolean hasCurrent() {
                return currentToken != null;
            }
            public boolean hasNext() {
                return scanner.hasNext();
            }
        }
        String result = (header?HIGHLIGHT:HIGHLIGHT_STRESSED)+"";
        int separator = -1;
        if(header) {
            separator = hoverMessage.indexOf(headerSeparator);
            result = result.concat(hoverMessage.substring(0,separator+1)+"\n");
        }
        MyScanner scanner = new MyScanner(hoverMessage.substring(separator+1));
        while (scanner.hasCurrent()) {
            String line = HIGHLIGHT_STRESSED+scanner.currentToken+" ";
            scanner.next();
            while(scanner.hasCurrent() && line.length()+scanner.currentToken.length()<LENGTH_OF_HOVER_LINE) {
                if(scanner.currentToken.equals("\n")) {
                    break;
                } else {
                    line = line.concat(scanner.currentToken+" ");
                    scanner.next();
                }
            }
            if(scanner.hasCurrent()) {
                line = line.concat("\n");
                if(scanner.currentToken.equals("\n")) {
                    scanner.next();
                }
            }
            result = result.concat(line);
        }
        return result;
    }
    

    public void sendPlayerOnlyCommandError(CommandSender cs) {
        sendErrorMessage(cs, "This command can only be run by a player.");
    }
    
    public void sendNoPermissionError(CommandSender cs) {
        sendErrorMessage(cs, "Sorry, you don't have permission.");
    }

    public void sendInvalidSubcommandError(CommandSender cs) {
        sendErrorMessage(cs, "Invalid subcommand.");
    }
    
    public void sendNotEnoughArgumentsError(CommandSender cs) {
        sendErrorMessage(cs, "Not enough arguments.");
    }

    public void sendFileNotFoundError(CommandSender cs) {
        sendErrorMessage(cs, "File not found.");
    }

    public void sendIOError(CommandSender cs) {
        sendErrorMessage(cs, "There was an IOError. Ask developer or admin for help.");
    }

}


