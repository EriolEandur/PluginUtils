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
import org.bukkit.Bukkit;
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
public class EmptyStoragePlotSnapshot extends StoragePlotSnapshot {

    private static final BlockData air = Bukkit.createBlockData(Material.AIR);
    private static final List<Entity> noEntities = new ArrayList<>();
    private static final List<BlockState> noTileEntities = new ArrayList<>();
    
    public EmptyStoragePlotSnapshot(IStoragePlot plot) {
        super(plot);
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        return air;
    }

    @Override
    public List<BlockState> getTileEntities() {
        return noTileEntities;
    }

    @Override
    public List<Entity> getEntities() {
        return noEntities;
    }

}
