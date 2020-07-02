package io.github.bananapuncher714.minecraftvideo.command;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.minecraftvideo.MinecraftVideo;
import io.github.bananapuncher714.minecraftvideo.video.McVideo;
import io.github.bananapuncher714.minecraftvideo.video.VideoPlayer;

public class VLCCommand implements CommandExecutor {
	private MinecraftVideo plugin;
	
	public VLCCommand( MinecraftVideo plugin ) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command command, String arg2, String[] args ) {
		if ( args.length == 0 ) {
			sender.sendMessage( "Valid arguments are 'unsafeplay' and 'unsafestop'" );
		} else if ( args.length > 0 ) {
			if ( args[ 0 ].equalsIgnoreCase( "unsafeplay" ) ) {
				unsafePlay( sender, command, args );
			} else if ( args[ 0 ].equalsIgnoreCase( "unsafestop" ) ) {
				unsafeStop( sender, command, args );
			}
		}
		return false;
	}
	
	private void unsafePlay( CommandSender sender, Command command, String[] args ) {
		if ( args.length > 2 ) {
			String url = args[ 1 ];
			int width = 256;
			int height = 192;
			if ( args.length > 2 ) {
				String[] dim = args[ 2 ].split( ":" );
				width = Integer.parseInt( dim[ 0 ] );
				height = Integer.parseInt( dim[ 1 ] );
			}
			VideoPlayer player = plugin.getPlayer( "default" );
			if ( player != null ) {
				player.terminate();
			}
			File videoFile = new File( plugin.getDataFolder() + "/" + url );
			if ( videoFile.exists() ) {
				url = videoFile.getAbsolutePath();
			}
			McVideo video = new McVideo( "test", url );
			VideoPlayer finalPlayer = new VideoPlayer( video, width, height );
			plugin.setVideoPlayer( "default", finalPlayer );
			Bukkit.getScheduler().scheduleSyncDelayedTask( plugin, new Runnable() {
				@Override
				public void run() {
					finalPlayer.start();
				}
			}, 20 );
			sender.sendMessage( "Playing " + video.getId() );
		} else {
			sender.sendMessage( "Usage: /" + command.getName() + " unsafeplay <mrl> [width:height]" );
		}
	}
	
	private void unsafeStop( CommandSender sender, Command command, String[] args ) {
		VideoPlayer player = plugin.removePlayer( "default" );
		if ( player != null ) {
			player.terminate();
		}
	}

}
