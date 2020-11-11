package net.npe.dds.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public interface TextureImage {

	/**
	 * TextureType describes what kind of Texture the DDS is. Regular 2D-Texture, Volume or Cubemap.
	 *
	 */
	public enum TextureType {
		/**
		 * Regular texture (plus MipMaps) with one slice.
		 */
		TEXTURE, 
		/**
		 * Cubemaps contain 6 slices (including MipMaps) for 6 sides of a cube.
		 */
		CUBEMAP, 
		/**
		 * Volume-textures contain many slices (including MipMaps).
		 */
		VOLUME
	}
	
	/**
	 * Topmost MipMap Index 
	 */
	public static final int TOP_MOST_MIP_MAP = 0;
	
	/**
	 * Pixelformat describes the way pixels are stored in the DDS.
	 * Either uncompressed or with a special compression format.
	 */
	public enum PixelFormat {
		DXT5, DXT4, DXT3, DXT2, DXT1,
		A8R8G8B8, X8R8G8B8, R8G8B8, 
		A1R5G5B5, R5G6B5, Unknown
	}
	
	/**
	 * Width of the topmost MipMap
	 * @return
	 */
	public int getHeight();

	/**
	 * Height of the topmost MipMap
	 * @return
	 */
	public int getWidth();
	

	/**
	 * Get the Format in which pixel are stored in the file as internal stored Integer-value.
	 * @return in
	 */
	public int getPixelformat();
	
	/**
	 * Sets the format in which pixel are stored in the file.
	 * @param pixelformat
	 */
	public void setPixelformat(final int pixelformat);
	
	/**
	 * Sets the format in which pixel are stored in the file.
	 * @param pixelformat
	 */
	public void setPixelformat(final PixelFormat pixelformat);

	/**
	 * Gets the format in which pixels are stored as a verbose {@link String}.
	 * @return
	 */
	public String getPixelformatVerbose();
	
	/**
	 * Returns true if the dds-file is compressed as DXT1-5
	 * @return boolean
	 */
	public boolean isCompressed();
	
	/**
	 * Depth of color for all channels
	 * @return int
	 */
	public int getDepth();
	
	/**
	 * Depth of color of each channel
	 * @return int
	 */
	public int getChannelDepth();

	/**
	 * Returns the absolute path to the {@link File}.
	 * @return
	 */
	public String getAbsolutePath();

	
	/**
	 * Returns the associated {@link File}
	 * @return File
	 */
	public File getFile();
	
	/**
	 * Returns whether or not the dds-file has MipMaps.
	 * Usually only textures whose size is a power of two may have mipmaps.
	 * @return boolean
	 */
	public boolean hasMipMaps();
	
	/**
	 * Activates the generation of MipMaps when saving the DDS to disc.
	 * @param generateMipMaps 
	 * @throws IllegalArgumentException 
	 */
	public void setHasMipMaps(final boolean generateMipMaps) throws IllegalArgumentException;
	
	/**
	 * Returns the number of MipMaps in this file.
	 * @return int Number of MipMaps
	 */
	public int getNumMipMaps();
	
	/**
	 * The DDS-Image can have different texture types. 
	 * Regular Texture, Volume-Texture and CubeMap
	 * @return TextureType Type of Texture
	 */
	public TextureType getTextureType();
	
	/**
	 * Write to disc
	 * @throws IOException
	 */
	public void write() throws IOException;
	
	/**
	 * Write this Image to disk.
	 * @param file
	 * @throws IOException
	 */
	public void write(final File file) throws IOException;
	
	
	/**
	 * Returns the topmost MipMap
	 * @return {@link BufferedImage}
	 */
	public BufferedImage getData();
	
	/**
	 * Returns the MipMap at the specified index.
	 * @param index
	 * @return
	 */
	public BufferedImage getMipMap(int index);
	
	/**
	 * Load the data from file into memory.
	 * @throws UnsupportedDataTypeException 
	 */
	public void loadImageData() throws IOException;
	
	/**
	 * Returns the stored MipMaps as a {@link BufferedImage}-Array
	 * @return
	 */
	public BufferedImage[] getAllMipMapsBI();
	
	/**
	 * returns the stored MipMaps as {@link ByteBuffer}-Array
	 * @return
	 */
	public List<BufferedImage> generateAllMipMaps();
	
	/**
	 * Sets a new {@link BufferedImage} as the Topmost MipMap and generates new MipMaps accordingly.
	 * @param bi
	 */
	public void setData(final BufferedImage bi);
}
