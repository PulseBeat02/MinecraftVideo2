package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.JetpImageUtil;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.WindowsVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

public class TestCase {

	// 4:3
//	private static final int width = 1024;
//	private static final int width = 640;
//	private static final int width = 512;
	
//	private static final int height = 768;
//	private static final int height = 480;
//	private static final int height = 384;

	// 16:9
	private static final int width = 1920;
//	private static final int width = 640;

	private static final int height = 1080;
//	private static final int height = 360;
	
	private final JFrame frame;

	private final JPanel videoSurface;

	private BufferedImage image;

	private EmbeddedMediaPlayer mediaPlayerComponent;
	
	private long lastUpdated = System.currentTimeMillis();
	
	public static void main( String[] args ) {
		final String url = "https://www.youtube.com/watch?v=nU21rCWkuJw";
//		final String url = "https://www.youtube.com/watch?v=xE7byQr1KyM";
		
		new NativeDiscovery().discover();
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				new TestCase( new String[] { url } );
			}
		});

	}

	public TestCase( String[] args ) {
		frame = new JFrame("Direct Media Player");
		frame.setBounds(100, 100, width, height);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		videoSurface = new VideoSurfacePanel();
		frame.setContentPane(videoSurface);
		image = GraphicsEnvironment
				.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice()
				.getDefaultConfiguration()
				.createCompatibleImage(width, height);
		BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {
			@Override
			public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
				return new RV32BufferFormat(width, height);
			}
			
			@Override
			public void allocatedBuffers(ByteBuffer[] buffers) {
			}
		};
		
		frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                System.exit(0);
            }
        });
		
    	MediaPlayerFactory factory = new MediaPlayerFactory();
    	mediaPlayerComponent = factory.mediaPlayers().newEmbeddedMediaPlayer();
    	
    	CallbackVideoSurface surface = new CallbackVideoSurface( bufferFormatCallback, new TutorialRenderCallbackAdapter(), false, new WindowsVideoSurfaceAdapter() );
    	
    	mediaPlayerComponent.videoSurface().set( surface );
    	
		frame.setVisible(true);
		
		mediaPlayerComponent.media().play( args[ 0 ] );
	}

	private class VideoSurfacePanel extends JPanel {

		private VideoSurfacePanel() {
			setBackground(Color.black);
			setOpaque(true);
			setPreferredSize(new Dimension(width, height));
			setMinimumSize(new Dimension(width, height));
			setMaximumSize(new Dimension(width, height));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			g2.drawImage(image, null, 0, 0);
			long value = System.currentTimeMillis() - lastUpdated;
			System.out.println( value );
			int frames = ( int ) ( 1000 / ( double ) value );
			g2.drawString( "FPS: " + frames, 10, 20 );
			lastUpdated = System.currentTimeMillis();
		}
	}

	private class TutorialRenderCallbackAdapter extends RenderCallbackAdapter {

		private TutorialRenderCallbackAdapter() {
			super(new int[width * height]);
		}

		@Override
		protected void onDisplay(MediaPlayer mediaPlayer, int[] rgbBuffer) {
			long time = System.currentTimeMillis();
			JetpImageUtil.dither( rgbBuffer, width );
			System.out.println( "dither time: " + ( System.currentTimeMillis() - time ) );
			// Wow is this line slow
			// Shouldn't even bother with BufferedImage
			image.setRGB( 0, 0, width, height, rgbBuffer, 0, width );
			videoSurface.repaint();
			
		}
	}
}
