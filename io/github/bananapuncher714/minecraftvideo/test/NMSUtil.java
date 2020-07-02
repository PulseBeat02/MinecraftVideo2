package io.github.bananapuncher714.minecraftvideo.test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_15_R1.MapIcon;
import net.minecraft.server.v1_15_R1.PacketPlayOutMap;
import net.minecraft.server.v1_15_R1.PlayerConnection;

/**
 * Converts a large byte[] array into an array of packets; Optimized so that each packet only contains what needs updating.
 * 
 * @author BananaPuncher714
 */
public class NMSUtil {
	private static final Map< UUID, PlayerConnection > connections = new ConcurrentHashMap< UUID, PlayerConnection >();

	private static Field[] MAP_FIELDS = new Field[ 9 ];

	private static long lastSent = System.currentTimeMillis();
	
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

	public static void display( int map, int width, int height, byte[] rgb, int videoWidth ) {
		int vidHeight = rgb.length / videoWidth;
		int pixH = height << 7;
		int pixW = width << 7;
		int xOff = ( pixW - videoWidth ) >> 1;
		int yOff = ( pixH - vidHeight ) >> 1;
		display( map, width, height, rgb, videoWidth, xOff, yOff );
	}

	public static void display( int map, int width, int height, byte[] rgb, int videoWidth, int xOff, int yOff ) {
		if ( System.currentTimeMillis() - lastSent < 40 ) {
			return;
		}
		lastSent = System.currentTimeMillis();
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
		
		for ( PlayerConnection connection : connections.values() ) {
			for ( PacketPlayOutMap packet : packetArray ) {
				connection.sendPacket( packet );
			}
		}
	}

	public static void addPlayer( Player player ) {
		connections.put( player.getUniqueId(), ( ( CraftPlayer ) player ).getHandle().playerConnection );
	}

	public static void removePlayer( Player player ) {
		connections.remove( player.getUniqueId() );
	}
}