package com.quantumlyy.gravekeeper;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import com.electronwill.nightconfig.core.io.WritingMode;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.enchantment.Enchantment;

import net.minecraft.nbt.CompoundNBT;

import net.minecraft.nbt.JsonToNBT;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class GraveKeeperConfig {
  
  public static final String CATEGORY_GENERAL = "general";
  public static final String CATEGORY_SOULBOUND = "soulbound";
  public static final String CATEGORY_GRAVE_LOCATION = "grave_location";
  
  public static final String SUBCATEGORY_SEARCH = "search";
  
  private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
  
  public static ForgeConfigSpec COMMON_CONFIG;
  
  public static ForgeConfigSpec.BooleanValue IGNORE_KEEP_INVENTORY;
  public static ForgeConfigSpec.BooleanValue DEBUG_LOGS;
  public static ForgeConfigSpec.ConfigValue<List<? extends String>> COMPATIBILITY_ORDER;
  
  public static ForgeConfigSpec.IntValue EXPIRE_TIME_SECONDS;
  
  public static ForgeConfigSpec.IntValue KEEP_SOULBOUND_AMOUNT;
  public static ForgeConfigSpec.BooleanValue ANY_ENCHANT_IS_SOULBOUND;
  public static ForgeConfigSpec.BooleanValue MOVE_SOULBOUND_ITEMS_TO_MAIN_INVENTORY;
  public static ForgeConfigSpec.ConfigValue<List<? extends String>> SOULBOUND_ENCHANTMENT_NAMES;
  public static ForgeConfigSpec.ConfigValue<List<? extends String>> SOULBOUND_TAG_STRINGS;
  
  public static ForgeConfigSpec.IntValue SPAWN_DIMENSION_ID;
  public static ForgeConfigSpec.IntValue USE_BED_OR_SPAWN_LOCATION_BELOW_Y;
  
  public static ForgeConfigSpec.IntValue SEARCH_MIN_ALTITUDE;
  public static ForgeConfigSpec.IntValue SEARCH_RADIUS_ABOVE_M;
  public static ForgeConfigSpec.IntValue SEARCH_RADIUS_BELOW_M;
  public static ForgeConfigSpec.IntValue SEARCH_RADIUS_HORIZONTAL_M;
  
  public static int EXPIRE_TIME_SECONDS_VALUE = 7200;
  public static boolean INSTANT_FOREIGN_COLLECTION = false;
  public static boolean OWNER_ONLY_COLLECTION = false;
  public static int USE_BED_OR_SPAWN_LOCATION_BELOW_Y_VALUE = 0;
  
  public static List<Enchantment> SOULBOUND_ENCHANTMENTS = null;
  public static List<CompoundNBT> SOULBOUND_TAGS = null;
  
  static {
    COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
    setupCommonConfigGeneral();
    COMMON_BUILDER.pop();
  
    COMMON_BUILDER.comment("Soulbound settings").push(CATEGORY_SOULBOUND);
    setupCommonConfigSoulbound();
    COMMON_BUILDER.pop();
  
    COMMON_BUILDER.comment("Grave location settings").push(CATEGORY_GRAVE_LOCATION);
    setupCommonConfigGraveLocation();
    COMMON_BUILDER.pop();
    
    COMMON_CONFIG = COMMON_BUILDER.build();
  }
  
  private static void setupCommonConfigGeneral() {
    IGNORE_KEEP_INVENTORY = COMMON_BUILDER
                                .comment("Whether the chests should still spawn when keepInventory is enabled.")
                                .define("ignoreKeepInventory", false);
    DEBUG_LOGS = COMMON_BUILDER
                     .comment("Enable console logs for debugging purpose.")
                     .define("enableDebugLogs", false);
    COMPATIBILITY_ORDER = COMMON_BUILDER
                              .comment(String.join("\n", new String[] {
                                  "Define which inventories are enabled and in which order they're processed. Missing mods are ignored when creating a grave.",
                                  "Use this to adjust soulbind priority between inventory type. Remove inventories you don't want to be saved in a grave.",
                                  "Note: Traveler's backpack are only saved if you disable block placement in the related mod.",
                                  "Valid vanilla values are: minecraft:armour, minecraft:main & minecraft:offhand.",
                                  "Valid modded values are: NONE"
                              }))
                              .defineList("inventorySorting", Arrays.asList("minecraft:armour", "minecraft:main", "minecraft:offhand"), s -> s instanceof String);
    EXPIRE_TIME_SECONDS = COMMON_BUILDER
                     .comment(String.join("\n", new String[] {
                         "Time in seconds after which other players will be able to collect ones grave",
                         "Use 0 to have an instant expiration and anyone is able to pick up the grave instantly",
                         "Use -1 or lower to remove expiration and only the owner will ever be able to pick up the grave."
                     }))
                     .defineInRange("expireTime", 7200, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }
  
  private static void setupCommonConfigSoulbound() {
    KEEP_SOULBOUND_AMOUNT = COMMON_BUILDER
                                .comment("The amount of soulbound items should be kept in player inventory, remaining will go into the grave.")
                                .defineInRange("amount", 5, 0, Integer.MAX_VALUE);
    ANY_ENCHANT_IS_SOULBOUND = COMMON_BUILDER
                                   .comment("Enable to consider any enchantment as a soulbinding one.")
                                   .define("anyEnchantIsSoulbinding", false);
    MOVE_SOULBOUND_ITEMS_TO_MAIN_INVENTORY = COMMON_BUILDER
                                                 .comment(String.join("\n", new String[] {
                                                     "Enable to workaround for soul binding mods that don't support modded inventories.",
                                                     "For example: CoFH doesn't support Baubles, Twilight Forest doesn't support TechGuns,",
                                                     "EnderIO doesn't support AetherLegacy, etc."
                                                 }))
                                                 .define("moveSoulboundItemsToMainInventory", true);
    SOULBOUND_ENCHANTMENT_NAMES = COMMON_BUILDER
                                      .comment("List of enchantment names that are considered as soulbinding")
                                      .defineList("enchantmentName", Arrays.asList("enderio:soulbound"), s -> s instanceof String);
    SOULBOUND_TAG_STRINGS = COMMON_BUILDER
                                      .comment("List of JSON based alternate NBT values that are considered as soulbinding when found on an item.")
                                      .defineList("tags", Arrays.asList("{ \"Botania_keepIvy\": 1b }", "{ \"spectreAnchor\": 0b }"), s -> s instanceof String);
  }
  
  private static void setupCommonConfigGraveLocation() {
    SPAWN_DIMENSION_ID = COMMON_BUILDER
                             .comment("Defines which spawn dimension to use when player has no bed set.")
                             .defineInRange("spawnDimension", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    USE_BED_OR_SPAWN_LOCATION_BELOW_Y = COMMON_BUILDER
                                            .comment(String.join("\n", new String[] {
                                                "Use Bed or spawn location when death happens with Y below this value.",
                                                "Use -1000 or lower to disable it, use 1000 or higher to force permanently.",
                                                "We first check bed in current dimension, then bed in spawn dimension, then we use the actual spawn.",
                                                "Note: default spawn is center of the world at Y = 0."
                                            }))
                                            .defineInRange("useBedOrSpawnLocationBelowY", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
  
    COMMON_BUILDER.comment("Grave search settings").push(SUBCATEGORY_SEARCH);
    
    SEARCH_MIN_ALTITUDE = COMMON_BUILDER
                              .comment("Force a minimum altitude before looking for a free spot (this also applies to home/spawn location).")
                              .defineInRange("minAltitude", 0, 0, Integer.MAX_VALUE);
    SEARCH_RADIUS_ABOVE_M = COMMON_BUILDER
                                .comment("How far to search around vertically above for a free spot to place the grave.")
                                .defineInRange("radiusAboveM", 10, 0, Integer.MAX_VALUE);
    SEARCH_RADIUS_BELOW_M = COMMON_BUILDER
                                .comment("How far to search around vertically below for a free spot to place the grave.")
                                // TODO: Confirm with Lem if the Math.abs was intended in the 1.12.x version
                                .defineInRange("radiusBelowM", 1, 0, Integer.MAX_VALUE);
    SEARCH_RADIUS_HORIZONTAL_M = COMMON_BUILDER
                                     .comment("How far to search around horizontally for a free spot to place the grave.")
                                     .defineInRange("radiusHorizontalM", 5, Integer.MIN_VALUE, Integer.MAX_VALUE);
    
    COMMON_BUILDER.pop();
  }
  
  public static void loadConfig(ForgeConfigSpec spec, Path path) {
    final CommentedFileConfig configData = CommentedFileConfig
                                               .builder(path)
                                               .sync()
                                               .autosave()
                                               .writingMode(WritingMode.REPLACE)
                                               .build();
    
    configData.load();
    spec.setConfig(configData);
    initialiseValues();
  }
  
  @SubscribeEvent
  public static void onLoad(final ModConfig.Loading configEvent) {
    initialiseValues();
  }
  
  @SubscribeEvent
  public static void onReload(final ModConfig.Reloading configEvent) {
    initialiseValues();
  }
  
  private static void initialiseValues() {
    EXPIRE_TIME_SECONDS_VALUE = Math.max(-1, EXPIRE_TIME_SECONDS.get());
    INSTANT_FOREIGN_COLLECTION = EXPIRE_TIME_SECONDS_VALUE == 0;
    OWNER_ONLY_COLLECTION = EXPIRE_TIME_SECONDS_VALUE == -1;
  
    if (USE_BED_OR_SPAWN_LOCATION_BELOW_Y.get() <= -1000) {
      USE_BED_OR_SPAWN_LOCATION_BELOW_Y_VALUE = Integer.MIN_VALUE;
    } else if (USE_BED_OR_SPAWN_LOCATION_BELOW_Y.get() >= 1000) {
      USE_BED_OR_SPAWN_LOCATION_BELOW_Y_VALUE = Integer.MAX_VALUE;
    } else {
      USE_BED_OR_SPAWN_LOCATION_BELOW_Y_VALUE = USE_BED_OR_SPAWN_LOCATION_BELOW_Y.get();
    }
  
    final List<? extends String> soulboundEnchantmentNames = SOULBOUND_ENCHANTMENT_NAMES.get();
    SOULBOUND_ENCHANTMENTS = new ArrayList<>(soulboundEnchantmentNames.size());
    for (final String name : soulboundEnchantmentNames) {
      final Enchantment enchantment = RegistryObject.of(new ResourceLocation(name), ForgeRegistries.ENCHANTMENTS).get();
      SOULBOUND_ENCHANTMENTS.add(enchantment);
    }
  
    final List<? extends String> soulboundTagsStrings = SOULBOUND_TAG_STRINGS.get();
    SOULBOUND_TAGS = new ArrayList<>(soulboundTagsStrings.size());
    for (final String stringTag : soulboundTagsStrings) {
      try {
        final CompoundNBT tagCompound = JsonToNBT.parseTag(stringTag);
        SOULBOUND_TAGS.add(tagCompound);
      } catch (final Exception exception) {
        GraveKeeper.logger.error(String.format("Error parsing '%s'", stringTag));
        exception.printStackTrace(GraveKeeper.printStreamError);
      }
    }
  }
  
}
