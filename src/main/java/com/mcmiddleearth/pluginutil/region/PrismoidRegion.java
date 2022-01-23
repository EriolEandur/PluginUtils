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
package com.mcmiddleearth.pluginutil.region;

//import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.math.BlockVector2;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

/**
 *
 * @author Eriol_Eandur
 */
public class PrismoidRegion extends Region{
 
    private List<Integer> xPoints = new ArrayList<>();
    
    private List<Integer> zPoints = new ArrayList<>();

    private List<Double> gradients = new ArrayList<>();
    
    private int minX, maxX, minZ, maxZ;
    
    private int minY, maxY;
    
    public PrismoidRegion(Location location, com.sk89q.worldedit.regions.Polygonal2DRegion weRegion) {
        super(location);
        if(weRegion.getWorld()==null || !weRegion.getWorld().getName().equals(location.getWorld().getName())) {
            return;
        }
        
        for(BlockVector2 point: weRegion.getPoints()) {
            xPoints.add(point.getBlockX());
            zPoints.add(point.getBlockZ());
        }
        minY = weRegion.getMinimumY();
        maxY = weRegion.getMaximumY();
        calculateBorders();
    }
    
    public PrismoidRegion(Location location, List<Integer> xPoints, List<Integer> zPoints, int minY, int maxY) {
        super(location);
        this.xPoints = xPoints;
        this.zPoints = zPoints;
        this.minY = minY;
        this.maxY = maxY;
        calculateBorders();
    }
    
    public Integer[] getXPoints() {
        return xPoints.toArray(new Integer[0]);
    }
    
    public Integer[] getZPoints() {
        return zPoints.toArray(new Integer[0]);
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    private void calculateBorders() {
        int j=xPoints.size()-1;
        gradients.clear();
        minX = Integer.MAX_VALUE;
        minZ = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        maxZ = Integer.MIN_VALUE;
        double angle = 0;
        for(int i = 0; i< xPoints.size();i++) {
            int x1 = xPoints.get(i);
            int x2 = xPoints.get((i+1)%xPoints.size());
            int x3 = xPoints.get((i+2)%xPoints.size());
            int z1 = zPoints.get(i);
            int z2 = zPoints.get((i+1)%zPoints.size());
            int z3 = zPoints.get((i+2)%zPoints.size());
            Vector edge1 = new Vector(x2-x1,0,z2-z1);
            Vector edge2 = new Vector(x3-x2,0,z3-z2);
            //angle = angle + edge1.angle(edge2);
            angle = angle + Math.asin(edge1.crossProduct(edge2)
                                      .multiply(1/(edge1.length()*edge2.length())).getY());
            gradients.add((z2 - z1 ) / ( 1.0*x2 - x1 ));
            if(x1<minX) minX = x1;
            if(x1>maxX) maxX = x1;
            if(z1<minZ) minZ = z1;
            if(z1>maxZ) maxZ = z1;
        }
        if(angle<0) {
            List<Integer> newXPoints = new ArrayList<>();
            List<Integer> newZPoints = new ArrayList<>();
            List<Double> newGradients = new ArrayList<>();
            for(int i = xPoints.size()-1; i>=0;i--) {
                newXPoints.add(xPoints.get(i));
                newZPoints.add(zPoints.get(i));
                if(i>0) {
                    newGradients.add(gradients.get(i-1));
                }
            }
            newGradients.add(gradients.get(gradients.size()-1));
            xPoints = newXPoints;
            zPoints = newZPoints;
            gradients = newGradients;
        }
    }
    
    private boolean isInsideX(Location loc) {
        if(!this.isValid() || !location.getWorld().equals(loc.getWorld())
                || loc.getBlockY()<minY || loc.getBlockY()>maxY) {
            return false;
        }
        boolean isInside = false;
        int j=xPoints.size()-1;
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        for(int i = 0; i< xPoints.size();i++) {
            int x1 = xPoints.get(i);
            int x2 = xPoints.get((i+1)%xPoints.size());
            int z1 = zPoints.get(i);
            int z2 = zPoints.get((i+1)%zPoints.size());
            int borderAddition = 0;
            if(z1>z2) borderAddition = 1;
            if(((z1 < z)!=(z2 < z)) && (x < Math.round((z - z1 ) / gradients.get(i)) + x1 + borderAddition)) {
                isInside= !isInside;
            }
        }
        return isInside;
    }
    
    private boolean isInsideZ(Location loc) {
        if(!this.isValid() || !location.getWorld().equals(loc.getWorld())
                || loc.getBlockY()<minY || loc.getBlockY()>maxY) {
            return false;
        }
        boolean isInside = false;
        int j=xPoints.size()-1;
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        for(int i = 0; i< xPoints.size();i++) {
            int x1 = xPoints.get(i);
            int x2 = xPoints.get((i+1)%xPoints.size());
            int z1 = zPoints.get(i);
            int z2 = zPoints.get((i+1)%zPoints.size());
            int borderAddition = 0;
            if(x1>x2) borderAddition =  - 1;
            if(((x1 < x)!=(x2 < x)) && (z > Math.round(gradients.get(i) * ( x - x1 )) + z1  + borderAddition)) {
                isInside= !isInside;
            }
        }
        return isInside;
    }
    
    @Override
    public boolean isNear(Location loc, int distance) {
        if(   loc.getBlockX()<minX-distance || loc.getBlockX()>maxX+distance 
           || loc.getBlockY()<minY-distance || loc.getBlockY()>maxY+distance 
           || loc.getBlockZ()<minZ-distance || loc.getBlockZ()>maxZ+distance ) {
            return false;
        }
        return true;
    }
    
    private boolean isAtCorner(Location loc) {
        if(loc.getBlockY()>=minY && loc.getBlockY()<=maxY) {
            for(int i = 0; i < xPoints.size();i++) {
                if(loc.getBlockX()==xPoints.get(i) && loc.getBlockZ()==zPoints.get(i)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean isInside(Location loc) {
        if(!isNear(loc,0)) {
            return false;
        }
        return isInsideZ(loc) || isInsideX(loc) || isAtCorner(loc);
    }
    
    @Override
    public boolean isValid() {
        return super.isValid() && xPoints.size()>2;
    }

    @Override
    public void save(ConfigurationSection config) {
        super.save(config);
        config.set("xPoints",xPoints);
        config.set("zPoints",zPoints);
        config.set("minY",minY);
        config.set("maxY",maxY);
    }
    
    public static boolean isValidConfig(ConfigurationSection config) {
        return config.contains("xPoints");
    }
    
    public static PrismoidRegion load(ConfigurationSection config) {
        Location location = Region.loadLocation(config);
        List<Integer> xPoints = config.getIntegerList("xPoints");
        List<Integer> zPoints = config.getIntegerList("zPoints");
        int minY = config.getInt("minY");
        int maxY = config.getInt("maxY");
        return new PrismoidRegion(location, xPoints, zPoints, minY, maxY);
    }
    
    @Override
    public String toString() {
        String result = super.toString()+" Height-level: "+minY+" - "+maxY+ " Points: ";
        for(int i=0;i<xPoints.size();i++) {
            result = result + "("+xPoints.get(i)+"/"+zPoints.get(i)+") ";
        }
        return result;
    }
}
