/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.message;

import static com.mcmiddleearth.pluginutil.message.MessageUtil.sendRawMessage;
import com.mcmiddleearth.pluginutil.message.config.FancyMessageConfigUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public final class FancyMessage {

    private final List<String[]> data = new ArrayList<>();

    private boolean runDirect = false;
    
    @Getter
    private ChatColor baseColor;
    
    @Getter
    private MessageUtil messageUtil;

    public FancyMessage(MessageUtil messageUtil) {
        this(MessageType.INFO, messageUtil);
    }
    
    public FancyMessage(MessageType messageType, MessageUtil messageUtil) {
        baseColor = messageType.getBaseColor();
        this.messageUtil = messageUtil;
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
    
    public FancyMessage(MessageType messageType, MessageUtil messageUtil, ChatColor baseColor) {
        this(messageType, messageUtil);
        this.baseColor = baseColor;
    }

    
    public FancyMessage addSimple(String text){
        data.add(new String[]{text,null,null});
        return this;
    }

    public FancyMessage addClickable(String text, String onClickCommand) {
        data.add(new String[]{text,onClickCommand,null});
        return this;
    }

    public FancyMessage addTooltipped(String text, String onHoverText) {
        data.add(new String[]{text,null,onHoverText});
        return this;
    }

    public FancyMessage addFancy(String text, String onClickCommand, String onHoverText) {
        data.add(new String[]{text,onClickCommand,onHoverText});
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
        String rawText = "[";
        String action;
        if(runDirect) {
            action = "run_command";
        } else {
            action = "suggest_command";
        }
        boolean first = true;
        for(String[] messageData: data) {
            String message = messageData[0];
            String command = messageData[1];
            String hoverText = messageData[2];
                if(message.contains("\"")) {
                }
            message = replaceQuotationMarks(message);
                if(message.contains("\"")) {
                }
            if(first) {
                first = false; 
            }
            else {
                rawText = rawText.concat(",");
            }
            rawText = rawText.concat("{\"text\":\""+message+"\",\"color\":\""+baseColorString()+"\"");
            if(command!=null) {
                String thisAction = action;
                if(command.startsWith("http")) {
                    thisAction = "open_url";
                }
                command = replaceQuotationMarks(command);
                rawText = rawText.concat(",\"clickEvent\":{\"action\":\""+thisAction+"\",\"value\":\"");
                rawText = rawText.concat(command+"\"}");
                //clickEvent = true;
            }
            if(hoverText!=null) {
                hoverText = replaceQuotationMarks(hoverText);
                if(hoverText.contains("\"")) {
                }
                //if(clickEvent) {
                //    rawText = rawText.concat(",");
                //}
                rawText = rawText.concat(",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"");
                rawText = rawText.concat(hoverText+"\"}");
            }
            rawText = rawText.concat("}");
        }
        rawText = rawText.concat("]");
        sendRawMessage(recipient, rawText);
        return this;
    }
    
    public void saveToConfig(ConfigurationSection config) {
        FancyMessageConfigUtil.store(data, config);
    }
    
    private String replaceQuotationMarks(String string) {
        return string.replaceAll("\"", "__stRe__\"").replaceAll("__stRe__", "\\\\");
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