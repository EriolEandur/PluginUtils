/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.message;

import com.google.gson.JsonObject;
import com.mcmiddleearth.pluginutil.message.config.FancyMessageConfigUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * This class provides an easy to use way to send clickable and tooltipped text chat messages to players.
 * When a player hovers with mouse cursor over a tooltipped message he will see the tooltip text.
 * When he clicks at a clickable message he will get the associated text in chat.
 * @author Eriol_Eandur
 */
public final class FancyMessage {

    private final List<String[]> data = new ArrayList<>();

    private boolean runDirect = false;
    
    private ChatColor baseColor;
    
    private MessageUtil messageUtil;

    /**
     * Create a new fancy info message.
     * @param messageUtil MessageUtil object to handle sending of the message.
     */
    public FancyMessage(MessageUtil messageUtil) {
        this(MessageType.INFO, messageUtil);
    }
    
    /**
     * Create a new fancy message of any type.
     *
     * @param messageType Type of the message (default color and prefix)
     * @param messageUtil MessageUtil object to handle sending of the message.
     */
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
    
    /**
     * Create a new fancy message of any type and base color.
     *
     * @param messageType Type of the message (default color and prefix)
     * @param messageUtil MessageUtil object to handle sending of the message.
     * @param baseColor Defaut color to use.
     */
    public FancyMessage(MessageType messageType, MessageUtil messageUtil, ChatColor baseColor) {
        this(messageType, messageUtil);
        this.baseColor = baseColor;
    }

    public ChatColor getBaseColor() {
        return baseColor;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    /**
     * Append a simple text to the message which is not tooltipped and not clickable.
     * @param text Text to append to the message
     * @return Message with new text appended
     */
    public FancyMessage addSimple(String text){
        data.add(new String[]{text,null,null});
        return this;
    }

    /**
     * Append a clickable text to the message.
     * @param text Text to append to the message
     * @param onClickCommand Text to put into player chat when he clicks the text
     * @return Message with new text appended
     */
    public FancyMessage addClickable(String text, String onClickCommand) {
        data.add(new String[]{text,onClickCommand,null});
        return this;
    }

    /**
     * Append a clickable text to the message.
     * @param text Text to append to the message
     * @param onHoverText Text to disply when a player hovers the mouse cursor over the text
     * @return Message with new text appended
     */
    public FancyMessage addTooltipped(String text, String onHoverText) {
        data.add(new String[]{text,null,onHoverText});
        return this;
    }

    /**
     * Append a clickable and tooltipped text to the message.
     * @param text Text to append to the message
     * @param onClickCommand Text to put into player chat when he clicks the text
     * @param onHoverText Text to disply when a player hovers the mouse cursor over the text
     * @return Message with new text appended
     */
    public FancyMessage addFancy(String text, String onClickCommand, String onHoverText) {
        data.add(new String[]{text,onClickCommand,onHoverText});
        return this;
    }
    
    /**
     * Defines a new default color which will be used for each new line.
     * @param color new default color
     * @return same message
     */
    public FancyMessage setBaseColor(ChatColor color) {
        baseColor = color;
        return this;
    }
    
    /**
     * Clicking at the message will excecute the associated text as a command instead of
     * puting it in text chat.
     * @return same message
     */
    public FancyMessage setRunDirect() {
        this.runDirect = true;
        return this;
    }

    /**
     * Send a fancy message to a player.
     * @param recipient Player who will get the message.
     * @return same message
     */
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
        MessageUtil.sendRawMessage(recipient, rawText);
        return this;
    }
    
    /**
     * Store the fancy message in a configuration.
     * @param config where to store the message
     */
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

    public List<String[]> getData() {
        return data;
    }

    public boolean isRunDirect() {
        return runDirect;
    }

    public JsonObject parseJson() {
        return JsonMessageParser.parse(this);

    }
}