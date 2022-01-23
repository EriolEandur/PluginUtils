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

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */
public class EntityUtil {
    
    private static JavaPlugin plugin;
    
    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    public static void store(File file, Entity entity) throws IOException{
        List<Entity> list = new ArrayList<>();
        list.add(entity);
        store(file, list);
    }
    
    public static void store(File file, Collection<Entity> entities) throws IOException {
        FileConfiguration config = new YamlConfiguration();
        store(config, entities);
        config.save(file);
    }
    
    public static void store(ConfigurationSection config, Collection<Entity> entities) {
        for(Entity entity: entities) {
            store(config, entity, entity.getEntityId()+"");
        }
    }
    
    public static void store(ConfigurationSection config, Entity entity, String key){
        if(entity instanceof Painting) {
            config.set(key, serializePainting((Painting) entity));
        }
        else if(entity instanceof ItemFrame) {
            config.set(key, serializeItemFrame((ItemFrame) entity));
        }
        else if(entity instanceof ArmorStand) {
            config.set(key, serializeArmorStand((ArmorStand) entity));
        }
    }
    
    public static boolean restore(File file, List<Entity> entityList) throws IOException, InvalidConfigurationException{
        FileConfiguration config = new YamlConfiguration();
        config.load(file);
        return restore(config, entityList);
    }
    
    public static boolean restore(ConfigurationSection config, List<Entity> entityList) {
        boolean result = true;
        for(String entityKey: config.getKeys(false)) {
            Entity entity = restore(config, entityKey);
            if(entity!=null) {
                entityList.add(entity);
            }
            else {
                result=false;
            }
        }
        return result;
    }
    
    public static Entity restore(ConfigurationSection config, String entityKey){
        ConfigurationSection entityData = config.getConfigurationSection(entityKey);
        EntityType type = EntityType.valueOf(entityData.getString("type"));
        switch(type) {
            case PAINTING:
                return deserializePainting(entityData);
            case ITEM_FRAME:
                return deserializeItemFrame(entityData);
            case ARMOR_STAND:
                return deserializeArmorStand(entityData);
            default:
                return null;
        }
    }
    
    public static Map<String,Object> serializeEntity(Entity entity) {
        Map<String,Object> result = new HashMap<>();
        Location loc = entity.getLocation();
        result.put("location", serializeLocation(loc));
        result.put("uuid", entity.getUniqueId().toString());
        return result;
    }
    
    public static Map<String,Object> serializeArmorStand(ArmorStand armor) {
        Map<String,Object> result = serializeEntity(armor);

        result.put("type", EntityType.ARMOR_STAND.name());
        
        Map<String,Object> items = new HashMap<>();
        items.put("boots", armor.getBoots().serialize());
        items.put("chestplate", armor.getChestplate().serialize());
        items.put("helmet", armor.getHelmet().serialize());
        items.put("leggins", armor.getLeggings().serialize());
        items.put("hand", armor.getItemInHand().serialize());
        
        Map<String,Object> pose = new HashMap<>();
        pose.put("body", serializeEulerAngle(armor.getBodyPose()));
        pose.put("leftArm", serializeEulerAngle(armor.getLeftArmPose()));
        pose.put("rightArm", serializeEulerAngle(armor.getRightArmPose()));
        pose.put("leftLeg", serializeEulerAngle(armor.getLeftLegPose()));
        pose.put("rightLeg", serializeEulerAngle(armor.getRightLegPose()));
        pose.put("head", serializeEulerAngle(armor.getHeadPose()));
        pose.put("hand", serializeEulerAngle(armor.getBodyPose()));
        
        result.put("items", items);
        result.put("pose", pose);
        result.put("arms", armor.hasArms());
        result.put("base", armor.hasBasePlate());
        result.put("gravity", armor.hasGravity());
        //result.put("marker", armor.isMarker());
        result.put("small", armor.isSmall());
        result.put("visible", armor.isVisible());
        return result;
    }
    
    public static Entity getExistingEntity(UUID uuid, World world, Class type) {
        Collection<Entity> entities = world.getEntitiesByClass(type);
        Entity result = null;
        for(Entity search: entities) {
            if(search.getUniqueId().equals(uuid)) {
                result = search;
                break;
            }
        }
        return result;
    }
    
    public static ArmorStand deserializeArmorStand(ConfigurationSection data) {
        Location loc = deserializeLocation(data.getConfigurationSection("location"));
        if(loc == null) {
            return null;
        }
        else {
            UUID uuid = UUID.fromString(data.getString("uuid"));
            ArmorStand armor = (ArmorStand) getExistingEntity(uuid, loc.getWorld(), ArmorStand.class);
            if(armor==null) {
                armor = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            }
            if(armor != null) {
                ConfigurationSection pose = data.getConfigurationSection("pose");
                armor.setBodyPose(deserializeEulerAngle(pose.getConfigurationSection("body")));
                armor.setLeftLegPose(deserializeEulerAngle(pose.getConfigurationSection("leftLeg")));
                armor.setRightLegPose(deserializeEulerAngle(pose.getConfigurationSection("rightLeg")));
                armor.setLeftArmPose(deserializeEulerAngle(pose.getConfigurationSection("leftArm")));
                armor.setRightArmPose(deserializeEulerAngle(pose.getConfigurationSection("rightArm")));
                armor.setHeadPose(deserializeEulerAngle(pose.getConfigurationSection("head")));

                ConfigurationSection items = data.getConfigurationSection("items");
                armor.setBoots(ItemStack.deserialize(getMap(items,"boots")));
                armor.setLeggings(ItemStack.deserialize(getMap(items,"leggins")));
                armor.setChestplate(ItemStack.deserialize(getMap(items,"chestplate")));
                armor.setHelmet(ItemStack.deserialize(getMap(items,"helmet")));
                armor.setItemInHand(ItemStack.deserialize(getMap(items,"hand")));
                armor.setBoots(ItemStack.deserialize(getMap(items,"boots")));
                
                armor.setArms((boolean)data.get("arms"));
                armor.setBasePlate((boolean)data.get("base"));
                armor.setVisible((boolean)data.get("visible"));
                //armor.setMarker((boolean)armor.get("marker"));
                armor.setSmall((boolean)data.get("small"));
                armor.setGravity((boolean)data.get("gravity"));
            }
            return armor;
        }
    }
    
    public static Map<String,Object> serializeItemFrame(ItemFrame frame) {
        Map<String,Object> result = serializeEntity(frame);
        result.put("type", EntityType.ITEM_FRAME.name());
        result.put("item", frame.getItem().serialize());
        result.put("rotation", frame.getRotation().name());
        result.put("face", frame.getFacing().name());
        return result;
    }
    
    public static ItemFrame deserializeItemFrame(ConfigurationSection data) {
        Location loc = deserializeLocation(data.getConfigurationSection("location"));
        if(loc == null) {
            return null;
        }
        else {
            loc = new Location(loc.getWorld(),loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            UUID uuid = UUID.fromString(data.getString("uuid"));
            ItemFrame frame = (ItemFrame) getExistingEntity(uuid, loc.getWorld(), ItemFrame.class);
            if(frame==null) {
                final BlockFace face = BlockFace.valueOf(data.getString("face"));
                final ItemStack item = ItemStack.deserialize(getMap(data,"item"));
                final Rotation rotation = Rotation.valueOf(data.getString("rotation"));
                frame = spawnItemFrame(loc,face,rotation, item);
                if(frame == null) {
                    CreateItemFrameLaterTask later = new CreateItemFrameLaterTask(loc, face, rotation, item);
                    later.setTask(later);
                    later.runTaskTimer(plugin, 1,1);
                }
            }
            else {
                frame.setItem(ItemStack.deserialize(getMap(data,"item")));
                frame.setRotation(Rotation.valueOf(data.getString("rotation")));
            }
            return frame;
        }
    }
    
    private static class CreateItemFrameLaterTask extends BukkitRunnable {
        
        BukkitRunnable task;

        public void setTask(BukkitRunnable task) {
            this.task = task;
        }

        int tries = 0;

        Location fLoc;
        BlockFace fFace;
        Rotation rotation;
        ItemStack item;
        
        public CreateItemFrameLaterTask (Location loc, BlockFace face, Rotation pRotation, ItemStack pItem) {
            fLoc = loc;
            fFace = face;
            rotation = pRotation;
            item = pItem;
        }
        
        @Override
        public void run() {
            ItemFrame itemFrame = spawnItemFrame(fLoc,fFace, rotation, item);
            tries ++;
            if(itemFrame !=null) {
                task.cancel();
            }
            else if(tries > 20) {
                Logger.getGlobal().info("Cannot create ItemFrame");
                task.cancel();
            }
        }
    }
    
    private static ItemFrame spawnItemFrame(Location loc, BlockFace face, Rotation rotation, ItemStack item) {
        try {
            ItemFrame frame = (ItemFrame) loc.getWorld().spawnEntity(loc, EntityType.ITEM_FRAME);
            frame.setFacingDirection(face);
            frame.setItem(item);
            frame.setRotation(rotation);
            return frame;
        }
        catch(NullPointerException | IllegalArgumentException e) {
            return null;
        }
    }

    public static Map<String,Object> serializePainting(Painting painting) {
        Map<String,Object> result = serializeEntity(painting);
        result.put("type", EntityType.PAINTING.name());
        result.put("art", painting.getArt().name());
        result.put("face", painting.getFacing().name());
        return result;
    }
    
    public static Painting deserializePainting(ConfigurationSection data) {
        Location loc = deserializeLocation(data.getConfigurationSection("location"));
        if(loc == null) {
            return null;
        }
        else {
            Art art = Art.valueOf((String) data.get("art"));
            BlockFace face = BlockFace.valueOf((String) data.get("face"));
            loc = new Location(loc.getWorld(),paintingCornerX(loc,art,face), paintingCornerY(loc,art,face), paintingCornerZ(loc,art,face));
            UUID uuid = UUID.fromString(data.getString("uuid"));
            Painting painting = (Painting) getExistingEntity(uuid, loc.getWorld(), Painting.class);
            if(painting==null) {
                painting = spawnPainting(loc, art, face);
                if(painting == null) {
                    CreatePaintingLaterTask later = new CreatePaintingLaterTask(loc, art, face);
                    later.setTask(later);
                    later.runTaskTimer(plugin, 1, 1);
                }
            }
            return painting;
        }
    }
    
    private static class CreatePaintingLaterTask extends BukkitRunnable {
        
        BukkitRunnable task;

        public void setTask(BukkitRunnable task) {
            this.task = task;
        }

        int tries = 0;

        Location fLoc;
        Art fArt;
        BlockFace fFace;
        
        public CreatePaintingLaterTask (Location loc, Art art, BlockFace face) {
            fLoc = loc;
            fArt = art;
            fFace = face;
        }
        
        @Override
        public void run() {
            Painting painting = spawnPainting(fLoc,fArt,fFace);
            tries ++;
            if(painting !=null) {
                task.cancel();
            }
            else if(tries > 20) {
                Logger.getGlobal().info("Cannot create Painting");
                task.cancel();
            }
        }
    }
    
    private static Painting spawnPainting(Location loc, Art art, BlockFace face) {
        Painting painting;
        try {
            painting = (Painting) loc.getWorld().spawnEntity(loc, EntityType.PAINTING);
            painting.setFacingDirection(face);
            painting.setArt(art, true);
        }
        catch(NullPointerException | IllegalArgumentException e) {
            return null;
        }
        return painting;
    }
    
    private static int paintingCornerX(Location loc, Art art, BlockFace face) {
        int x = loc.getBlockX();
        switch(face) {
            case SOUTH:
                x = x - (art.getBlockWidth()>1 ? 1 : 0);
        }
        return x;
    }
    
    private static int paintingCornerY(Location loc, Art art, BlockFace face) {
        int y = loc.getBlockY();
        y = y - (art.getBlockHeight()==2 ? 1 : 0) ;
        y = y - (art.getBlockHeight()==4 ? 1 : 0) ;
        return y;
    }
    
    private static int paintingCornerZ(Location loc, Art art, BlockFace face) {
        int z = loc.getBlockZ();
        switch(face) {
            case WEST:
                z = z - (art.getBlockWidth()>1 ? 1 : 0);
        }
        return z;
    }
    
    private static Map<String,Object> serializeLocation(Location loc) {
        Map<String,Object> result = new HashMap<>();
        result.put("x", loc.getX());
        result.put("y", loc.getY());
        result.put("z", loc.getZ());
        result.put("yaw", loc.getYaw());
        result.put("pitch", loc.getPitch());
        result.put("world", loc.getWorld().getName());
        return result;
    }
    
    private static Location deserializeLocation(ConfigurationSection data) {
        World world = Bukkit.getWorld(data.getString("world"));
        if(world == null) {
            return null;
        }
        else {
            return new Location(world, (Double) data.get("x"), 
                                       (Double) data.get("y"), 
                                       (Double) data.get("z"), 
                                       ((Double) data.get("yaw")).floatValue(), 
                                       ((Double) data.get("pitch")).floatValue());
        }
    }
    
    private static Map<String,Object> serializeEulerAngle(EulerAngle angle) {
        Map<String,Object> result = new HashMap<>();
        result.put("x", angle.getX());
        result.put("y", angle.getY());
        result.put("z", angle.getZ());
        return result;
    }
    
    private static EulerAngle deserializeEulerAngle(ConfigurationSection data) {
        return new EulerAngle((Double) data.get("x"),
                              (Double) data.get("y"),
                              (Double) data.get("z"));
    }
    
    private static Map<String,Object> getMap(ConfigurationSection data, String key) {
        return (data.getConfigurationSection(key)).getValues(true);
    }
}
