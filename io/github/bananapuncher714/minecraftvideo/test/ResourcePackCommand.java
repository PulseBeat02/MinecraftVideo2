package io.github.bananapuncher714.minecraftvideo.test;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.minecraftvideo.MinecraftVideo;
import io.github.bananapuncher714.minecraftvideo.video.process.VideoLoader;

public class ResourcePackCommand implements CommandExecutor {
	private MinecraftVideo plugin;
	
	public ResourcePackCommand( MinecraftVideo main ) {
		this.plugin = main;
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command arg1, String arg2, String[] args ) {
		
		if ( args.length > 0 ) {
			if ( args[ 0 ].equalsIgnoreCase( "get" ) ) {
				if ( args.length > 2 ) {
					 String id = args[ 1 ];
					 String mrl = args[ 2 ];
					 
					 VideoLoader loader = new VideoLoader( new File( plugin.getDataFolder() + "/videos" ), id, mrl );
					 new Thread() {
						 @Override
						 public void run() {
							 loader.load();
							 plugin.getHandler().sendResourcePack( null, new File( loader.getBaseFolder() + "/" + id + ".zip" ) );
						 }
					 }.start();
					 
					 sender.sendMessage( "Sending resource pack... Please wait" );
				} else {
					sender.sendMessage( "Must be proper format!" );
				}
			} else {
				sender.sendMessage( "Incorrect argument" );
			}
		} else {
			sender.sendMessage( "Must provide argument!" );
		}
		
		return false;
	}

}
