/**
 * 
 */
package net.npe.dds.ddsutil;

/**
 * @author danielsenff
 *
 */
public class NonCubicDimensionException extends IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	public NonCubicDimensionException() {
		super("MipMaps can not be generated, The image dimensions must be a power of 2");
	}
	
}
