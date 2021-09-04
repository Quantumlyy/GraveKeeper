package dev.quantumly.gravekeeper.core;

import javax.annotation.Nonnull;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DeathHandler {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(@Nonnull final LivingDeathEvent event) {
		// TODO: this
	}
	
}
