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
package com.mcmiddleearth.pluginutil.message.config;

import com.mcmiddleearth.pluginutil.message.FancyMessage;
import com.mcmiddleearth.pluginutil.message.MessageType;
import com.mcmiddleearth.pluginutil.message.MessageUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Eriol_Eandur
 */
public class FancyMessageConfigUtil {
    
    public static FancyMessage newFromConfig(ConfigurationSection config, MessageUtil messageUtil, MessageType type) throws MessageParseException {
        FancyMessage message = new FancyMessage(type, messageUtil);
        return addFromConfig(message, config);
    }
    
    public static FancyMessage newFromConfig(ConfigurationSection config, MessageUtil messageUtil, MessageType type, ChatColor baseColor) throws MessageParseException {
        FancyMessage message = new FancyMessage(type, messageUtil, baseColor);
        return addFromConfig(message, config);
    }
    
    public static FancyMessage addFromConfig(FancyMessage message, ConfigurationSection config) 
                                                            throws MessageParseException {
//Logger.getGlobal().info("Start addFromConfig ****** "+ (config!=null));
        List<String> lines = config.getStringList("message");
        return addFromStringList(message, lines);
    }
    
    public static FancyMessage addFromStringList(FancyMessage message, List<String> lines)  
                                                            throws MessageParseException {
        if(lines == null) {
            return message;
        }
        String messageData="";
        for(String cLine: lines) {
            messageData = messageData.concat(cLine+" ");
        }
        String line = messageData;
//Logger.getGlobal().info("parse FancyMessage: "+line);
        List<MessageToken> tokenList = tokenise(line);
        Iterator<MessageToken> tokenIterator = tokenList.iterator();
        boolean inClickable = false;
        boolean inTooltipped = false;
        String clickCommand = "";
        String toolTip = "";
        while(tokenIterator.hasNext()) {
            MessageToken currentToken = tokenIterator.next();
//Logger.getGlobal().info("CurrentToken: "+currentToken.getType().toString()+" - "+currentToken.getText());
            if(!inClickable && !inTooltipped) {
                switch(currentToken.getType()) {
                    case TEXT:
                        message.addSimple(currentToken.getText());
//Logger.getGlobal().info("ADDSIMPLE - "+currentToken.getText());
                        break;
                    case CLICK_START:
                        inClickable = true;
                        if(tokenIterator.hasNext()) {
                            currentToken = tokenIterator.next();
                            if(currentToken.getType().equals(MessageTokenType.TEXT)) {
                                clickCommand=currentToken.getText();
                            } else {
                                throw new MessageParseException("Text token with onClick command expected.");
                            }
                        } else {
                            throw new MessageParseException("Token after onClick start token expected.");
                        }
                        break;
                    case HOVER_START:
                        inTooltipped = true;
                        if(tokenIterator.hasNext()) {
                            currentToken = tokenIterator.next();
                            if(currentToken.getType().equals(MessageTokenType.TEXT)) {
                                toolTip=message.getMessageUtil().hoverFormat(currentToken.getText(),"",false);
                            } else {
                                throw new MessageParseException("Text token with tooltip expected.");
                            }
                        } else {
                            throw new MessageParseException("Token after onHover start token expected.");
                        }
                        break;
                    default:
                        throw new MessageParseException("Unexpected Token (simple state): "+currentToken.getType().toString());
                }
            } else if(inClickable && !inTooltipped) {
                switch(currentToken.getType()) {
                    case TEXT:
                        message.addClickable(currentToken.getText(),clickCommand);
//Logger.getGlobal().info("ADD CLICKABLE - **"+currentToken.getText()+"** **"+clickCommand);
                        break;
                    case CLICK_END:
                        inClickable = false;
                        break;
                    case HOVER_START:
                        inTooltipped = true;
                        if(tokenIterator.hasNext()) {
                            currentToken = tokenIterator.next();
                            if(currentToken.getType().equals(MessageTokenType.TEXT)) {
                                toolTip=message.getMessageUtil().hoverFormat(currentToken.getText(),"",false);
                            } else {
                                throw new MessageParseException("Text token with tooltip expected.");
                            }
                        } else {
                            throw new MessageParseException("Token after onHover start token expected.");
                        }
                        break;
                    default:
                        throw new MessageParseException("Unexpected Token (clickable state): "+currentToken.getType().toString());
                }    
            } else if(!inClickable && inTooltipped) {
                switch(currentToken.getType()) {
                    case TEXT:
                        message.addTooltipped(currentToken.getText(), toolTip);
//Logger.getGlobal().info("ADD HOVER - **"+currentToken.getText()+"** **"+toolTip);
                        break;
                    case HOVER_END:
                        inTooltipped = false;
                        break;
                    case CLICK_START:
                        inClickable = true;
                        if(tokenIterator.hasNext()) {
                            currentToken = tokenIterator.next();
                            if(currentToken.getType().equals(MessageTokenType.TEXT)) {
                                clickCommand=currentToken.getText();
                            } else {
                                throw new MessageParseException("Text token with onClick command expected.");
                            }
                        } else {
                            throw new MessageParseException("Token after onClick start token expected.");
                        }
                        break;
                    default:
                        throw new MessageParseException("Unexpected Token (tooltipped state): "+currentToken.getType().toString());
                }
            } else {
                switch(currentToken.getType()) {
                    case TEXT:
                        message.addFancy(currentToken.getText(), clickCommand, toolTip);
//Logger.getGlobal().info("ADD FANCY - **"+currentToken.getText()+"** **"+clickCommand+"** **"+toolTip);
                        break;
                    case HOVER_END:
                        inTooltipped = false;
                        break;
                    case CLICK_END:
                        inClickable = false;
                        break;
                    default:
                        throw new MessageParseException("Unexpected Token (fancy state): "+currentToken.getType().toString());
                }
            }
        }
        return message;
    }
        
    private static final String clickStart = "[Click=\"";
    private static final String clickEnd = "[/Click]";
    private static final String hoverStart = "[Hover=\"";
    private static final String hoverEnd = "[/Hover]";
    private static final String endOfStartTag = "\"]";
    
    private static List<MessageToken> tokenise(String line) {
        List<MessageToken> tokenList = new ArrayList<>();
//Logger.getGlobal().info("    TOKENIZE: "+line);
        while(line.length()>0) {
            int clStart = firstAppereance(line,clickStart);
            int hoStart = firstAppereance(line,hoverStart);
            int clEnd = firstAppereance(line,clickEnd);
            int hoEnd = firstAppereance(line,hoverEnd);
            int firstPosition = Math.min(Math.min(clStart, hoStart),
                                         Math.min(clEnd, hoEnd));
            if(firstPosition > 0) {
                tokenList.add(new MessageToken(line.substring(0,Math.min(firstPosition,line.length()))));
                line = line.substring(Math.min(firstPosition,line.length()));
//Logger.getGlobal().info("    ADDEDTEXT: "+tokenList.get(tokenList.size()-1).getText());
            } else {
                if(clStart == 0) {
                    tokenList.add(new MessageToken(MessageTokenType.CLICK_START));
                    line = line.substring(8);
                    int endPosition = Math.min(firstAppereance(line,endOfStartTag),line.length());
                    tokenList.add(new MessageToken(line.substring(0,endPosition)));
                    line = line.substring(endPosition+2);
//Logger.getGlobal().info("   ADDED CLICK START");
                } else if(clEnd == 0) {
                    tokenList.add(new MessageToken(MessageTokenType.CLICK_END));
                    line = line.substring(8);
//Logger.getGlobal().info("   ADDED CLICK END");
                } else if(hoStart == 0) {
                    tokenList.add(new MessageToken(MessageTokenType.HOVER_START));
                    line = line.substring(8);
                    int endPosition = Math.min(firstAppereance(line,endOfStartTag),line.length());
                    tokenList.add(new MessageToken(line.substring(0,endPosition)));
                    line = line.substring(endPosition+2);
//Logger.getGlobal().info("   ADDED HOVER START");
                } else {
                    tokenList.add(new MessageToken(MessageTokenType.HOVER_END));
                    line = line.substring(8);
//Logger.getGlobal().info("   ADDED HOVER END");
                }
            }
        }
    return tokenList;        
    }
        
    private static int firstAppereance(String line, String search) {
        int index = line.indexOf(search);
        return (index==-1?Integer.MAX_VALUE:index);
    }
    
    public static void store(List<String[]> data, ConfigurationSection config) {
        String serialised = "";
        for(String[] msgData : data) {
            String text = msgData[0];
            if(msgData[1]==null && msgData[2]==null) {
                serialised = serialised.concat(text);
            } else  if(msgData[1]!=null && msgData[2]==null) {
                serialised = serialised.concat(clickStart+msgData[1]+endOfStartTag+text+clickEnd);
            } else  if(msgData[1]==null && msgData[2]!=null) {
                serialised = serialised.concat(hoverStart+msgData[2]+endOfStartTag+text+hoverEnd);
            } else {
                serialised = serialised.concat(hoverStart+msgData[2]+endOfStartTag
                                              +clickStart+msgData[1]+endOfStartTag+text+clickEnd);
            }
        }
        config.set("message", serialised);
    }

    
}
