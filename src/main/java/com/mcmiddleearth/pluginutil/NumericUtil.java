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
package com.mcmiddleearth.pluginutil;

import java.util.Random;

/**
 * Numbers utility class
 * @author Eriol_Eandur
 */
public class NumericUtil {
    
    public static int getInt(String str) {
        try {
            return Integer.parseInt(str.trim());
        }
        catch(NumberFormatException e) {
            return -1;
        }
    }
    
    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int getShort(String str) {
        try {
            return Short.parseShort(str.trim());
        }
        catch(NumberFormatException e) {
            return -1;
        }
    }
    
    public static boolean isShort(String s) {
        try {
            Short.parseShort(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static long getLong(String str) {
        try {
            return Long.parseLong(str.trim());
        }
        catch(NumberFormatException e) {
            return -1;
        }
    }
    
    public static boolean isLong(String s) {
        try {
            Long.parseLong(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int getRandom(int lower, int upper) {
        Random random = new Random();
        return random.nextInt((upper - lower) + 1) + lower;
    }




}
