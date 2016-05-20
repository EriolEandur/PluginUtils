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
package com.mcmiddleearth.pluginutils;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.EulerAngle;

/**
 *
 * @author Eriol_Eandur
 */
public class ConfigurationUtil {
    
    public static Map<String,Object> serializeLocation(Location loc) {
        Map<String,Object> result = new HashMap<>();
        result.put("x", loc.getX());
        result.put("y", loc.getY());
        result.put("z", loc.getZ());
        result.put("yaw", loc.getYaw());
        result.put("pitch", loc.getPitch());
        result.put("world", loc.getWorld().getName());
        return result;
    }
    
    public static Location deserializeLocation(Map<String,Object> data) {
        World world = Bukkit.getWorld((String) data.get("world"));
        if(world == null) {
            return null;
        }
        else {
            return new Location(world, getDouble(data,"x"), 
                                       getDouble(data,"y"),
                                       getDouble(data,"z"),
                                       getFloat(data,"yaw"),
                                       getFloat(data,"pitch"));
        }
    }
    
    public static Map<String,Object> serializeEulerAngle(EulerAngle angle) {
        Map<String,Object> result = new HashMap<>();
        result.put("x", angle.getX());
        result.put("y", angle.getY());
        result.put("z", angle.getZ());
        return result;
    }
    
    public static EulerAngle deserializeEulerAngle(Map<String,Object> data) {
        return new EulerAngle((Double) data.get("x"),
                              (Double) data.get("y"),
                              (Double) data.get("z"));
    }
    
    public static Map<String,Object> getMap(Map<String,Object> data, String key) {
        Object value = data.get(key);
        if(value instanceof ConfigurationSection) {
            return ((ConfigurationSection)value).getValues(true);
        }
        else {
            return (Map<String,Object>) value;
        }
    }
    
    private static double getDouble(Map<String,Object> data, String key) {
        Object value = data.get(key);
        if(value instanceof Float) {
            return ((Float)value).doubleValue();
        }
        if(value instanceof Double) {
            return (Double) value;
        }
        if(value instanceof Integer) {
            return ((Integer)value).doubleValue();
        }
        return 0;
    }
    
    private static float getFloat(Map<String,Object> data, String key) {
        Object value = data.get(key);
        if(value instanceof Float) {
            return (Float)value;
        }
        if(value instanceof Double) {
            return ((Double) value).floatValue();
        }
        if(value instanceof Integer) {
            return ((Integer)value).floatValue();
        }
        return 0;
    }
}
