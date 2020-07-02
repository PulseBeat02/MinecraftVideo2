package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
	@EventHandler
	private void onEvent( PlayerJoinEvent event ) {
		MinecraftVideo.getInstance().getHandler().registerPlayer( event.getPlayer() );
	}
	
	@EventHandler
	private void onEvent( PlayerQuitEvent event ) {
		MinecraftVideo.getInstance().getHandler().unregisterPlayer( event.getPlayer() );
		MinecraftVideo.getInstance().panic();
	}
}
