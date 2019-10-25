/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil;

import java.util.List;

/**
 *
 * @author Eriol_Eandur
 */
public class StringUtil {
    
    public static String concat(String[] strings) {
        String result = "";
        for(String str : strings) {
            result += str + " ";
        }
        return result;
    }
    
    public static int parseInt(String string) {
        int radius;
        try {
            radius = Integer.parseInt(string);
        }
        catch(NumberFormatException e) {
            radius = 0;
        }
        return radius;
    }
    
    public static String concat(List<String> list, String connection) {
        String result = "";
        for(String entry: list) {
            result = result + connection + entry;
        }
        return (result.length()>connection.length()?result.substring(connection.length()):result);
    }

}
