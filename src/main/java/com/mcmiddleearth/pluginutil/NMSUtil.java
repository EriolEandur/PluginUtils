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

import java.lang.reflect.InvocationTargetException;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Utiliy class for NMS methods to a PlayerConnection using reflection
 * @author Eriol_Eandur
 */
public class NMSUtil {
    
    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + "." + name);
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
}
