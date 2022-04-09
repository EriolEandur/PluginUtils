/*
 * Copyright (C) 2019 Eriol_Eandur
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class NBTTagUtil {
 
    public static boolean hasKey(Object tag, String key) {
        try {
            if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
                return (Boolean) NMSUtil.invokeNMS("nbt.NBTTagCompound","e",
                                                   new Class[]{String.class},tag,key);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NBTTagUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public static Object getCompound(Object tag, String key) {
        try {
            if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
                Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","p"/*getCompound*/,
                                                   new Class[]{String.class},tag,key);
                return NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(result)?result:null;
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NBTTagUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Object getTagList(Object tag, String key) {
        try {
            if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
                Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","c"/*get*/,
                                                   new Class[]{String.class},tag,key);
                return NMSUtil.getNMSClass("nbt.NBTTagList").isInstance(result)?result:null;
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NBTTagUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Integer getInt(Object tag, String key) {
        try {
            if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
                Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","h"/*getInt*/,
                                                   new Class[]{String.class},tag,key);
                return result instanceof Integer?(Integer)result:null;
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NBTTagUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Short getShort(Object tag, String key) {
        try {
            if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
                Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","g"/*getShort*/,
                                                   new Class[]{String.class},tag,key);
                return result instanceof Short?(Short)result:null;
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NBTTagUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Long getLong(Object tag, String key) {
        try {
            if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
                Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","i"/*getLong*/,
                                                   new Class[]{String.class},tag,key);
                return result instanceof Long?(Long)result:null;
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NBTTagUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static String getString(Object tag, String key) {
        try {
            if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
                Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","l"/*getString*/,
                                                   new Class[]{String.class},tag,key);
                return result instanceof String?(String)result:null;
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NBTTagUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Float getFloat(Object tag, String key) {
        try {
            if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
                Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","j"/*getFloat*/,
                                                   new Class[]{String.class},tag,key);
                return result instanceof Float?(Float)result:null;
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NBTTagUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Double getDouble(Object tag, String key) {
        try {
            if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
                Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","k"/*getDouble*/,
                                                   new Class[]{String.class},tag,key);
                return result instanceof Double?(Double)result:null;
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NBTTagUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static String asString(Object tag) {
        try {
            if(NMSUtil.getNMSClass("nbt.NBTBase").isInstance(tag)) {
                return (String) NMSUtil.invokeNMS("nbt.NBTBase","toString",new Class[]{},tag);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NBTTagUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public static Object createNBTTagLong(long value) {
        return NMSUtil.invokeNMS("nbt.NBTTagLong","a",new Class[]{long.class},null,value);
    }
    
    public static Object createNBTTagInt(int value) {
        return NMSUtil.invokeNMS("nbt.NBTTagInt","a",new Class[]{int.class},null,value);
    }
    
    public static Object createNBTTagByte(byte value) {
        return NMSUtil.invokeNMS("nbt.NBTTagByte","a",new Class[]{byte.class},null,value);
    }
    
    public static Object createNBTTagFloat(float value) {
        return NMSUtil.invokeNMS("nbt.NBTTagFloat","a",new Class[]{float.class},null,value);
    }
    
    public static Object createNBTTagDouble(double value) {
        return NMSUtil.invokeNMS("nbt.NBTTagDouble","a",new Class[]{double.class},null,value);
    }
    
    public static Object createNBTCompound() {
        return NMSUtil.createNMSObject("nbt.NBTTagCompound",null);
    }

    public static Object createNBTTagIntArray(int[] values) {
        return NMSUtil.createNMSObject("nbt.NBTTagIntArray",new Class[]{int[].class}, (Object) values);
    }
}
