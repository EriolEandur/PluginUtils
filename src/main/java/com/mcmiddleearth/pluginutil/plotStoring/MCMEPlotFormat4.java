/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.plotStoring;

import com.mcmiddleearth.pluginutil.NMSUtil;
import com.mcmiddleearth.pluginutil.PluginUtilsPlugin;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class MCMEPlotFormat4 implements PlotStorageFormat {
    
    //private static final String entityExt = ".emcme";
    
    /*
     * Binary format:
     * (number of Bytes: content)
     * 4: Version number
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
     * 4: number of biome palette entries
     *     4:          <datalength> length of biome palette entry data
     *     <datalength>: biome name in UTF-8 charset
     * All blocks in area, ordered by x,z,y:
     *     Before each x,z column:
     *     4: Index of biome palette entry of this column
     *     4: y coordinate of highest block in column
     *     For each in column:
     *     4: Index of palette entries for this block
     * All Tile Entities in area
     *     4: number of Tile Entities
     *     variable: nbt data of Tile Entities
     * Other Entities in area (Paintings, Item Frames, Armor Stands)
     *     4: number of Entities
     *     variable: nbt data of Entities
    */
    
    public static final int VERSION = 1;
    
    @Override
    public void save(IStoragePlot plot, DataOutputStream out) throws IOException{
//Logger.getGlobal().info("Don't use this Async!!!");
            StoragePlotSnapshot snap = new StoragePlotSnapshot(plot);
            save(plot, out, snap);
    }
    
    @Override        
    public void save(IStoragePlot plot, DataOutputStream out, StoragePlotSnapshot snap) throws IOException{
        try {
            /*String name = world.getName();
            byte[] nameBytes = name.getBytes(Charset.forName("UTF-8"));
            out.write(nameBytes.length);
            out.write(nameBytes);*/
            //List<Object> complexBlocks = new ArrayList<>();
            out.writeInt(VERSION);
            out.writeInt(plot.getLowCorner().getBlockX());
            out.writeInt(plot.getLowCorner().getBlockY());
            out.writeInt(plot.getLowCorner().getBlockZ());
            out.writeInt(plot.getHighCorner().getBlockX()-plot.getLowCorner().getBlockX()+1);
            out.writeInt(plot.getHighCorner().getBlockY()-plot.getLowCorner().getBlockY()+1);
            out.writeInt(plot.getHighCorner().getBlockZ()-plot.getLowCorner().getBlockZ()+1);
            Map<BlockData,Integer> paletteMap = new HashMap<>();
            List<BlockData> palette = new ArrayList<>();
            Map<Biome,Integer> biomePaletteMap = new HashMap<>();
            List<Biome> biomePalette = new ArrayList<>();
            for(int x = plot.getLowCorner().getBlockX(); x <= plot.getHighCorner().getBlockX(); ++x) {
                for(int z = plot.getLowCorner().getBlockZ(); z <= plot.getHighCorner().getBlockZ(); ++z) {
                    Biome biome = snap.getBiome(x, z);
                    Integer biomePaletteIndex = biomePaletteMap.get(biome);
                    if(biomePaletteIndex==null) {
                        biomePaletteMap.put(biome, palette.size());
                        biomePalette.add(biome);
                    }
                    for(int y = plot.getLowCorner().getBlockY(); y <= plot.getHighCorner().getBlockY()
                                                              && y <= snap.getMaxY(x, z); ++y) {
                        BlockData blockData = snap.getBlockData(x, y, z);
                        Integer paletteIndex = paletteMap.get(blockData);
                        if(paletteIndex==null) {
                            paletteMap.put(blockData, palette.size());
                            palette.add(blockData);
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
            out.writeInt(biomePalette.size()); //write length of palette
            for(int i=0; i<biomePalette.size();i++) {
                String biomeDataString = biomePalette.get(i).name();
                byte[] biomeDataBytes = biomeDataString.getBytes(Charset.forName("UTF-8"));
                out.writeInt(biomeDataBytes.length); //write length of next blockdata
                out.write(biomeDataBytes);
            }
            for(int x = plot.getLowCorner().getBlockX(); x <= plot.getHighCorner().getBlockX(); ++x) {
                for(int z = plot.getLowCorner().getBlockZ(); z <= plot.getHighCorner().getBlockZ(); ++z) {
                    out.writeInt(biomePaletteMap.get(snap.getBiome(x, z)));
                    out.writeInt(snap.getMaxY(x, z));
                    for(int y = plot.getLowCorner().getBlockY(); y <= plot.getHighCorner().getBlockY()
                                                              && y <= snap.getMaxY(x, z); ++y) {
                        BlockData blockData = snap.getBlockData(x, y, z);
                        out.writeInt(paletteMap.get(blockData));
                    }
                }
            }
            List tileEntities = new ArrayList();
            for(BlockState state: snap.getTileEntities()) {
                Object nbt = NMSUtil.invokeCraftBukkit("block.CraftBlockEntityState","getSnapshotNBT",
                                                       null, state);
                tileEntities.add(nbt);
            }
            out.writeInt(tileEntities.size());
            for(Object nbt: tileEntities) {
                Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("NBTTagCompound"),DataOutput.class};
                NMSUtil.invokeNMS("NBTCompressedStreamTools","a",argsClasses,null,nbt,(DataOutput)out);
            }
            Collection<Entity> entities = snap.getEntities();/*plot.getWorld()
                 .getNearbyEntities(new BoundingBox(plot.getLowCorner().getBlockX(),
                                                    plot.getLowCorner().getBlockY(),
                                                    plot.getLowCorner().getBlockZ(),
                                                    plot.getHighCorner().getBlockX(),
                                                    plot.getHighCorner().getBlockY(),
                                                    plot.getHighCorner().getBlockZ()),
                        new MCMEEntityFilter());*/
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
            Logger.getLogger(MCMEPlotFormat4.class.getName()).log(Level.SEVERE, null, ex);
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
     * @param in
     * @throws IOException 
     * @throws com.mcmiddleearth.pluginutil.plotStoring.InvalidRestoreDataException 
     */
    @Override
    public void load(final Location location, Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException{
        try {
            int version = in.readInt();
            if(version!=VERSION) {
                throw new InvalidRestoreDataException("Invalid storage data version: "+version+" ("+VERSION+" expected)");
            }
            Location originalLoc = new Location(location.getWorld(),in.readInt(),in.readInt(),in.readInt());
            final Vector shift = location.clone().subtract(originalLoc).toVector();
//Logger.getGlobal().info("Shift: "+shift.getBlockX()+" "+shift.getBlockY()+" "+shift.getBlockZ());
//Logger.getGlobal().info("Size: "+size.getBlockX()+" "+size.getBlockY()+" "+size.getBlockZ());
            Vector originalSize = new Vector(in.readInt(),in.readInt(),in.readInt());
//Logger.getGlobal().info("OrininalSize: "+originalSize.getBlockX()+" "+originalSize.getBlockY()+" "+originalSize.getBlockZ());
            if(size==null) {
                size = originalSize;
            }
            final Vector finalSize = size;
            if(!size.equals(originalSize)) {
                throw new InvalidRestoreDataException("Unexpected Restore Data Size");
            }
//Logger.getGlobal().info("Size: "+size.getBlockX()+" "+size.getBlockY()+" "+size.getBlockZ());
//Logger.getGlobal().info("finalSize: "+finalSize.getBlockX()+" "+finalSize.getBlockY()+" "+finalSize.getBlockZ());
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
            int biomePaletteLength = in.readInt();
            Map<Integer,Biome> biomePalette = new HashMap<>(biomePaletteLength);
            for(int i = 0; i<biomePaletteLength; i++) {
                int dataLength = in.readInt();
                byte[] byteData = new byte[dataLength];
                in.readFully(byteData);
                Biome biome = Biome.valueOf(new String(byteData,Charset.forName("UTF-8")));
                biomePalette.put(i, biome);
            }
            for(int x = location.getBlockX(); x <= location.getBlockX()+size.getBlockX()-1; ++x) {
                for(int z = location.getBlockZ(); z <= location.getBlockZ()+size.getBlockZ()-1; ++z) {
                    Biome biome = biomePalette.get(in.readInt());
                    location.getWorld().setBiome(x, z, biome);
                    int maxY = in.readInt();
                    for(int y = location.getBlockY(); y <= location.getBlockY()+size.getBlockY()-1; ++y) {
                        Location loc = new Location(location.getWorld(), x, y, z);
//Logger.getGlobal().info("Block: "+location.getWorld().getName()+" "+x+" "+y+" "+z+" "+loc.getBlock().getType());
                        if(y<=maxY) {
                            loc.getBlock().setBlockData(palette.get(in.readInt()),false);
                        } else {
                            loc.getBlock().setBlockData(Bukkit.createBlockData(Material.AIR),false);
                        }
                    }
                }
            }
            int tileEntityLength = in.readInt();
            final List tileEntityDatas = new ArrayList();
            for(int i=0; i< tileEntityLength; i++) {
                Class[] argsClasses = new Class[]{DataInput.class,NMSUtil.getNMSClass("NBTReadLimiter")};
                Object nbt = NMSUtil.invokeNMS("NBTCompressedStreamTools","a",argsClasses,null,
                                               (DataInput)in,NMSUtil.getNMSField("NBTReadLimiter","a",null));  
                tileEntityDatas.add(nbt);
            }
//Logger.getGlobal().info("load nbt TileEntity: "+nbt.toString());
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
                    /*for(Object nbt: tileEntityDatas) {
                        try {
                            Object nmsWorld = NMSUtil.invokeCraftBukkit("CraftWorld", "getHandle", null, location.getWorld());
                            Logger.getGlobal().info("TileEntity: "+((NBTTagCompound)nbt).asString());
                            Class[]argsClasses = new Class[]{NMSUtil.getNMSClass("NBTTagCompound"),
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
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }*/
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
                            Logger.getLogger(MCMEPlotFormat4.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
//Logger.getGlobal().info("Size: "+finalSize.getBlockX()+" "+finalSize.getBlockY()+" "+finalSize.getBlockZ());
                    Location high = location.clone().add(finalSize.toLocation(location.getWorld()));
                    NMSUtil.updatePlayerChunks(location, high);
                }
            }.runTaskLater(PluginUtilsPlugin.getInstance(), 1);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MCMEPlotFormat4.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void load(Location location, int rotations, Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void load(DataInputStream in) throws IOException, InvalidRestoreDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void load(Location location, int rotations, boolean[] flip, Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static class MCMEEntityFilter implements Predicate<Entity>{

        @Override
        public boolean test(Entity entity) {
            return entity instanceof Painting
                || entity instanceof ItemFrame
                || entity instanceof ArmorStand;
        }
    
    }
    
    public static class StoragePlotSnapshot4 {
        
        private final IStoragePlot plot;

        private final ChunkSnapshot[][] chunks;
        private final List<BlockState> tileEntities;
        private final List<Entity> entities;
        
        private final int lowX;
        private final int lowZ;
        private final int highX;
        private final int highZ;
        
        private final Block lowCorner;
        private final Block highCorner;
        
        public StoragePlotSnapshot4(IStoragePlot plot) {
            this.plot = plot;
            World world = plot.getWorld();
            lowCorner = plot.getLowCorner().getBlock();
            highCorner = plot.getHighCorner().getBlock();
            lowX = lowCorner.getChunk().getX();
            lowZ = lowCorner.getChunk().getZ();
            highX = highCorner.getChunk().getX();
            highZ = highCorner.getChunk().getZ();
//Logger.getGlobal().info("corners: "+lowX+" "+highX+" - "+lowZ + " "+highZ);
            
            chunks = new ChunkSnapshot[highX-lowX+1][highZ-lowZ+1];
            tileEntities = new ArrayList<>();
            entities = new ArrayList<>();
            MCMEEntityFilter filter = new MCMEEntityFilter();
            for(int i=0; i< chunks.length;i++) {
                for(int j=0; j<chunks[i].length;j++) {
                    Chunk chunk = world.getChunkAt(lowX+i, lowZ+j);
                    chunks[i][j] = chunk.getChunkSnapshot(true, true, false);
                    for(BlockState state: chunk.getTileEntities()) {
                        if(plot.isInside(state.getLocation())) {
                            tileEntities.add(state);
                        }
                    }
                    for(Entity entity: chunk.getEntities()) {
                        if(plot.isInside(entity.getLocation())
                                && filter.test(entity)) {
                            entities.add(entity);
                        }
                    }
                }
            }
        }
        
        public BlockData getBlockData(int x, int y, int z) {
            return chunks[getIndexX(x)][getIndexZ(z)].getBlockData(getInsideChunk(x), y, getInsideChunk(z));
        }
        
        public Biome getBiome(int x, int z) {
///Logger.getGlobal().info("GetBiome at: "+x+" "+(x%16));
            return chunks[getIndexX(x)][getIndexZ(z)].getBiome(getInsideChunk(x), getInsideChunk(z));
        }
        
        public int getMaxY(int x, int z) {
            return chunks[getIndexX(x)][getIndexZ(z)].getHighestBlockYAt(getInsideChunk(x), getInsideChunk(z))+1;
        }
        
        public List<BlockState> getTileEntities() {
            return tileEntities;
        }
        
        public List<Entity> getEntities() {
            return entities;
        }
        
        public ChunkSnapshot[][] getChunks() {
            return chunks;
        }
        
        private int getIndexX(int x) {
//Logger.getGlobal().info("getIndexX: "+x +" "+(x>=0?x/16:(x+1)/16-1));
            return (x>=0?x/16:(x+1)/16-1)-lowX;
        }
        private int getIndexZ(int z) {
//Logger.getGlobal().info("getIndexZ: "+z +" "+(z>=0?z/16:(z+1)/16-1));
            return (z>=0?z/16:(z+1)/16-1)-lowZ;
        }
        
        private int getInsideChunk(int worldCoord) {
            int result = worldCoord % 16;
            return (result>=0?result:16+result);
        }
        
        
    }
        
}
