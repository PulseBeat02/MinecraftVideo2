package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.video;

import java.util.UUID;

import org.bukkit.entity.Entity;

import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.MinecraftVideo;

public class EntityNameCallback {
	UUID[] viewers;
	Entity[] entities;
	int width;
	int delay;
	
	long lastUpdated = 0;
	
	public EntityNameCallback( UUID[] viewers, Entity[] stands, int videoWidth, int delay ) {
		this.viewers = viewers;
		this.entities = stands;
		this.width = videoWidth;
		this.delay = delay;
	}
	
	public void send( int[] data ) {
		long time = System.currentTimeMillis(); 
		if ( time - lastUpdated >= delay ) { 
			lastUpdated = time;
//			Bukkit.getScheduler().runTaskLater( MinecraftVideo.getInstance(), () -> {
				MinecraftVideo.getInstance().getHandler().display( viewers, entities, data, width );
//			}, 1 );
		}
	}
}
