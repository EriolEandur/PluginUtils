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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class ReflectionUtil {
    
    public static void showFields(Object object) {
        Logger.getGlobal().info("***************************************************");
        Field[] fields = object.getClass().getDeclaredFields();
        Logger.getGlobal().info("Fields for "+object.getClass().toString()+":");
        for(Field field: fields) {
            Logger.getGlobal().info(field.toString());
        }
        Logger.getGlobal().info("***************************************************");
    }
    
    public static void showMethods(Object object) {
        Logger.getGlobal().info("***************************************************");
        Method[] methods = object.getClass().getMethods();
        Logger.getGlobal().info("Methods for "+object.getClass().toString()+":");
        for(Method method: methods) {
            Logger.getGlobal().info(method.toString());
        }
        Logger.getGlobal().info("***************************************************");
    }
    
    public static void showDeclaredMethods(Object object) {
        Logger.getGlobal().info("***************************************************");
        Method[] methods = object.getClass().getDeclaredMethods();
        Logger.getGlobal().info("Methods for "+object.getClass().toString()+":");
        for(Method method: methods) {
            Logger.getGlobal().info(method.toString());
        }
        Logger.getGlobal().info("***************************************************");
    }
    
    public static Object getField(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            if(field!=null) {
                    return field.get(object);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(ReflectionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Class getFieldClass(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            if(field!=null) {
                    return field.getType();
            }
        } catch (IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(ReflectionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Object invoke(Object object, String methodName, Object[] args) {
        try {
            Class[] argsClasses = new Class[args.length];
            for(int i=0;i<args.length;i++) {
                argsClasses[i] = args[i].getClass();
            }
            Method method = object.getClass().getDeclaredMethod(methodName, argsClasses);
            if(method!=null) {
                    return method.invoke(object, args);
            }
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchMethodException | InvocationTargetException ex) {
            Logger.getLogger(ReflectionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
 
    public static Class getMethodReturnClass(Object object, String methodName, Object[] args) {
        try {
            Class[] argsClasses = new Class[args.length];
            for(int i=0;i<args.length;i++) {
                argsClasses[i] = args[i].getClass();
            }
            Method method = object.getClass().getDeclaredMethod(methodName, argsClasses);
            if(method!=null) {
                    return method.getReturnType();
            }
        } catch (IllegalArgumentException | SecurityException | NoSuchMethodException ex) {
            Logger.getLogger(ReflectionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    

}
