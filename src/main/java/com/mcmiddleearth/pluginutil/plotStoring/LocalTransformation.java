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
import java.util.logging.Logger;
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
public class LocalTransformation {

    private final byte rotations;
    private final Vector center;
    private final Vector lowCorner;
    private final Vector size;
    private final boolean[] flips;

    public LocalTransformation(Vector lowCorner, Vector size, int rotations, boolean[] flips) {
        this.rotations = (byte)(rotations%4);
        this.lowCorner = lowCorner.clone();
        this.flips = flips;
        this.size = size.clone();
        center = lowCorner.clone().add(size.clone().multiply(0.5));
//Logger.getGlobal().info("Local Trans: "+this.lowCorner.getX()+" "+this.lowCorner.getY()+" "+this.lowCorner.getZ());
    }

    public Vector transformVector(Vector vector,boolean isBlockVector) {
        return flipVector(rotateVector(vector, isBlockVector),isBlockVector);
    }
    
    private Vector flipVector(Vector vector, boolean isBlockVector) {
//Logger.getGlobal().info("rot vector: "+vector.getX()+" "+vector.getY()+" "+vector.getZ());
        Vector rotatedSize;
        if(rotations%2==0) {
            rotatedSize = size;
        } else {
            rotatedSize = new Vector(size.getZ(),size.getY(),size.getX());
        }
        Vector rotatedCenter = lowCorner.clone().add(rotatedSize.clone().multiply(0.5));
        Vector local = vector.clone().subtract(rotatedCenter);
        int blockShift = isBlockVector?1:0;
//Logger.getGlobal().info("flipVector: "+lowCorner.getX()+" "+center.getX()+" "+local.getX()+" "+blockShift);
        return  new Vector(flips[0]?rotatedCenter.getX()-local.getX()-blockShift:vector.getX(),
                           flips[1]?rotatedCenter.getY()-local.getY()-blockShift:vector.getY(),
                           flips[2]?rotatedCenter.getZ()-local.getZ()-blockShift:vector.getZ());
    }
    
    private Vector rotateVector(Vector vector, boolean isBlockVector) {
        Vector local;
        switch(rotations%4) {
            case 0:
                return vector;
            case 2:
                local = vector.clone().subtract(center);
                break;
            default:
                local = vector.clone().subtract(lowCorner);
        }
        int blockShift = (isBlockVector?1:0);
        switch(rotations%4) {
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

    public float transformYaw(float yaw) {
//Logger.getGlobal().info("yaw: "+yaw);
       float rotated = rotateYaw(yaw);
//Logger.getGlobal().info("rotated: "+rotated);
       float flipped = flipYaw(rotated);
//Logger.getGlobal().info("flipped: "+flipped);
       return flipped;
       //return flipYaw(rotateYaw(yaw));
    }

    public float rotateYaw(float yaw) {
        return (yaw + rotations * 90) % 360;
    }

    public float flipYaw(float yaw) {
        if(flips[2]) {
            return (540-yaw) % 360;
        }
        if(flips[0]) {
            return (360-yaw) % 360;
        }
        return yaw;
    }

    public BlockData transformBlockData(BlockData data) {
        return flipBlockData(rotateBlockData(data));
    }
    
    private BlockData flipBlockData(BlockData data) {
        if(data instanceof Directional) {
            ((Directional)data).setFacing(BlockUtil
                               .flipBlockFace(((Directional)data).getFacing(),flips));
        } else if(data instanceof MultipleFacing) {
            MultipleFacing clone = (MultipleFacing) data.clone();
            for(BlockFace face: ((MultipleFacing)clone).getAllowedFaces()) {
                BlockFace flippedFace = BlockUtil.flipBlockFace(face, flips);
//Logger.getGlobal().info("rotatedFace: "+rotatedFace);
                ((MultipleFacing)data).setFace(flippedFace, 
                                               ((MultipleFacing)clone).hasFace(face));
            }
        } else if(data instanceof Rotatable) {
            ((Rotatable)data).setRotation(BlockUtil
                               .flipBlockFace(((Rotatable)data).getRotation(),flips));
         }
        return data;
    }
    
    private BlockData rotateBlockData(BlockData data) {
        if(data instanceof Directional) {
            ((Directional)data).setFacing(BlockUtil
                               .rotateBlockFace(((Directional)data).getFacing(),rotations));
        } else if(data instanceof Orientable && (rotations==1 || rotations == 3)) {
            switch(((Orientable)data).getAxis()) {
                case X: 
                    ((Orientable)data).setAxis(Axis.Z);
                    break;
                case Y:
            }
        } else if(data instanceof MultipleFacing) {
            MultipleFacing clone = (MultipleFacing) data.clone();
            for(BlockFace face: ((MultipleFacing)clone).getAllowedFaces()) {
                BlockFace rotatedFace = BlockUtil.rotateBlockFace(face, rotations);
//Logger.getGlobal().info("rotatedFace: "+rotatedFace);
                ((MultipleFacing)data).setFace(rotatedFace, 
                                               ((MultipleFacing)clone).hasFace(face));
            }
        } else if(data instanceof Rotatable) {
            ((Rotatable)data).setRotation(BlockUtil
                               .rotateBlockFace(((Rotatable)data).getRotation(),rotations));
         }
        return data;
    }

    public byte transformHangingEntity(String type, byte face) {
        return flipHangingEntity(type,rotateHangingEntity(type, face));
    }
    
    private byte flipHangingEntity(String type, byte face) {
        if(type.equals("minecraft:painting")) {
            if(flips[2] && face%2 == 0) {
                return (byte) ((face+2)%4);
            } else if(flips[0] && face%2 == 1) {
                return (byte) ((face+2)%4);
            }
        } else if(type.equals("minecraft:item_frame")) {
            if(flips[1] && face <= 1) {
                return (byte) ((face+1)%2);
            } else if(flips[2] && (face == 2 || face == 3)) {
                return (byte) ((face-1)%2+2);
            } else if(flips[0] && face >= 4) {
                return (byte) ((face-3)%2+4);
            }
        }
        return face;
    }

    private byte rotateHangingEntity(String type, byte face) {
        if(type.equals("minecraft:painting")) {
            if(face >=0) {
                return (byte) ((face+rotations)%4);
            }
        } else if(type.equals("minecraft:item_frame")) {
            if(face > 1) {
                byte[] order = new byte[]{5,3,4,2};
                for(int i=0; i< order.length;i++) {
                    if(order[i]==face) {
                        return order[(i+rotations)%order.length];
                    }
                }
            }
        }
        return face;
    }
    
    public Byte transformItemRotation(Byte facing, Byte itemRot) {
        return flipItemRotation(rotateHangingEntity("minecraft:item_frame",facing), rotateItemRotation(facing,itemRot));
    }
    
    private Byte rotateItemRotation(Byte facing, Byte itemRot) {
        if(facing==1) {
            itemRot = (byte)(itemRot+2*rotations);
        } else if(facing == 0) {
            itemRot = (byte)(itemRot+(8-2*rotations));
        }
        return (byte)(itemRot%8);
    }
    
    private Byte flipItemRotation(Byte facing, Byte itemRot) {
        if(flips[2] && (facing==0 || facing == 1)) {
            itemRot = flipY(itemRot);
        } else if(flips[2]) {
            itemRot = flipXVerticalFacing(itemRot);
        }
        if(flips[1] && (facing==0 || facing == 1)) {
            itemRot = flipY(itemRot);
        } else if(flips[1]) {
            itemRot = flipYVerticalFacing(itemRot);
        }
        if(flips[0] && (facing==0 || facing == 1)) {
            itemRot = flipX(itemRot);
        } else if(flips[0]) {
            itemRot = flipXVerticalFacing(itemRot);
        }
        return itemRot;
    }
    
    private Byte flipXVerticalFacing(Byte itemRot) {
        Byte flippedRot = itemRot;
        switch(itemRot) {
            case 2:
            case 6:
               flippedRot = (byte) ((itemRot+4)%8);
               break;
            case 1:
               flippedRot = 7;
               break;
            case 7:
               flippedRot = 1;
               break;
            case 3:
               flippedRot = 5;
               break;
            case 5:
               flippedRot = 3;
               break;
        }
        return flippedRot;
    }
    
    private Byte flipYVerticalFacing(Byte itemRot) {
        Byte flippedRot = itemRot;
        switch(itemRot) {
            case 0:
            case 4:
               flippedRot = (byte) ((itemRot+4)%8);
               break;
            case 1:
               flippedRot = 3;
               break;
            case 3:
               flippedRot = 1;
               break;
            case 5:
               flippedRot = 7;
               break;
            case 7:
               flippedRot = 5;
               break;
        }
        return flippedRot;
    }
    
    private Byte flipX(Byte itemRot) {
        Byte flippedRot = itemRot;
        switch(itemRot) {
            case 1:
            case 5:
               flippedRot = (byte) ((itemRot+4)%8);
               break;
            case 0:
               flippedRot = 6;
               break;
            case 6:
               flippedRot = 0;
               break;
            case 2:
               flippedRot = 4;
               break;
            case 4:
               flippedRot = 2;
               break;
        }
        return flippedRot;
    }
    
    private Byte flipY(Byte itemRot) {
        Byte flippedRot = itemRot;
        switch(itemRot) {
            case 3:
            case 7:
               flippedRot = (byte) ((itemRot+4)%8);
               break;
            case 0:
               flippedRot = 2;
               break;
            case 2:
               flippedRot = 0;
               break;
            case 4:
               flippedRot = 6;
               break;
            case 6:
               flippedRot = 4;
               break;
        }
        return flippedRot;
    }
    
}
