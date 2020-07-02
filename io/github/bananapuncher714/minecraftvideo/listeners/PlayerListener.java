package io.github.bananapuncher714.minecraftvideo.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.minecraftvideo.MinecraftVideo;

public class PlayerListener implements Listener {
	@EventHandler
	private void onPlayerLoginEvent( PlayerJoinEvent event ) {
		MinecraftVideo.getInstance().getHandler().registerPlayer( event.getPlayer() );
		Bukkit.getScheduler().scheduleSyncDelayedTask( MinecraftVideo.getInstance(), new Runnable() {
			@Override
			public void run() {
				MinecraftVideo.getInstance().getHandler().display( new UUID[] { event.getPlayer().getUniqueId() }, 0, 5, 5, MinecraftVideo.BACKGROUND, 128 * 5 );
				MinecraftVideo.getInstance().getUserPacks().remove( event.getPlayer().getUniqueId() );
			}
		}, 10 );
		
	}
	
	@EventHandler
	private void onPlayerQuitEvent( PlayerQuitEvent event ) {
		MinecraftVideo.getInstance().getHandler().unregisterPlayer( event.getPlayer().getUniqueId() );
	}
	
	@EventHandler
	private void onMapInitializeEvent( MapInitializeEvent event ) {
		MapView view = event.getMap();
		if ( MinecraftVideo.getInstance().isRegistered( view.getId() ) ) {
			for ( MapRenderer renderer : view.getRenderers() ) {
				view.removeRenderer( renderer );
			}
		}
	}
}
