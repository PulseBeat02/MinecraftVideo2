package io.github.bananapuncher714.minecraftvideo.video;

import org.bukkit.map.MapFont.CharacterSprite;
import org.bukkit.map.MinecraftFont;

import io.github.bananapuncher714.minecraftvideo.MinecraftVideo;
import io.github.bananapuncher714.minecraftvideo.api.PacketHandler;
import io.github.bananapuncher714.minecraftvideo.util.JetpImageUtil;

public class AsyncPacketSender extends Thread {
	// For FPS testing
	private static long last = System.currentTimeMillis();

	protected volatile boolean running = true;
	protected int currentFrame;
	protected VideoPlayer player;

	protected final PacketHandler handler; 
	
	protected AsyncPacketSender( VideoPlayer player ) {
		this.player = player;
		handler = MinecraftVideo.getInstance().getHandler();
	}

	@Override
	public void run() {
		while ( running ) {
			Object[] pack = player.getBufferAndFrame();
			if ( pack == null ) {
				continue;
			}
			int[] buffer = ( int[] ) pack[ 0 ];
			currentFrame = ( int ) pack[ 1 ];

			byte[] map = JetpImageUtil.dither( player.getWidth(), buffer );
//			byte[] map = McImageUtil.dither( player.getWidth(), buffer );
//			byte[] map = ImageUtil.simplify( buffer );

			while ( player.getCurrentFrame() < currentFrame ) {
				try {
					Thread.sleep( 50 );
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}
			}

			if ( currentFrame % 50 == 0 ) {
				System.out.println( "Sending frame " + currentFrame );
			}

			long value = System.currentTimeMillis() - last;
			int frames = ( int ) ( 1000 / ( double ) value );
			last = System.currentTimeMillis();

			// Display the fps
			char[] chars = String.valueOf( frames ).toCharArray();
			int x = 0;
			int yoff = 10;
			int xoff = 10;
			for ( int i = 0; i < chars.length; i++ ) {
				CharacterSprite sprite = MinecraftFont.Font.getChar( chars[ i ] );
				for ( int w = 0; w < sprite.getWidth(); w++ ) {
					for ( int h = 0; h < sprite.getHeight(); h++ ) {
						if ( sprite.get( h, w ) ) {
							map[ ( yoff + h ) * player.getWidth() + x + w + xoff ] = 34;
						}
					}
				}

				x += sprite.getWidth() + 1;
			}
			handler.display( null, player.getMapId(), player.getMapWidth(), player.getMapHeight(), map, player.getWidth() );

			player.finishedFrame();
		}
	}

	protected void terminate() {
		running = false;
	}
}
