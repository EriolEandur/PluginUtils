/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil;

import com.mojang.authlib.GameProfile;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Eriol_Eandur
 */
public class BlockUtil {
    
    public static boolean isSameBlock(Location loc1, Location loc2) {
        return loc1.getBlockX()==loc2.getBlockX() 
            && loc1.getBlockY()==loc2.getBlockY() 
            && loc1.getBlockZ()==loc2.getBlockZ(); 
    }
        
    public static void store(File file, Collection<Object> objects) throws IOException {
        FileConfiguration config = new YamlConfiguration();
        store(config, objects);
        config.save(file);
    }
    
    public static void store(ConfigurationSection config, Collection<Object> objects) {
        List<Entity> entities = new ArrayList<>();
        List<Block> blocks = new ArrayList<>();
        for(Object object: objects) {
            if(object instanceof Entity) {
                entities.add((Entity)object);
            }
            else if(object instanceof Block) {
                blocks.add((Block) object);
            }
        }
        EntityUtil.store(config.createSection("entities"), entities);
        storeBlocks(config.createSection("blocks"), blocks);
    }
 
    public static void storeBlocks(ConfigurationSection config, Collection<Block> blocks) {
        List<Block> simple = new ArrayList<>();
        List<Block> complex = new ArrayList<>();
        for(Block block: blocks) {
            if(isSimple(block)) {
                simple.add(block);
            }
            else {
                complex.add(block);
            }
        }
        if(!simple.isEmpty()) {
            config.set("world", simple.get(0).getWorld().getName());
            storeSimple(config, simple);
        }
        if(!complex.isEmpty()) {
            config.set("world", complex.get(0).getWorld().getName());
            storeComplex(config, complex);
        }
    }
    
    public static boolean restore(File file, List<Entity> entities,
                                  List<BlockState> blocks, boolean updateBlocks) throws IOException, InvalidConfigurationException{
        FileConfiguration config = new YamlConfiguration();
        config.load(file);
        return restore(config, entities, blocks, updateBlocks);
    }
    
    public static boolean restore(ConfigurationSection config, List<Entity> entities,
                                  List<BlockState> blocks, boolean updateBlocks) {
        return EntityUtil.restore(config.getConfigurationSection("entities"),entities)
            & restore(config.getConfigurationSection("blocks"), blocks, updateBlocks);
    }
    
    public static boolean restore(ConfigurationSection config, List<BlockState> blocks, boolean updateBlocks){
        return restoreSimple(config, blocks, updateBlocks)
            & restoreComplex(config, blocks, updateBlocks);
    }
    
    private static void storeSimple(ConfigurationSection config, List<Block> blocks) {
        List<int[]> blockdata = new ArrayList<>();
        for(Block block: blocks) {
            blockdata.add(getBlockData(block));
        }
        config.set("simple", blockdata);
    }
    
    private static boolean restoreSimple(ConfigurationSection config, List<BlockState> blocks, boolean updateBlocks) {
        List<?> blockDataList = config.getList("simple");
        if(blockDataList==null) {
            return true;
        }
        World world = Bukkit.getWorld(config.getString("world"));
        if(world!=null) {
            for(Object rawData: blockDataList) {
                BlockState state = getBaseBlockState(world, rawData);
                blocks.add(state);
                if(updateBlocks) {
                    state.update(true, false);
                }
            }
            return true;
        }
        return false;
    }
    
    private static void storeComplex(ConfigurationSection config, List<Block> blocks) {
        List<Map<String,Object>> blockDataList = new ArrayList<>();
        for(Block block: blocks) {
            Map<String,Object> blockData= new HashMap<>();
            blockData.put("basic", getBlockData(block));
            switch(block.getType()) {
                case LEGACY_WALL_SIGN:
                case LEGACY_SIGN_POST:
                    blockData.put("lines", ((Sign)block.getState()).getLines());
                    break;
                case LEGACY_STANDING_BANNER:
                case LEGACY_WALL_BANNER:
                    blockData.put("color", ((Banner)block.getState()).getBaseColor().name());
                    List<Map<String,Object>> patternList = new ArrayList<>();
                    for(Pattern pattern : ((Banner)block.getState()).getPatterns()) {
                        patternList.add(pattern.serialize());
                    }
                    blockData.put("pattern", patternList);
                    break;
                case LEGACY_SKULL:
                    blockData.put("type", ((Skull)block.getState()).getSkullType().name());
                    blockData.put("rotation", ((Skull)block.getState()).getRotation().name());
                    blockData.put("owner", ((Skull)block.getState()).getOwner());
                    if(((Skull)block.getState()).getSkullType().equals(SkullType.PLAYER)) {
                        blockData.put("headItem", serializeHead((Skull)block.getState()));
                    }
                    break;
            }
            blockDataList.add(blockData);
        }
        config.set("complex", blockDataList);
    }

    private static boolean restoreComplex(ConfigurationSection config, List<BlockState> blocks, boolean updateBlocks) {
        List<?> blockDataList = config.getList("complex");
        if(blockDataList==null) {
            return true;
        }
        World world = Bukkit.getWorld(config.getString("world"));
        if(world!=null) {
            for(Object rawData: blockDataList) {
                Map<String,Object> data = (Map<String,Object>) rawData;
                BlockState state = getBaseBlockState(world, data.get("basic"));
                if(updateBlocks) {
                    state.update(true, false);
                    state = state.getBlock().getState();
                    switch(state.getType()) {
                    case LEGACY_WALL_SIGN:
                    case LEGACY_SIGN_POST:
                        for(int i= 0; i<4;i++) {
                            String[] lines = ((List<String>) data.get("lines")).toArray(new String[0]);
                            ((Sign) state).setLine(i, lines[i]);
                        }
                        break;
                    case LEGACY_STANDING_BANNER:
                    case LEGACY_WALL_BANNER:
                        DyeColor color = DyeColor.valueOf((String) data.get("color"));
                        ((Banner) state).setBaseColor(color);
                        List<Map<String,Object>> patternList = (List<Map<String,Object>>) data.get("pattern");
                        for(Map<String,Object> patternData : patternList) {
                            Pattern pattern = new Pattern(patternData);
                            ((Banner) state).addPattern(new Pattern(patternData));
                        }
                        break;
                    case LEGACY_SKULL:
                        ((Skull) state).setSkullType(SkullType.valueOf((String) data.get("type")));
                        if(((Skull)state).getSkullType().equals(SkullType.PLAYER)) {
                            deserializeHead(state.getBlock(), (ItemStack)data.get("headItem"));
                        }
                        state = state.getBlock().getState();
                        ((Skull) state).setRotation(BlockFace.valueOf((String) data.get("rotation")));
                        ((Skull) state).setOwner((String) data.get("owner"));
                        break;
                    }
                    state.update(true, false);
                }
                blocks.add(state);
            }
            return true;
        }
        return false;
    }
    
    private static BlockState getBaseBlockState(World world, Object rawData) {
                List<Integer> data = (List<Integer>) rawData;
                BlockState state = world.getBlockAt(data.get(0), data.get(1), data.get(2)).getState();
                state.setType(LegacyMaterialUtil.getMaterial(data.get(3)));
                state.setRawData(data.get(4).byteValue());
                return state;
    }
    
    private static int[] getBlockData(Block block) {
        int[] data = new int[5];
        data[0] = block.getLocation().getBlockX();
        data[1] = block.getLocation().getBlockY();
        data[2] = block.getLocation().getBlockZ();
        data[3] = block.getType().getId();
        data[4] = block.getData();
        return data;
    }

    private static boolean isSimple(Block block) {
        switch(block.getType()) {
                case LEGACY_WALL_SIGN:
                case LEGACY_SIGN_POST:
                case LEGACY_STANDING_BANNER:
                case LEGACY_WALL_BANNER:
                case LEGACY_SKULL:
                    return false;
                default:
                    return true;
        }
    }
    
    public static boolean isTransparent(Location loc) {
        Material mat = loc.getBlock().getType();
        switch(mat) {
            case LEGACY_AIR:
            case LEGACY_GLASS:
            case LEGACY_LONG_GRASS:
            case LEGACY_STRING:
            case LEGACY_FLOWER_POT:
            case LEGACY_WALL_SIGN:
            case LEGACY_SIGN_POST:
            case LEGACY_LEAVES:
            case LEGACY_STAINED_GLASS:
            case LEGACY_WEB:
            case LEGACY_DEAD_BUSH:
            case LEGACY_TORCH:
            case LEGACY_REDSTONE_TORCH_ON:
            case LEGACY_REDSTONE_TORCH_OFF:
            case LEGACY_LEVER:
            case LEGACY_WOOD_BUTTON:
            case LEGACY_STONE_BUTTON:
            case LEGACY_TRAP_DOOR:
            case LEGACY_WOOD_DOOR:
            case LEGACY_IRON_DOOR:
            case LEGACY_WOOD_PLATE:
            case LEGACY_STONE_PLATE:
            case LEGACY_IRON_PLATE:
            case LEGACY_GOLD_PLATE:
            case LEGACY_THIN_GLASS:
            case LEGACY_VINE:
            case LEGACY_IRON_FENCE:
            case LEGACY_FENCE:
            case LEGACY_SAPLING:
            case LEGACY_ANVIL:
            case LEGACY_ENCHANTMENT_TABLE:
            case LEGACY_DOUBLE_PLANT:
            case LEGACY_SNOW:
            case LEGACY_CARPET:
            case LEGACY_STAINED_GLASS_PANE:
            case LEGACY_BED_BLOCK:
            case LEGACY_SKULL:
            case LEGACY_LADDER:
            case LEGACY_RAILS:
            case LEGACY_POWERED_RAIL:
            case LEGACY_ACTIVATOR_RAIL:
            case LEGACY_DETECTOR_RAIL:
            case LEGACY_STANDING_BANNER:
            case LEGACY_WALL_BANNER:
                return true;
            default: 
                return false;
        }
    }
 
    private static void deserializeHead(Block block, ItemStack head) {
        try {
            BlockState blockState = block.getState();
            blockState.setType(Material.LEGACY_SKULL);
            blockState.update(true, false);
            blockState = block.getState();
            Skull skullData = (Skull) blockState;
            skullData.setSkullType(SkullType.PLAYER);
            Field profileField = head.getItemMeta().getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            GameProfile profile = (GameProfile) profileField.get(head.getItemMeta());
            profileField = skullData.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullData, profile);
            skullData.setRawData((byte)1);
            skullData.setRotation(BlockFace.NORTH_NORTH_EAST);
            skullData.update(true, false);
        } catch (NoSuchFieldException | SecurityException e) {
            Bukkit.getLogger().log(Level.SEVERE, "No such method exception during reflection.", e);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to use reflection.", e);
        }
    }

    private static ItemStack serializeHead(Skull skullBlockState) {
        try {
            Field profileField = skullBlockState.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            GameProfile profile = (GameProfile) profileField.get(skullBlockState);

            ItemStack head = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
            ItemMeta headMeta = head.getItemMeta();
            
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
            head.setItemMeta(headMeta);
            return head;
        } catch (NoSuchFieldException | SecurityException e) {
            Bukkit.getLogger().log(Level.SEVERE, "No such method exception during reflection.", e);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to use reflection.", e);
        }
        return null;
    }


}
