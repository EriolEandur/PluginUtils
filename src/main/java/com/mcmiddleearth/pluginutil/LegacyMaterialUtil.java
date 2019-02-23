/*
 * Copyright (C) 2018 MCME
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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;

/**
 *
 * @author Eriol_Eandur
 */
public class LegacyMaterialUtil {
    
    private final static Material[] legacyMaterials = new Material[]{
        Material.LEGACY_AIR, Material.LEGACY_STONE, Material.LEGACY_GRASS, Material.LEGACY_DIRT, Material.LEGACY_COBBLESTONE, Material.LEGACY_WOOD, Material.LEGACY_SAPLING, Material.LEGACY_BEDROCK, Material.LEGACY_WATER, Material.LEGACY_STATIONARY_WATER, Material.LEGACY_LAVA, Material.LEGACY_STATIONARY_LAVA, Material.LEGACY_SAND, Material.LEGACY_GRAVEL, Material.LEGACY_GOLD_ORE, Material.LEGACY_IRON_ORE, Material.LEGACY_COAL_ORE, Material.LEGACY_LOG, Material.LEGACY_LEAVES, Material.LEGACY_SPONGE, Material.LEGACY_GLASS, Material.LEGACY_LAPIS_ORE, Material.LEGACY_LAPIS_BLOCK, Material.LEGACY_DISPENSER, Material.LEGACY_SANDSTONE, Material.LEGACY_NOTE_BLOCK, Material.LEGACY_BED_BLOCK, Material.LEGACY_POWERED_RAIL, Material.LEGACY_DETECTOR_RAIL, Material.LEGACY_PISTON_STICKY_BASE, Material.LEGACY_WEB, Material.LEGACY_LONG_GRASS, Material.LEGACY_DEAD_BUSH, Material.LEGACY_PISTON_BASE, Material.LEGACY_PISTON_EXTENSION, Material.LEGACY_WOOL, Material.LEGACY_PISTON_MOVING_PIECE, Material.LEGACY_YELLOW_FLOWER, Material.LEGACY_RED_ROSE, Material.LEGACY_BROWN_MUSHROOM, Material.LEGACY_RED_MUSHROOM, Material.LEGACY_GOLD_BLOCK, Material.LEGACY_IRON_BLOCK, Material.LEGACY_DOUBLE_STEP, Material.LEGACY_STEP, Material.LEGACY_BRICK, Material.LEGACY_TNT, Material.LEGACY_BOOKSHELF, Material.LEGACY_MOSSY_COBBLESTONE, Material.LEGACY_OBSIDIAN, Material.LEGACY_TORCH, Material.LEGACY_FIRE, Material.LEGACY_MOB_SPAWNER, Material.LEGACY_WOOD_STAIRS, Material.LEGACY_CHEST, Material.LEGACY_REDSTONE_WIRE, Material.LEGACY_DIAMOND_ORE, Material.LEGACY_DIAMOND_BLOCK, Material.LEGACY_WORKBENCH, Material.LEGACY_CROPS, Material.LEGACY_SOIL, Material.LEGACY_FURNACE, Material.LEGACY_BURNING_FURNACE, Material.LEGACY_SIGN_POST, Material.LEGACY_WOODEN_DOOR, Material.LEGACY_LADDER, Material.LEGACY_RAILS, Material.LEGACY_COBBLESTONE_STAIRS, Material.LEGACY_WALL_SIGN, Material.LEGACY_LEVER, Material.LEGACY_STONE_PLATE, Material.LEGACY_IRON_DOOR_BLOCK, Material.LEGACY_WOOD_PLATE, Material.LEGACY_REDSTONE_ORE, Material.LEGACY_GLOWING_REDSTONE_ORE, Material.LEGACY_REDSTONE_TORCH_OFF, Material.LEGACY_REDSTONE_TORCH_ON, Material.LEGACY_STONE_BUTTON, Material.LEGACY_SNOW, Material.LEGACY_ICE, Material.LEGACY_SNOW_BLOCK, Material.LEGACY_CACTUS, Material.LEGACY_CLAY, Material.LEGACY_SUGAR_CANE_BLOCK, Material.LEGACY_JUKEBOX, Material.LEGACY_FENCE, Material.LEGACY_PUMPKIN, Material.LEGACY_NETHERRACK, Material.LEGACY_SOUL_SAND, Material.LEGACY_GLOWSTONE, Material.LEGACY_PORTAL, Material.LEGACY_JACK_O_LANTERN, Material.LEGACY_CAKE_BLOCK, Material.LEGACY_DIODE_BLOCK_OFF, Material.LEGACY_DIODE_BLOCK_ON, Material.LEGACY_STAINED_GLASS, Material.LEGACY_TRAP_DOOR, Material.LEGACY_MONSTER_EGGS, Material.LEGACY_SMOOTH_BRICK, Material.LEGACY_HUGE_MUSHROOM_1, Material.LEGACY_HUGE_MUSHROOM_2, Material.LEGACY_IRON_FENCE, Material.LEGACY_THIN_GLASS, Material.LEGACY_MELON_BLOCK, Material.LEGACY_PUMPKIN_STEM, Material.LEGACY_MELON_STEM, Material.LEGACY_VINE, Material.LEGACY_FENCE_GATE, Material.LEGACY_BRICK_STAIRS, Material.LEGACY_SMOOTH_STAIRS, Material.LEGACY_MYCEL, Material.LEGACY_WATER_LILY, Material.LEGACY_NETHER_BRICK, Material.LEGACY_NETHER_FENCE, Material.LEGACY_NETHER_BRICK_STAIRS, Material.LEGACY_NETHER_WARTS, Material.LEGACY_ENCHANTMENT_TABLE, Material.LEGACY_BREWING_STAND, Material.LEGACY_CAULDRON, Material.LEGACY_ENDER_PORTAL, Material.LEGACY_ENDER_PORTAL_FRAME, Material.LEGACY_ENDER_STONE, Material.LEGACY_DRAGON_EGG, Material.LEGACY_REDSTONE_LAMP_OFF, Material.LEGACY_REDSTONE_LAMP_ON, Material.LEGACY_WOOD_DOUBLE_STEP, Material.LEGACY_WOOD_STEP, Material.LEGACY_COCOA, Material.LEGACY_SANDSTONE_STAIRS, Material.LEGACY_EMERALD_ORE, Material.LEGACY_ENDER_CHEST, Material.LEGACY_TRIPWIRE_HOOK, Material.LEGACY_TRIPWIRE, Material.LEGACY_EMERALD_BLOCK, Material.LEGACY_SPRUCE_WOOD_STAIRS, Material.LEGACY_BIRCH_WOOD_STAIRS, Material.LEGACY_JUNGLE_WOOD_STAIRS, Material.LEGACY_COMMAND, Material.LEGACY_BEACON, Material.LEGACY_COBBLE_WALL, Material.LEGACY_FLOWER_POT, Material.LEGACY_CARROT, Material.LEGACY_POTATO, Material.LEGACY_WOOD_BUTTON, Material.LEGACY_SKULL, Material.LEGACY_ANVIL, Material.LEGACY_TRAPPED_CHEST, Material.LEGACY_GOLD_PLATE, Material.LEGACY_IRON_PLATE, Material.LEGACY_REDSTONE_COMPARATOR_OFF, Material.LEGACY_REDSTONE_COMPARATOR_ON, Material.LEGACY_DAYLIGHT_DETECTOR, Material.LEGACY_REDSTONE_BLOCK, Material.LEGACY_QUARTZ_ORE, Material.LEGACY_HOPPER, Material.LEGACY_QUARTZ_BLOCK, Material.LEGACY_QUARTZ_STAIRS, Material.LEGACY_ACTIVATOR_RAIL, Material.LEGACY_DROPPER, Material.LEGACY_STAINED_CLAY, Material.LEGACY_STAINED_GLASS_PANE, Material.LEGACY_LEAVES_2, Material.LEGACY_LOG_2, Material.LEGACY_ACACIA_STAIRS, Material.LEGACY_DARK_OAK_STAIRS, Material.LEGACY_SLIME_BLOCK, Material.LEGACY_BARRIER, Material.LEGACY_IRON_TRAPDOOR, Material.LEGACY_PRISMARINE, Material.LEGACY_SEA_LANTERN, Material.LEGACY_HAY_BLOCK, Material.LEGACY_CARPET, Material.LEGACY_HARD_CLAY, Material.LEGACY_COAL_BLOCK, Material.LEGACY_PACKED_ICE, Material.LEGACY_DOUBLE_PLANT, Material.LEGACY_STANDING_BANNER, Material.LEGACY_WALL_BANNER, Material.LEGACY_DAYLIGHT_DETECTOR_INVERTED, Material.LEGACY_RED_SANDSTONE, Material.LEGACY_RED_SANDSTONE_STAIRS, Material.LEGACY_DOUBLE_STONE_SLAB2, Material.LEGACY_STONE_SLAB2, Material.LEGACY_SPRUCE_FENCE_GATE, Material.LEGACY_BIRCH_FENCE_GATE, Material.LEGACY_JUNGLE_FENCE_GATE, Material.LEGACY_DARK_OAK_FENCE_GATE, Material.LEGACY_ACACIA_FENCE_GATE, Material.LEGACY_SPRUCE_FENCE, Material.LEGACY_BIRCH_FENCE, Material.LEGACY_JUNGLE_FENCE, Material.LEGACY_DARK_OAK_FENCE, Material.LEGACY_ACACIA_FENCE, Material.LEGACY_SPRUCE_DOOR, Material.LEGACY_BIRCH_DOOR, Material.LEGACY_JUNGLE_DOOR, Material.LEGACY_ACACIA_DOOR, Material.LEGACY_DARK_OAK_DOOR, Material.LEGACY_END_ROD, Material.LEGACY_CHORUS_PLANT, Material.LEGACY_CHORUS_FLOWER, Material.LEGACY_PURPUR_BLOCK, Material.LEGACY_PURPUR_PILLAR, Material.LEGACY_PURPUR_STAIRS, Material.LEGACY_PURPUR_DOUBLE_SLAB, Material.LEGACY_PURPUR_SLAB, Material.LEGACY_END_BRICKS, Material.LEGACY_BEETROOT_BLOCK, Material.LEGACY_GRASS_PATH, Material.LEGACY_END_GATEWAY, Material.LEGACY_COMMAND_REPEATING, Material.LEGACY_COMMAND_CHAIN, Material.LEGACY_FROSTED_ICE, Material.LEGACY_MAGMA, Material.LEGACY_NETHER_WART_BLOCK, Material.LEGACY_RED_NETHER_BRICK, Material.LEGACY_BONE_BLOCK, Material.LEGACY_STRUCTURE_VOID, Material.LEGACY_OBSERVER, Material.LEGACY_WHITE_SHULKER_BOX, Material.LEGACY_ORANGE_SHULKER_BOX, Material.LEGACY_MAGENTA_SHULKER_BOX, Material.LEGACY_LIGHT_BLUE_SHULKER_BOX, Material.LEGACY_YELLOW_SHULKER_BOX, Material.LEGACY_LIME_SHULKER_BOX, Material.LEGACY_PINK_SHULKER_BOX, Material.LEGACY_GRAY_SHULKER_BOX, Material.LEGACY_SILVER_SHULKER_BOX, Material.LEGACY_CYAN_SHULKER_BOX, Material.LEGACY_PURPLE_SHULKER_BOX, Material.LEGACY_BLUE_SHULKER_BOX, Material.LEGACY_BROWN_SHULKER_BOX, Material.LEGACY_GREEN_SHULKER_BOX, Material.LEGACY_RED_SHULKER_BOX, Material.LEGACY_BLACK_SHULKER_BOX, Material.LEGACY_WHITE_GLAZED_TERRACOTTA, Material.LEGACY_ORANGE_GLAZED_TERRACOTTA, Material.LEGACY_MAGENTA_GLAZED_TERRACOTTA, Material.LEGACY_LIGHT_BLUE_GLAZED_TERRACOTTA, Material.LEGACY_YELLOW_GLAZED_TERRACOTTA, Material.LEGACY_LIME_GLAZED_TERRACOTTA, Material.LEGACY_PINK_GLAZED_TERRACOTTA, Material.LEGACY_GRAY_GLAZED_TERRACOTTA, Material.LEGACY_SILVER_GLAZED_TERRACOTTA, Material.LEGACY_CYAN_GLAZED_TERRACOTTA, Material.LEGACY_PURPLE_GLAZED_TERRACOTTA, Material.LEGACY_BLUE_GLAZED_TERRACOTTA, Material.LEGACY_BROWN_GLAZED_TERRACOTTA, Material.LEGACY_GREEN_GLAZED_TERRACOTTA, Material.LEGACY_RED_GLAZED_TERRACOTTA, Material.LEGACY_BLACK_GLAZED_TERRACOTTA, Material.LEGACY_CONCRETE, Material.LEGACY_CONCRETE_POWDER, Material.LEGACY_STRUCTURE_BLOCK, Material.LEGACY_IRON_SPADE, Material.LEGACY_IRON_PICKAXE, Material.LEGACY_IRON_AXE, Material.LEGACY_FLINT_AND_STEEL, Material.LEGACY_APPLE, Material.LEGACY_BOW, Material.LEGACY_ARROW, Material.LEGACY_COAL, Material.LEGACY_DIAMOND, Material.LEGACY_IRON_INGOT, Material.LEGACY_GOLD_INGOT, Material.LEGACY_IRON_SWORD, Material.LEGACY_WOOD_SWORD, Material.LEGACY_WOOD_SPADE, Material.LEGACY_WOOD_PICKAXE, Material.LEGACY_WOOD_AXE, Material.LEGACY_STONE_SWORD, Material.LEGACY_STONE_SPADE, Material.LEGACY_STONE_PICKAXE, Material.LEGACY_STONE_AXE, Material.LEGACY_DIAMOND_SWORD, Material.LEGACY_DIAMOND_SPADE, Material.LEGACY_DIAMOND_PICKAXE, Material.LEGACY_DIAMOND_AXE, Material.LEGACY_STICK, Material.LEGACY_BOWL, Material.LEGACY_MUSHROOM_SOUP, Material.LEGACY_GOLD_SWORD, Material.LEGACY_GOLD_SPADE, Material.LEGACY_GOLD_PICKAXE, Material.LEGACY_GOLD_AXE, Material.LEGACY_STRING, Material.LEGACY_FEATHER, Material.LEGACY_SULPHUR, Material.LEGACY_WOOD_HOE, Material.LEGACY_STONE_HOE, Material.LEGACY_IRON_HOE, Material.LEGACY_DIAMOND_HOE, Material.LEGACY_GOLD_HOE, Material.LEGACY_SEEDS, Material.LEGACY_WHEAT, Material.LEGACY_BREAD, Material.LEGACY_LEATHER_HELMET, Material.LEGACY_LEATHER_CHESTPLATE, Material.LEGACY_LEATHER_LEGGINGS, Material.LEGACY_LEATHER_BOOTS, Material.LEGACY_CHAINMAIL_HELMET, Material.LEGACY_CHAINMAIL_CHESTPLATE, Material.LEGACY_CHAINMAIL_LEGGINGS, Material.LEGACY_CHAINMAIL_BOOTS, Material.LEGACY_IRON_HELMET, Material.LEGACY_IRON_CHESTPLATE, Material.LEGACY_IRON_LEGGINGS, Material.LEGACY_IRON_BOOTS, Material.LEGACY_DIAMOND_HELMET, Material.LEGACY_DIAMOND_CHESTPLATE, Material.LEGACY_DIAMOND_LEGGINGS, Material.LEGACY_DIAMOND_BOOTS, Material.LEGACY_GOLD_HELMET, Material.LEGACY_GOLD_CHESTPLATE, Material.LEGACY_GOLD_LEGGINGS, Material.LEGACY_GOLD_BOOTS, Material.LEGACY_FLINT, Material.LEGACY_PORK, Material.LEGACY_GRILLED_PORK, Material.LEGACY_PAINTING, Material.LEGACY_GOLDEN_APPLE, Material.LEGACY_SIGN, Material.LEGACY_WOOD_DOOR, Material.LEGACY_BUCKET, Material.LEGACY_WATER_BUCKET, Material.LEGACY_LAVA_BUCKET, Material.LEGACY_MINECART, Material.LEGACY_SADDLE, Material.LEGACY_IRON_DOOR, Material.LEGACY_REDSTONE, Material.LEGACY_SNOW_BALL, Material.LEGACY_BOAT, Material.LEGACY_LEATHER, Material.LEGACY_MILK_BUCKET, Material.LEGACY_CLAY_BRICK, Material.LEGACY_CLAY_BALL, Material.LEGACY_SUGAR_CANE, Material.LEGACY_PAPER, Material.LEGACY_BOOK, Material.LEGACY_SLIME_BALL, Material.LEGACY_STORAGE_MINECART, Material.LEGACY_POWERED_MINECART, Material.LEGACY_EGG, Material.LEGACY_COMPASS, Material.LEGACY_FISHING_ROD, Material.LEGACY_WATCH, Material.LEGACY_GLOWSTONE_DUST, Material.LEGACY_RAW_FISH, Material.LEGACY_COOKED_FISH, Material.LEGACY_INK_SACK, Material.LEGACY_BONE, Material.LEGACY_SUGAR, Material.LEGACY_CAKE, Material.LEGACY_BED, Material.LEGACY_DIODE, Material.LEGACY_COOKIE, Material.LEGACY_MAP, Material.LEGACY_SHEARS, Material.LEGACY_MELON, Material.LEGACY_PUMPKIN_SEEDS, Material.LEGACY_MELON_SEEDS, Material.LEGACY_RAW_BEEF, Material.LEGACY_COOKED_BEEF, Material.LEGACY_RAW_CHICKEN, Material.LEGACY_COOKED_CHICKEN, Material.LEGACY_ROTTEN_FLESH, Material.LEGACY_ENDER_PEARL, Material.LEGACY_BLAZE_ROD, Material.LEGACY_GHAST_TEAR, Material.LEGACY_GOLD_NUGGET, Material.LEGACY_NETHER_STALK, Material.LEGACY_POTION, Material.LEGACY_GLASS_BOTTLE, Material.LEGACY_SPIDER_EYE, Material.LEGACY_FERMENTED_SPIDER_EYE, Material.LEGACY_BLAZE_POWDER, Material.LEGACY_MAGMA_CREAM, Material.LEGACY_BREWING_STAND_ITEM, Material.LEGACY_CAULDRON_ITEM, Material.LEGACY_EYE_OF_ENDER, Material.LEGACY_SPECKLED_MELON, Material.LEGACY_MONSTER_EGG, Material.LEGACY_EXP_BOTTLE, Material.LEGACY_FIREBALL, Material.LEGACY_BOOK_AND_QUILL, Material.LEGACY_WRITTEN_BOOK, Material.LEGACY_EMERALD, Material.LEGACY_ITEM_FRAME, Material.LEGACY_FLOWER_POT_ITEM, Material.LEGACY_CARROT_ITEM, Material.LEGACY_POTATO_ITEM, Material.LEGACY_BAKED_POTATO, Material.LEGACY_POISONOUS_POTATO, Material.LEGACY_EMPTY_MAP, Material.LEGACY_GOLDEN_CARROT, Material.LEGACY_SKULL_ITEM, Material.LEGACY_CARROT_STICK, Material.LEGACY_NETHER_STAR, Material.LEGACY_PUMPKIN_PIE, Material.LEGACY_FIREWORK, Material.LEGACY_FIREWORK_CHARGE, Material.LEGACY_ENCHANTED_BOOK, Material.LEGACY_REDSTONE_COMPARATOR, Material.LEGACY_NETHER_BRICK_ITEM, Material.LEGACY_QUARTZ, Material.LEGACY_EXPLOSIVE_MINECART, Material.LEGACY_HOPPER_MINECART, Material.LEGACY_PRISMARINE_SHARD, Material.LEGACY_PRISMARINE_CRYSTALS, Material.LEGACY_RABBIT, Material.LEGACY_COOKED_RABBIT, Material.LEGACY_RABBIT_STEW, Material.LEGACY_RABBIT_FOOT, Material.LEGACY_RABBIT_HIDE, Material.LEGACY_ARMOR_STAND, Material.LEGACY_IRON_BARDING, Material.LEGACY_GOLD_BARDING, Material.LEGACY_DIAMOND_BARDING, Material.LEGACY_LEASH, Material.LEGACY_NAME_TAG, Material.LEGACY_COMMAND_MINECART, Material.LEGACY_MUTTON, Material.LEGACY_COOKED_MUTTON, Material.LEGACY_BANNER, Material.LEGACY_END_CRYSTAL, Material.LEGACY_SPRUCE_DOOR_ITEM, Material.LEGACY_BIRCH_DOOR_ITEM, Material.LEGACY_JUNGLE_DOOR_ITEM, Material.LEGACY_ACACIA_DOOR_ITEM, Material.LEGACY_DARK_OAK_DOOR_ITEM, Material.LEGACY_CHORUS_FRUIT, Material.LEGACY_CHORUS_FRUIT_POPPED, Material.LEGACY_BEETROOT, Material.LEGACY_BEETROOT_SEEDS, Material.LEGACY_BEETROOT_SOUP, Material.LEGACY_DRAGONS_BREATH, Material.LEGACY_SPLASH_POTION, Material.LEGACY_SPECTRAL_ARROW, Material.LEGACY_TIPPED_ARROW, Material.LEGACY_LINGERING_POTION, Material.LEGACY_SHIELD, Material.LEGACY_ELYTRA, Material.LEGACY_BOAT_SPRUCE, Material.LEGACY_BOAT_BIRCH, Material.LEGACY_BOAT_JUNGLE, Material.LEGACY_BOAT_ACACIA, Material.LEGACY_BOAT_DARK_OAK, Material.LEGACY_TOTEM, Material.LEGACY_SHULKER_SHELL, Material.LEGACY_IRON_NUGGET, Material.LEGACY_KNOWLEDGE_BOOK, Material.LEGACY_GOLD_RECORD, Material.LEGACY_GREEN_RECORD, Material.LEGACY_RECORD_3, Material.LEGACY_RECORD_4, Material.LEGACY_RECORD_5, Material.LEGACY_RECORD_6, Material.LEGACY_RECORD_7, Material.LEGACY_RECORD_8, Material.LEGACY_RECORD_9, Material.LEGACY_RECORD_10, Material.LEGACY_RECORD_11, Material.LEGACY_RECORD_12};

    public static Material getLegacyMaterial(Material mat) {
        if(mat==null) {
            return null;
        }
        if(mat.isLegacy()) {
            return mat;
        }
        for(Material search: legacyMaterials) {
            if(search.isBlock()) {
                for(byte i=0; i<16; i++) {
                    Material compare = getBlockData(search, i).getMaterial();
                    if(compare.equals(mat)) {
                        return search;
                    }
                }
            }
        }
        return null;
    }
    
    public static Material getMaterial(int id) {
        for(Material mat: Material.values()) {
            if(mat.getId()==id) {
                return mat;
            }
        }
        for(Material mat: legacyMaterials) {
            try {
                //Material material = Material.valueOf(Material.LEGACY_PREFIX+mat.name());
                if(mat.getId()==id) {
                    return mat;
                }
//Logger.getGlobal().info(""+material.name()+"["+material.getId()+"]");
            } catch (IllegalArgumentException e) {}
        }
        return null;
    }
    
    public static BlockData getBlockData(Material blockMat, byte rawData){
        if(blockMat==null) {
            return null;
        } 
        World world = Bukkit.getWorld("world");
        if(world==null) {
            return null;
        }
        BlockState state = world.getBlockAt(0,0,1).getState();
        state.setType(blockMat);
        state.setRawData(rawData);
        return state.getBlockData();
    }
    
    public static BlockData getBlockData(int id, byte rawData) {
        BlockData special = getSpecialBlockData(id, rawData);
        if(special!=null) {
            return special;
        } else {
            return getBlockData(getMaterial(id),rawData);
        }
    }
    
    public static BlockData getSpecialBlockData(int id, byte rawData) {
        BlockData data = getBlockData(getMaterial(id),rawData);
        switch(id) {
            case 125:
            case 43:
            case 181:
            case 204:
                ((Slab)data).setType(Slab.Type.DOUBLE);
                break;
            case 17:
                switch(rawData) {
                    case 12:
                        return Bukkit.createBlockData(Material.OAK_WOOD);
                    case 13:
                        return Bukkit.createBlockData(Material.SPRUCE_WOOD);
                    case 14:
                        return Bukkit.createBlockData(Material.BIRCH_WOOD);
                    case 15:
                        return Bukkit.createBlockData(Material.JUNGLE_WOOD);
                }
            case 162:
                switch(rawData) {
                    case 12:
                        return Bukkit.createBlockData(Material.ACACIA_WOOD);
                    case 13:
                        return Bukkit.createBlockData(Material.DARK_OAK_WOOD);
                }
        }
        return data;
    }
    
}
