package net.npe.dds.model;

/**
 * 
 */

import gr.zdimensions.jsquish.Squish.CompressionType;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import net.npe.dds.jogl.DDSImage;

import net.npe.dds.compression.ARGBBufferDecompressor;
import net.npe.dds.compression.BufferDecompressor;
import net.npe.dds.compression.DXTBufferDecompressor;

import net.npe.dds.ddsutil.MipMapsUtil;
import net.npe.dds.ddsutil.PixelFormats;


/**
 * @author danielsenff
 *
 */
public class DDSFile extends AbstractTextureImage{

	protected TextureType textureType;
	private DDSImage ddsimage;
	
	/**
	 * @param filename 
	 */
	public DDSFile(final String filename) {
		this(new File(filename));
	}
	
	/**
	 * Constructs a DDSFile from a {@link File}
	 * @param file
	 */
	public DDSFile(final File file) {
		this.file = file;
		try {
			init(DDSImage.read(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructs a DDSFile from a {@link File} and a {@link DDSImage}
	 * @param file
	 * @param ddsimage
	 */
	public DDSFile(final File file, final DDSImage ddsimage) {
		this.file = file;
		init(ddsimage);
	}
	
	/**
	 * Constructs a DDSFile from a file path and a {@link DDSImage}
	 * @param filename
	 * @param ddsimage
	 */
	public DDSFile(final String filename, final DDSImage ddsimage) {
		this(new File(filename), ddsimage);
	}

	/**
	 * 
	 * @param filename
	 * @param bi
	 * @param pixelformat
	 * @param hasMipMaps
	 */
	public DDSFile(final File filename, 
			final BufferedImage bi, 
			final int pixelformat, 
			final boolean hasMipMaps) {
		this.file = filename;
		
		this.width  = bi.getWidth();
		this.height = bi.getHeight();
		this.pixelformat = pixelformat;
		this.hasMipMaps = hasMipMaps;
		if(hasMipMaps) {
			this.numMipMaps = MipMapsUtil.calculateMaxNumberOfMipMaps(width, height);
		} else {
			this.numMipMaps = 1;
		}
		this.mipMaps = new MipMaps(this.numMipMaps);
		this.mipMaps.setMipMap(0, bi); 
	}
	
	/**
	 * @param ddsimage
	 */
	protected void init(final DDSImage ddsimage) {
		this.ddsimage 		= ddsimage;
		this.width  		= ddsimage.getWidth();
		this.height 		= ddsimage.getHeight();
		this.depth 			= ddsimage.getDepth();
		this.pixelformat 	= ddsimage.getPixelFormat();
		this.textureType	= getTextureType(ddsimage);
		this.numMipMaps 	= ddsimage.getNumMipMaps();
		this.mipMaps 		= new MipMaps(this.numMipMaps);
		this.hasMipMaps		= (ddsimage.getNumMipMaps() > 1); // there is always at least the topmost MipMap
	}

	/**
	 * Load the ImageData for the specified MipMap from original {@link DDSImage}.
	 * @param mipmap
	 * @throws UnsupportedDataTypeException 
	 */
	public void loadImageData(int mipmap) throws IOException {
		if(mipmap <= this.numMipMaps ) {
			
			int width = MipMaps.getMipMapSizeAtIndex(mipmap, ddsimage.getWidth());
			int height = MipMaps.getMipMapSizeAtIndex(mipmap, ddsimage.getHeight());
			ByteBuffer data = ddsimage.getMipMap(mipmap).getData();
			
			BufferDecompressor bufferDecompressor;
			if(isCompressed()) {
				CompressionType compressionType = 
						PixelFormats.getSquishCompressionFormat(ddsimage.getPixelFormat());
				bufferDecompressor = new DXTBufferDecompressor(
						data,
						width, 
						height, 
						compressionType);
			} else {
				bufferDecompressor = new ARGBBufferDecompressor(
						data,
						width, 
						height, 
						this.pixelformat);
			}
			this.mipMaps.addMipMap(bufferDecompressor.getImage());
		}
	}
	
	@Override
	public void loadImageData() throws IOException {
		for (int i = 0; i < this.numMipMaps; i++) {
			loadImageData(i);
		}
	}
	
	@Override
	public String toString() {
		return this.file.getAbsolutePath() + PixelFormats.verbosePixelformat(this.pixelformat);
	}
	
	@Override
	public boolean equals(Object second) {
		if(second != null && second instanceof DDSFile) {
			DDSFile secondFile = (DDSFile) second;
			return (this.getFile().getAbsoluteFile().equals(secondFile.getFile().getAbsoluteFile()) && 
					this.hasMipMaps() == secondFile.hasMipMaps() &&
					this.getPixelformat() == secondFile.getPixelformat() &&
					this.getHeight() == secondFile.getHeight() &&
					this.getWidth() == secondFile.getWidth());	
		}
		return false;
	}
	
//	public ByteBuffer[] getMipMapData() {
//		ByteBuffer[] buffer = new ByteBuffer[ddsimage.getNumMipMaps()+1];
//		for (int i = 0; i < buffer.length; i++) {
//			buffer[i] = ddsimage.getMipMap(i).getData();
//		}
//		return buffer;
//	}
	
	/**
	 * The DDS-Image can have different texture types. 
	 * Regular Texture, Volume-Texture and CubeMap
	 * @return TextureType Type of Texture
	 */
	public TextureType getTextureType() {
		return this.textureType;
	}
	
	/**
	 * The DDS-Image can have different texture types. 
	 * Regular Texture, Volume-Texture and CubeMap
	 * This returns the textureType from a {@link DDSImage}
	 * @param ddsimage
	 * @return
	 */
	public static TextureType getTextureType(final DDSImage ddsimage) {
		if(ddsimage.isCubemap()) {
			return TextureType.CUBEMAP;
		} else if (ddsimage.isVolume()) {
			return TextureType.VOLUME;
		} else {
			return TextureType.TEXTURE;
		}
	}
	
	public void write(final File targetFile) throws IOException {
		ByteBuffer[] mipmaps = new ByteBuffer[getNumMipMaps()];
		for (int i = 0; i < mipmaps.length; i++) {
			mipmaps[i] = DDSImage.read(this.file).getMipMap(i).getData();
		}
		
		DDSImage outputDDS = DDSImage.createFromData(this.pixelformat, width, height, mipmaps);
		outputDDS.write(file);
		outputDDS.close();
	}
	
	/**
	 * Checks if the {@link File} is a valid DDS-Image
	 * @param file 
	 * @return 
	 * @throws IOException 
	 * 
	 */
	public static boolean isValidDDSImage(final File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		boolean isDDSImage = DDSImage.isDDSImage(fis);
		fis.close();
		return isDDSImage;
	}
	
	/**
	 * Returns the stored MipMaps as a {@link BufferedImage}-Array
	 * @return
	 */
	public BufferedImage[] getAllMipMapsBI(){
		return mipMaps.getAllMipMapsArray();
	}
	
	/**
	 * returns the stored MipMaps as {@link ByteBuffer}-Array
	 * @return
	 */
	public List<BufferedImage> generateAllMipMaps(){
		MipMaps mipMaps = new MipMaps();
		mipMaps.generateMipMaps(getTopMipMap());
		return mipMaps.getAllMipMaps();
	}

	public BufferedImage getMipMap(int index) {
		return this.mipMaps.getMipMap(index);
	}

}
