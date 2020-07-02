package io.github.bananapuncher714.minecraftvideo.video;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import io.github.bananapuncher714.minecraftvideo.MinecraftVideo;
import io.github.bananapuncher714.minecraftvideo.api.PacketHandler;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

public class VideoPlayer {
	private final VideoPlayer instance;
	
	private final DirectMediaPlayerComponent mediaPlayerComponent;
	private final ReentrantLock bufferLock = new ReentrantLock( true );
	private SimpleRenderCallback callback;
	
	private final Set< AsyncPacketSender > threadpool = new HashSet< AsyncPacketSender >();
	private final int threadCount = 4;

	private int[] buffer;
	
	private final int width;
	private final int height;
	
	private int mapId = 0;
	private int mapWidth = 5;
	private int mapHeight = 5;
	
	private int frame;
	private int currentFrame;
	
	protected byte[] background;
	private final PacketHandler handler;
	private final McVideo video;
	
	public VideoPlayer( McVideo video, int width, int height ) {
		instance = this;
		this.width = width >> 1 << 1;
		this.height = height >> 1 << 1;
		this.video = video;
		
		handler = MinecraftVideo.getInstance().getHandler();
		
		BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {
			@Override
			public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
				return new RV32BufferFormat(width, height);
			}
		};
		mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
			@Override
			protected String[] onGetMediaPlayerFactoryArgs() {
				return new String[] { "--no-audio" };
			}
			
			@Override
			protected RenderCallback onGetRenderCallback() {
				callback = new SimpleRenderCallback( width, height );
				return callback;
			}
			
			@Override
			public void playing(MediaPlayer mediaPlayer) {
				frame = 0;
				currentFrame = 0;
				instance.playing();
				callback.reset();
			}

			@Override
			public void finished( MediaPlayer player ) {
				clearBuffer();
				instance.finished();
			}
		};
		
		for ( int i = 0; i < threadCount; i++ ) {
			AsyncPacketSender thread = new AsyncPacketSender( this );
			threadpool.add( thread );
			thread.start();
		}
	}
	
	/**
	 * Called before the video starts playing
	 */
	public void playing() {
		System.out.println( "STARTED PLAYING" );
		// Temporary
		// Reset the background
		byte[] background = MinecraftVideo.BACKGROUND;
		if ( background == null || background.length != 128 * mapWidth * 128 * mapHeight ) {
			background = new byte[ 128 * mapWidth * 128 * mapHeight ];
		}
		handler.display( null, 0, mapWidth, mapHeight, background, 128 * mapWidth );
	}
	
	/**
	 * Called when the video is finished playing
	 */
	public void finished() {
		System.out.println( "STOPPED PLAYING" );
		// Temporary
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep( 1000 );
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}
				byte[] background = MinecraftVideo.BACKGROUND;
				if ( background == null || background.length != 128 * mapWidth * 128 * mapHeight ) {
					System.out.println( "Invalid background found!" );
					background = new byte[ 128 * mapWidth * 128 * mapHeight ];
				} else {
					System.out.println( "Displaying background..." );
				}
				handler.display( null, 0, mapWidth, mapHeight, background, 128 * mapWidth );
			}
		}.start();
	}
	
	public McVideo getVideo() {
		return video;
	}
	
	public void setMapId( int id ) {
		mapId = id;
	}
	
	public void setMapDimensions( int width, int height ) {
		mapWidth = width;
		mapHeight = height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getMapId() {
		return mapId;
	}
	
	public int getMapWidth() {
		return mapWidth;
	}
	
	public int getMapHeight() {
		return mapHeight;
	}
	
	public byte[] getBackground() {
		return background;
	}
	
	public void setBackground( byte[] buf ) {
		background = buf;
	}
	
	public DirectMediaPlayer getPlayer() {
		return mediaPlayerComponent.getMediaPlayer();
	}
	
	public void start() {
		stop();
		frame = 0;
		currentFrame = 0;
		callback.reset();
		mediaPlayerComponent.getMediaPlayer().setPlaySubItems( true );
		mediaPlayerComponent.getMediaPlayer().playMedia( video.getMrl() );
	}

	private void clearBuffer() {
		bufferLock.lock();
		buffer = null;
		bufferLock.unlock();
	}
	
	protected Object[] getBufferAndFrame() {
		bufferLock.lock();
		if ( buffer == null ) {
			bufferLock.unlock();
			return null;
		}
		int[] arr = buffer.clone();
		int count = frame++;
		bufferLock.unlock();
		return new Object[] { arr, count };
	}
	
	protected int getCurrentFrame() {
		bufferLock.lock();
		int val = currentFrame;
		bufferLock.unlock();
		return val;
	}
	
	protected void finishedFrame() {
		bufferLock.lock();
		currentFrame++;
		bufferLock.unlock();
	}
	
	public void terminate() {
		if ( mediaPlayerComponent.getMediaPlayer().isPlaying() ) {
			mediaPlayerComponent.getMediaPlayer().stop();
			finished();
		}
		mediaPlayerComponent.getMediaPlayer().release();
		for ( AsyncPacketSender sender : threadpool ) {
			sender.terminate();
		}
		clearBuffer();
	}
	
	public void stop() {
		if ( mediaPlayerComponent.getMediaPlayer().isPlaying() ) {
			mediaPlayerComponent.getMediaPlayer().stop();
		}
		clearBuffer();
	}
	
	public class SimpleRenderCallback extends RenderCallbackAdapter {
		boolean set = false;
		private SimpleRenderCallback( int width, int height ) {
			super( new int[ width * height ] );
		}

		protected void reset() {
			set = false;
		}

		@Override
		protected void onDisplay( DirectMediaPlayer mediaPlayer, int[] rgbBuffer ) {
			if ( !set ) {
				buffer = rgbBuffer;
				set = true;
			}
		}
	}
}
