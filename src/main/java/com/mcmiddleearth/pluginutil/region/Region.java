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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Eriol_Eandur
 */
public abstract class Region{
    
    protected Location location;
    
    protected Region(Location location) {
        this.location = location;
    }
    
    public static Location loadLocation(ConfigurationSection config) {
        World world = Bukkit.getWorld(config.getString("world"));
        return new Location (world, config.getInt("locationX"),
                                    config.getInt("locationY"),
                                    config.getInt("locationZ"),
                                    (float)config.getDouble("locationYaw"),
                                    (float)config.getDouble("locationPitch"));
    }

    public Location getLocation() {
        return location;
    }

    public void save(ConfigurationSection config) {
        config.set("world", location.getWorld().getName());
        config.set("locationX", location.getBlockX());
        config.set("locationY", location.getBlockY());
        config.set("locationZ", location.getBlockZ());
        config.set("locationYaw", location.getYaw());
        config.set("locationPitch", location.getPitch());
    }
 
    public abstract boolean isInside(Location location);
    
    public abstract boolean isNear(Location location, int distance);
    
    public boolean isValid() {
        return location != null && location.getWorld()!=null;
    }
    
    public World getWorld() {
        return location.getWorld();
    }
    
    @Override
    public String toString() {
        return "Location: "+location.toString();
    }
    
}
