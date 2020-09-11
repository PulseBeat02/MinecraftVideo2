package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.internet;

import java.io.File;

import org.bukkit.entity.Player;

import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.MinecraftVideo;

public class ResourcePackHandler {
	
	public static void handle(Player p, String url) throws Exception {
		
		File[] files = YoutubeExtractor.getFiles(p, url);
		ResourcePackCreation.createEmptyZipFile(new VideoResource(files[0], files[1]));
		
		HTTPServer server = new HTTPServer(MinecraftVideo.port);
		server.start();
		
		String ip = "http://" + MinecraftVideo.getExternalIp() + ":" + MinecraftVideo.port;
		
		for (Player player : p.getWorld().getPlayers()) {
			
			player.setResourcePack(ip);
			
		}
		
	}

}
