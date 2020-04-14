/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.plotStoring;

import com.mcmiddleearth.pluginutil.NBTTagUtil;
import com.mcmiddleearth.pluginutil.NMSUtil;
import com.mojang.authlib.GameProfile;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import net.minecraft.server.v1_15_R1.GameProfileSerializer;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.TileEntitySkull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
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
    
    @Getter
    private Vector resultSize;
    
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
                Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("NBTTagCompound"),DataOutput.class};
//Logger.getGlobal().info("saving nbt TileEntity: "+nbt.toString());
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
                Object nbt = NBTTagUtil.createNBTCompound();
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
        try {
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
                shift = location.clone().subtract(originalLoc).toVector();
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
                BlockData blockData = Bukkit.getServer()
                                            .createBlockData(new String(byteData,Charset.forName("UTF-8")));
                palette.put(i, rotation.transformBlockData(blockData));
            }
            int biomePaletteLength = in.readInt();
            Map<Integer,Biome> biomePalette = new HashMap<>(biomePaletteLength);
            for(int i = 0; i<biomePaletteLength; i++) {
                int dataLength = in.readInt();
                byte[] byteData = new byte[dataLength];
                in.readFully(byteData);
                String biomeName = new String(byteData,Charset.forName("UTF-8"));
//Logger.getGlobal().info("Biome name: "+biomeName);
                Biome biome = Biome.valueOf(biomeName);
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
            /*for(int x = location.getBlockX(); x <= location.getBlockX()+size.getBlockX()-1; ++x) {
                for(int z = location.getBlockZ(); z <= location.getBlockZ()+size.getBlockZ()-1; ++z) {
                    Biome biome = biomePalette.get(in.readInt());
                    location.getWorld().setBiome(x, z, biome);
                    int maxY = in.readInt();
                    for(int y = location.getBlockY(); y <= location.getBlockY()+size.getBlockY()-1; ++y) {
                        Location loc = rotation.rotateVector(new Vector(x, y, z),true).toLocation(location.getWorld());
//Logger.getGlobal().info("Block: "+location.getWorld().getName()+" "+x+" "+y+" "+z+" "+loc.getBlock().getType());
//Logger.getGlobal().log(Level.INFO, "Rotated: {0} {1} {2}", new Object[]{loc.getBlockX(),loc.getBlockY(),loc.getBlockZ()});
                        if(y<=maxY) {
                            loc.getBlock().setBlockData(palette.get(in.readInt()),false);
                        } else {
                            loc.getBlock().setBlockData(Bukkit.createBlockData(Material.AIR),false);
                        }
                    }
                }
            }*/
            //if(true) return;
//Logger.getGlobal().info("Error!!!!!!!!!!!!!!!!!!");
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
            /*new BukkitRunnable() {
                @Override
                public void run() {*/
                    for(Object nbt: tileEntityDatas) {
                        try {
                            Object nmsWorld = NMSUtil.invokeCraftBukkit("CraftWorld", "getHandle", null, location.getWorld());
        //  Logger.getGlobal().info("TileEntity: "+((NBTTagCompound)nbt).asString());
                            Class[]argsClasses = new Class[]{NMSUtil.getNMSClass("NBTTagCompound")/*,
                                NMSUtil.getNMSClass("World")*/};
        //Logger.getGlobal().info("loading nbt TileEntity: "+nbt.toString());
                            Object entity = NMSUtil.invokeNMS("TileEntity","create",argsClasses, null,nbt/*,nmsWorld*/);
                            Object position = NMSUtil.invokeNMS("TileEntity", "getPosition", null, entity);
                            argsClasses = new Class[]{double.class,double.class,double.class};
                            //get position of tile entity and apply transform
                            Object newPosition = NMSUtil.invokeNMS("BlockPosition", "a", argsClasses, position, shift.getBlockX(),
                                    shift.getBlockY(),
                                    shift.getBlockZ());
                            final Vector rotatedVector = rotation.transformVector(NMSUtil.toVector(newPosition),true);

                            TileEntity tileEntity = (TileEntity) entity;
                            if (!(tileEntity instanceof TileEntitySkull)) {
                                //set Tile Entity not persistent when called for Player Heads
                                newPosition = NMSUtil.toBlockPosition(rotatedVector);
                                NMSUtil.invokeNMS("TileEntity", "setPosition", null, entity, newPosition);
                                argsClasses = new Class[]{NMSUtil.getNMSClass("BlockPosition"),
                                    NMSUtil.getNMSClass("TileEntity")};
                                NMSUtil.invokeNMS("WorldServer","setTileEntity",argsClasses,nmsWorld,
                                        NMSUtil.invokeNMS("TileEntity","getPosition",null,entity), entity);
                                
                            } else {
                                //custom head
        //Logger.getGlobal().info("Custom head");
                                TileEntitySkull var6 = (TileEntitySkull)tileEntity;
                                GameProfile var7 = null;
                                //if (var4.hasTag()) {
                                //    NBTTagCompound var8 = var4.getTag();
                                    NBTTagCompound var8 = ((NBTTagCompound)nbt);
                                    if (var8.hasKeyOfType("Owner", 10)) {
        //Logger.getGlobal().info("Custom head type 10");
                                        var7 = GameProfileSerializer.deserialize(var8.getCompound("Owner"));
                                    } else if (var8.hasKeyOfType("Owner", 8) && !var8.getString("SkullOwner").equals("")) {
        //Logger.getGlobal().info("Custom head type 8");
                                        var7 = new GameProfile(null, var8.getString("Owner"));
                                    }
                                //}
                                //var6.setGameProfile(var7);
                                final GameProfile finalProfile = var7;
                                try {
                                //Logger.getGlobal().info("Head Profile: "+finalProfile.toString());
                                    Skull skullData = (Skull) rotatedVector.toLocation(location.getWorld()).getBlock().getState();
                                    //Logger.getGlobal().info("block: "+rotatedVector.toString());
                                    //Logger.getGlobal().info("skullData: "+skullData.getBlockData().getAsString());
                                    Field profileField = skullData.getClass().getDeclaredField("profile");
                                    profileField.setAccessible(true);
                                    //Logger.getGlobal().info("Profile: "+profileField.get(skullData));
                                    profileField.set(skullData, finalProfile);
                                    //Logger.getGlobal().info("skullData2: "+skullData.getBlockData().getAsString());
                                    //Logger.getGlobal().info("Profile 2: "+profileField.get(skullData).toString());
                                    skullData.update(true, false);
                                    skullData = (Skull) rotatedVector.toLocation(location.getWorld()).getBlock().getState();
                                    //Logger.getGlobal().info("skullData3: "+skullData.getBlockData().getAsString());
                                    //Logger.getGlobal().info("Profile 3: "+profileField.get(skullData).toString());
                                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
                                    Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                /*new BukkitRunnable() {
                                @Override
                                public void run() {
                                }
                                }.runTaskLater(PluginUtilsPlugin.getInstance(), 10);*/ 
                                /*new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                    }
                                }.runTaskLater(PluginUtilsPlugin.getInstance(), 10);*/
                            }
                        } catch (ClassNotFoundException | SecurityException | IllegalArgumentException ex) {
                            Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    for(Object nbt: entityDatas) {
                        try {
                            
                            //move to new position
                            Object list = NMSUtil.invokeNMS("NBTTagCompound","getList",
                                                new Class[]{String.class,int.class},
                                                nbt,"Pos",6); // 6 = content type double > NBTBase
                            Class[] argsClassesA = new Class[]{int.class,
                                                               NMSUtil.getNMSClass("NBTBase")};
                            Class[] argsClassesB = new Class[]{double.class};
                            Class[] argsClassesC = new Class[]{int.class};
                            double[] position = new double[3];
                            for(int i = 0; i<3; i++) {
                                position[i] = (double)NMSUtil.invokeNMS("NBTTagList", "h", 
                                                                argsClassesC,list,i);
                            }
//Logger.getGlobal().log(Level.INFO, "NBT: "+nbt.toString());
                            Vector newPosition = rotation.transformVector(new Vector(position[0]+shift.getBlockX(),
                                                                            position[1]+shift.getBlockY(),
                                                                            position[2]+shift.getBlockZ()),
                                                                            false);
//Logger.getGlobal().log(Level.INFO, "Rotated: {0} {1} {2}", new Object[]{newPosition.getBlockX(),newPosition.getBlockY(),newPosition.getBlockZ()});
                            NMSUtil.invokeNMS("NBTTagList","a",argsClassesA,list,
                                              0,NBTTagUtil.createNBTTagDouble(newPosition.getX()));        
                            NMSUtil.invokeNMS("NBTTagList","a",argsClassesA,list,
                                              1,NBTTagUtil.createNBTTagDouble(newPosition.getY()));        
                            NMSUtil.invokeNMS("NBTTagList","a",argsClassesA,list,
                                              2,NBTTagUtil.createNBTTagDouble(newPosition.getZ()));        
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
                            NMSUtil.invokeNMS("NBTTagCompound","set",
                                              new Class[]{String.class,NMSUtil.getNMSClass("NBTBase")},
                                              nbt,"Pos",list);
                            
                            //rotate entity
                            Object rotList = NMSUtil.invokeNMS("NBTTagCompound","getList",
                                                new Class[]{String.class,int.class},
                                                nbt,"Rotation",5); // 5= content type float > NBTBase
                            float yaw = (float) NMSUtil.invokeNMS("NBTTagList", "i", 
                                                                new Class[]{int.class},rotList,0);
                            NMSUtil.invokeNMS("NBTTagList","a",argsClassesA,rotList,
                                              0,NBTTagUtil.createNBTTagFloat(rotation.transformYaw(yaw)));        
                            NMSUtil.invokeNMS("NBTTagCompound","set",
                                              new Class[]{String.class,NMSUtil.getNMSClass("NBTBase")},
                                              nbt,"Rotation",rotList);
 
                            //rotate facing of hanging entities
                            String type = (String) NMSUtil.invokeNMS("NBTTagCompound","getString",
                                                new Class[]{String.class},
                                                nbt,"id");
                            Byte facing=0;
                            if(type.equals("minecraft:painting") || type.equals("minecraft:item_frame")) {
                                facing = (Byte)  NMSUtil.invokeNMS("NBTTagCompound","getByte",
                                                    new Class[]{String.class},
                                                    nbt,"Facing");
                                Byte transformedFacing = rotation.transformHangingEntity(type,facing);
                                Object nbtFacing= NBTTagUtil.createNBTTagByte(transformedFacing);
                                NMSUtil.invokeNMS("NBTTagCompound","set",
                                                  new Class[]{String.class,NMSUtil.getNMSClass("NBTBase")},
                                                  nbt,"Facing",nbtFacing);
                                if(type.equals("minecraft:item_frame") /*&& transformedFacing < 2*/) {
                                    Byte itemRot = (Byte)  NMSUtil.invokeNMS("NBTTagCompound","getByte",
                                                        new Class[]{String.class},
                                                        nbt,"ItemRotation");
Logger.getGlobal().info("itemFrame: "+newPosition.getX()+" "+newPosition.getY()+" "+newPosition.getZ()+" "+facing +" "+itemRot);
                                    itemRot = rotation.transformItemRotation(facing,itemRot);
                                    Object nbtItemRot= NBTTagUtil.createNBTTagByte(itemRot);
                                    NMSUtil.invokeNMS("NBTTagCompound","set",
                                                      new Class[]{String.class,NMSUtil.getNMSClass("NBTBase")},
                                                      nbt,"ItemRotation",nbtItemRot);
                                }
                            }
                            
                            //put Tile Tags for hanging entities.
                            int tileX = tileCoord(newPosition.getX());
                            int tileY = tileCoord(newPosition.getY());
                            int tileZ = tileCoord(newPosition.getZ());
                            if(type.equals("minecraft:painting")) {
//Logger.getGlobal().log(Level.INFO,"painting: {0} {1} {2} {3} {4}",new Object[]{facing,newPosition.getX(),tileX,newPosition.getZ(),tileZ});
                                if(facing==2 && (newPosition.getX()-tileX>0.6)) {
                                    tileX++;
                                } else if(facing==3 && (newPosition.getZ()-tileZ>0.6)) {
                                    tileZ++;
                                }
                            }
                            Object nbtTileX = NBTTagUtil.createNBTTagInt(tileX);
                            Object nbtTileY = NBTTagUtil.createNBTTagInt(tileY);
                            Object nbtTileZ= NBTTagUtil.createNBTTagInt(tileZ);
                            NMSUtil.invokeNMS("NBTTagCompound","set",
                                              new Class[]{String.class,NMSUtil.getNMSClass("NBTBase")},
                                              nbt,"TileX",nbtTileX);
                            NMSUtil.invokeNMS("NBTTagCompound","set",
                                              new Class[]{String.class,NMSUtil.getNMSClass("NBTBase")},
                                              nbt,"TileY",nbtTileY);
                            NMSUtil.invokeNMS("NBTTagCompound","set",
                                              new Class[]{String.class,NMSUtil.getNMSClass("NBTBase")},
                                              nbt,"TileZ",nbtTileZ);
                            
                            //give random UUID to entity
                            UUID uuid = UUID.randomUUID();
                            Object nbtLeast = NBTTagUtil.createNBTTagLong(uuid.getLeastSignificantBits());
                            Object nbtMost = NBTTagUtil.createNBTTagLong(uuid.getMostSignificantBits());
                            NMSUtil.invokeNMS("NBTTagCompound","set",
                                              new Class[]{String.class,NMSUtil.getNMSClass("NBTBase")},
                                              nbt,"UUIDLeast",nbtLeast);
                            NMSUtil.invokeNMS("NBTTagCompound","set",
                                              new Class[]{String.class,NMSUtil.getNMSClass("NBTBase")},
                                              nbt,"UUIDLeast",nbtMost);
                            
                            //create entity
//Logger.getGlobal().log(Level.INFO, "************************************");
//Logger.getGlobal().log(Level.INFO, "NBT: "+nbt.toString());
                            Object nmsWorld = NMSUtil.invokeCraftBukkit("CraftWorld", "getHandle", null,
                                                                        location.getWorld());
                            Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("NBTTagCompound"),
                                                              NMSUtil.getNMSClass("World")};
                            Object entity = NMSUtil.invokeNMS("EntityTypes","a",argsClasses,null,nbt,nmsWorld);
                             
//Logger.getGlobal().info("ENTITY: "+entity.getClass().getCanonicalName());
                             //add entity to world
                            argsClasses = new Class[]{NMSUtil.getNMSClass("Entity"),
                                                      CreatureSpawnEvent.SpawnReason.CUSTOM.getClass()};
                            NMSUtil.invokeNMS("WorldServer","addEntity",argsClasses,nmsWorld,((Optional)entity).get(), 
                                              CreatureSpawnEvent.SpawnReason.CUSTOM);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
//Logger.getGlobal().info("Size: "+finalSize.getBlockX()+" "+finalSize.getBlockY()+" "+finalSize.getBlockZ());
                    Location high = location.clone().add(finalSize.toLocation(location.getWorld()));
                    //location.getWorld().save();
                    /*for(int i=location.getChunk().getX(); i<= high.getChunk().getX();i++) {
                        for(int j = location.getChunk().getZ();  j<=high.getChunk().getZ();j++) {
                            Chunk chunk = location.getWorld().getChunkAt(i,j);
Logger.getGlobal().info("Chunk: "+location.getChunk().getX()+" "+i+" "+high.getChunk().getX()); 
Logger.getGlobal().info("Chunk: "+location.getChunk().getZ()+" "+j+" "+high.getChunk().getZ()); 
                            chunk.unload(true);
                        }
                    }*/
                    NMSUtil.updatePlayerChunks(location, high);
            //    }
            //}.runTaskLater(PluginUtilsPlugin.getInstance(), 1);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MCMEPlotFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
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

}
