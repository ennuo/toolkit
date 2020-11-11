/**
 * 
 */
package net.npe.dds.compression;

import gr.zdimensions.jsquish.Squish;
import gr.zdimensions.jsquish.Squish.CompressionType;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import net.npe.dds.ddsutil.ByteBufferedImage;


/**
 * Compressor for DXT-Compression
 * @author danielsenff
 *
 */
public class DXTBufferCompressor {

//	byte[] compressedData;
	protected byte[] byteData;
	protected int[] intData;
	protected Dimension dimension;
	protected CompressionType compressionType;
	
	/**
	 * @param data Byte-Array should store ARGB
	 * @param width
	 * @param height
	 * @param compressionType
	 */
	public DXTBufferCompressor(final byte[] data, 
			final int width, 
			final int height, 
			final Squish.CompressionType compressionType) {
		this(data, new Dimension(width, height), compressionType);
	}
	
	/**
	 * @param byteBuffer ByteBuffer should store ARGB
	 * @param width
	 * @param height
	 * @param compressionType
	 */
	public DXTBufferCompressor(final ByteBuffer byteBuffer, 
			final int width, 
			final int height, 
			final Squish.CompressionType compressionType) {
		this(toByteArray(byteBuffer), 
				new Dimension(width, height), 
				compressionType);
	}
	
	
	/**
	 * @param image
	 * @param compressionType
	 */
	public DXTBufferCompressor(final BufferedImage image, 
			final Squish.CompressionType compressionType) {
		
		this(ByteBufferedImage.convertBIintoARGBArray((BufferedImage) image),
				new Dimension(image.getWidth(null), image.getHeight(null)),
				compressionType	);
	}

	/**
	 * @param data Byte-Array should store ARGB
	 * @param dimension
	 * @param compressionType
	 */
	public DXTBufferCompressor(final byte[] data, 
			final Dimension dimension, 
			final Squish.CompressionType compressionType) {
		this.byteData = data;
		this.dimension = dimension;
		this.compressionType = compressionType;
	}
	
	/**
	 * @param data
	 * @param dimension
	 * @param compressionType
	 */
	public DXTBufferCompressor(final int[] data, 
			final Dimension dimension, 
			final Squish.CompressionType compressionType) {
		this.intData = data;
		this.dimension = dimension;
		this.compressionType = compressionType;
	}

	
	/**
	 * @return ByteBuffer
	 */
	public ByteBuffer getByteBuffer() {
		byte[] compressedData;
		try {
			
			// the data-Array given to the squishCompressToArray is expected to be
			// width * height * 4 -> with RGBA, which means, if we got RGB, we need to add A!
			if(byteData.length < dimension.height*dimension.width*4) {
				System.out.println("blow up array from RGB to ARGB");
				byteData = convertRGBArraytiRGBAArray(byteData, dimension);
			}
			
			compressedData = squishCompressToArray(byteData, dimension.width, dimension.height, compressionType);
			return ByteBuffer.wrap(compressedData);
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	private byte[] convertRGBArraytiRGBAArray(byte[] data, final Dimension dimension) {
		
		int rgbLength = data.length;
		int rgbaLength = dimension.width * dimension.height * 4;
		
		byte[] rgbaBuffer = new byte[rgbaLength];
		
		// populate new array
		// we always copy 3 byte chunks, skip one byte, which we set to 255 and take the next 3 byte
		int loopN = 0;
		for (int i = 0; i < rgbLength; i=i+3) {
			
			int srcPos = i; 
			int destPos = i+loopN;
			
			System.arraycopy(data, srcPos, rgbaBuffer, destPos, 3);
			loopN++;
		}
		
		
		return rgbaBuffer;
	}

	/**
	 * Get the Byte-array held by this object.
	 * @return
	 */
	public byte[] getArray() {
		try {
			return squishCompressToArray(byteData, dimension.width, dimension.height, compressionType);
		} catch (final DataFormatException e) {
			e.printStackTrace();
		}
		return byteData;
	}

	/**
	 * Compresses the RGBA-byte-array into a DXT-compressed {@link ByteBuffer}.
	 * @param rgba
	 * @param height
	 * @param width
	 * @param compressionType
	 * @return
	 */
//	private static ByteBuffer squishCompress(final byte[] rgba, 
//			final int width, 
//			final int height, 
//			final Squish.CompressionType compressionType) {
//		
//		
//		ByteBuffer buffer = ByteBuffer.wrap(squishCompressToArray(rgba, width, height, compressionType));
//		return buffer;
//	}


	/**
	 * Compresses the RGBA-byte-array into a DXT-compressed byte-array.
	 * @param rgba Byte-Array needs to be in RGBA-order
	 * @param height
	 * @param width
	 * @param compressionType
	 * @return
	 * @throws DataFormatException 
	 */
	private static byte[] squishCompressToArray(final byte[] rgba, 
			final int width, 
			final int height, 
			final Squish.CompressionType compressionType) throws DataFormatException {
		
		// expected array length
		int length = width * height * 4;
		if (rgba.length != length) throw new DataFormatException("unexpected length:" + 
				rgba.length +  " instead of "+ length);
		
		int storageRequirements = Squish.getStorageRequirements(width, height, compressionType);
		
		return Squish.compressImage(rgba, 
				width, 
				height, 
				new byte[storageRequirements], 
				compressionType, 
				Squish.CompressionMethod.CLUSTER_FIT);
	} 
	
	private static byte[] squishCompressToArray(final int[] rgba, 
			final int width, 
			final int height, 
			final Squish.CompressionType compressionType) throws DataFormatException {
		
		// expected array length
		int length = width * height;
		if (rgba.length != length) throw new DataFormatException("unexpected length:" + 
				rgba.length +  " instead of "+ length);
		
		int storageRequirements = Squish.getStorageRequirements(width, height, compressionType);
		
		return Squish.compressImage(rgba, 
				width, 
				height, 
				new byte[storageRequirements], 
				compressionType, 
				Squish.CompressionMethod.CLUSTER_FIT);
	} 
	
	
	/**
	 * Compresses a {@link ByteBuffer} into a DXT-compressed {@link ByteBuffer}
	 * @param buffer
	 * @param width
	 * @param height
	 * @param compressionType
	 * @return
	 */
//	private static ByteBuffer squishCompress(final ByteBuffer bytebuffer, 
//			final int width ,final int height, final Squish.CompressionType compressionType) {
//		
//		//byte[] rgba = toByteArray(bytebuffer);
//		return squishCompress(byteBuffer, width, height, compressionType);
//	}

	private static byte[] toByteArray(final ByteBuffer bytebuffer) {
		byte[] rgba = new byte[bytebuffer.capacity()];
		bytebuffer.get(rgba);
		return rgba;
	}
	
	/**
	 * @return
	 */
	public int getStorageRequirements() {
		return getStorageRequirements(dimension, compressionType);
		
	}
	
	/**
	 * Return the length of the required {@link ByteBuffer} for the image
	 * @param width
	 * @param height
	 * @param type
	 * @return
	 */
	public static int getStorageRequirements(final int width, final int height, 
			final Squish.CompressionType type) {
		return Squish.getStorageRequirements(width, height, type);
	}
	
	/**
	 * Return the length of the required {@link ByteBuffer} for the image
	 * @param imageDimension
	 * @param type
	 * @return
	 */
	public static int getStorageRequirements(final Dimension imageDimension, 
			final Squish.CompressionType type) {
		return Squish.getStorageRequirements((int)imageDimension.getWidth(), (int)imageDimension.getHeight(), type);
	}
	
}
