package com.mcmiddleearth.pluginutil.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.json.simple.JSONObject;

public class JsonMessageParser {

    public static JsonObject parse(FancyMessage message) {
        JsonObject result = new JsonObject();
        result.addProperty("color",message.getBaseColor()+"");
        //result.add("text", new JsonPrimitive(""));
        JsonArray extra = new JsonArray();
        result.add("extra", extra);
        for(String[] data: message.getData()) {
            JsonObject part = new JsonObject();
            extra.add(part);
                part.add("extra",parseColoredText(data[0]));

                JsonObject hover = new JsonObject();
                part.add("hoverEvent",hover);
                    JsonObject showText = new JsonObject();
                    hover.add("showText",showText);
                        showText.add("extra",parseColoredText(data[2]));

                JsonObject click = new JsonObject();
                part.add("clickEvent",click);
                    click.addProperty("action", (data[1].startsWith("http")?"open_url":
                                                                 (message.isRunDirect()?"run_command":"suggest_command")));
                    click.addProperty("value",data[1]);
        }
        return result;
    }

    public static void main(String[] args) {
        JsonObject obj = parseColoredText("#ff0099Hey &c\\&Du#00aaff&l");
        //obj = parseColoredText("#ff0099Hey");
        System.out.println(obj.toString());
    }

    public static JsonObject parseColoredText(String text) {
        JsonObject result = new JsonObject();
        JsonObject current = result;
        boolean firstPart = true;
        Format status = new Format();
        text = text.replaceAll("§","&");
        text = text.replaceAll("\\\\#","§");
        String[] newColorSplit = text.split("#");
        String color = "";
        for(int i = 0; i < newColorSplit.length; i++) {
            newColorSplit[i] = newColorSplit[i].replace('§','#');
            if(newColorSplit[i].length()>0) {
                if(i > 0) {
                    color = "#"+newColorSplit[i].substring(0,6);
                    newColorSplit[i] = newColorSplit[i].substring(6);
                }
                newColorSplit[i] = newColorSplit[i].replaceAll("\\\\&","§");
                String[] oldColorSplit = newColorSplit[i].split("&");
                for(int j = 0; j < oldColorSplit.length; j++) {
                    if(!firstPart) {
                        JsonArray extra = new JsonArray();
                        current.add("extra",extra);
                        current = new JsonObject();
                        extra.add(current);
                    } else {
                        firstPart = false;
                    }
                    if (j == 0) {
                        if(!color.equals("")) {
                            current.addProperty("color", color);
                            color = "";
                        }
                    } else {
                        char formattingCode = oldColorSplit[j].charAt(0);
                        switch(formattingCode) {
                            case 'k': status.obfuscated = true; current.addProperty("obfuscated",true); break;
                            case 'l': status.bold = true; current.addProperty("bold",true); break;
                            case 'm': status.strikethrough = true; current.addProperty("strikethrough",true); break;
                            case 'n': status.underline = true; current.addProperty("underline",true); break;
                            case 'o': status.italic = true; current.addProperty("italic",true); break;
                            case 'r':
                            case 'f': setColor(current, status,"white"); break;
                            case '0': setColor(current, status,"black"); break;
                            case '1': setColor(current, status,"dark_blue"); break;
                            case '2': setColor(current, status,"dark_green"); break;
                            case '3': setColor(current, status,"dark_cyan"); break;
                            case '4': setColor(current, status,"dark_red"); break;
                            case '5': setColor(current, status,"purple"); break;
                            case '6': setColor(current, status,"gold"); break;
                            case '7': setColor(current, status,"gray"); break;
                            case '8': setColor(current, status,"dark_gray"); break;
                            case '9': setColor(current, status,"blue"); break;
                            case 'a': setColor(current, status,"green"); break;
                            case 'b': setColor(current, status,"aqua"); break;
                            case 'c': setColor(current, status,"red"); break;
                            case 'd': setColor(current, status,"light_purple"); break;
                            case 'e': setColor(current, status,"yellow"); break;
                        }
                        oldColorSplit[j] = oldColorSplit[j].substring(1);
                    }
                    if(oldColorSplit[j].length()>0) {
                        current.addProperty("text",oldColorSplit[j]);
                    }
                }
            }
        }
        return result;
    }

    private static void setColor(JsonObject current, Format status, String color) {
        if(status.obfuscated) current.addProperty("obfuscated",false);
        if(status.bold) current.addProperty("bold",false);
        if(status.strikethrough) current.addProperty("strikethrough",false);
        if(status.underline) current.addProperty("underline",false);
        if(status.italic) current.addProperty("italic",false);
    }

    private static class Format {
        public boolean obfuscated;
        public boolean bold;
        public boolean strikethrough;
        public boolean underline;
        public boolean italic;
    }
}
