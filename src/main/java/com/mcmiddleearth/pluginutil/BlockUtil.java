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
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
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
        List<String> blockdata = new ArrayList<>();
        for(Block block: blocks) {
            blockdata.add(getBlockInformation(block));
        }
        config.set("simple", blockdata);
    }
    
    private static boolean restoreSimple(ConfigurationSection config, List<BlockState> blocks, boolean updateBlocks) {
        List<String> blockList = config.getStringList("simple");
        if(blockList.isEmpty()) {
            return true;
        }
        String worldName = config.getString("world");
        if(worldName==null) {
            return false;
        }
        World world = Bukkit.getWorld(worldName);
        if(world!=null) {
            for(String rawData: blockList) {
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
            blockData.put("basic", getBlockInformation(block));
            if(block.getState() instanceof Sign) {
                blockData.put("lines", ((Sign)block.getState()).getLines());
            } else if(block.getState() instanceof Banner) {
                blockData.put("color", ((Banner)block.getState()).getBaseColor().name());
                List<Map<String,Object>> patternList = new ArrayList<>();
                for(Pattern pattern : ((Banner)block.getState()).getPatterns()) {
                    patternList.add(pattern.serialize());
                }
                blockData.put("pattern", patternList);
            } else if(block.getState() instanceof Skull) {
                //blockData.put("type", ((Skull)block.getState()).getSkullType().name());
                //blockData.put("rotation", ((Skull)block.getState()).getRotation().name());
                blockData.put("owner", ((Skull)block.getState()).getOwningPlayer().getUniqueId().toString());
                if((block.getType().equals(Material.PLAYER_HEAD) || block.getType().equals(Material.PLAYER_WALL_HEAD))) {
                    //blockData.put("headItem", serializeHead((Skull)block.getState()));
                    blockData.put("headItemMeta", serializeHeadMeta((Skull)block.getState()));
                }
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
        String worldName = config.getString("world");
        if(worldName==null) {
            return false;
        }
        World world = Bukkit.getWorld(worldName);
        if(world!=null) {
            for(Object rawData: blockDataList) {
                Map<String,Object> data = (Map<String,Object>) rawData;
                BlockState state = getBaseBlockState(world, (String)data.get("basic"));
                if(updateBlocks) {
                    state.update(true, false);
                    state = state.getBlock().getState();
                    if(state instanceof Sign) {
                        for(int i= 0; i<4;i++) {
                            String[] lines = ((List<String>) data.get("lines")).toArray(new String[0]);
                            ((Sign) state).setLine(i, lines[i]);
                        }
                        state.update(true, false);
                    } else if(state instanceof Banner) {
                        DyeColor color = DyeColor.valueOf((String) data.get("color"));
                        ((Banner) state).setBaseColor(color);
                        List<Map<String,Object>> patternList = (List<Map<String,Object>>) data.get("pattern");
                        for(Map<String,Object> patternData : patternList) {
                            Pattern pattern = new Pattern(patternData);
                            ((Banner) state).addPattern(pattern);
                        }
                        state.update(true, false);
                    } else if(state instanceof Skull) {
                        //((Skull) state).setSkullType(SkullType.valueOf((String) data.get("type")));
                        if(state.getType().equals(Material.PLAYER_HEAD) 
                          || state.getType().equals(Material.PLAYER_WALL_HEAD)) {
                            ItemStack item = new ItemStack(state.getType());//ItemStack)data.get("headItem");
//Logger.getGlobal().info("meta : "+item.getItemMeta());
                            ItemMeta meta = (ItemMeta)data.get("headItemMeta");
//Logger.getGlobal().info("meta extra: "+meta);
                            item.setItemMeta(meta);
                            deserializeHead(state.getBlock(), item);
                        }
                        state = state.getBlock().getState();
                        //((Skull) state).setRotation(BlockFace.valueOf((String) data.get("rotation")));
                        ((Skull) state).setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString((String)data.get("owner"))));
                        //break;
                    } else {
                        state.update(true, false);
                    }
                }
                blocks.add(state);
            }
            return true;
        }
        return false;
    }
    
    private static BlockState getBaseBlockState(World world, String data) {
        String[] blockInfo = data.split("::");
        BlockState state = world.getBlockAt(Integer.parseInt(blockInfo[0]),
                                            Integer.parseInt(blockInfo[1]),
                                            Integer.parseInt(blockInfo[2])).getState();
        BlockData blockData = Bukkit.createBlockData(blockInfo[3]);
        state.setBlockData(blockData);
        //state.setType(LegacyMaterialUtil.getMaterial(data.get(3)));
        //state.setRawData(data.get(4).byteValue());
        return state;
    }
    
    private static String getBlockInformation(Block block) {
        int[] data = new int[5];
        data[0] = block.getLocation().getBlockX();
        data[1] = block.getLocation().getBlockY();
        data[2] = block.getLocation().getBlockZ();
        /*data[3] = block.getType().getId();
        data[4] = block.getData();*/
        return data[0]+"::"+data[1]+"::"+data[2]+"::"+block.getBlockData().getAsString();
    }

    private static boolean isSimple(Block block) {
        BlockState state = block.getState();
        return !(  state instanceof Sign
                || state instanceof Banner
                || state instanceof Skull);
                
        /*        case LEGACY_WALL_SIGN:
                case LEGACY_SIGN_POST:
                case LEGACY_STANDING_BANNER:
                case LEGACY_WALL_BANNER:
                case LEGACY_SKULL:
                    return false;
                default:
                    return true;
        }*/
    }
    
    public static boolean isTransparent(Location loc) {
        Material mat = loc.getBlock().getType();
        return mat.isOccluding();
        /*switch(mat) {
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
        }*/
    }
 
    private static void deserializeHead(Block block, ItemStack head) {
        try {
            
//Logger.getGlobal().info("meta new: "+head.getItemMeta());
            BlockState blockState = block.getState();
            //blockState.setType(head.getType());
            //blockState.update(true, false);
            //blockState = block.getState();
            Skull skullData = (Skull) blockState;
            //skullData.setSkullType(SkullType.PLAYER);
            Field profileField = head.getItemMeta().getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            GameProfile profile = (GameProfile) profileField.get(head.getItemMeta());
            profileField = skullData.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullData, profile);
            //skullData.setRawData((byte)1);
            //skullData.setRotation(BlockFace.NORTH_NORTH_EAST);
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

            //ItemStack head = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
            ItemStack head = new ItemStack(skullBlockState.getType());
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

    private static ItemMeta serializeHeadMeta(Skull skullBlockState) {
        try {
            Field profileField = skullBlockState.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            GameProfile profile = (GameProfile) profileField.get(skullBlockState);

            //ItemStack head = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
            ItemStack head = new ItemStack(skullBlockState.getType());
            ItemMeta headMeta = head.getItemMeta();
            
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
            //head.setItemMeta(headMeta);
            return headMeta;
        } catch (NoSuchFieldException | SecurityException e) {
            Bukkit.getLogger().log(Level.SEVERE, "No such method exception during reflection.", e);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to use reflection.", e);
        }
        return null;
    }

    public static BlockFace rotateBlockFace(BlockFace face, int repetitions) {
        if(repetitions <= 0) {
            return face;
        }
        BlockFace result;
        /*if(face.equals(BlockFace.UP) && repetitions == 2) {
            return BlockFace.DOWN;
        }
        if(face.equals(BlockFace.DOWN) && repetitions == 2) {
            return BlockFace.UP;
        }*/
        switch(face) {
            case NORTH:
                result = BlockFace.EAST;
                break;
            case EAST:
                result = BlockFace.SOUTH;
                break;
            case SOUTH:
                result = BlockFace.WEST;
                break;
            case WEST:
                result = BlockFace.NORTH;
                break;
            case NORTH_EAST:
                result = BlockFace.SOUTH_EAST;
                break;
            case SOUTH_EAST:
                result = BlockFace.SOUTH_WEST;
                break;
            case SOUTH_WEST:
                result = BlockFace.NORTH_WEST;
                break;
            case NORTH_WEST:
                result = BlockFace.NORTH_EAST;
                break;
            case NORTH_NORTH_EAST:
                result = BlockFace.EAST_SOUTH_EAST;
                break;
            case EAST_SOUTH_EAST:
                result = BlockFace.SOUTH_SOUTH_WEST;
                break;
            case SOUTH_SOUTH_WEST:
                result = BlockFace.WEST_NORTH_WEST;
                break;
            case WEST_NORTH_WEST:
                result = BlockFace.NORTH_NORTH_EAST;
                break;
            case EAST_NORTH_EAST:
                result = BlockFace.SOUTH_SOUTH_EAST;
                break;
            case SOUTH_SOUTH_EAST:
                result = BlockFace.WEST_SOUTH_WEST;
                break;
            case WEST_SOUTH_WEST:
                result = BlockFace.NORTH_NORTH_WEST;
                break;
            case NORTH_NORTH_WEST:
                result = BlockFace.EAST_NORTH_EAST;
                break;
            case UP:
                result = BlockFace.UP;
                break;
            case DOWN:
                result = BlockFace.DOWN;
                break;
            default:
                result = BlockFace.SELF;
                break;
        }
        return rotateBlockFace(result, repetitions-1);
    }
    
    public static BlockFace flipBlockFace(BlockFace face, boolean[] axis) {
        if(!axis[0] && !axis[1] && !axis[2]) {
            return face;
        }
        BlockFace result;
        /*if(face.equals(BlockFace.UP) && repetitions == 2) {
            return BlockFace.DOWN;
        }
        if(face.equals(BlockFace.DOWN) && repetitions == 2) {
            return BlockFace.UP;
        }*/
        switch(face) {
            case NORTH:
                result = axis[2]?BlockFace.SOUTH:face;
                break;
            case EAST:
                result = axis[0]?BlockFace.WEST:face;
                break;
            case SOUTH:
                result = axis[2]?BlockFace.SOUTH:face;
                break;
            case WEST:
                result = axis[0]?BlockFace.EAST:face;
                break;
            case NORTH_EAST:
                result = axis[0]?(axis[2]?BlockFace.SOUTH_WEST:BlockFace.NORTH_WEST):(axis[2]?BlockFace.SOUTH_EAST:face);
                break;
            case SOUTH_EAST:
                result = axis[0]?(axis[2]?BlockFace.NORTH_WEST:BlockFace.SOUTH_WEST):(axis[2]?BlockFace.NORTH_EAST:face);
                break;
            case SOUTH_WEST:
                result = axis[0]?(axis[2]?BlockFace.NORTH_EAST:BlockFace.SOUTH_EAST):(axis[2]?BlockFace.NORTH_WEST:face);
                break;
            case NORTH_WEST:
                result = axis[0]?(axis[2]?BlockFace.SOUTH_EAST:BlockFace.NORTH_EAST):(axis[2]?BlockFace.SOUTH_WEST:face);
                break;
            case NORTH_NORTH_EAST:
                result = axis[0]?(axis[2]?BlockFace.SOUTH_SOUTH_WEST:BlockFace.NORTH_NORTH_WEST):(axis[2]?BlockFace.SOUTH_SOUTH_EAST:face);
                break;
            case EAST_SOUTH_EAST:
                result = axis[0]?(axis[2]?BlockFace.WEST_NORTH_WEST:BlockFace.WEST_SOUTH_WEST):(axis[2]?BlockFace.EAST_NORTH_EAST:face);
                break;
            case SOUTH_SOUTH_WEST:
                result = axis[0]?(axis[2]?BlockFace.NORTH_NORTH_EAST:BlockFace.SOUTH_SOUTH_EAST):(axis[2]?BlockFace.NORTH_NORTH_WEST:face);
                break;
            case WEST_NORTH_WEST:
                result = axis[0]?(axis[2]?BlockFace.EAST_SOUTH_EAST:BlockFace.EAST_NORTH_EAST):(axis[2]?BlockFace.WEST_SOUTH_WEST:face);
                break;
            case EAST_NORTH_EAST:
                result = axis[0]?(axis[2]?BlockFace.WEST_SOUTH_WEST:BlockFace.WEST_NORTH_WEST):(axis[2]?BlockFace.EAST_SOUTH_EAST:face);
                break;
            case SOUTH_SOUTH_EAST:
                result = axis[0]?(axis[2]?BlockFace.NORTH_NORTH_WEST:BlockFace.SOUTH_SOUTH_WEST):(axis[2]?BlockFace.NORTH_NORTH_EAST:face);
                break;
            case WEST_SOUTH_WEST:
                result = axis[0]?(axis[2]?BlockFace.EAST_NORTH_EAST:BlockFace.EAST_SOUTH_EAST):(axis[2]?BlockFace.WEST_NORTH_WEST:face);
                break;
            case NORTH_NORTH_WEST:
                result = axis[0]?(axis[2]?BlockFace.EAST_SOUTH_EAST:BlockFace.EAST_NORTH_EAST):(axis[2]?BlockFace.WEST_SOUTH_WEST:face);
                break;
            case UP:
                result = axis[1]?BlockFace.DOWN:face;
                break;
            case DOWN:
                result = axis[1]?BlockFace.UP:face;
                break;
            default:
                result = BlockFace.SELF;
                break;
        }
        return result;
    }
    


}
