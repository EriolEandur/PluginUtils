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
package com.mcmiddleearth.pluginutils;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author Eriol_Eandur
 */
public class FileUtil {
 
    /*public static FilenameFilter getFileExtFilter(final String extension) {
        return new FilenameFilter () {
            @Override
            public boolean accept(File file, String string) {
                return string.endsWith("."+extension);
            }
        };
    }*/
    
    public static FileFilter getFileExtFilter(final String extension) {
        return new FileFilter () {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith("."+extension) && !file.isDirectory();
            }
        };
    }

    public static FileFilter getFileOnlyFilter() {
        return new FileFilter () {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory();
            }
        };
    }

    public static FileFilter getDirFilter() {
        return new FileFilter () {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
    }
    
    public static String getShortName(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf('.'));
    }

    public static String getRelativePath(File file, File directory) {
        return file.getPath().replace(directory.getPath(), "").substring(1);
    }

}
