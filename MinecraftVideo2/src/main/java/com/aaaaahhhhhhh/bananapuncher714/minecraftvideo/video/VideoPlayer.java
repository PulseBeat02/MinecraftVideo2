package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.video;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.WindowsVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

public class VideoPlayer {
	String url;
	int width;
	int height;
	Consumer< int[] > callback;
	
	private EmbeddedMediaPlayer mediaPlayerComponent;
	
	public VideoPlayer( String url, int width, int height, Consumer< int[] > callback ) {
		this.url = url;
		this.width = width;
		this.height = height;
		this.callback = callback;
	}
	
	public String getUrl() {
		return url;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public void start() {
		if ( mediaPlayerComponent != null ) {
			mediaPlayerComponent.release();
		}
		
		mediaPlayerComponent = new uk.co.caprica.vlcj.factory.MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
		
		BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {
			@Override
			public BufferFormat getBufferFormat( int sourceWidth, int sourceHeight ) {
				return new RV32BufferFormat( width, height );
			}

			@Override
			public void allocatedBuffers( ByteBuffer[] buffers ) {
			}
		};
		
		CallbackVideoSurface surface = new CallbackVideoSurface( bufferFormatCallback, new MinecraftRenderCallback(), false, new WindowsVideoSurfaceAdapter() );
		mediaPlayerComponent.videoSurface().set( surface );
		
		
		
		mediaPlayerComponent.media().play( url );
	}
	
	public void pause() {
		if ( mediaPlayerComponent != null ) {
			mediaPlayerComponent.controls().pause();
		}
	}
	
	public void resume() {
		if ( mediaPlayerComponent != null ) {
			mediaPlayerComponent.controls().start();
		}
	}
	
	public void stop() {
		if ( mediaPlayerComponent != null ) {
			mediaPlayerComponent.controls().stop();
		}
	}
	
	public void release() {
		if ( mediaPlayerComponent != null ) {
			mediaPlayerComponent.release();
			mediaPlayerComponent = null;
		}
	}
	
	private class MinecraftRenderCallback extends RenderCallbackAdapter {
		private MinecraftRenderCallback() {
			super( new int[ width * height ] );
		}
		
		@Override
		protected void onDisplay( uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, int[] buffer ) {
			callback.accept( buffer );
		}
	}
}