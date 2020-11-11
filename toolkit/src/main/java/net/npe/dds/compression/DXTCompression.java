/**
 * 
 */
package net.npe.dds.compression;

import gr.zdimensions.jsquish.Squish;

import java.awt.Dimension;
import java.nio.ByteBuffer;


/**
 * @author danielsenff
 *
 */
public class DXTCompression {

	

	
	/**
	 * Return the length of the required {@link ByteBuffer} for the image
	 * @param width
	 * @param height
	 * @param type
	 * @return
	 */
	public static int getStorageRequirements(final int width, final int height, final Squish.CompressionType type) {
		return Squish.getStorageRequirements(width, height, type);
	}
	
	/**
	 * Return the length of the required {@link ByteBuffer} for the image
	 * @param imageDimension
	 * @param type
	 * @return
	 */
	public static int getStorageRequirements(final Dimension imageDimension, final Squish.CompressionType type) {
		return Squish.getStorageRequirements((int)imageDimension.getWidth(), (int)imageDimension.getHeight(), type);
	}
	
	
	


	
	

	/**
	 * Compresses the RGBA-byte-array into a DXT-compressed byte-array.
	 * @param rgba
	 * @param height
	 * @param width
	 * @param compressionType
	 * @return
	 */
	public static byte[] squishCompressToArray(final byte[] rgba, 
			final int width ,final int height, 
			final Squish.CompressionType compressionType) {
		
		int storageRequirements = Squish.getStorageRequirements(width, height, compressionType);
		
		return Squish.compressImage(rgba, 
				width, 
				height, 
				new byte[storageRequirements], 
				compressionType, 
				Squish.CompressionMethod.CLUSTER_FIT);
	} 
	
	/**
	 * Compresses the RGBA-byte-array into a DXT-compressed {@link ByteBuffer}.
	 * @param rgba
	 * @param height
	 * @param width
	 * @param compressionType
	 * @return
	 */
	public static ByteBuffer squishCompress(final byte[] rgba, 
			final int width ,final int height, final Squish.CompressionType compressionType) {
		
		int storageRequirements = Squish.getStorageRequirements(width, height, compressionType);
		ByteBuffer buffer = ByteBuffer.allocateDirect(storageRequirements); 
		buffer.put(Squish.compressImage(rgba, 
				width, 
				height, 
				new byte[storageRequirements], 
				compressionType, 
				Squish.CompressionMethod.CLUSTER_FIT));
		buffer.rewind();
		return buffer;
	}
	
	/**
	 * @param bytebuffer
	 * @param width
	 * @param height
	 * @param compressionType
	 * @return
	 */
	public static ByteBuffer squishCompress(final ByteBuffer bytebuffer, 
			final int width ,final int height, final Squish.CompressionType compressionType) {
		
		byte[] rgba = new byte[bytebuffer.capacity()]; 
		bytebuffer.get(rgba);
		return squishCompress(rgba, width, height, compressionType);
	}
	
}
