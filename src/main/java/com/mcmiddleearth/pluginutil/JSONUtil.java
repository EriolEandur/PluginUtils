/*
 * Copyright (C) 2015 MCME
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
package com.mcmiddleearth.pluginutil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.simple.JSONObject;

/**
 *
 * @author Eriol_Eandur
 */
public class JSONUtil {
    
    public static JSONObject jLocation(Location loc) {
        JSONObject jObject = new JSONObject();
        jObject.put("world", loc.getWorld().getName());
        jObject.put("x", loc.getX());
        jObject.put("y", loc.getY());
        jObject.put("z", loc.getZ());        
        jObject.put("pitch", loc.getPitch());
        jObject.put("yaw", loc.getYaw());
        return jObject;
    }
    
    public static boolean getBoolean(JSONObject jObject, String key){
        Object input = jObject.get(key);
        if(input==null){
            return false;
        }
        return (Boolean) input;
    }
    
    public static int getInteger(JSONObject jObject, String key){
        Object input = jObject.get(key);
        if(input==null){
            return 0;
        }
        return ((Long) input).intValue();
    }
    
    public static String getString(JSONObject jObject, String key){
        Object input = jObject.get(key);
        if(input==null){
            return "";
        }
        return (String) input;
    }
    
    public static Location getLocation(JSONObject object, String key){
        JSONObject jObject = (JSONObject) object.get(key);
        if(jObject==null){
            return null;
        }
        World world = Bukkit.getWorld((String) jObject.get("world"));
        if(world == null) {
            return null;
        }
        Location loc =  new Location(world,  (Double) jObject.get("x"), 
                                             (Double) jObject.get("y"), 
                                             (Double) jObject.get("z"), 
                                             ((Double) jObject.get("yaw")).floatValue(),
                                             ((Double) jObject.get("pitch")).floatValue());
        return loc;
    }
    
}
