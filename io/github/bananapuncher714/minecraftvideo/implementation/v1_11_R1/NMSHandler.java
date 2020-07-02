package io.github.bananapuncher714.minecraftvideo.implementation.v1_11_R1;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.minecraftvideo.MinecraftVideo;
import io.github.bananapuncher714.minecraftvideo.api.PacketHandler;
import io.github.bananapuncher714.minecraftvideo.util.Util;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_11_R1.MapIcon;
import net.minecraft.server.v1_11_R1.PacketDataSerializer;
import net.minecraft.server.v1_11_R1.PacketPlayInResourcePackStatus;
import net.minecraft.server.v1_11_R1.PacketPlayInResourcePackStatus.EnumResourcePackStatus;
import net.minecraft.server.v1_11_R1.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_11_R1.PacketPlayOutCustomSoundEffect;
import net.minecraft.server.v1_11_R1.PacketPlayOutMap;
import net.minecraft.server.v1_11_R1.PacketPlayOutResourcePackSend;
import net.minecraft.server.v1_11_R1.PlayerConnection;
import net.minecraft.server.v1_11_R1.SoundCategory;

public class NMSHandler implements PacketHandler {
	public static final int PACKET_THRESHOLD_MS = 40;

	private static Field[] MAP_FIELDS = new Field[ 9 ];

	static {
		try {
			MAP_FIELDS[ 0 ] = PacketPlayOutMap.class.getDeclaredField( "a" );
			MAP_FIELDS[ 1 ] = PacketPlayOutMap.class.getDeclaredField( "b" );
			MAP_FIELDS[ 2 ] = PacketPlayOutMap.class.getDeclaredField( "c" );
			MAP_FIELDS[ 3 ] = PacketPlayOutMap.class.getDeclaredField( "d" );
			MAP_FIELDS[ 4 ] = PacketPlayOutMap.class.getDeclaredField( "e" );
			MAP_FIELDS[ 5 ] = PacketPlayOutMap.class.getDeclaredField( "f" );
			MAP_FIELDS[ 6 ] = PacketPlayOutMap.class.getDeclaredField( "g" );
			MAP_FIELDS[ 7 ] = PacketPlayOutMap.class.getDeclaredField( "h" );
			MAP_FIELDS[ 8 ] = PacketPlayOutMap.class.getDeclaredField( "i" );
			
			for ( Field field : MAP_FIELDS ) {
				field.setAccessible( true );
			}
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}

	private final Map< UUID, PlayerConnection > playerConnections = new ConcurrentHashMap< UUID, PlayerConnection >();
	private final Map< UUID, Long > lastUpdated = new ConcurrentHashMap< UUID, Long >();
	private final boolean[] maps = new boolean[ Short.MAX_VALUE ];
	private final Set< UUID > packTracker = Collections.synchronizedSet( new HashSet< UUID >() );
	
	@Override
	public void display( UUID[] viewers, int map, int width, int height, byte[] rgb, int videoWidth ) {
		int vidHeight = rgb.length / videoWidth;
		int pixH = height << 7;
		int pixW = width << 7;
		int xOff = ( pixW - videoWidth ) >> 1;
		int yOff = ( pixH - vidHeight ) >> 1;
		display( viewers, map, width, height, rgb, videoWidth, xOff, yOff );
	}

	@Override
	public void display( UUID[] viewers, int map, int width, int height, byte[] rgb, int videoWidth, int xOff, int yOff ) {
		int vidHeight = rgb.length / videoWidth;
		int pixW = width << 7;
		int negXOff = xOff + videoWidth;
		int negYOff = yOff + vidHeight;
		int xDif = pixW - videoWidth;
		int top = yOff * pixW + xOff;

		PacketPlayOutMap[] packetArray = new PacketPlayOutMap[ ( int ) ( ( Math.ceil( negXOff / 128.0 ) - ( xOff >> 7 ) ) * ( Math.ceil( negYOff / 128.0 ) - ( yOff >> 7 ) ) ) ];
		int arrIndex = 0;

		for ( int x = xOff >> 7; x < Math.ceil( negXOff / 128.0 ); x++ ) {
			int relX = x << 7;
			for ( int y = yOff >> 7; y < Math.ceil( negYOff / 128.0 ); y++ ) {
				int relY = y << 7;

				int topX = Math.max( 0, xOff - relX );
				int topY = Math.max( 0, yOff - relY );
				int xDiff = Math.min( 128 - topX, negXOff - ( relX + topX ) );
				int yDiff = Math.min( 128 - topY, negYOff - ( relY + topY ) );

				byte[] mapData = new byte[ xDiff * yDiff ];
				for ( int ix = topX; ix < xDiff + topX; ix++ ) {
					int xPos = relX + ix;
					for ( int iy = topY; iy < yDiff + topY; iy++ ) {
						int yPos = relY + iy;
						int normalizedSlot = ( yPos * pixW + xPos ) - top;
						int index = normalizedSlot - ( int ) ( Math.floor( normalizedSlot / pixW ) * xDif );
						int val = ( iy - topY ) * xDiff + ix - topX;
						mapData[ val ] = rgb[ index ];
					}
				}

				int mapId = map + width * y + x;
				PacketPlayOutMap packet = new PacketPlayOutMap();

				try {
					MAP_FIELDS[ 0 ].set( packet, mapId );
					MAP_FIELDS[ 1 ].set( packet, ( byte ) 0 );
					MAP_FIELDS[ 2 ].set( packet, false );
					MAP_FIELDS[ 3 ].set( packet, new MapIcon[ 0 ] );
					MAP_FIELDS[ 4 ].set( packet, topX );
					MAP_FIELDS[ 5 ].set( packet, topY );
					MAP_FIELDS[ 6 ].set( packet, xDiff );
					MAP_FIELDS[ 7 ].set( packet, yDiff );
					MAP_FIELDS[ 8 ].set( packet, mapData );
				} catch ( Exception exception ) {
					exception.printStackTrace();
				}

				packetArray[ arrIndex++ ] = packet;
			}
		}

		if ( viewers == null ) {
			for ( UUID uuid : playerConnections.keySet() ) {
				Object val = lastUpdated.get( uuid );
				if ( val == null || System.currentTimeMillis() - ( long ) val > PACKET_THRESHOLD_MS ) {
					lastUpdated.put( uuid, System.currentTimeMillis() );
					PlayerConnection connection = playerConnections.get( uuid );
					for ( PacketPlayOutMap packet : packetArray ) {
						connection.sendPacket( packet );
					}
				}
			}
		} else {
			for ( UUID uuid : viewers ) {
				Object val = lastUpdated.get( uuid );
				if ( val == null || System.currentTimeMillis() - ( long ) val > PACKET_THRESHOLD_MS ) {
					lastUpdated.put( uuid, System.currentTimeMillis() );
					PlayerConnection connection = playerConnections.get( uuid );
					if ( connection != null ) {
						for ( PacketPlayOutMap packet : packetArray ) {
							connection.sendPacket( packet );
						}
					}
				}
			}
		}
	}

	@Override
	public boolean onPacketInterceptOut( Player viewer, Object packet ) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean onPacketInterceptIn( Player viewer, Object packet ) {
		if ( packet instanceof PacketPlayInResourcePackStatus ) {
			onResourcePackStatus( viewer, ( PacketPlayInResourcePackStatus ) packet );
		}
		return true;
	}
	
	private boolean onResourcePackStatus( Player player, PacketPlayInResourcePackStatus packet ) {
		MinecraftVideo.log( "Recieved resource pack status " + packet.status + " from " + player.getName() );
		if ( packet.status == EnumResourcePackStatus.DECLINED || packet.status == EnumResourcePackStatus.FAILED_DOWNLOAD || packet.status == EnumResourcePackStatus.SUCCESSFULLY_LOADED ) {
			packTracker.remove( player.getUniqueId() );
		}
		return true;
	}
	
	@Override
	public void sendResourcePack( UUID player, File file ) {
		PlayerConnection connection = null;
		if ( player != null ) {
			connection = playerConnections.get( player );
			if ( connection == null ) {
				return;
			}
		}
		try {
			MinecraftVideo.log( "Sending resource pack download link to " + player );
			MinecraftVideo.log( "URL: " + "http://" + MinecraftVideo.getInstance().getExternalIp() + ":" + MinecraftVideo.HTTPD_PORT + "/" + file.getName() );
			MinecraftVideo.log( "Resource Pack SHA-1 Hash: " + Util.calcSHA1( file ).toLowerCase() );
			PacketPlayOutResourcePackSend packet = new PacketPlayOutResourcePackSend( "http://" + MinecraftVideo.getInstance().getExternalIp() + ":" + MinecraftVideo.HTTPD_PORT + "/" + file.getName(), Util.calcSHA1( file ).toLowerCase() );
			if ( connection != null ) {
				connection.sendPacket( packet );
			} else {
				for ( PlayerConnection connections : playerConnections.values() ) {
					connections.sendPacket( packet );
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void playSound( UUID uuid, String id, Location location, double volume ) {
		PacketPlayOutCustomSoundEffect packet = new PacketPlayOutCustomSoundEffect( id, SoundCategory.MASTER, location.getX(), location.getY(), location.getZ(), ( float ) volume, 1f );
		
		if ( uuid == null ) {
			for ( PlayerConnection connection : playerConnections.values() ) {
				connection.sendPacket( packet );
			}
		} else {
			PlayerConnection connection = playerConnections.get( uuid );
			if ( connection != null ) {
				connection.sendPacket( packet );
			}
		}
	}
	
	@Override
	public void stopSound( UUID uuid, String id ) {
		PacketDataSerializer dataSerializer = new PacketDataSerializer( Unpooled.buffer() );
		dataSerializer.a( SoundCategory.MASTER.a() );
		dataSerializer.a( id );
		PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload( "MC|StopSound", dataSerializer );
		
		if ( uuid == null ) {
			for ( PlayerConnection connection : playerConnections.values() ) {
				connection.sendPacket( packet );
			}
		} else {
			PlayerConnection connection = playerConnections.get( uuid );
			if ( connection != null ) {
				connection.sendPacket( packet );
			}
		}
	}

	@Override
	public void registerPlayer( Player player ) {
		playerConnections.put( player.getUniqueId(), ( ( CraftPlayer ) player ).getHandle().playerConnection );
	}

	@Override
	public void unregisterPlayer( UUID uuid ) {
		playerConnections.remove( uuid );
	}

	@Override
	public void registerMap( int id ) {
		maps[ id ] = true;
	}
	
	@Override
	public boolean isRegistered( int id ) {
		return maps[ id ];
	}

	@Override
	public void unregisterMap( int id ) {
		maps[ id ] = false;
	}

	@Override
	public void trackPack( UUID uuid ) {
		packTracker.add( uuid );
	}

	@Override
	public boolean isPackConfirmed() {
		return packTracker.isEmpty();
	}

	@Override
	public void resetTrackPacker() {
		packTracker.clear();
	}
}
