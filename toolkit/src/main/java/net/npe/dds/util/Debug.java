/**
 * 
 */
package net.npe.dds.util;

/**
 * @author danielsenff
 *
 */
public class Debug {


	/**
	 * Quick debug output for X- and Y-values
	 * @param rgb
	 */
	@SuppressWarnings("unused")
	private static void sysoXY(final int x, final int y) {
		System.out.println("x: "+ x +" y: "+ y);
	}
	
	/**
	 * Quick debug output for RGBA-Arrays.
	 * @param rgb
	 */
	public static void sysoRGBA(int[] rgb) {
		System.out.println("R: " + rgb[0]  + " G "+ rgb[1] +" B "+ rgb[2] +" A "+ rgb[3]);
	}
	
	/**
	 * Quick debug output for RGB-Arrays.
	 * @param rgb
	 */
	public static void sysoRGBA(byte[] rgb) {
		System.out.println("R: " + rgb[0]  + " G "+ rgb[1] +" B "+ rgb[2] +" A "+ rgb[3]);
	}
	
}
