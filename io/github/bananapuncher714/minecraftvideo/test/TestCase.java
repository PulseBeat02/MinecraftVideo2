package io.github.bananapuncher714.minecraftvideo.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

public class TestCase {

	// 4:3
	private static final int width = 512;

	private static final int height = 384;

	// 16:9
//	private static final int width = 640;
//
//	private static final int height = 360;
	
	private final JFrame frame;

	private final JPanel videoSurface;

	private final BufferedImage image;

	private final DirectMediaPlayerComponent mediaPlayerComponent;

	private long lastUpdated = System.currentTimeMillis();
	
	public static void main( String[] args ) {
		new NativeDiscovery().discover();
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				new TestCase( new String[] { "https://www.youtube.com/watch?v=Nafii87gdzs" } );
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
		};
		mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
			@Override
			protected RenderCallback onGetRenderCallback() {
				return new TutorialRenderCallbackAdapter();
			}
		};
		frame.setVisible(true);
		mediaPlayerComponent.getMediaPlayer().setPlaySubItems( true );
		mediaPlayerComponent.getMediaPlayer().playMedia(args[0]);
//		mediaPlayerComponent.getMediaPlayer().playMedia( "https://www.youtube.com/watch?v=Nafii87gdzs" );
//		mediaPlayerComponent.getMediaPlayer().playMedia( "https://www.youtube.com/watch?v=TH0ZXfmjRqE" );
//		mediaPlayerComponent.getMediaPlayer().playMedia( "https://www.youtube.com/watch?v=Cyxixzi2dgQ" );
	}

	private class VideoSurfacePanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

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
		protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
			// Simply copy buffer to the image and repaint
//			image.setRGB( 0, 0, width, height, ImageUtil.dither( width, rgbBuffer ), 0, width );
			image.setRGB( 0, 0, width, height, rgbBuffer, 0, width );
			videoSurface.repaint();
		}
	}
}
