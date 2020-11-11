package net.npe.dds.ddsutil;


import gr.zdimensions.jsquish.Squish;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileLockInterruptionException;

import net.npe.dds.jogl.DDSImage;
import net.npe.dds.jogl.TEXImage;
import net.npe.dds.model.TextureMap;
import net.npe.dds.util.FileUtil;
import net.npe.dds.util.ImageUtils;

import net.npe.dds.compression.DXTBufferCompressor;
import net.npe.dds.compression.DXTBufferDecompressor;



/**
 * Easy loading, saving and manipulation of DDSImages and DXT-Compression.
 * 
 * @author danielsenff
 *
 */
public class DDSUtil {

	
	/**
	 * Topmost MipMap Index 
	 */
	public static final int TOP_MOST_MIP_MAP = 0;

	private DDSUtil() {	}

	@Deprecated
	public static BufferedImage decompressTexture(final File file) throws IOException {
		return read(file);
	}
	
	/**
	 * Create a {@link BufferedImage} from a DXT-compressed 
	 * dds-texture {@link FileLockInterruptionException}.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage read(final File file) throws IOException {
		if(file.getName().endsWith(".dds"))
			return loadBufferedImage(DDSImage.read(file));
		else
			return loadBufferedImage(TEXImage.read(file));
	}
	
	/**
	 * Create a {@link BufferedImage} from a DXT-compressed {@link DDSImage}
	 * @param image
	 * @return
	 * @throws UnsupportedDataTypeException 
	 */
	public static BufferedImage loadBufferedImage (final DDSImage image) throws IOException {
		if (image.isCompressed())
			return decompressTexture(
				image.getMipMap(0).getData(), 
				image.getWidth(), 
				image.getHeight(), 
				findCompressionFormat(image));
		else
			return loadBufferedImageFromByteBuffer(
				image.getMipMap(0).getData(), 
				image.getWidth(), 
				image.getHeight(), 
				image);
	}
	
	/**
	 * @param data
	 * @param width
	 * @param height
	 * @param ddsimage
	 * @return
	 */
	public static BufferedImage loadBufferedImageFromByteBuffer(
			ByteBuffer data, int width, int height,
			DDSImage ddsimage) {
		
		// check pixelformat
		if(ddsimage.getPixelFormat() == DDSImage.D3DFMT_A8R8G8B8) {
			// data in buffer in 4 byte chunks ordered ARGB
			ByteBufferedImage bi = new ByteBufferedImage(width, height, data);
			
		} else if(ddsimage.getPixelFormat() == DDSImage.D3DFMT_A8R8G8B8) {
			
			
		}
		
		
		// if weird or unknown check RGB-offsets
		
		
		return null;
	}

	/**
	 * Create a {@link BufferedImage} from a DXT-compressed {@link DDSImage}
	 * @param image
	 * @return
	 * @throws UnsupportedDataTypeException 
	 */
	@Deprecated
	public static BufferedImage decompressTexture (final DDSImage image) throws IOException {
		return loadBufferedImage(image);
	}
	
	/**
	 * Create a {@link BufferedImage} from a DXT-compressed {@link TEXImage}
	 * @param image
	 * @return
	 * @throws UnsupportedDataTypeException 
	 */
	public static BufferedImage loadBufferedImage(final TEXImage image) throws IOException {
		return decompressTexture(
				image.getEmbeddedMaps(0).getMipMap(0).getData(), 
				image.getWidth(), 
				image.getHeight(), 
				findCompressionFormat(image.getEmbeddedMaps(0)));
	}
	
	/**
	 * Create a {@link BufferedImage} from a DXT-compressed {@link TEXImage}
	 * @param image
	 * @return
	 * @throws UnsupportedDataTypeException 
	 */
	@Deprecated
	public static BufferedImage decompressTexture (final TEXImage image) throws IOException {
		return loadBufferedImage(image);
	}
	
	/**
	 * Create a {@link BufferedImage} from a DXT-compressed Byte-array.
	 * @param compressedData
	 * @param width
	 * @param height
	 * @param compressionType
	 * @return
	 */
	public static BufferedImage decompressTexture(final byte[] compressedData, 
			final int width, 
			final int height, 
			final Squish.CompressionType compressionType) {
		return new DXTBufferDecompressor(compressedData, width, height, compressionType).getImage();
	}

	/**
	 * Create a {@link BufferedImage} from a DXT-compressed ByteBuffer.
	 * @param textureBuffer
	 * @param width
	 * @param height
	 * @param compressionType
	 * @return
	 */
	public static BufferedImage decompressTexture(final ByteBuffer textureBuffer, 
			final int width, 
			final int height, 
			final Squish.CompressionType compressionType) {
		return new DXTBufferDecompressor(textureBuffer, width, height, compressionType).getImage();
	}
	
	/**
	 * Create a {@link BufferedImage} from a DXT-compressed ByteBuffer.
	 * @param textureBuffer
	 * @param width
	 * @param height
	 * @param pixelformat
	 * @return
	 * @throws UnsupportedDataTypeException 
	 */
	public static BufferedImage decompressTexture(final ByteBuffer textureBuffer, 
			final int width, 
			final int height, 
			final int pixelformat) throws IOException {
		Squish.CompressionType compressionType = PixelFormats.getSquishCompressionFormat(pixelformat); 
		return new DXTBufferDecompressor(textureBuffer, width, height, compressionType).getImage();
	}
	
	/**
	 * Compresses a {@link BufferedImage} into a {@link ByteBuffer}
	 * @param image
	 * @param compressionType
	 * @return
	 */
	public static ByteBuffer compressTexture(final BufferedImage image, 
			final Squish.CompressionType compressionType) {
		return new DXTBufferCompressor(image, compressionType).getByteBuffer();
	}
	
	/**
	 * @param image
	 * @param compressionType
	 * @return
	 */
	public static byte[] compressTextureToArray(final BufferedImage image, 
			final Squish.CompressionType compressionType) {
		return new DXTBufferCompressor(image, compressionType).getArray();
	}
	
	/**
	 * Writes a DDS-Image file to disk.
	 * @param destinationfile 
	 * @param sourceImage 
	 * @param pixelformat 
	 * @param generateMipMaps 
	 * @throws IOException 
	 * 
	 */
	public static void write(final File destinationfile, 
			BufferedImage sourceImage, 
			final int pixelformat,
			boolean generateMipMaps) throws IOException {
		
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		
		//convert RGB to RGBA image
		if(!sourceImage.getColorModel().hasAlpha()) 
			sourceImage = ImageUtils.convert(sourceImage, BufferedImage.TYPE_4BYTE_ABGR);
		
		TextureMap maps = TextureFactory.createTextureMap(generateMipMaps, sourceImage);
		
		ByteBuffer[] mipmapBuffer = null;
		
		if (PixelFormats.isDXTCompressed(pixelformat)) {
			mipmapBuffer = maps.getDXTCompressedBuffer(pixelformat);
		} else 
			mipmapBuffer = maps.getUncompressedBuffer();
		
		writeDDSImage(destinationfile, mipmapBuffer, width, height, pixelformat);
	}
	
	/**
	 * TODO: what about DXT1?
	 * @param file 
	 * @param map 
	 * @param pixelformat 
	 * @throws IOException 
	 * 
	 */
	public void write(final File file, 
			final TextureMap map, 
			final int pixelformat) throws IOException {
		writeDDSImage(file, map.getDXTCompressedBuffer(pixelformat), 
				map.getWidth(), 
				map.getHeight(), 
				pixelformat);
	}

	private static DDSImage writeDDSImage(final File file,
			final ByteBuffer[] mipmapBuffer, 
			final int width, 
			final int height,
			final int pixelformat) throws IllegalArgumentException, IOException {
		
		DDSImage writedds = DDSImage.createFromData(pixelformat, width, height, mipmapBuffer);
//		writedds.debugPrint();
		writedds.write(file);
		return writedds;
	}
	
	/**
	 * Returns the PixelFormat of a {@link File}
	 * This makes file-IO, therefor handle with caution!
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static int getCompressionType(final File file) throws IOException {
		return DDSImage.read(file).getPixelFormat();	
	}
	
	private static Squish.CompressionType findCompressionFormat(DDSImage ddsimage) throws IOException {
		int pixelFormat = ddsimage.getPixelFormat();
		return PixelFormats.getSquishCompressionFormat(pixelFormat);
	}

	/**
	 * Returns true for file formats supported by this library.
	 * Currently TEX and DDS reading is supported.
	 * @param file
	 * @return
	 */
	public static boolean isReadSupported(final File file) {
		String fileSuffix = FileUtil.getFileSuffix(file);
		return fileSuffix.endsWith("dds")
			|| fileSuffix.endsWith("tex");
	}
}
