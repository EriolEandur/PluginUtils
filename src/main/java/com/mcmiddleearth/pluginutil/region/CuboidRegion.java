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

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

/**
 *
 * @author Eriol_Eandur
 */
public class CuboidRegion extends Region{
 
    private Vector minCorner, maxCorner;
    
    public CuboidRegion(Location location, com.sk89q.worldedit.regions.CuboidRegion weRegion) {
        super(location);
        if(weRegion.getWorld()==null || !weRegion.getWorld().getName().equals(location.getWorld().getName())) {
            return;
        }
        minCorner = new Vector(weRegion.getMinimumPoint().getBlockX(),
                               weRegion.getMinimumPoint().getBlockY(),
                               weRegion.getMinimumPoint().getBlockZ());
        maxCorner = new Vector(weRegion.getMaximumPoint().getBlockX(),
                               weRegion.getMaximumPoint().getBlockY(),
                               weRegion.getMaximumPoint().getBlockZ());
    }
    
    public CuboidRegion(Location location, Vector min, Vector max) {
        super(location);
        minCorner = min;
        maxCorner = max;
    }

    public Vector getMinCorner() {
        return minCorner;
    }

    public Vector getMaxCorner() {
        return maxCorner;
    }

    @Override
    public boolean isInside(Location loc) {
        return this.isNear(loc,0);
    }
    
    @Override
    public boolean isNear(Location loc, int distance) {
        return this.isValid()
                && location.getWorld().equals(loc.getWorld())
                && minCorner.getBlockX()-distance<=loc.getBlockX()
                && minCorner.getBlockY()-distance<=loc.getBlockY()
                && minCorner.getBlockZ()-distance<=loc.getBlockZ()
                && maxCorner.getBlockX()+distance>=loc.getBlockX()
                && maxCorner.getBlockY()+distance>=loc.getBlockY()
                && maxCorner.getBlockZ()+distance>=loc.getBlockZ();
    }
    
    @Override
    public boolean isValid() {
        return super.isValid() && minCorner!=null && maxCorner!=null;
    }

    public void setCorners(Vector pos1, Vector pos2) {
        minCorner = new Vector(Math.min(pos1.getBlockX(),pos2.getBlockX()),
                               Math.min(pos1.getBlockY(),pos2.getBlockY()),
                               Math.min(pos1.getBlockZ(),pos2.getBlockZ()));
        maxCorner = new Vector(Math.max(pos1.getBlockX(),pos2.getBlockX()),
                               Math.max(pos1.getBlockY(),pos2.getBlockY()),
                               Math.max(pos1.getBlockZ(),pos2.getBlockZ()));
    }
    
    @Override
    public void save(ConfigurationSection config) {
        super.save(config);
        config.set("minCornerX", minCorner.getBlockX());
        config.set("minCornerY", minCorner.getBlockY());
        config.set("minCornerZ", minCorner.getBlockZ());
        config.set("maxCornerX", maxCorner.getBlockX());
        config.set("maxCornerY", maxCorner.getBlockY());
        config.set("maxCornerZ", maxCorner.getBlockZ());
    }
    
    public static boolean isValidConfig(ConfigurationSection config) {
        return config.contains("minCornerX");
    }
    
    public static CuboidRegion load(ConfigurationSection config) {
        Location location = Region.loadLocation(config);
        return new CuboidRegion(location, new Vector(config.getInt("minCornerX",0),
                                                    config.getInt("minCornerY",0),
                                                    config.getInt("minCornerZ",0)),
                                          new Vector( config.getInt("maxCornerX",0),
                                                    config.getInt("maxCornerY",0),
                                                    config.getInt("maxCornerZ",0)));
    }
    
    @Override
    public String toString() {
        return super.toString()+" Corners: "+minCorner.toString()+"; "+maxCorner.toString();
    }
}
