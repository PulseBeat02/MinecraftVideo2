package io.github.bananapuncher714.minecraftvideo.util;

import java.awt.Image;
import java.awt.image.BufferedImage;

import org.bukkit.map.MapPalette;

public final class McImageUtil {
	private static int[] PALETTE;
	private static byte[] CACHE = new byte[ 256 * 256 * 256 ];
	private static final double[] MULTIPLIERS = { 0.4375D, 0.1875D, 0.3125D, 0.0625D };

	public final static void init() {
	}

	static {
		PALETTE = new int[ 128 ];
		
		fillColors();
//		grayscale();

		for ( int r = 0; r < 256; r += 16 ) {
			for ( int g = 0; g < 256; g += 16 ) {
				for ( int b = 0; b < 256; b += 16 ) {
					System.out.println( "Meshing " + r + ", " + g + ", " + b );
					getBestColor( ( r << 16 ) + ( g << 8 ) + b );
				}				
			}
		}
	}
	
	private static void fillColors() {
		for ( byte i = 4; i > -127; i++ ) {
			PALETTE[ i ] = MapPalette.getColor( i ).getRGB() & 0xffffff;
		}
	}
	
//	private static void grayscale() {
//		for ( byte i = 0; i > -127; i++ ) {
//			int color = MapPalette.getColor( i ).getRGB() & 0xffffff;
//			if ( color >> 16 == ( color >> 8 & 255 ) && color >> 16 == ( color & 255 ) ) {
//				PALETTE[ i ] = color;
//			}
//		}
//	}

	public static int getBestColor( int rgb ) {
		if ( CACHE[ rgb ] > 3 ) {
			return PALETTE[ CACHE[ rgb ] ];
		}
		byte val = 0;
		int best_color = 0;
		double best_distance = -1.0D;
		int red = rgb >> 16;
		int green = rgb >> 8& 0xff;
		int blue = rgb & 0xff;
		for ( byte index = 0; index > -127; index++ ) {
			int check_color = PALETTE[ index ];
			if ( check_color == 0 ) {
				continue;
			}
			double distance = getDistance( red, green, blue, check_color);
			if ((distance < best_distance) || (best_distance == -1.0D)) {
				best_distance = distance;
				best_color = check_color;
				val = index;
			}
		}
		CACHE[ best_color ] = val;
		return best_color;
	}

	private static double getDistance( int red, int green, int blue, int color_2 ) {
		int red2 = color_2 >> 16;
		double red_avg = ( red + red2 ) / 2.0D;
		int r = red - red2;
		int g = green - ( color_2 >> 8 & 255 );
		int b = blue - ( color_2 & 255 );
		double weight_red = 2.0D + red_avg / 256.0D;
//		double weight_green = 4.0D;
		double weight_blue = 2.0D + (255.0D - red_avg) / 256.0D;
		return weight_red * r * r + 4.0D * g * g + weight_blue * b * b;
	}

	public static byte[] simplify( int[] buffer ) {
		byte[] map = new byte[ buffer.length ];
		for ( int index = 0; index < buffer.length; index++ ) {
			map[ index ] = CACHE[ getBestColor( buffer[ index ] ) ];
		}
		return map;
	}

	public static byte[] dither( int width, int[] buffer ) {
		int height = buffer.length / width;
		int[][] dither_buffer = new int[2][Math.max(width, height) * 3];

		byte[] map = new byte[ buffer.length ];
		int[] y_temps = { 0, 1, 1, 1 };
		for (int x = 0; x < width; x++) {
			dither_buffer[0] = dither_buffer[1];
			dither_buffer[1] = new int[Math.max(width, height) * 3];
			for (int y = 0; y < height; y++) {
				int rgb = buffer[ y * width + x ] & 0xffffff;
				int red = rgb >> 16;
				int green = rgb >> 8 & 255;
				int blue = rgb & 255;
				red = Math.max(0, Math.min(red + dither_buffer[0][(y * 3)], 255));
				green = Math.max(0, Math.min(green + dither_buffer[0][(y * 3 + 1)], 255));
				blue = Math.max(0, Math.min(blue + dither_buffer[0][(y * 3 + 2)], 255));
				int matched_color = getBestColor( ( red << 16 ) + ( green << 8 ) + blue );
				int delta_r = red - ( matched_color >> 16 );
				int delta_g = green - ( matched_color >> 8 & 255 );
				int delta_b = blue - ( matched_color & 255 );
				int[] x_temps = { y + 1, y - 1, y, y + 1 };
				for (int i = 0; i < x_temps.length; i++) {
					int temp_y = y_temps[i];
					int temp_x = x_temps[i];
					if ((temp_y < height) && (temp_x < width) && (temp_x > 0)) {
						dither_buffer[temp_y][(temp_x * 3)] = ((int)(dither_buffer[temp_y][(temp_x * 3)] + MULTIPLIERS[i] * delta_r));

						dither_buffer[temp_y][(temp_x * 3 + 1)] = ((int)(dither_buffer[temp_y][(temp_x * 3 + 1)] + MULTIPLIERS[i] * delta_g));

						dither_buffer[temp_y][(temp_x * 3 + 2)] = ((int)(dither_buffer[temp_y][(temp_x * 3 + 2)] + MULTIPLIERS[i] * delta_b));
					}
				}
				map[ y * width + x ] = CACHE[ matched_color ];
			}
		}
		return map;
	}
	
	public static byte[] convert( Image input, int width, int height ) {
		BufferedImage image = ImageUtil.toBufferedImage( input.getScaledInstance( width, height, Image.SCALE_SMOOTH ) );
		int[] imageArr = new int[ width * height ];
		for ( int w = 0; w < width; w++ ) {
			for ( int h = 0; h < height; h++ ) {
				imageArr[ h * width + w ] = image.getRGB( w, h ) & 0xffffff;
			}
		}
		return McImageUtil.dither( image.getWidth(), imageArr );
	}
}
