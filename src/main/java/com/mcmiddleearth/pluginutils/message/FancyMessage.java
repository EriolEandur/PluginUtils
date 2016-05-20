/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutils.message;

import static com.mcmiddleearth.pluginutils.message.MessageUtil.sendRawMessage;
import java.util.LinkedHashMap;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public final class FancyMessage {

    private final LinkedHashMap<String,String[]> data = new LinkedHashMap<>();

    private boolean runDirect = false;
    
    @Getter
    private ChatColor baseColor;

    public FancyMessage(MessageUtil messageUtil) {
        this(MessageType.INFO, messageUtil);
    }
    
    public FancyMessage(MessageType messageType, MessageUtil messageUtil) {
        baseColor = messageType.getBaseColor();
        String prefix = "";
        switch(messageType) {
            case INFO:
            case ERROR:
            case HIGHLIGHT:
                prefix = messageUtil.getPREFIX();
                break;
            case INFO_INDENTED:
            case ERROR_INDENTED:
            case HIGHLIGHT_INDENTED:
                prefix = messageUtil.getNOPREFIX();
        }
        addSimple(prefix);
    }
    
    public FancyMessage addSimple(String text){
        data.put(text,new String[]{null,null});
        return this;
    }

    public FancyMessage addClickable(String text, String onClickCommand) {
        data.put(text,new String[]{onClickCommand,null});
        return this;
    }

    public FancyMessage addTooltipped(String text, String onHoverText) {
        data.put(text,new String[]{null,onHoverText});
        return this;
    }

    public FancyMessage addFancy(String text, String onClickCommand, String onHoverText) {
        data.put(text,new String[]{onClickCommand,onHoverText});
        return this;
    }

    public FancyMessage setBaseColor(ChatColor color) {
        baseColor = color;
        return this;
    }
    
    public FancyMessage setRunDirect() {
        this.runDirect = true;
        return this;
    }

    public FancyMessage send(Player recipient) {
    //public static void sendFancyMessage(Player sender, FancyMessage fancyMessage) {
    //    LinkedHashMap<String,String[]> data = fancyMessage.getData();
        String rawText = "[";
        String action;
        if(runDirect) {
            action = "run_command";
        } else {
            action = "suggest_command";
        }
        boolean first = true;
        for(String message: data.keySet()) {
            if(first) {
                first = false; 
            }
            else {
                rawText = rawText.concat(",");
            }
            rawText = rawText.concat("{\"text\":\""+message+"\",\"color\":\""+baseColorString()+"\"");
            String command = data.get(message)[0];
            boolean clickEvent = false;
            if(command!=null) {
                rawText = rawText.concat(",\"clickEvent\":{\"action\":\""+action+"\",\"value\":\"");
                rawText = rawText.concat(command+"\"}");
                clickEvent = true;
            }
            command = data.get(message)[1];
            if(command!=null) {
                if(clickEvent) {
                    rawText = rawText.concat(",");
                }
                rawText = rawText.concat("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"");
                rawText = rawText.concat(command+"\"}");
            }
            rawText = rawText.concat("}");
        }
        rawText = rawText.concat("]");
        sendRawMessage(recipient, rawText);
        return this;
    }
    
    private String baseColorString() {
        switch(baseColor) {
            case BLACK:
                return "black"; 
            case DARK_BLUE:
                return "dark_blue"; 
            case DARK_GREEN:
                return "dark_green"; 
            case DARK_AQUA:
                return "dark_aqua"; 
            case DARK_RED:
                return "dark_red"; 
            case DARK_PURPLE:
                return "dark_purple"; 
            case GOLD:
                return "gold"; 
            case GRAY:
                return "gray"; 
            case DARK_GRAY:
                return "dark_gray"; 
            case BLUE:
                return "blue";
            case GREEN:
                return "green"; 
            case AQUA:
                return "aqua"; 
            case RED:
                return "red"; 
            case LIGHT_PURPLE:
                return "light_purple"; 
            case YELLOW:
                return "yellow"; 
            case WHITE:
                return "white"; 
            case BOLD:
                return "bold"; 
            case UNDERLINE:
                return "underline"; 
            case ITALIC:
                return "italic"; 
            case STRIKETHROUGH:
                return "strikethrough";
            case MAGIC:
                return "obfuscated";
            default:
                return "reset";
        }
    }
}