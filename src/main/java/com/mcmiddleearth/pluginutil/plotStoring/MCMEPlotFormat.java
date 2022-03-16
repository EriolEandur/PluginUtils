/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.plotStoring;

import com.mcmiddleearth.pluginutil.NBTTagUtil;
import com.mcmiddleearth.pluginutil.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class MCMEPlotFormat implements PlotStorageFormat {
    
    //private static final String entityExt = ".emcme";
    
    /*
     * Binary format:
     * (number of Bytes: content)
     * 4: Version number
     * 4: <length> of world name bytes
     *    <length>: world name in UTF-8 charset
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
    
    public static final int VERSION = 2;
    /* version changelog:
     * 2: Added world name after version
    */
    
    private Vector resultSize;

    public Vector getResultSize() {
        return resultSize;
    }

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
            byte[] worldNameBytes = plot.getWorld().getName().getBytes(Charset.forName("UTF-8"));
            out.writeInt(worldNameBytes.length);
            out.write(worldNameBytes);
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
                        biomePaletteMap.put(biome, biomePalette.size());
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
                    int biomeIndex = biomePaletteMap.get(snap.getBiome(x, z));
//Logger.getGlobal().info("save biome: "+biomeIndex +" "+snap.getBiome(x,z)+"save maxY: "+snap.getMaxY(x,z));
                    out.writeInt(biomeIndex);
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
                Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("nbt.NBTTagCompound"),DataOutput.class};
//Logger.getGlobal().info("saving nbt TileEntity: "+nbt.toString());
                NMSUtil.invokeNMS("nbt.NBTCompressedStreamTools","a",argsClasses,null,nbt,(DataOutput)out);
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
                Object nbt = NBTTagUtil.createNBTCompound();
                Object nmsEntity = NMSUtil.invokeCraftBukkit("entity.CraftEntity", "getHandle",
                                                             null, entity);
                NMSUtil.invokeNMS("nbt.NBTTagCompound","a",null, nbt,"id",
                                  NMSUtil.invokeNMS("world.entity.Entity","bk",null, nmsEntity));
                nbt = NMSUtil.invokeNMS("world.entity.Entity","f",null, nmsEntity,nbt);
                Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("nbt.NBTTagCompound"),DataOutput.class};
                NMSUtil.invokeNMS("nbt.NBTCompressedStreamTools","a",argsClasses,null,nbt,(DataOutput)out);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public Vector readSize(DataInputStream in) throws IOException {
        in.readInt(); //Version
        int nameBytes = in.readInt();  //world name length
        in.read(new byte[nameBytes]); //world name
        in.readInt();in.readInt();in.readInt(); //Location
        return new Vector(in.readInt(),in.readInt(),in.readInt());
    }
    
    @Override
    public void load(DataInputStream in) throws IOException, InvalidRestoreDataException {
        load(null, null, in);
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
     * @param location Where to place the restore data may be null to use stored location
     * @param size Expected size of restore data, may be null to skip size test
     * @param in
     * @throws IOException 
     * @throws com.mcmiddleearth.pluginutil.plotStoring.InvalidRestoreDataException 
     */
    @Override
    public void load(final Location location, Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException{
            load(location, 0 , size, in);
    }
    
    @Override
    public void load(Location loca, final int rotations, Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException{
        this.load(loca, rotations, new boolean[3], size, in);
    }
    
    @Override
    public void load(Location loca, final int rotations, boolean[] flip,
                     Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException{
        load(loca, rotations, flip, true, true, size, in);
    }

    public void load(Location loca, final int rotations, boolean[] flip,
                     final boolean withAir, final boolean withBiome,
                     Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException{
        load(loca, rotations,flip,withAir,withBiome,size, false, in);
    }

    public void load(Location loca, final int rotations, boolean[] flip,
    final boolean withAir, final boolean withBiome,
    Vector size, boolean legacyBlocks, DataInputStream in) throws IOException, InvalidRestoreDataException{
        //try {
        resultSize = null;
        int version = in.readInt();
        if(version!=VERSION) {
            throw new InvalidRestoreDataException("Invalid storage data version: "+version+" ("+VERSION+" expected)");
        }
        int worldNameDataLength = in.readInt();
        byte[] worldNameByteData = new byte[worldNameDataLength];
        in.readFully(worldNameByteData);
        String worldName =  new String(worldNameByteData,Charset.forName("UTF-8"));
        World originalWorld = Bukkit.getWorld(worldName);
        Location originalLoc;
        final Location location;
        final Vector shift;
        if(originalWorld!=null) {
            originalLoc = new Location(originalWorld,in.readInt(),in.readInt(),in.readInt());
        } else if(loca != null) {
            originalLoc = new Location(loca.getWorld(),in.readInt(),in.readInt(),in.readInt());
        } else {
            throw new InvalidRestoreDataException("Missing restore world: "+worldName);
        }
        if(loca!=null) {
            location = loca.getBlock().getLocation();
            shift = location.toVector().subtract(originalLoc.toVector());
        } else {
            location = originalLoc;
            shift = new Vector(0,0,0);
        }
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
        resultSize = size;
        final LocalTransformation rotation = new LocalTransformation(location.toVector(),size,rotations,flip);
//Logger.getGlobal().info("Size: "+size.getBlockX()+" "+size.getBlockY()+" "+size.getBlockZ());
//Logger.getGlobal().info("finalSize: "+finalSize.getBlockX()+" "+finalSize.getBlockY()+" "+finalSize.getBlockZ());
        int maxX;
        int maxZ;
        if(rotations%2==1) {
            maxX=location.getBlockX()+size.getBlockZ();
            maxZ=location.getBlockZ()+size.getBlockX();
        } else {
            maxX=location.getBlockX()+size.getBlockX();
            maxZ=location.getBlockZ()+size.getBlockZ();
        }
//Logger.getGlobal().log(Level.INFO,"area: {0} {1} {2} ---- {3} {4} {5} ",
//                                new Object[]{location.getBlockX(),location.getBlockY(),location.getBlockZ(),
//                                             maxX,location.getBlockY()+size.getBlockY(),maxZ});
        Collection<Entity> entities = location.getWorld()
                 .getNearbyEntities(new BoundingBox(location.getBlockX(),
                                                    location.getBlockY(),
                                                    location.getBlockZ(),
                                                    maxX+1,
                                                    location.getBlockY()+size.getBlockY()+1,
                                                    maxZ+1),
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
            String blockDataString;
            if(legacyBlocks) {
                blockDataString = blockMappings(new String(byteData, StandardCharsets.UTF_8));
            } else {
                blockDataString = new String(byteData, StandardCharsets.UTF_8);
            }
            BlockData blockData = Bukkit.getServer()
                                        .createBlockData(blockDataString);
            palette.put(i, rotation.transformBlockData(blockData));
        }
        int biomePaletteLength = in.readInt();
        Map<Integer,Biome> biomePalette = new HashMap<>(biomePaletteLength);
        for (int i = 0; i < biomePaletteLength; i++) {
            int dataLength = in.readInt();
            byte[] byteData = new byte[dataLength];
            in.readFully(byteData);
            String biomeName = new String(byteData, Charset.forName("UTF-8"));
            //Logger.getGlobal().info("Biome name: "+biomeName);
            Biome biome = Biome.PLAINS;
            try {
                biome = Biome.valueOf(biomeName);
            } catch (IllegalArgumentException ignore) {}
            //Logger.getGlobal().info("Biome palette entry: "+i+" "+biome);
            biomePalette.put(i, biome);
        }
//log("location",location);
        Vector rotatedLowCorner = rotation.transformVector(location.toVector(),true);
//log("rotated", rotatedLowCorner);
//log("size",size);
        Vector rotatedHighCorner = rotation.transformVector(location.toVector()
                                              .add(size).subtract(new Vector(1,1,1)),true);
//log("rotatedHigh", rotatedHighCorner);
        int xIncrement = (rotatedLowCorner.getBlockX()<=rotatedHighCorner.getBlockX()?1:-1);
//Logger.getGlobal().info("xinc " +xIncrement);
        int zIncrement = (rotatedLowCorner.getBlockZ()<=rotatedHighCorner.getBlockZ()?1:-1);
//Logger.getGlobal().info("zinc " +zIncrement);
        int firstStart, firstEnd, firstInc, secondStart, secondEnd, secondInc;
        int temp;
        if(rotations%2==0) {
            firstStart = rotatedLowCorner.getBlockX();
            firstEnd = rotatedHighCorner.getBlockX();
            firstInc = xIncrement;
            secondStart = rotatedLowCorner.getBlockZ();
            secondEnd = rotatedHighCorner.getBlockZ();
            secondInc = zIncrement;
            /*if(flip[0]) {
//Logger.getGlobal().info("flip x");
                temp = firstStart;
                firstStart = firstEnd;
                firstEnd = temp;
                firstInc = -firstInc;
            }
            if(flip[2]) {
//Logger.getGlobal().info("flip z");
                temp = secondStart;
                secondStart = secondEnd;
                secondEnd = temp;
                secondInc = - secondInc;
            }*/
        } else {
            firstStart = rotatedLowCorner.getBlockZ();
            firstEnd = rotatedHighCorner.getBlockZ();
            firstInc = zIncrement;
            secondStart = rotatedLowCorner.getBlockX();
            secondEnd = rotatedHighCorner.getBlockX();
            secondInc = xIncrement;
            /*if(flip[2]) {
//Logger.getGlobal().info("flip x rotated");
                temp = firstStart;
                firstStart = firstEnd;
                firstEnd = temp;
                firstInc = -firstInc;
            }
            if(flip[0]) {
//Logger.getGlobal().info("flip z rotated");
                temp = secondStart;
                secondStart = secondEnd;
                secondEnd = temp;
                secondInc = - secondInc;
            }*/
        }
        BlockData air = Bukkit.createBlockData(Material.AIR);
        for(int i = firstStart;
                        i != firstEnd+firstInc;
                                i=i+firstInc) {
            for(int j = secondStart;
                        j != secondEnd+secondInc;
                                j=j+secondInc) {
                int x,z;
                if(rotations%2==0) {
                    x=i;
                    z=j;
                } else {
                    x=j;
                    z=i;
                }
                int biomeIndex = in.readInt();
//Logger.getGlobal().info("biome index: "+biomeIndex);                    
                Biome biome = biomePalette.get(biomeIndex);
//Logger.getGlobal().info(""+biome);
                if(biome != null && withBiome) {
                    location.getWorld().setBiome(x, z, biome);
                }
                int maxY = in.readInt() + shift.getBlockY();
                int yStart = location.getBlockY();
                int yEnd = yStart+size.getBlockY();
                int yInc = 1;
                int columnCountMax = Math.min(yEnd,maxY)-yStart;
                int columnCount = 0;
                if(flip[1]) {
//Logger.getGlobal().info("flip y");
                    temp = yStart;
                    yStart = yEnd-1;
                    yEnd = temp-1;
                    yInc = -1;
                }
                for(int y = yStart; y != yEnd; y = y+ yInc) {
                    //Location loc = rotation.rotateVector(new Vector(x, y, z),true).toLocation(location.getWorld());
//Logger.getGlobal().info("Block: "+location.getWorld().getName()+" "+x+" "+y+" "+z+" "+location.getBlock().getType()+" maxY "+maxY+" cC "+columnCount+" ccM "+columnCountMax);
//Logger.getGlobal().log(Level.INFO, "Rotated: {0} {1} {2}", new Object[]{location.getBlockX(),location.getBlockY(),location.getBlockZ()});
                    //if(y<=maxY) {
                    if(columnCount <= columnCountMax) {
                        BlockData data = palette.get(in.readInt());
                        if(withAir || !data.getMaterial().equals(Material.AIR)) {
                            location.getWorld().getBlockAt(x, y, z).setBlockData(data,false);
                        }
                        columnCount++;
                    } else {
                        if(withAir) {
                            location.getWorld().getBlockAt(x, y, z).setBlockData(air,false);
                        }
                    }
                }
            }
        }
        int tileEntityLength = in.readInt();
        final List tileEntityDatas = new ArrayList();
        try {
            for (int i = 0; i < tileEntityLength; i++) {
                Class[] argsClasses = new Class[]{DataInput.class, NMSUtil.getNMSClass("nbt.NBTReadLimiter")};
                Object nbt = NMSUtil.invokeNMS("nbt.NBTCompressedStreamTools", "a", argsClasses, null,
                        (DataInput) in, NMSUtil.getNMSField("nbt.NBTReadLimiter", "a", null));
                tileEntityDatas.add(nbt);
            }
        }catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
            for (Object nbt : tileEntityDatas) {
                try {
                    Object nmsWorld = NMSUtil.invokeCraftBukkit("CraftWorld", "getHandle", null, location.getWorld());
                    /*Object blockposition = NMSUtil.createNMSObject("core.BlockPosition",
                            new Class[]{int.class, int.class, int.class},
                            NMSUtil.invokeNMS("nbt.NBTTagCompound", "h",//getInt",
                                    new Class[]{String.class}, nbt, "x"),
                            NMSUtil.invokeNMS("nbt.NBTTagCompound", "h",
                                    new Class[]{String.class}, nbt, "y"),
                            NMSUtil.invokeNMS("nbt.NBTTagCompound", "h",
                                    new Class[]{String.class}, nbt, "z"));*/
                    Object blockposition = NMSUtil.invokeNMS("world.level.block.entity.TileEntity","c",
                            new Class[]{NMSUtil.getNMSClass("nbt.NBTTagCompound")},null, nbt);
                    Class[] argsClasses = new Class[]{int.class, int.class, int.class};
                    Object newPosition = NMSUtil.invokeNMS("core.BlockPosition", "c", argsClasses, blockposition, shift.getBlockX(),
                            shift.getBlockY(),
                            shift.getBlockZ());
                    final Vector rotatedVector = rotation.transformVector(NMSUtil.toVector(newPosition), true);
                    newPosition = NMSUtil.toBlockPosition(rotatedVector);
                    NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*setInt*/, new Class[]{String.class, int.class}, nbt, "x",
                            NMSUtil.invokeNMS("core.BlockPosition", "u"/*"getX"*/, null, newPosition));
                    NMSUtil.invokeNMS("nbt.NBTTagCompound", "a", new Class[]{String.class, int.class}, nbt, "y",
                            NMSUtil.invokeNMS("core.BlockPosition", "v"/*"getY"*/, null, newPosition));
                    NMSUtil.invokeNMS("nbt.NBTTagCompound", "a", new Class[]{String.class, int.class}, nbt, "z",
                            NMSUtil.invokeNMS("core.BlockPosition", "w"/*"getZ"*/, null, newPosition));
                    Object chunk = NMSUtil.invokeNMS("world.level.World", "l"/*"getChunkAtWorldCoords"*/,
                            new Class[]{newPosition.getClass()}, nmsWorld, newPosition);
                    Object iBlockState = NMSUtil.invokeNMS("world.level.chunk.Chunk", "a_"/*"getType"*/,
                            new Class[]{newPosition.getClass()}, chunk, newPosition);
                    argsClasses = new Class[]{NMSUtil.getNMSClass("core.BlockPosition"),
                            NMSUtil.getNMSClass("world.level.block.state.IBlockData"),
                            NMSUtil.getNMSClass("nbt.NBTTagCompound")};
//Logger.getGlobal().info("loading nbt TileEntity: " + nbt.toString());
                    Object entity = NMSUtil.invokeNMS("world.level.block.entity.TileEntity", "a"/*"create"*/,
                            argsClasses, null, newPosition, iBlockState, nbt/*,nmsWorld*/);
                    if (entity != null) {
                        NMSUtil.invokeNMS("world.level.chunk.Chunk", "a"/*"setTileEntity"*/,
                                new Class[]{NMSUtil.getNMSClass("world.level.block.entity.TileEntity")},
                                chunk, entity);
                    } else {
                        Logger.getLogger(MCMEPlotFormat.class.getSimpleName()).info("Warning! Tile entity skipped!");
                    }
                } catch (ClassNotFoundException | SecurityException | IllegalArgumentException | NullPointerException ex) {
                    Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        final int entityLength = in.readInt();
        final List entityDatas = new ArrayList();
        try {
            for (int i = 0; i < entityLength; i++) {
                Class[] argsClasses = new Class[]{DataInput.class, NMSUtil.getNMSClass("nbt.NBTReadLimiter")};
                entityDatas.add(NMSUtil.invokeNMS("nbt.NBTCompressedStreamTools", "a", argsClasses,
                        null, (DataInput) in,
                        NMSUtil.getNMSField("nbt.NBTReadLimiter", "a", null)));
            }
        }catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        for (Object nbt : entityDatas) {
            try {

                //move to new position
                Object list = NMSUtil.invokeNMS("nbt.NBTTagCompound", "c"/*"getList"*/,
                        new Class[]{String.class, int.class},
                        nbt, "Pos", 6); // 6 = content type double > NBTBase
                Class[] argsClassesA = new Class[]{int.class,
                        NMSUtil.getNMSClass("nbt.NBTBase")};
                Class[] argsClassesB = new Class[]{double.class};
                Class[] argsClassesC = new Class[]{int.class};
                double[] position = new double[3];
                for (int i = 0; i < 3; i++) {
                    position[i] = (double) NMSUtil.invokeNMS("nbt.NBTTagList", "h",
                            argsClassesC, list, i);
                }
//Logger.getGlobal().log(Level.INFO, "NBT: "+nbt.toString());
                Vector newPosition = rotation.transformVector(new Vector(position[0] + shift.getBlockX(),
                                position[1] + shift.getBlockY(),
                                position[2] + shift.getBlockZ()),
                        false);
//Logger.getGlobal().log(Level.INFO, "Rotated: {0} {1} {2}", new Object[]{newPosition.getBlockX(),newPosition.getBlockY(),newPosition.getBlockZ()});
                NMSUtil.invokeNMS("nbt.NBTTagList", "a", argsClassesA, list,
                        0, NBTTagUtil.createNBTTagDouble(newPosition.getX()));
                NMSUtil.invokeNMS("nbt.NBTTagList", "a", argsClassesA, list,
                        1, NBTTagUtil.createNBTTagDouble(newPosition.getY()));
                NMSUtil.invokeNMS("nbt.NBTTagList", "a", argsClassesA, list,
                        2, NBTTagUtil.createNBTTagDouble(newPosition.getZ()));
                /*NMSUtil.invokeNMS("NBTTagList","a",argsClassesA,list,
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
                                             +shift.getBlockZ()));*/
                //list.a(1,new NBTTagDouble(list.k(1)+shift.getBlockY()));
                //list.a(2,new NBTTagDouble(list.k(2)+shift.getBlockZ()));
                NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*"set"*/,
                        new Class[]{String.class, NMSUtil.getNMSClass("nbt.NBTBase")},
                        nbt, "Pos", list);

                //rotate entity
                Object rotList = NMSUtil.invokeNMS("nbt.NBTTagCompound", "c"/*"getList"*/,
                        new Class[]{String.class, int.class},
                        nbt, "Rotation", 5); // 5= content type float > NBTBase
                float yaw = (float) NMSUtil.invokeNMS("nbt.NBTTagList", "i",
                        new Class[]{int.class}, rotList, 0);
                NMSUtil.invokeNMS("nbt.NBTTagList", "a", argsClassesA, rotList,
                        0, NBTTagUtil.createNBTTagFloat(rotation.transformYaw(yaw)));
                NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*"set"*/,
                        new Class[]{String.class, NMSUtil.getNMSClass("nbt.NBTBase")},
                        nbt, "Rotation", rotList);

                //rotate facing of hanging entities
                String type = (String) NMSUtil.invokeNMS("nbt.NBTTagCompound", "l"/*"getString"*/,
                        new Class[]{String.class},
                        nbt, "id");
                Byte facing = 0;
                if (type.equals("minecraft:painting") || type.equals("minecraft:item_frame")) {
                    facing = (Byte) NMSUtil.invokeNMS("nbt.NBTTagCompound", "f"/*"getByte"*/,
                            new Class[]{String.class},
                            nbt, "Facing");
                    Byte transformedFacing = rotation.transformHangingEntity(type, facing);
                    Object nbtFacing = NBTTagUtil.createNBTTagByte(transformedFacing);
                    NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*"set"*/,
                            new Class[]{String.class, NMSUtil.getNMSClass("nbt.NBTBase")},
                            nbt, "Facing", nbtFacing);
                    if (type.equals("minecraft:item_frame") /*&& transformedFacing < 2*/) {
                        Byte itemRot = (Byte) NMSUtil.invokeNMS("nbt.NBTTagCompound", "f"/*"getByte"*/,
                                new Class[]{String.class},
                                nbt, "ItemRotation");
//Logger.getGlobal().info("itemFrame: "+newPosition.getX()+" "+newPosition.getY()+" "+newPosition.getZ()+" "+facing +" "+itemRot);
                        itemRot = rotation.transformItemRotation(facing, itemRot);
                        Object nbtItemRot = NBTTagUtil.createNBTTagByte(itemRot);
                        NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*"set"*/,
                                new Class[]{String.class, NMSUtil.getNMSClass("nbt.NBTBase")},
                                nbt, "ItemRotation", nbtItemRot);
                    }
                }

                //put Tile Tags for hanging entities.
                int tileX = tileCoord(newPosition.getX());
                int tileY = tileCoord(newPosition.getY());
                int tileZ = tileCoord(newPosition.getZ());
                if (type.equals("minecraft:painting")) {
//Logger.getGlobal().log(Level.INFO,"painting: {0} {1} {2} {3} {4}",new Object[]{facing,newPosition.getX(),tileX,newPosition.getZ(),tileZ});
                    if (facing == 2 && (newPosition.getX() - tileX > 0.6)) {
                        tileX++;
                    } else if (facing == 3 && (newPosition.getZ() - tileZ > 0.6)) {
                        tileZ++;
                    }
                }
                Object nbtTileX = NBTTagUtil.createNBTTagInt(tileX);
                Object nbtTileY = NBTTagUtil.createNBTTagInt(tileY);
                Object nbtTileZ = NBTTagUtil.createNBTTagInt(tileZ);
                NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*"set"*/,
                        new Class[]{String.class, NMSUtil.getNMSClass("nbt.NBTBase")},
                        nbt, "TileX", nbtTileX);
                NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*"set"*/,
                        new Class[]{String.class, NMSUtil.getNMSClass("nbt.NBTBase")},
                        nbt, "TileY", nbtTileY);
                NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*"set"*/,
                        new Class[]{String.class, NMSUtil.getNMSClass("nbt.NBTBase")},
                        nbt, "TileZ", nbtTileZ);

                //give random UUID to entity
                UUID uuid = UUID.randomUUID();
                int[] uuidIntArrayRepresentation = new int[4];
                uuidIntArrayRepresentation[0] = (int) (uuid.getMostSignificantBits() >> 32);
                uuidIntArrayRepresentation[1] = (int) uuid.getMostSignificantBits();
                uuidIntArrayRepresentation[2] = (int) (uuid.getLeastSignificantBits() >> 32);
                uuidIntArrayRepresentation[3] = (int) uuid.getLeastSignificantBits();
                Object nbtIntArray = NBTTagUtil.createNBTTagIntArray(uuidIntArrayRepresentation);
                Object nbtLeast = NBTTagUtil.createNBTTagLong(uuid.getLeastSignificantBits());
                Object nbtMost = NBTTagUtil.createNBTTagLong(uuid.getMostSignificantBits());
                NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*"set"*/,
                        new Class[]{String.class, NMSUtil.getNMSClass("nbt.NBTBase")},
                        nbt, "UUIDLeast", nbtLeast);
                NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*"set"*/,
                        new Class[]{String.class, NMSUtil.getNMSClass("nbt.NBTBase")},
                        nbt, "UUIDLeast", nbtMost);
                NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*"set"*/,
                        new Class[]{String.class, NMSUtil.getNMSClass("nbt.NBTBase")},
                        nbt, "UUID", nbtIntArray);

                //create entity
//Logger.getGlobal().log(Level.INFO, "************************************");
//Logger.getGlobal().log(Level.INFO, "NBT: "+nbt.toString());
                Object nmsWorld = NMSUtil.invokeCraftBukkit("CraftWorld", "getHandle", null,
                        location.getWorld());
                Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("nbt.NBTTagCompound"),
                        NMSUtil.getNMSClass("world.level.World")};
                Object entity = NMSUtil.invokeNMS("world.entity.EntityTypes", "a", argsClasses, null, nbt, nmsWorld);

//Logger.getGlobal().info("ENTITY: "+entity.getClass().getCanonicalName());
                //add entity to world
                argsClasses = new Class[]{NMSUtil.getNMSClass("world.entity.Entity"),
                        CreatureSpawnEvent.SpawnReason.CUSTOM.getClass()};
                NMSUtil.invokeNMS("server.level.WorldServer", "addFreshEntity"/*"addEntity"*/, argsClasses, nmsWorld, ((Optional) entity).get(),
                        CreatureSpawnEvent.SpawnReason.CUSTOM);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Location high = location.clone().add(finalSize.toLocation(location.getWorld()));
        NMSUtil.updatePlayerChunks(location, high);
    }
    
    private static int tileCoord(double entityCoord) {
        if(entityCoord>=0) {
            return (int) (entityCoord-0.0000001d);
        } else {
            return (int) (entityCoord-0.0000001d)-1;
        }
    }
      
    private void log(String name, Location loc) {
        Logger.getGlobal().info(name+" "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ());
    }
    private void log(String name, Vector loc) {
        Logger.getGlobal().info(name+" "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ());
    }

    private String blockMappings(String blockData) {
        if(blockData.contains("level")) {
            blockData = blockData.replace("cauldron","water_cauldron");
        }
        return blockData;
    }

}
