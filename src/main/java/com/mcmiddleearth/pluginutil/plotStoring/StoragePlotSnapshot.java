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
package com.mcmiddleearth.pluginutil.plotStoring;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;

/**
 *
 * @author Eriol_Eandur
 */
public class StoragePlotSnapshot {

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

    public StoragePlotSnapshot(IStoragePlot plot) {
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
                chunks[i][j] = chunk.getChunkSnapshot(false, true, false);
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
        for(int y = lowCorner.getWorld().getMaxHeight()-1; y>0; y--) {
            if(!chunks[getIndexX(x)][getIndexZ(z)]
                     .getBlockType(getInsideChunk(x), y, getInsideChunk(z))
                     .equals(Material.AIR)) {
                return y;
            } 
        }
        return 0;
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
