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
package com.mcmiddleearth.pluginutil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Utiliy class for NMS methods to a PlayerConnection using reflection
 * @author Eriol_Eandur
 */
public class NMSUtil {
    
    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + "." + name);
    }

    public static Class<?> getCraftBukkitClass(String name) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
    }

    public static void sendPacket(Player player, Object packet) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, ClassNotFoundException {
        Object handle = player.getClass().getMethod("getHandle").invoke(player);
        Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
        playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
    }

    public static Object getTileEntity(Block block) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        Object nmsWorld = block.getWorld().getClass().getMethod("getHandle")
                                                          .invoke(block.getWorld());
        Object blockPosition = NMSUtil.getNMSClass("BlockPosition").getConstructor(int.class,int.class,int.class)
                                      .newInstance(block.getX(),block.getY(),block.getZ());
        return nmsWorld.getClass().getMethod("getTileEntity", blockPosition.getClass())
                                  .invoke(nmsWorld, blockPosition);
    }
    
    public static void updatePlayerChunks(Location low, Location high) {
        if(true) return; //not implemented in 1.14
        Object nmsWorld = NMSUtil.invokeCraftBukkit("CraftWorld","getHandle",null,low.getWorld());
        Object pcm = NMSUtil.invokeNMS("WorldServer", "getPlayerChunkMap",null,nmsWorld);
        for(int x=low.getChunk().getX();x<=high.getChunk().getX();x++) {
            for(int z = low.getChunk().getZ();z<=high.getChunk().getZ();z++) {
                Object pc = NMSUtil.invokeNMS("PlayerChunkMap","getChunk",
                                              new Class[]{int.class,int.class},pcm,x, z);
                if(pc!=null) {
                    for(Object player: (Iterable)NMSUtil.getNMSField("PlayerChunk","players", pc)) {
                        NMSUtil.invokeNMS("PlayerChunk", "sendChunk", null, pc, player);
                    }
                }
            }
        }
    }
    
    public static void updatePlayerChunks(Player player, Location low, Location high) {
        Object nmsWorld = NMSUtil.invokeCraftBukkit("CraftWorld","getHandle",null,low.getWorld());
        Object pcm = NMSUtil.invokeNMS("WorldServer", "getPlayerChunkMap",null,nmsWorld);
        for(int x=low.getChunk().getX();x<=high.getChunk().getX();x++) {
            for(int z = low.getChunk().getZ();z<=high.getChunk().getZ();z++) {
                Object pc = NMSUtil.invokeNMS("PlayerChunkMap","getChunk",
                                              new Class[]{int.class,int.class},pcm,x, z);
                if(pc!=null) {
                    Object nmsPlayer = NMSUtil.invokeCraftBukkit("entity.CraftPlayer", "getHandle", null, player);
                    NMSUtil.invokeNMS("PlayerChunk", "sendChunk", null, pc, nmsPlayer);
                    /*for(Object nmsPlayer: (Iterable)NMSUtil.getNMSField("PlayerChunk","players", pc)) {
                        if(player==null 
                                || (NMSUtil.getCraftBukkitClass(("")NMSUtil.invokeNMS("EntityPlayer","getBukkitEntity",null,nmsPlayer)
                                          .getUniqueId().equals(player.getUniqueId())) {
                        }
                    }*/
                }
            }
        }
    }
    
    public static Object invokeCraftBukkit(String className, String methodName, Class[] argsClasses, 
                                           Object object, Object... args) {
        try {
            Class clazz = getCraftBukkitClass(className);
            return invoke(clazz,methodName, argsClasses, object, args);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(NMSUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static Object invokeNMS(String className, String methodName, Class[] argsClasses, 
                                   Object object, Object... args) {
        try {
            Class clazz = getNMSClass(className);
            return invoke(clazz,methodName, argsClasses, object, args);
        } catch (ClassNotFoundException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
            Logger.getLogger(NMSUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Object invoke(Class<?> clazz, String methodName, Class[] argsClasses, 
                                Object object, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        if(argsClasses==null) {
            argsClasses = new Class[args.length];
            for(int i=0; i<args.length; i++) {
                argsClasses[i] = args[i].getClass();
            }
        }
        Method method;
        try {
            method = clazz.getMethod(methodName, argsClasses);
        } catch (NoSuchMethodException ex) {
            method = clazz.getDeclaredMethod(methodName, argsClasses);
        }
        return method.invoke(object, args);
    }
    
    public static Object createNMSObject(String className,Class[] argsClasses,Object... args) {
        try {
            Class clazz = getNMSClass(className);
            if(argsClasses==null) {
                argsClasses = new Class[args.length];
                for(int i=0; i<args.length; i++) {
                    argsClasses[i] = args[i].getClass();
                }
            }
            Constructor constr = clazz.getConstructor(argsClasses);
            return constr.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(NMSUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Object getNMSField(String className, String fieldName, Object object) {
        try {
            Class clazz = getNMSClass(className);
            Field field = clazz.getField(fieldName);
            return field.get(object);
                    
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(NMSUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Object getCraftBukkitField(String className, String fieldName, Object object) {
        try {
            Class clazz = getCraftBukkitClass(className);
            Field field = clazz.getField(fieldName);
            return field.get(object);
                    
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(NMSUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Object getCraftBukkitDeclaredField(String className, String fieldName, Object object) {
        try {
            Class clazz = getCraftBukkitClass(className);
//for(Field field : clazz.getDeclaredFields()) {
//    Logger.getGlobal().info("Field: *"+field.getName()+"* searching for: *"+fieldName+"*");
//}
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
                    
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(NMSUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Vector toVector(Object blockPosition) {
        return new Vector((int) invokeNMS("BaseBlockPosition","getX",null,blockPosition),
                          (int) invokeNMS("BaseBlockPosition","getY",null,blockPosition),
                          (int) invokeNMS("BaseBlockPosition","getZ",null,blockPosition));
    }                       
    
    public static Object toBlockPosition(Vector vector) {
        return createNMSObject("BlockPosition",
                               new Class[]{int.class,int.class,int.class},
                               vector.getBlockX(),
                               vector.getBlockY(),
                               vector.getBlockZ());
    }
    
    public static void calcLight(Location loc) {
        Object blockPosition = NMSUtil.createNMSObject("BlockPosition", new Class[]{int.class,int.class,int.class}, 
                                                       loc.getBlockX(),loc.getBlockY(), loc.getBlockZ());
//Logger.getGlobal().info("blocPosition "+blockPosition);
        Chunk chunk = loc.getChunk();
        Object nmsChunk = NMSUtil.invokeCraftBukkit("CraftChunk", "getHandle", new Class[0], chunk);
//Logger.getGlobal().info("nmsChunk"+nmsChunk);
        Object lightEngine = NMSUtil.invokeNMS("Chunk", "e", new Class[0], nmsChunk);
//Logger.getGlobal().info("sync "+Bukkit.isPrimaryThread());
//Logger.getGlobal().info("lightEngine "+lightEngine);
//Logger.getGlobal().info("getLight: "+NMSUtil.invokeNMS("LightEngine", "b", new Class[]{blockPosition.getClass(),int.class}, lightEngine, blockPosition, 0));
        NMSUtil.invokeNMS("LightEngine", "a", new Class[]{blockPosition.getClass()}, lightEngine, blockPosition);
    }
    
    public static void calcLight(Chunk chunk, List<Vector> positions) {
        Object nmsChunk = NMSUtil.invokeCraftBukkit("CraftChunk", "getHandle", new Class[0], chunk);
//Logger.getGlobal().info("nmsChunk"+nmsChunk);
        Object lightEngine = NMSUtil.invokeNMS("Chunk", "e", new Class[0], nmsChunk);
//Logger.getGlobal().info("sync "+Bukkit.isPrimaryThread());
//Logger.getGlobal().info("lightEngine "+lightEngine);
//Logger.getGlobal().info("getLight: "+NMSUtil.invokeNMS("LightEngine", "b", new Class[]{blockPosition.getClass(),int.class}, lightEngine, blockPosition, 0));
        positions.forEach(pos -> {
//Logger.getGlobal().info("blocPosition "+((chunk.getX()<<4)+pos.getBlockX())+" "+pos.getBlockY()+" "+((chunk.getZ()<<4)+pos.getBlockZ()));
            Object blockPosition = NMSUtil.createNMSObject("BlockPosition", new Class[]{int.class,int.class,int.class}, 
                                                           (chunk.getX() << 4) + pos.getBlockX(), pos.getBlockY(), (chunk.getZ() << 4) + pos.getBlockZ());
            NMSUtil.invokeNMS("ILightEngine", "a", new Class[]{blockPosition.getClass(),boolean.class}, lightEngine, blockPosition,true);
        });
    }
}
