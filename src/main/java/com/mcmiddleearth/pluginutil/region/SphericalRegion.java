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

/**
 *
 * @author Eriol_Eandur
 */
public class SphericalRegion extends Region{
 
    private int radius;
    
    public SphericalRegion(Location location, int radius) {
        super(location);
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public boolean isInside(Location loc) {
        return isNear(loc,0);
    }
    
    @Override
    public boolean isNear(Location loc, int distance) {
        return this.isValid()
                && location.getWorld().equals(loc.getWorld())
                && location.distance(loc)<=radius+distance;
    }
    
    @Override
    public void save(ConfigurationSection config) {
        super.save(config);
        config.set("radius", radius);
    }
    
    public static boolean isValidConfig(ConfigurationSection config) {
        return config.contains("radius");
    }
    
    public static SphericalRegion load(ConfigurationSection config) {
        Location location = Region.loadLocation(config);
        return new SphericalRegion(location, config.getInt("radius"));
    }
    
    @Override
    public String toString() {
        return super.toString()+" Radius: "+radius;
    }

}
