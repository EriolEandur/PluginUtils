/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.plotStoring;

import com.mcmiddleearth.pluginutil.NMSUtil;
import com.mcmiddleearth.pluginutil.PluginUtilsPlugin;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
/*import net.minecraft.server.v1_13_R2.EntityTypes;
import net.minecraft.server.v1_13_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_13_R2.NBTReadLimiter;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.TileEntity;*/
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
/*import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;*/
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/**
 *
 * @author Eriol_Eandur
 */
public class MCMEPlotFormat implements PlotStorageFormat {
    
    //private static final String entityExt = ".emcme";
    
    /*
     * Binary format:
     * (number of Bytes: content)
     * 4: Low Corner x
     * 4: Low Corner y
     * 4: Low Corner z
     * 4: Size x
     * 4: Size y
     * 4: Size z
     * 4: number of palette entries
     *     Palette entry:
     *     4:          <datalength> length of palette entry data
     *     <datalength>: blockData in UTF-8 charset
     * All blocks in area, ordered by x,y,z:
     *     Block Entry:
     *     4: Index of palette entries for this block
     * All Tile Entities in area
     *     4: number of Tile Entities
     *     variable: nbt data of Tile Entities
     * Other Entities in area (Paintings, Item Frames, Armor Stands)
     *     4: number of Entities
     *     variable: nbt data of Entities
    */
    
    @Override
    public void save(IStoragePlot plot, DataOutputStream out) throws IOException{
        try {
            /*String name = world.getName();
            byte[] nameBytes = name.getBytes(Charset.forName("UTF-8"));
            out.write(nameBytes.length);
            out.write(nameBytes);*/
            //List<Object> complexBlocks = new ArrayList<>();
            out.writeInt(plot.getLowCorner().getBlockX());
            out.writeInt(plot.getLowCorner().getBlockY());
            out.writeInt(plot.getLowCorner().getBlockZ());
            out.writeInt(plot.getHighCorner().getBlockX()-plot.getLowCorner().getBlockX()+1);
            out.writeInt(plot.getHighCorner().getBlockY()-plot.getLowCorner().getBlockY()+1);
            out.writeInt(plot.getHighCorner().getBlockZ()-plot.getLowCorner().getBlockZ()+1);
            Map<BlockData,Integer> paletteMap = new HashMap<>();
            List<BlockData> palette = new ArrayList<>();
            for(int x = plot.getLowCorner().getBlockX(); x <= plot.getHighCorner().getBlockX(); ++x) {
                for(int y = plot.getLowCorner().getBlockY(); y <= plot.getHighCorner().getBlockY(); ++y) {
                    for(int z = plot.getLowCorner().getBlockZ(); z <= plot.getHighCorner().getBlockZ(); ++z) {
                        Block block = plot.getWorld().getBlockAt(x, y, z);
                        if(paletteMap.get(block.getBlockData())==null) {
                            paletteMap.put(block.getBlockData(), palette.size());
                            palette.add(block.getBlockData());
                        }
                    }
                }
            }
            out.writeInt(palette.size()); //write length of palette
            for(int i=0; i<palette.size();i++) {
                String blockDataString = palette.get(i).getAsString();
                byte[] blockDataBytes = blockDataString.getBytes(Charset.forName("UTF-8"));
                out.writeInt(blockDataBytes.length); //write length of next blockdata
                out.write(blockDataBytes);
            }
            for(int x = plot.getLowCorner().getBlockX(); x <= plot.getHighCorner().getBlockX(); ++x) {
                for(int y = plot.getLowCorner().getBlockY(); y <= plot.getHighCorner().getBlockY(); ++y) {
                    for(int z = plot.getLowCorner().getBlockZ(); z <= plot.getHighCorner().getBlockZ(); ++z) {
                        Block block = plot.getWorld().getBlockAt(x, y, z);
                        out.writeInt(paletteMap.get(block.getBlockData()));
                    }
                }
            }
            List tileEntities = new ArrayList();
            for(int x = plot.getLowCorner().getBlockX(); x <= plot.getHighCorner().getBlockX(); ++x) {
                for(int y = plot.getLowCorner().getBlockY(); y <= plot.getHighCorner().getBlockY(); ++y) {
                    for(int z = plot.getLowCorner().getBlockZ(); z <= plot.getHighCorner().getBlockZ(); ++z) {
                        Block block = plot.getWorld().getBlockAt(x, y, z);
                        BlockState state = block.getState();
                        if(NMSUtil.getCraftBukkitClass("block.CraftBlockEntityState").isInstance(state)) {
                            Object nbt = NMSUtil.invokeCraftBukkit("block.CraftBlockEntityState","getSnapshotNBT",
                                                                   null, state);
//Logger.getGlobal().info("save nbt TileEntity: "+nbt.toString());
                            tileEntities.add(nbt);
                        }
                    }
                }
            }
            out.writeInt(tileEntities.size());
            for(Object nbt: tileEntities) {
                Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("NBTTagCompound"),DataOutput.class};
                NMSUtil.invokeNMS("NBTCompressedStreamTools","a",argsClasses,null,nbt,(DataOutput)out);
            }
            Collection<Entity> entities = plot.getWorld()
                 .getNearbyEntities(new BoundingBox(plot.getLowCorner().getBlockX(),
                                                    plot.getLowCorner().getBlockY(),
                                                    plot.getLowCorner().getBlockZ(),
                                                    plot.getHighCorner().getBlockX(),
                                                    plot.getHighCorner().getBlockY(),
                                                    plot.getHighCorner().getBlockZ()),
                        new MCMEEntityFilter());
            out.writeInt(entities.size());
            for(Entity entity: entities) {
                Object nbt = NMSUtil.createNMSObject("NBTTagCompound",null);
                Object nmsEntity = NMSUtil.invokeCraftBukkit("entity.CraftEntity", "getHandle",
                                                             null, entity);
                NMSUtil.invokeNMS("NBTTagCompound","setString",null, nbt,"id", 
                                  NMSUtil.invokeNMS("Entity","getSaveID",null, nmsEntity));
                nbt = NMSUtil.invokeNMS("Entity","save",null, nmsEntity,nbt);
                Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("NBTTagCompound"),DataOutput.class};
                NMSUtil.invokeNMS("NBTCompressedStreamTools","a",argsClasses,null,nbt,(DataOutput)out);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    @Override
    public void load(final IStoragePlot plot, DataInputStream in) throws IOException, InvalidRestoreDataException {
        Location loc = new Location(plot.getWorld(),plot.getLowCorner().getBlockX(),
                                                    plot.getLowCorner().getBlockY(),
                                                    plot.getLowCorner().getBlockZ());
        Vector size = new Vector(plot.getHighCorner().getBlockX()-plot.getLowCorner().getBlockX()+1,
                                 plot.getHighCorner().getBlockY()-plot.getLowCorner().getBlockY()+1,
                                 plot.getHighCorner().getBlockZ()-plot.getLowCorner().getBlockZ()+1);
        load(loc, size, in);
    }               
    
    /**
     * Loads MCME Storage Plot data from file and places it at the specified location.
     * If specified size doesn't fit with size of storage data an IOException is thrown.
     * @param location Where to place the restore data
     * @param size Expected size of restore data, may be null to skip size test
     * @param file File to read restore data from
     * @throws IOException 
     */
    @Override
    public void load(final Location location, Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException{
        try {
            Location originalLoc = new Location(location.getWorld(),in.readInt(),in.readInt(),in.readInt());
            final Vector shift = location.clone().subtract(originalLoc).toVector();
//Logger.getGlobal().info("Shift: "+shift.getBlockX()+" "+shift.getBlockY()+" "+shift.getBlockZ());
            Vector originalSize = new Vector(in.readInt(),in.readInt(),in.readInt());
            if(size==null) {
                size = originalSize;
            }
            if(!size.equals(originalSize)) {
                throw new InvalidRestoreDataException("Unexpected Restore Data Size");
            }
//Logger.getGlobal().info("Size: "+size.getBlockX()+" "+size.getBlockY()+" "+size.getBlockZ());
            Collection<Entity> entities = location.getWorld()
                     .getNearbyEntities(new BoundingBox(location.getBlockX(),
                                                        location.getBlockY(),
                                                        location.getBlockZ(),
                                                        location.getBlockX()+size.getBlockX()-1,
                                                        location.getBlockY()+size.getBlockY()-1,
                                                        location.getBlockZ()+size.getBlockZ()-1),
                            new MCMEEntityFilter());
            for(Entity entity: entities) {
                entity.remove();
            }
            
            int paletteLength = in.readInt();
//Logger.getGlobal().info("Palette: "+paletteLength);
            Map<Integer,BlockData> palette = new HashMap<>(paletteLength);
            for(int i = 0; i<paletteLength; i++) {
                int dataLength = in.readInt();
                byte[] byteData = new byte[dataLength];
                in.readFully(byteData);
                BlockData blockData = Bukkit.getServer().createBlockData(new String(byteData,Charset.forName("UTF-8")));
                palette.put(i, blockData);
            }
            for(int x = location.getBlockX(); x <= location.getBlockX()+size.getBlockX()-1; ++x) {
                for(int y = location.getBlockY(); y <= location.getBlockY()+size.getBlockY()-1; ++y) {
                    for(int z = location.getBlockZ(); z <= location.getBlockZ()+size.getBlockZ()-1; ++z) {
                        Location loc = new Location(location.getWorld(), x, y, z);
//Logger.getGlobal().info("Block: "+location.getWorld().getName()+" "+x+" "+y+" "+z+" "+loc.getBlock().getType());
                        loc.getBlock().setBlockData(palette.get(in.readInt()),false);
                    }
                }
            }
            int tileEntityLength = in.readInt();
            for(int i=0; i< tileEntityLength; i++) {
                Class[] argsClasses = new Class[]{DataInput.class,NMSUtil.getNMSClass("NBTReadLimiter")};
                Object nbt = NMSUtil.invokeNMS("NBTCompressedStreamTools","a",argsClasses,null,
                                               (DataInput)in,NMSUtil.getNMSField("NBTReadLimiter","a",null));  
                Object nmsWorld = NMSUtil.invokeCraftBukkit("CraftWorld", "getHandle", null, location.getWorld());
                argsClasses = new Class[]{NMSUtil.getNMSClass("NBTTagCompound"),
                                          NMSUtil.getNMSClass("World")};
                Object entity = NMSUtil.invokeNMS("TileEntity","create",argsClasses, null,nbt,nmsWorld);
                Object position = NMSUtil.invokeNMS("TileEntity", "getPosition", null, entity);
                argsClasses = new Class[]{double.class,double.class,double.class};
                Object newPosition = NMSUtil.invokeNMS("BlockPosition", "a", argsClasses, position, shift.getBlockX(),
                                                                                        shift.getBlockY(),
                                                                                        shift.getBlockZ());
                NMSUtil.invokeNMS("TileEntity", "setPosition", null, entity, newPosition);
                argsClasses = new Class[]{NMSUtil.getNMSClass("BlockPosition"),
                                          NMSUtil.getNMSClass("TileEntity")};
                NMSUtil.invokeNMS("WorldServer","setTileEntity",argsClasses,nmsWorld,
                                  NMSUtil.invokeNMS("TileEntity","getPosition",null,entity), entity);
//Logger.getGlobal().info("load nbt TileEntity: "+nbt.toString());
            }
            final int entityLength = in.readInt();
            final List entityDatas = new ArrayList();
            for(int i=0; i<entityLength; i++) {
                Class[] argsClasses = new Class[]{DataInput.class,NMSUtil.getNMSClass("NBTReadLimiter")};
                entityDatas.add(NMSUtil.invokeNMS("NBTCompressedStreamTools","a",argsClasses,
                                                  null,(DataInput)in,
                                NMSUtil.getNMSField("NBTReadLimiter","a",null)));
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    for(Object nbt: entityDatas) {
                        try {
                            Object list = NMSUtil.invokeNMS("NBTTagCompound","getList",
                                                new Class[]{String.class,int.class},
                                                nbt,"Pos",6);
                            Class[] argsClassesA = new Class[]{int.class,
                                                               NMSUtil.getNMSClass("NBTBase")};
                            Class[] argsClassesB = new Class[]{double.class};
                            Class[] argsClassesC = new Class[]{int.class};
                            NMSUtil.invokeNMS("NBTTagList","a",argsClassesA,list,
                                              0,NMSUtil.createNMSObject("NBTTagDouble",argsClassesB, 
                                                        ((double)NMSUtil.invokeNMS("NBTTagList", "k", 
                                                                argsClassesC,list,0))
                                                         +shift.getBlockX()));
                            NMSUtil.invokeNMS("NBTTagList","a",argsClassesA,list,
                                              1,NMSUtil.createNMSObject("NBTTagDouble",argsClassesB, 
                                                        ((double)NMSUtil.invokeNMS("NBTTagList", "k", 
                                                                argsClassesC,list,1))
                                                         +shift.getBlockY()));
                            NMSUtil.invokeNMS("NBTTagList","a",argsClassesA,list,
                                              2,NMSUtil.createNMSObject("NBTTagDouble",argsClassesB, 
                                                        ((double)NMSUtil.invokeNMS("NBTTagList", "k", 
                                                                argsClassesC,list,2))
                                                         +shift.getBlockZ()));
                            //list.a(1,new NBTTagDouble(list.k(1)+shift.getBlockY()));
                            //list.a(2,new NBTTagDouble(list.k(2)+shift.getBlockZ()));
                            NMSUtil.invokeNMS("NBTTagCompound","set",
                                              new Class[]{String.class,NMSUtil.getNMSClass("NBTBase")},
                                              nbt,"Pos",list);
                            Object nmsWorld = NMSUtil.invokeCraftBukkit("CraftWorld", "getHandle", null,
                                                                        location.getWorld());
                            Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("NBTTagCompound"),
                                                              NMSUtil.getNMSClass("World")};
                            Object entity = NMSUtil.invokeNMS("EntityTypes","a",argsClasses,null,nbt,nmsWorld);
                            /*Object position = NMSUtil.invokeNMS("Entity", "bI", null, entity);
                            argsClasses = new Class[]{double.class,double.class,double.class};
                            Object newPosition = NMSUtil.invokeNMS("Vec3D","add",argsClasses,position,shift.getBlockX(),
                                                                                             shift.getBlockY(),
                                                                                             shift.getBlockZ());
                            NMSUtil.invokeNMS("Entity", "setPosition", argsClasses, entity, 
                                              NMSUtil.getNMSField("Vec3D","x",newPosition),
                                              NMSUtil.getNMSField("Vec3D","y",newPosition),
                                              NMSUtil.getNMSField("Vec3D","z",newPosition));*/
                            argsClasses = new Class[]{NMSUtil.getNMSClass("Entity"),
                                                      CreatureSpawnEvent.SpawnReason.CUSTOM.getClass()};
                            NMSUtil.invokeNMS("WorldServer","addEntity",argsClasses,nmsWorld,entity, 
                                              CreatureSpawnEvent.SpawnReason.CUSTOM);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }.runTaskLater(PluginUtilsPlugin.getInstance(), 1);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class MCMEEntityFilter implements Predicate<Entity>{

        @Override
        public boolean test(Entity entity) {
            return entity instanceof Painting
                || entity instanceof ItemFrame
                || entity instanceof ArmorStand;
        }
    
    }
        
}
