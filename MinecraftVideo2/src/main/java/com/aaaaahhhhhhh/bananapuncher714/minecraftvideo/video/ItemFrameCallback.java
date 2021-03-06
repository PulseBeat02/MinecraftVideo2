package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.video;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.JetpImageUtil;
import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.MinecraftVideo;

public class ItemFrameCallback {
	
	UUID[] viewers;
	
	int map;
	int width;
	int height;
	int videoWidth;
	int delay;
	int iterations;
	
	long sum;
	long lastUpdated;
	
	public ItemFrameCallback( UUID[] viewers, int map, int width, int height, int videoWidth, int delay ) {
		this.viewers = viewers;
		this.map = map;
		this.width = width;
		this.height = height;
		this.videoWidth = videoWidth;
		this.delay = delay;
		this.iterations = 0;
		this.sum = 0;
	}

	public UUID[] getViewers() {
		return viewers;
	}
	
	public void setMap( int map ) {
		this.map = map;
	}

	public void setWidth( int width ) {
		this.width = width;
	}

	public void setHeight( int height ) {
		this.height = height;
	}

	public int getMap() {
		return map;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getDelay() {
		return delay;
	}

	public void send( int[] data ) {
		
		long time = System.currentTimeMillis();
		long difference = time - lastUpdated;
		
		iterations++;
		sum += difference;
		
		if (iterations == 10) {
			
			System.out.println("Average Time Space (10 Iterations): " + sum/10);
			iterations = 0;
			sum = 0;
			
		}

		if ( difference >= delay ) {
			lastUpdated = time;
			ByteBuffer dithered = JetpImageUtil.ditherIntoMinecraft( data, videoWidth );
			MinecraftVideo.getInstance().getHandler().display( viewers, map, width, height, dithered, videoWidth );
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
