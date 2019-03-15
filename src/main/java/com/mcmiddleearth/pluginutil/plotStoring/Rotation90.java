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

import com.mcmiddleearth.pluginutil.BlockUtil;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.util.Vector;

/**
 *
 * @author Eriol_Eandur
 */
public class Rotation90 {

    private final byte repetitions;
    private final Vector center;
    private final Vector lowCorner;
    private final Vector size;

    public Rotation90(Vector lowCorner, Vector size, int repetitions) {
        this.repetitions = (byte)(repetitions%4);
        this.lowCorner = lowCorner.clone();
        this.size = size.clone();
        center = lowCorner.clone().add(size.clone().multiply(0.5));
    }

    public Vector rotateVector(Vector vector,boolean isBlockVector) {

        Vector local;
        switch(repetitions%4) {
            case 0:
                return vector;
            case 2:
                local = vector.clone().subtract(center);
                break;
            default:
                local = vector.clone().subtract(lowCorner);
        }
        int blockShift = (isBlockVector?1:0);
        switch(repetitions%4) {
            case 1:
                return new Vector(-local.getZ()+size.getZ()-blockShift,local.getY(),local.getX()).add(lowCorner);
            case 2:
                return new Vector(-local.getX()-blockShift,local.getY(),-local.getZ()-blockShift).add(center);
            case 3:
                return new Vector(local.getZ(),local.getY(),-local.getX()+size.getX()-blockShift).add(lowCorner);
            default:
                return vector;
        }
    }

    public float rotateYaw(float yaw) {
        return (yaw + repetitions * 90) % 360;
    }

    public BlockData rotateBlockData(BlockData data) {
        if(data instanceof Directional) {
            ((Directional)data).setFacing(BlockUtil
                               .rotateBlockFace(((Directional)data).getFacing(),repetitions));
        } else if(data instanceof Orientable && (repetitions==1 || repetitions == 3)) {
            switch(((Orientable)data).getAxis()) {
                case X: 
                    ((Orientable)data).setAxis(Axis.Z);
                    break;
                case Y:
            }
        } else if(data instanceof MultipleFacing) {
            MultipleFacing clone = (MultipleFacing) data.clone();
            for(BlockFace face: ((MultipleFacing)clone).getAllowedFaces()) {
                BlockFace rotatedFace = BlockUtil.rotateBlockFace(face, repetitions);
//Logger.getGlobal().info("rotatedFace: "+rotatedFace);
                ((MultipleFacing)data).setFace(rotatedFace, 
                                               ((MultipleFacing)clone).hasFace(face));
            }
        } else if(data instanceof Rotatable) {
            ((Rotatable)data).setRotation(BlockUtil
                               .rotateBlockFace(((Rotatable)data).getRotation(),repetitions));
         }
        return data;
    }

    public byte rotateHangingEntity(String type, byte face) {
        if(type.equals("minecraft:painting")) {
            if(face >=0) {
                return (byte) ((face+repetitions)%4);
            }
        } else if(type.equals("minecraft:item_frame")) {
            if(face > 1) {
                byte[] order = new byte[]{5,3,4,2};
                for(int i=0; i< order.length;i++) {
                    if(order[i]==face) {
                        return order[(i+repetitions)%order.length];
                    }
                }
            }
        }
        return face;
    }
}
