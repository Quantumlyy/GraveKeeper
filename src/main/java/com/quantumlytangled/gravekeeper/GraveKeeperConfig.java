package com.quantumlytangled.gravekeeper;

import com.quantumlytangled.gravekeeper.compatibility.CompatAetherLegacy;
import com.quantumlytangled.gravekeeper.compatibility.CompatArmour;
import com.quantumlytangled.gravekeeper.compatibility.CompatBaubles;
import com.quantumlytangled.gravekeeper.compatibility.CompatCosmeticArmorReworked;
import com.quantumlytangled.gravekeeper.compatibility.CompatGalacticCraftCore;
import com.quantumlytangled.gravekeeper.core.InventoryHandler;
import com.quantumlytangled.gravekeeper.compatibility.CompatMain;
import com.quantumlytangled.gravekeeper.compatibility.CompatOffHand;
import com.quantumlytangled.gravekeeper.compatibility.CompatTechGuns;
import com.quantumlytangled.gravekeeper.compatibility.CompatTravelersBackpack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

public class GraveKeeperConfig {
  
  public static boolean IGNORE_KEEP_INVENTORY = false;
  public static boolean DEBUG_LOGS = false;
  public static String[] COMPATIBILITY_ORDER = new String[] {
      "minecraft:armour", "minecraft:main", "minecraft:offhand",
      "aether_legacy", "baubles", "cosmeticarmorreworked", "galacticraftcore", "techguns", "travelersbackpack"
  };
  
  public static int EXPIRE_TIME_SECONDS = 7200;
  public static boolean INSTANT_FOREIGN_COLLECTION = false;
  public static boolean OWNER_ONLY_COLLECTION = false;
  
  private static String[] CHARM_ARMOR_HELD_NAMES = new String[] {
      "twilightforest:charm_of_keeping_1"
  };
  private static String[] CHARM_ARMOR_HOTBAR_NAMES = new String[] {
      "twilightforest:charm_of_keeping_2"
  };
  private static String[] CHARM_FULL_NAMES = new String[] {
      "twilightforest:charm_of_keeping_3"
  };
  
  public static List<Item> CHARM_ARMOR_HELD_ITEMS = null;
  public static List<Item> CHARM_ARMOR_HOTBAR_ITEMS = null;
  public static List<Item> CHARM_FULL_ITEMS = null;
  public static List<Item> CHARM_ITEMS = null;

  public static boolean MOVE_SOULBOUND_ITEMS_TO_MAIN_INVENTORY = true;
  public static int KEEP_SOULBOUND_AMOUNT = 5;
  public static boolean ANY_ENCHANT_IS_SOULBOUND = false;

  private static String[] SOULBOUND_ENCHANTMENT_NAMES = new String[]{
      "enderio:soulbound",
      "cofhcore:soulbound",
      "aoa3:intervention"
  };
  public static String[] SOULBOUND_TAG_STRINGS = new String[]{
      "{ \"Botania_keepIvy\": 1b }",
      "{ \"spectreAnchor\": 0b }"
  };

  public static List<Enchantment> SOULBOUND_ENCHANTMENTS = null;
  public static List<NBTTagCompound> SOULBOUND_TAGS = null;

  public static int SEARCH_MIN_ALTITUDE = 0;
  public static int SEARCH_RADIUS_ABOVE_M = 10;
  public static int SEARCH_RADIUS_BELOW_M = -1;
  public static int SEARCH_RADIUS_HORIZONTAL_M = 5;
  public static int SPAWN_DIMENSION_ID = 0;
  public static int USE_BED_OR_SPAWN_LOCATION_BELOW_Y = 0;
  
  public static void onFMLpreInitialization(final File fileConfigDirectory) {
    
    loadConfig(new File(fileConfigDirectory, GraveKeeper.MODID + ".yml"));
    
    final ArrayList<String> compatibilityAdded = new ArrayList<>(10);
    for (final String nameCompatibility : COMPATIBILITY_ORDER) {
      if (compatibilityAdded.contains(nameCompatibility)) {
        GraveKeeper.logger.error(String.format("Skipping duplicated compatibility name %s",
            nameCompatibility ));
        continue;
      }
      
      switch (nameCompatibility) {
        case "minecraft:armour":
          InventoryHandler.addCompatibilityWrapper(CompatArmour.getInstance());
          compatibilityAdded.add(nameCompatibility);
          break;
          
        case "minecraft:main":
          InventoryHandler.addCompatibilityWrapper(CompatMain.getInstance());
          compatibilityAdded.add(nameCompatibility);
          break;
          
        case "minecraft:offhand":
          InventoryHandler.addCompatibilityWrapper(CompatOffHand.getInstance());
          compatibilityAdded.add(nameCompatibility);
          break;
          
        default:
          if (!Loader.isModLoaded(nameCompatibility)) {
            GraveKeeper.logger.info(String.format("Skipping compatibility for non-loaded mod %s",
                nameCompatibility ));
            continue;
          }
          switch (nameCompatibility) {
            case "aether_legacy":
              InventoryHandler.addCompatibilityWrapper(CompatAetherLegacy.getInstance());
              compatibilityAdded.add(nameCompatibility);
              break;
            
            case "baubles":
              InventoryHandler.addCompatibilityWrapper(CompatBaubles.getInstance());
              compatibilityAdded.add(nameCompatibility);
              break;

            case "cosmeticarmorreworked":
              InventoryHandler.addCompatibilityWrapper(CompatCosmeticArmorReworked.getInstance());
              compatibilityAdded.add(nameCompatibility);
              break;

            case "galacticraftcore":
              InventoryHandler.addCompatibilityWrapper(CompatGalacticCraftCore.getInstance());
              compatibilityAdded.add(nameCompatibility);
              break;

            case "techguns":
              InventoryHandler.addCompatibilityWrapper(CompatTechGuns.getInstance());
              compatibilityAdded.add(nameCompatibility);
              break;
            
            case "travelersbackpack":
              InventoryHandler.addCompatibilityWrapper(CompatTravelersBackpack.getInstance());
              compatibilityAdded.add(nameCompatibility);
              break;
            
            default:
              GraveKeeper.logger.error(String.format("Skipping unknown compatibility name %s",
                    nameCompatibility ));
              break;
          }
          // continue;
          break;
      }
    }
  }
  
  public static void loadConfig(final File file) {
    final Configuration config = new Configuration(file);
    config.load();
    
    IGNORE_KEEP_INVENTORY = config
        .get("general", "ignore_keep_inventory", IGNORE_KEEP_INVENTORY,
            "Whether the chests should still spawn when keepInventory is enabled")
        .getBoolean(false);
    DEBUG_LOGS = config
        .get("general", "enable_debug_logs", DEBUG_LOGS,
            "Enable console logs for debugging purpose")
        .getBoolean(false);
    COMPATIBILITY_ORDER = config
        .get("general", "inventory_sorting", COMPATIBILITY_ORDER, String.join("\n", new String[] {
            "Define which inventories are enabled and in which order they're processed. Missing mods are ignored when creating a grave.",
            "Use this to adjust soulbind priority between inventory type. Remove inventories you don't want to be saved in a grave.",
            "Note: Traveler's backpack are only saved if you disable block placement in the related mod.",
            "Valid vanilla values are: minecraft:armour, minecraft:main & minecraft:offhand.",
            "Valid modded values are: aether_legacy, baubles, cosmeticarmorreworked, galacticraftcore, techguns, travelersbackpack." }))
        .getStringList();
    
    EXPIRE_TIME_SECONDS = config
        .get("general", "expire_time", EXPIRE_TIME_SECONDS, String.join("\n", new String[] {
            "Time in seconds after which other players will be able to collect ones grave",
            "Use 0 to have an instant expiration and anyone is able to pick up the grave instantly",
            "Use -1 or lower to remove expiration and only the owner will ever be able to pick up the grave"
        }))
        .getInt(7200);
    EXPIRE_TIME_SECONDS = Math.max(-1, EXPIRE_TIME_SECONDS);
    INSTANT_FOREIGN_COLLECTION = EXPIRE_TIME_SECONDS == 0;
    OWNER_ONLY_COLLECTION = EXPIRE_TIME_SECONDS == -1;
    
    CHARM_ARMOR_HELD_NAMES = config
        .get("charm", "charm_armor_held_names", CHARM_ARMOR_HELD_NAMES, String.join("\n", new String[] {
            "List of charm item names that will keep armor and held items",
            "Warning: items listed here are responsible to save & restore inventory on death, GraveKeeper won't do it for them." }))
        .getStringList();
    CHARM_ARMOR_HOTBAR_NAMES = config
        .get("charm", "charm_armor_hotbar_names", CHARM_ARMOR_HOTBAR_NAMES, String.join("\n", new String[] {
            "List of charm item names that will keep armor and hotbar items",
            "Warning: items listed here are responsible to save & restore inventory on death, GraveKeeper won't do it for them." }))
        .getStringList();
    CHARM_FULL_NAMES = config
        .get("charm", "charm_full_names", CHARM_FULL_NAMES, String.join("\n", new String[] {
            "List of charm item names that will keep armor, baubles, off hand and main inventory items",
            "Warning: items listed here are responsible to save & restore inventory on death, GraveKeeper won't do it for them." }))
        .getStringList();
    
    KEEP_SOULBOUND_AMOUNT = config
        .get("soulbound", "amount", KEEP_SOULBOUND_AMOUNT,
            "The amount of soulbound items should be kept in player inventory, remaining will go into the grave")
        .getInt(5);
    // ANY_ENCHANT_IS_SOULBOUND = config
    //     .get("soulbound", "any_enchant_is_soulbound", ANY_ENCHANT_IS_SOULBOUND,
    //         "Enable to consider any enchantment as a soulbound one.")
    //     .getBoolean(false);
    MOVE_SOULBOUND_ITEMS_TO_MAIN_INVENTORY = config
        .get("soulbound", "move_soulbound_items_to_main_inventory", MOVE_SOULBOUND_ITEMS_TO_MAIN_INVENTORY, String.join("\n", new String[] {
            "Enable to workaround for soul binding mods that don't support modded inventories.",
            "For example: CoFH doesn't support Baubles, Twilight Forest doesn't support TechGuns,",
            "EnderIO doesn't support AetherLegacy, etc." }))
        .getBoolean(true);
    SOULBOUND_ENCHANTMENT_NAMES = config
        .get("soulbound", "soulbound_enchantment_names", SOULBOUND_ENCHANTMENT_NAMES,
            "List of enchantment names that are considered as soulbinding")
        .getStringList();
    SOULBOUND_TAG_STRINGS = config
        .get("soulbound", "soulbound_tags", SOULBOUND_TAG_STRINGS,
            "List of JSON based alternate NBT values that are considered as soulbinding when found on an item.")
        .getStringList();
    
    SEARCH_MIN_ALTITUDE = Math.abs(config
        .get("grave_location", "search_min_altitude", SEARCH_MIN_ALTITUDE,
            "Force a minimum altitude before looking for a free spot (this also applies to home/spawn location).")
        .getInt(0));
    SEARCH_RADIUS_ABOVE_M = Math.abs(config
        .get("grave_location", "search_radius_above_m", SEARCH_RADIUS_ABOVE_M,
            "How far to search around vertically above for a free spot to place the grave.")
        .getInt(10));
    SEARCH_RADIUS_BELOW_M = Math.abs(config
        .get("grave_location", "search_radius_below_m", SEARCH_RADIUS_BELOW_M,
            "How far to search around vertically below for a free spot to place the grave.")
        .getInt(-1));
    SEARCH_RADIUS_HORIZONTAL_M = config
        .get("grave_location", "search_radius_horizontal_m", SEARCH_RADIUS_HORIZONTAL_M,
            "How far to search around horizontally for a free spot to place the grave.")
        .getInt(5);
    SPAWN_DIMENSION_ID = config
        .get("grave_location", "spawn_dimension", SPAWN_DIMENSION_ID,
            "Defines which spawn dimension to use when player has no bed set.")
        .getInt(0);
    USE_BED_OR_SPAWN_LOCATION_BELOW_Y = config
        .get("grave_location", "use_bed_or_spawn_location_below_y", USE_BED_OR_SPAWN_LOCATION_BELOW_Y, String.join("\n", new String[] {
            "Use Bed or spawn location when death happens with Y below this value.",
            "Use -1000 or lower to disable it, use 1000 or higher to force permanently.",
            "We first check bed in current dimension, then bed in spawn dimension, then we use the actual spawn.",
            "Note: default spawn is center of the world at Y = 0." }))
        .getInt(0);
    if (USE_BED_OR_SPAWN_LOCATION_BELOW_Y <= -1000) {
      USE_BED_OR_SPAWN_LOCATION_BELOW_Y = Integer.MIN_VALUE;
    } else if (USE_BED_OR_SPAWN_LOCATION_BELOW_Y >= 1000) {
      USE_BED_OR_SPAWN_LOCATION_BELOW_Y = Integer.MAX_VALUE;
    }
    
    config.save();
  }
  
  public static void onFMLpostInitialization() {
    // resolve registry names
    CHARM_ARMOR_HELD_ITEMS = new ArrayList<>(CHARM_ARMOR_HELD_NAMES.length);
    for (final String name : CHARM_ARMOR_HELD_NAMES) {
      final Item item = Item.REGISTRY.getObject(new ResourceLocation(name));
      if (item != null) {
        CHARM_ARMOR_HELD_ITEMS.add(item);
      }
    }
    
    CHARM_ARMOR_HOTBAR_ITEMS = new ArrayList<>(CHARM_ARMOR_HOTBAR_NAMES.length);
    for (final String name : CHARM_ARMOR_HOTBAR_NAMES) {
      final Item item = Item.REGISTRY.getObject(new ResourceLocation(name));
      if (item != null) {
        CHARM_ARMOR_HOTBAR_ITEMS.add(item);
      }
    }
    
    CHARM_FULL_ITEMS = new ArrayList<>(CHARM_FULL_NAMES.length);
    for (final String name : CHARM_FULL_NAMES) {
      final Item item = Item.REGISTRY.getObject(new ResourceLocation(name));
      if (item != null) {
        CHARM_FULL_ITEMS.add(item);
      }
    }
    
    CHARM_ITEMS = new ArrayList<>(
        CHARM_ARMOR_HELD_ITEMS.size()
      + CHARM_ARMOR_HOTBAR_ITEMS.size()
      + CHARM_FULL_ITEMS.size() );
    CHARM_ITEMS.addAll(CHARM_ARMOR_HELD_ITEMS);
    CHARM_ITEMS.addAll(CHARM_ARMOR_HOTBAR_ITEMS);
    CHARM_ITEMS.addAll(CHARM_FULL_ITEMS);
    
    SOULBOUND_ENCHANTMENTS = new ArrayList<>(SOULBOUND_ENCHANTMENT_NAMES.length);
    for (final String name : SOULBOUND_ENCHANTMENT_NAMES) {
      final Enchantment enchantment = Enchantment.REGISTRY.getObject(new ResourceLocation(name));
      if (enchantment != null) {
        SOULBOUND_ENCHANTMENTS.add(enchantment);
      }
    }

    SOULBOUND_TAGS = new ArrayList<>(SOULBOUND_TAG_STRINGS.length);
    for (final String stringTag : SOULBOUND_TAG_STRINGS) {
      try {
        final NBTTagCompound tagCompound = JsonToNBT.getTagFromJson(stringTag);
        SOULBOUND_TAGS.add(tagCompound);
      } catch (final Exception exception) {
        GraveKeeper.logger.error(String.format("Error parsing '%s'", stringTag));
        exception.printStackTrace(GraveKeeper.printStreamError);
      }
    }
  }
}