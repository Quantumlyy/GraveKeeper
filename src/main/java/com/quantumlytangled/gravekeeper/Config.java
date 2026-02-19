package com.quantumlytangled.gravekeeper;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
	
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	
	public static final ModConfigSpec.BooleanValue IGNORE_KEEP_INVENTORY = BUILDER
			                                                                       .comment("Whether the chests should still spawn when keepInventory is enabled")
			                                                                       .define("ignore_keep_inventory", false);
	
	public static final ModConfigSpec.BooleanValue DEBUG_LOGS = BUILDER
			                                                            .comment("Enable console logs for debugging purpose")
			                                                            .define("debug_logs", false);
	
	public static final ModConfigSpec.IntValue EXPIRE_TIME_SECONDS = BUILDER
			                                                                 .comment("Time in seconds after which other players can collect the grave. 0 = instant, -1 = never expires")
			                                                                 .defineInRange("expire_time", 7200, -1, Integer.MAX_VALUE);
	
	public static final ModConfigSpec.IntValue SEARCH_MIN_ALTITUDE = BUILDER
			                                                                 .comment("Force a minimum altitude before looking for a free spot")
			                                                                 .defineInRange("search_min_altitude", 0, 0, Integer.MAX_VALUE);
	
	public static final ModConfigSpec.IntValue SEARCH_RADIUS_ABOVE_M = BUILDER
			                                                                   .comment("How far to search vertically above for a free spot")
			                                                                   .defineInRange("search_radius_above_m", 10, 0, Integer.MAX_VALUE);
	
	public static final ModConfigSpec.IntValue SEARCH_RADIUS_BELOW_M = BUILDER
			                                                                   .comment("How far to search vertically below for a free spot")
			                                                                   .defineInRange("search_radius_below_m", 10, 0, Integer.MAX_VALUE);
	
	public static final ModConfigSpec.IntValue SEARCH_RADIUS_HORIZONTAL_M = BUILDER
			                                                                        .comment("How far to search horizontally for a free spot")
			                                                                        .defineInRange("search_radius_horizontal_m", 5, 0, Integer.MAX_VALUE);
	
	public static final ModConfigSpec.ConfigValue<String> SPAWN_DIMENSION = BUILDER
			                                                                        .comment("Defines which spawn dimension to use when player has no bed set")
			                                                                        .define("spawn_dimension", "minecraft:overworld");
	
	public static final ModConfigSpec.IntValue USE_BED_OR_SPAWN_LOCATION_BELOW_Y = BUILDER
			                                                                               .comment("Use bed or spawn location when death happens below this Y value. -1000 to disable, 1000 to force always")
			                                                                               .defineInRange("use_bed_or_spawn_location_below_y", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
	
	static final ModConfigSpec SPEC = BUILDER.build();
}