package io.github.bananapuncher714.minecraftvideo.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
//import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class DitherTest {
	private static final int width = 640;
	private static final int height = 480;
	
	private final JFrame frame;

	private final JPanel videoSurface;
	
	public static void main( String[] args ) {
//		int trials = 1000;
//		int passes = 0;
//		for ( int i = 0; i < trials; i++ ) {
//			int color = ThreadLocalRandom.current().nextInt() & 0xffffff;
//			int color1 = McImageUtil.getBestColor( color );
//			int color2 = MapColor.getColor( JetpImageUtil.getBestColor( color >> 16, color >> 8 & 0xff, color & 0xff ) ).getRGB() & 0xffffff;
//			
//			if ( color1 != color2 ) {
//				System.out.println( "Failed at " + color + ": " + color1 + "" + color2 );
//			} else {
//				passes++;
//			}
//		}
//		System.out.println( "Passed " + passes + "/" + trials );
		
		// File base = new File( System.getProperty( "user.dir" ) + "/image.png" );
		
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				new DitherTest();
			}
		});
	}
	
	public DitherTest() {
		frame = new JFrame("Test");
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
	}
}
