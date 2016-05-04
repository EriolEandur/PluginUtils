/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                case WALL_SIGN:
                case SIGN_POST:
                    blockData.put("lines", ((Sign)block.getState()).getLines());
                    break;
                case STANDING_BANNER:
                case WALL_BANNER:
                    blockData.put("color", ((Banner)block.getState()).getBaseColor().name());
                    List<Map<String,Object>> patternList = new ArrayList<>();
                    for(Pattern pattern : ((Banner)block.getState()).getPatterns()) {
                        patternList.add(pattern.serialize());
                    }
                    blockData.put("pattern", patternList);
                    break;
                case SKULL:
                    blockData.put("type", ((Skull)block.getState()).getSkullType().name());
                    blockData.put("rotation", ((Skull)block.getState()).getRotation().name());
                    blockData.put("owner", ((Skull)block.getState()).getOwner());
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
                    case WALL_SIGN:
                    case SIGN_POST:
                        for(int i= 0; i<4;i++) {
                            String[] lines = ((List<String>) data.get("lines")).toArray(new String[0]);
                            ((Sign) state).setLine(i, lines[i]);
                        }
                        break;
                    case STANDING_BANNER:
                    case WALL_BANNER:
                        DyeColor color = DyeColor.valueOf((String) data.get("color"));
                        ((Banner) state).setBaseColor(color);
                        List<Map<String,Object>> patternList = (List<Map<String,Object>>) data.get("pattern");
                        for(Map<String,Object> patternData : patternList) {
                            Pattern pattern = new Pattern(patternData);
                            ((Banner) state).addPattern(new Pattern(patternData));
                        }
                        break;
                    case SKULL:
                        ((Skull) state).setSkullType(SkullType.valueOf((String) data.get("type")));
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
                state.setType(Material.getMaterial(data.get(3)));
                state.setRawData(data.get(4).byteValue());
                return state;
    }
    
    private static int[] getBlockData(Block block) {
        int[] data = new int[5];
        data[0] = block.getLocation().getBlockX();
        data[1] = block.getLocation().getBlockY();
        data[2] = block.getLocation().getBlockZ();
        data[3] = block.getTypeId();
        data[4] = block.getData();
        return data;
    }

    private static boolean isSimple(Block block) {
        switch(block.getType()) {
                case WALL_SIGN:
                case SIGN_POST:
                case STANDING_BANNER:
                case WALL_BANNER:
                case SKULL:
                    return false;
                default:
                    return true;
        }
    }
    
    public static boolean isTransparent(Location loc) {
        Material mat = loc.getBlock().getType();
        switch(mat) {
            case AIR:
            case GLASS:
            case LONG_GRASS:
            case STRING:
            case FLOWER_POT:
            case WALL_SIGN:
            case SIGN_POST:
            case LEAVES:
            case STAINED_GLASS:
            case WEB:
            case DEAD_BUSH:
            case TORCH:
            case REDSTONE_TORCH_ON:
            case REDSTONE_TORCH_OFF:
            case LEVER:
            case WOOD_BUTTON:
            case STONE_BUTTON:
            case TRAP_DOOR:
            case WOOD_DOOR:
            case IRON_DOOR:
            case WOOD_PLATE:
            case STONE_PLATE:
            case IRON_PLATE:
            case GOLD_PLATE:
            case THIN_GLASS:
            case VINE:
            case IRON_FENCE:
            case FENCE:
            case SAPLING:
            case ANVIL:
            case ENCHANTMENT_TABLE:
            case DOUBLE_PLANT:
            case SNOW:
            case CARPET:
            case STAINED_GLASS_PANE:
            case BED_BLOCK:
            case SKULL:
            case LADDER:
            case RAILS:
            case POWERED_RAIL:
            case ACTIVATOR_RAIL:
            case DETECTOR_RAIL:
            case STANDING_BANNER:
            case WALL_BANNER:
                return true;
            default: 
                return false;
        }
    }
  

}
