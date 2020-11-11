/**
 * 
 */
package net.npe.dds.ddsutil;

import java.awt.Dimension;


/**
 * Some helper methods for MipMap generation
 * @author danielsenff
 *
 */
public class MipMapsUtil {

	/**
	 * Topmost MipMap Index 
	 */
	public static final int TOP_MOST_MIP_MAP = 0;

	/**
	 * Number of MipMaps that will be generated from this image sizes.
	 * @param width
	 * @param height
	 * @return
	 */
	public static int calculateMaxNumberOfMipMaps(final int width, final int height) {
		return ((int) Math.floor(Math.log(Math.max(width, height)) / Math.log(2.0)))+1; // plus original
	}

	/**
	 * Number of MipMaps that will be generated from this image dimension.
	 * @param dimension
	 * @return
	 */
	public static int calculateMaxNumberOfMipMaps(final Dimension dimension) {
		return calculateMaxNumberOfMipMaps(dimension.width, dimension.height);
	}

	/**
	 * Checks if a value is a power of two
	 * @param value
	 * @return
	 */
	public static boolean isPowerOfTwo(final int value) {
		double p = Math.floor(Math.log(value) / Math.log(2.0));
		double n = Math.pow(2.0, p);
	    return (n==value);
	}

}
