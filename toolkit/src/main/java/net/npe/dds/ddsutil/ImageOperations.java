package net.npe.dds.ddsutil;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.Buffer;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;


/**
 * General image functions for handling Images, BufferedImages and Arrays
 * @author danielsenff
 *
 */
public class ImageOperations {

	/**
	 * Possible Display modes to select a specific channel. 
	 * So far only RGB is supported.
	 */
	public enum ChannelMode {	
	/**
	 * Display RGB with screen-overlayed Alpha-Channel. 
	 */	RGBA, 
	/**
	 * RGB without Alpha
	 */	RGB, 
	/**
	 * Red color channel
	 */	RED, 
	/**
	 * Green color channel
	 */	GREEN, 
	/**
	 * Blue color channel
	 */ BLUE, 
	 /**
	 * Alpha color channel
	 */	 ALPHA }
	
	
	private ImageOperations() {}
	
	/**
	 * Reads RGB colors from a single int color value.
	 * @param c int with 3 colors included
	 * @return int-array with RGB-values
	 */
	public static int[] readPixelRGB(final int c) {
		int[] color = { 
					255,
					(c & 0x00ff0000) >> 16, 
					(c & 0x0000ff00) >> 8, 
					(c & 0x000000ff)};
		return color;
	}
	
	
	/**
	 * Reads ARGB colors from a single int color value.
	 * @param c int with 4 colors included
	 * @return int-array with ARGB-values
	 */
	public static int[] readPixelARGB(final int c) {
		// this is dirty I think, I got the problem, that the a- value gets -1, instead of 255
		// so I use this method to get a positive value instead ...
		int a = unsignedByteToInt((byte) ((c & 0xff000000) >> 24));
//		System.out.println(a);
//		System.out.println((c & 0xff000000) >> 24);
		int[] color = { 
					a, 						//a
					(c & 0x00ff0000) >> 16, //r
					(c & 0x0000ff00) >> 8, 	//g
					(c & 0x000000ff)}; 		//b
		return color;
	}
	
	
	/**
	 * Write single color values into one int by byte-shifting. For RGB-pixelformat.
	 * Returns an 32byte integer with 255 alpha
	 * @param r
	 * @param b
	 * @param g
	 * @return
	 */
	public static int writePixelRGB(final int r, final int g, final int b) {
		return 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
	}
	
	public static int writePixelRGB(final int[] color) {
		return writePixelRGB(color[0], color[1], color[2]);
	}
	
	/**
	 * Write single color values into one int by byte-shifting. For ARGB-pixelsformat
	 * @param a
	 * @param r
	 * @param b
	 * @param g
	 * @return
	 */
	public static int writePixelARGB(final int a, final int r, final int g, final int b) {
		return 0x00000000 + ((a & 0xff) << 24) + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
	}
	
	/**
	 * @param color
	 * @return
	 */
	public static int writePixelARGB(final int[] color) {
		return writePixelARGB(color[0], color[1], color[2], color[3]);
	}
	
	/**
	 * Cast a Byte into an unsigned Integer
	 * @param b
	 * @return unsigned Integer
	 */
	public static int unsignedByteToInt(final byte b) {
		return (int) b & 0xFF;
    }
	
	
	
		
	/**
	 * Paint Channel in a specified value (0-255)
	 * Channel RGBA (0,1,2,3)
	 * This is for {@link BufferedImage}
	 * @param bi
	 * @param channel
	 * @param color
	 * @return
	 */
	public static byte[] paintValueInChannel(final BufferedImage bi, final int channel, final float color) {
		return paintChannelInValue(ByteBufferedImage.convertBIintoARGBArray(bi), 
				bi.getWidth(), 
				bi.getHeight(), 
				channel, color);
	}
	
	/**
	 * Paint a specific RGB-color channel in a color
	 * Channel RGBA (0,1,2,3)
	 * This is for {@link ByteBuffer}
	 * @param bytebuffer 
	 * @param width
	 * @param height
	 * @param channel
	 * @param greyValue
	 * @return
	 */
	public static byte[] paintValueInChannel(final ByteBuffer bytebuffer, 
			final int width, final int height, final int channel, final float greyValue) {
		byte[] data = new byte[bytebuffer.capacity()];
		bytebuffer.get(data);
		                   
		return paintChannelInValue(data, 
				width, 
				height, 
				channel, greyValue);
	}
	
	
	/**
	 * Paint Channel in a specified value (0-255)
	 * Channel RGBA (0,1,2,3)
	 * @param rgba
	 * @param width 
	 * @param height 
	 * @param channel to paint in
	 * @param color
	 * @return
	 */
	public static byte[] paintChannelInValue(final byte[] rgba, 
			final int width, final int height, final int channel, final float color) {
		
		int limit =  width * height * 4;
		
		for (int pos = 0; pos < (limit)-3; pos = pos + 4) {
			rgba[pos + channel] = (byte) convertColor(color);
		}
		
		return rgba;
	}
	
	

	/**
	 * Conversion between float color value to an integer value 
	 * @param value color
	 * @return
	 */
	public static int convertColor(final float value) {
		return (int) (255*value);
	}
	
	/**
	 * Conversion between double color value to a integer value 
	 * @param value color
	 * @return
	 */
	public static int convertColor(final double value) {
		return (int) (255*value);
	}
	
	/**
	 * Checks a color array if all values are within the possible range of values.
	 * If the limits are exceeded, the value is set to equal the limit. 
	 * @param color
	 * @param lowerLimit Upper limit of values
	 * @param upperLimit Lower limit of values
	 * @return
	 */
	public static int [] limitColorBoundaries(int[] color, final int lowerLimit, final int upperLimit) {
		for (int i = 0; i < color.length; i++) {
			color[i] = checkValueLimits(color[i], lowerLimit, upperLimit);
		}
		return color;
	}
	
	/**
	 * Checks if the value is within defined limits.
	 * @param value
	 * @param lowerLimit
	 * @param upperLimit
	 * @return
	 */
	public static int checkValueLimits(int value, final int lowerLimit, final int upperLimit) {
		if (value < lowerLimit) { 
			return lowerLimit;
		} else if (value > upperLimit) { 
			return upperLimit;
		} else {
			return value;
		}
	}
		
}
