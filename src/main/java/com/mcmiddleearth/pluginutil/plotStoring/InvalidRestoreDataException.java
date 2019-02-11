/* 
 *  Copyright (C) 2015 Minecraft Middle Earth
 * 
 *  This file is part of PlotBuild.
 * 
 *  PlotBuild is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  PlotBuild is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with PlotBuild.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.pluginutil.plotStoring;

/**
 *
 * @author Ivan1pl
 */
public class InvalidRestoreDataException extends Exception {
    
    public InvalidRestoreDataException() {
        super();
    }
    
    public InvalidRestoreDataException(String message) {
        super(message);
    }
    
    public InvalidRestoreDataException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidRestoreDataException(Throwable cause) {
        super(cause);
    }
    
}
