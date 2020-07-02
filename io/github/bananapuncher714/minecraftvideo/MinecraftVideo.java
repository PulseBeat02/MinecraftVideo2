package io.github.bananapuncher714.minecraftvideo;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.minecraftvideo.api.PacketHandler;
import io.github.bananapuncher714.minecraftvideo.command.VLCCommand;
import io.github.bananapuncher714.minecraftvideo.command.VideoCommand;
import io.github.bananapuncher714.minecraftvideo.httpd.MineHttpd;
import io.github.bananapuncher714.minecraftvideo.listeners.PlayerListener;
import io.github.bananapuncher714.minecraftvideo.resoucepack.SoundPackWrapper;
import io.github.bananapuncher714.minecraftvideo.tinyprotocol.TinyProtocol;
import io.github.bananapuncher714.minecraftvideo.util.FileUtil;
import io.github.bananapuncher714.minecraftvideo.util.JetpImageUtil;
import io.github.bananapuncher714.minecraftvideo.util.ReflectionUtil;
import io.github.bananapuncher714.minecraftvideo.video.VideoPlayer;
import io.github.bananapuncher714.minecraftvideo.video.process.YoutubeDownloader;
import io.netty.channel.Channel;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

public class MinecraftVideo extends JavaPlugin implements Listener {
	private static MinecraftVideo instance;

	public static final int HTTPD_PORT = 6911;
	public static byte[] BACKGROUND = new byte[ 0 ];
	
	public MinecraftVideo() {
		instance = this;
	}

	protected Map< UUID, Set< String > > users = new ConcurrentHashMap< UUID, Set< String > >();
	
	protected Map< String, VideoPlayer > videoPlayers = new ConcurrentHashMap< String, VideoPlayer >();
	protected PacketHandler handler;
	protected TinyProtocol tProtocol;
	protected MineHttpd httpd;
	protected VideoCache vCache;
	protected final String ip = isLocalhost() ? "localhost" : Bukkit.getIp();

	/**
	 * Toggle whether or not this server is localhost
	 * @return
	 */
	private static boolean isLocalhost() {
		return true;
	}
	
	@Override
	public void onEnable() {
		saveResource( "README.md", true );
		saveResource( "background.png", false );
		
		// Check for VLC's required libraries
		log( "Checking for required vlc libraries..." );
		if ( !new NativeDiscovery().discover() ) {
			severe( ChatColor.RED + "No VLC installation found! Disabling " + getName() );
			Bukkit.getPluginManager().disablePlugin( this );
			return;
		}
		log( "VLC version detected! " + LibVlc.INSTANCE.libvlc_get_version() );

		log( "External ip is " + ip );
		
		// Get a new PacketHandler for NMS related stuff
		log( "Constructing new NMS handler" );
		handler = ReflectionUtil.getNewPacketHandlerInstance();
		if ( handler == null ) {
			severe( ChatColor.RED + "This version(" + ReflectionUtil.VERSION + ") is not supported! Disabling " + getName() );
			Bukkit.getPluginManager().disablePlugin( this );
			return;
		}
		
		// Create a new mini http daemon for servicing resource packs listening on port 25566
		log( "Starting resource pack httpd..." );
		startHttpd();
		log( "Finished starting resource pack httpd" );

		tProtocol = new TinyProtocol( this ) {
			@Override
			public Object onPacketOutAsync( Player player, Channel channel, Object packet ) {
				return handler.onPacketInterceptOut( player, packet ) ? packet : null;
			}

			@Override
			public Object onPacketInAsync( Player player, Channel channel, Object packet ) {
				return handler.onPacketInterceptIn( player, packet ) ? packet : null;
			}
		};
		
		log( "Creating video cache..." );
		File videoCacheFile = new File( getDataFolder() + "/cache" );
		FileUtil.saveToFile( getResource( "pack.png" ), new File( videoCacheFile, "pack.png" ), false );
		vCache = new VideoCache( videoCacheFile );
		log( "Successfully created video cache" );
		log( "Sound packs registered at " + vCache.getSoundCacheLocation() );
		log( "Videos registered at " + vCache.getVideoCacheLocation() );
		
		log( "Initializing Youtube-dl..." );
		if ( YoutubeDownloader.setDir( new File( getDataFolder() + "/" + "youtube-dl" ) ) ) {
			log( "Youtube-dl has been found!" );
		} else {
			log( "Youtube-dl was not found and has been downloaded!" );
		}
		log( "Youtube-dl successfully initialized" );
		
		setDefaultBackground();
		log( "Registering commands..." );
		registerCommands();
		log( "Registering listeners..." );
		registerListeners();
		log( "Finished setting up " + getName() );
	}

	@Override
	public void onDisable() {
		log( "Shutting down video players" );
		for ( VideoPlayer player : videoPlayers.values() ) {
			if ( player != null ) {
				player.terminate();
			}
		}
		log( "Shutting down resource pack httpd" );
		httpd.terminate();
	}
	
	private void startHttpd() {
		try {
			httpd = new MineHttpd( HTTPD_PORT ) {
				@Override
				public File requestFileCallback( String request ) {
					// This will attempt to get an uber pack that contains many sounds
					SoundPackWrapper wrapper = vCache.getByName( request );
					if ( wrapper != null ) {
						return wrapper.getFile();
					} else {
						return null;
					}
				}
			};
			// Start the web server
			httpd.start();
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
	}
	
	private void setDefaultBackground() {
		File file = new File( getDataFolder(), "background.png" );
		if ( !file.exists() ) {
			try {
				log( "File not detected! Loading from jar..." );
				BACKGROUND = JetpImageUtil.convert( ImageIO.read( getResource( "background.png" ) ), 5 * 128, 5 * 128 );
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		} else {
			try {
				log( "Detected background file! Loading..." );
				BACKGROUND = JetpImageUtil.convert( ImageIO.read( file ), 5 * 128, 5 * 128 );
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}

	private void registerListeners() {
		Bukkit.getPluginManager().registerEvents( new PlayerListener(), this );
	}
	
	private void registerCommands() {
		VideoCommand videoCommand = new VideoCommand( this );
		getCommand( "video" ).setExecutor( videoCommand );
		getCommand( "video" ).setTabCompleter( videoCommand );
		getCommand( "vlc" ).setExecutor( new VLCCommand( this ) );
	}
	
	public static MinecraftVideo getInstance() {
		return instance;
	}
	
	public VideoCache getVideoCache() {
		return vCache;
	}
	
	public VideoPlayer getPlayer( String id ) {
		return videoPlayers.get( id );
	}
	
	public VideoPlayer setVideoPlayer( String id, VideoPlayer player ) {
		return videoPlayers.put( id, player );
	}
	
	public VideoPlayer removePlayer( String id ) {
		return videoPlayers.remove( id );
	}

	public void registerMap( int id ) {
		Bukkit.getMap( ( short ) id ).getRenderers().clear();
		handler.registerMap( id );
	}
	
	public boolean isRegistered( int id ) {
		return handler.isRegistered( id );
	}
	
	public Map< UUID, Set< String > > getUserPacks() {
		return users;
	}

	public void unregisterMap( int id ) {
		handler.unregisterMap( id );
	}

	public PacketHandler getHandler() {
		return handler;
	}
	
	public String getExternalIp() {
		return ip;
	}
	
	public static void log( String message ) {
		instance.getLogger().info( message );
	}
	
	public static void severe( String message ) {
		instance.getLogger().severe( message );
	}
}
