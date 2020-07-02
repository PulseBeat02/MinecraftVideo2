package io.github.bananapuncher714.minecraftvideo.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import io.github.bananapuncher714.minecraftvideo.MinecraftVideo;
import io.github.bananapuncher714.minecraftvideo.resoucepack.SoundPackWrapper;
import io.github.bananapuncher714.minecraftvideo.video.McVideo;
import io.github.bananapuncher714.minecraftvideo.video.VideoPlayer;
import io.github.bananapuncher714.minecraftvideo.video.process.VideoLoader;

public class VideoCommand implements CommandExecutor, TabCompleter {
	private MinecraftVideo plugin;

	public VideoCommand( MinecraftVideo plugin ) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command arg1, String arg2, String[] args ) {
		if ( args.length == 0 ) {
			if ( sender.isOp() ) {
				sender.sendMessage( ChatColor.RED + "Valid arguments are load, play, stop, list, and delete" );
			} else {
				sender.sendMessage( ChatColor.RED + "Valid arguments are play, stop, and list" );
			}
		} else if ( args.length > 0 ) {
			if ( args[ 0 ].equalsIgnoreCase( "load" ) ) {
				loadVideo( sender, args );
			} else if ( args[ 0 ].equalsIgnoreCase( "play" ) ) {
				playVideo( sender, args );
			} else if ( args[ 0 ].equalsIgnoreCase( "stop" ) ) {
				stopVideo( sender, args );
			} else if ( args[ 0 ].equalsIgnoreCase( "list" ) ) {
				list( sender );
			} else if ( args[ 0 ].equalsIgnoreCase( "delete" ) ) {
				delete( sender, args );
			} else {
				if ( sender.isOp() ) {
					sender.sendMessage( ChatColor.RED + "Valid arguments are load, play, stop, list, and delete" );
				} else {
					sender.sendMessage( ChatColor.RED + "Valid arguments are play, stop, and list" );
				}
			}
		}
		return false;
	}
	
	@Override
	public List< String > onTabComplete( CommandSender sender, Command command, String label, String[] args ) {
		List< String > aos = new ArrayList< String >();
		if ( args.length == 0 || args.length == 1 ) {
			if ( sender.isOp() ) {
				aos.add( "load" );
				aos.add( "delete" );
			}
			aos.add( "list" );
			aos.add( "stop" );
			aos.add( "play" );
		} else if ( args.length == 2 ) {
			if ( sender.isOp() ) {
				if ( args[ 0 ].equalsIgnoreCase( "delete" ) ) {
					aos.addAll( plugin.getVideoCache().getVideoList() );
				}
			}
			if ( args[ 0 ].equalsIgnoreCase( "play" ) ) {
				aos.addAll( plugin.getVideoCache().getVideoList() );
			}
		}
		
		List< String > completions = new ArrayList< String >();
		StringUtil.copyPartialMatches( args[ args.length - 1 ], aos, completions );
		Collections.sort( completions );
		return completions;
	}
	
	private void stopVideo( CommandSender sender, String[] args ) {
		VideoPlayer vPlayer = plugin.removePlayer( "default" );
		if ( vPlayer != null ) {
			vPlayer.terminate();
			sender.sendMessage( ChatColor.LIGHT_PURPLE + "Stopped playing " + vPlayer.getVideo().getId() );
		} else {
			sender.sendMessage( ChatColor.RED + "No video is playing!" );
		}
	}
	
	private void playVideo( CommandSender sender, String[] args ) {
		if ( args.length > 1 && args.length < 4 ) {
			String id = args[ 1 ];
			McVideo video = plugin.getVideoCache().getVideo( id );
			if ( video == null ) {
				sender.sendMessage( ChatColor.RED + "Video does not exist!" );
				return;
			}
			
			int width = 640;
			int height = 360;
			if ( args.length > 2 ) {
				String[] dim = args[ 2 ].split( ":" );
				width = Integer.parseInt( dim[ 0 ] );
				height = Integer.parseInt( dim[ 1 ] );
			}
			
			VideoPlayer player = plugin.getPlayer( "default" );
			if ( player != null ) {
				player.terminate();
			}
			
			sender.sendMessage( ChatColor.AQUA + "Video " + id + " set at " + width + "x" + height + " pixels" );
			for ( Player online : Bukkit.getOnlinePlayers() ) {
				Set< String > values = plugin.getUserPacks().get( online.getUniqueId() );
				if ( values == null || !values.contains( id ) ) {
					sender.sendMessage( ChatColor.BLUE + "Sending audio pack..." );
					SoundPackWrapper wrapper = plugin.getVideoCache().getUberSoundpack( id );
					plugin.getUserPacks().put( online.getUniqueId(), wrapper.getIds() );
					
					plugin.getHandler().trackPack( online.getUniqueId() );
					plugin.getHandler().sendResourcePack( online.getUniqueId(), wrapper.getFile() );
				}
			}
			
			final Location location;
			if ( sender instanceof Player ) {
				location = ( ( Player ) sender ).getLocation();
			} else {
				location = null;
			}
			VideoPlayer finalPlayer = new VideoPlayer( video, width, height ) {
				@Override
				public void playing() {
					if ( location != null ) {
						new Thread() {
							@Override
							public void run() {
								try {
									Thread.sleep( 100 );
								} catch ( InterruptedException e ) {
									e.printStackTrace();
								}
								plugin.getHandler().playSound( null, video.getId(), location, 30 );
							}
						}.start();
					}
				}
				
				@Override
				public void finished() {
					super.finished();
					plugin.getHandler().stopSound( null, video.getId() );
				}
			};
			plugin.setVideoPlayer( "default", finalPlayer );
			Bukkit.getScheduler().scheduleSyncDelayedTask( plugin, new Runnable() {
				@Override
				public void run() {
					new Thread() {
						// Wait either 10 seconds or until all players confirm the resource pack
						private long initiated = System.currentTimeMillis();
						private long timeout = 30_000;
						@Override
						public void run() {
							Bukkit.broadcastMessage( ChatColor.GOLD + "Waiting for players to accept resource pack..." );
							while ( !plugin.getHandler().isPackConfirmed() && System.currentTimeMillis() - initiated < timeout ) {
								try {
									Thread.sleep( 100 );
								} catch ( InterruptedException e ) {
									e.printStackTrace();
								}
							}
							Bukkit.broadcastMessage( ChatColor.LIGHT_PURPLE + "Playing " + id + " in 1 second!" );
							plugin.getHandler().resetTrackPacker();
							try {
								Thread.sleep( 1000 );
							} catch ( InterruptedException e ) {
								e.printStackTrace();
							}
							finalPlayer.start();
						}
					}.start();
				}
			}, 10 );
			
			for ( int i = 0; i < finalPlayer.getMapHeight() * finalPlayer.getMapWidth(); i++ ) {
				plugin.registerMap( finalPlayer.getMapId() + i );
			}
			
			sender.sendMessage( ChatColor.GOLD + "Waiting to play " + id );
		} else {
			sender.sendMessage( ChatColor.RED + "Usage: /video play <id> [width:height]" );
		}
	}
	
	private void loadVideo( CommandSender sender, String[] args ) {
		if ( !sender.isOp() ) {
			return;
		}
		if ( args.length == 3 ) {
			String id = args[ 1 ].toLowerCase();
			McVideo video = plugin.getVideoCache().getVideo( id );
			if ( video != null ) {
				sender.sendMessage( ChatColor.RED + id + " is already loaded!" );
				return;
			}
			
			String mrl = args[ 2 ];
			File videoFile = new File( plugin.getDataFolder() + "/" + mrl );
			if ( videoFile.exists() ) {
				mrl = videoFile.getAbsolutePath();
			}
			
			VideoLoader loader = new VideoLoader( plugin.getVideoCache().getVideoCacheLocation(), id, mrl );
			new Thread() {
				 @Override
				 public void run() {
					 plugin.getVideoCache().registerMcVideo( loader.load() );
					 Bukkit.broadcastMessage( ChatColor.LIGHT_PURPLE + "Finished loading " + id );
				 }
			 }.start();
			 sender.sendMessage( ChatColor.LIGHT_PURPLE + "Loading " + id + "..." );
		} else {
			sender.sendMessage( ChatColor.RED + "Usage: /video load <id> <mrl>" );
		}
	}

	private void list( CommandSender sender ) {
		StringBuilder videoList = new StringBuilder( ChatColor.GREEN + "Loaded videos: " + ChatColor.GRAY );
		for ( String video : plugin.getVideoCache().getVideoList() ) {
			videoList.append( video + " " );
		}
		sender.sendMessage( videoList.toString() );
		if ( !sender.isOp() ) {
			return;
		}
		
		StringBuilder builder = new StringBuilder( ChatColor.BLUE + "Video folders found: " + ChatColor.GRAY );
		for ( File file : plugin.getVideoCache().getVideoCacheLocation().listFiles() ) {
			builder.append( file.getName() + " " );
		}
		sender.sendMessage( builder.toString() );
	}
	
	private void delete( CommandSender sender, String[] args ) {
		if ( !sender.isOp() ) {
			return;
		}
		if ( args.length == 2 ) {
			plugin.getVideoCache().delete( args[ 1 ] );
			sender.sendMessage( ChatColor.GREEN + "Deleted successfully!" );
		} else {
			sender.sendMessage( ChatColor.RED + "Must provide id!" );
		}
	}
}
