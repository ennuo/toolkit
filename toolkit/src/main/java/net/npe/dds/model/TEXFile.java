package net.npe.dds.model;

import gr.zdimensions.jsquish.Squish.CompressionType;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import net.npe.dds.jogl.DDSImage;
import net.npe.dds.jogl.TEXImage;

import net.npe.dds.compression.DXTBufferDecompressor;

import net.npe.dds.ddsutil.PixelFormats;

/**
 * TEX for GP4-Texture format.
 * @author dahie
 *
 */
public class TEXFile  extends AbstractTextureImage {

	private TEXImage teximage;

	/**
	 * @param filename 
	 */
	public TEXFile(final String filename) {
		this(new File(filename));
	}
	
	/**
	 * @param file
	 */
	public TEXFile(final File file) {
		this.file = file;
		TEXImage teximage = null;
		try {
			teximage = TEXImage.read(file);
			init(teximage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param image
	 */
	protected void init(final TEXImage image) {
		this.teximage 		= image;
		this.width  		= image.getWidth();
		this.height 		= image.getHeight();
		this.depth 			= image.getDepth();
		this.pixelformat 	= image.getPixelFormat();
		this.numMipMaps 	= image.getNumMipMaps();
		this.mipMaps 		= new MipMaps(this.numMipMaps);
		this.hasMipMaps		= (image.getNumMipMaps() > 1); // there is always at least the topmost MipMap
	}
	
	@Override
	public void write(final File file) throws IOException {
		ByteBuffer[] mipmaps = new ByteBuffer[getNumMipMaps()];
		for (int i = 0; i < mipmaps.length; i++) {
			DDSImage image = TEXImage.read(this.file).getAllEmbeddedMaps().get(i);
			mipmaps[i] = image.getAllMipMaps()[0].getData();
		}
		
		TEXImage outputTEX = TEXImage.createFromData(this.pixelformat, width, height, mipmaps);
		outputTEX.write(file);
		outputTEX.close();
	}
	
	@Override
	public TextureType getTextureType() {
		return TextureType.TEXTURE;
	}
	
	/**
	 * Checks if the {@link File} is a valid DDS-Image
	 * @param file 
	 * @return 
	 * @throws IOException 
	 * 
	 */
	public static boolean isValidTEXImage(final File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		boolean isTEXImage = TEXImage.isTEXImage(fis);
		fis.close();
		return isTEXImage;
	}
	
	/**
	 * Returns the stored MipMaps as a {@link BufferedImage}-Array
	 * @return
	 */
	@Override
	public BufferedImage[] getAllMipMapsBI(){
		MipMaps mipMaps = new MipMaps();
		mipMaps.generateMipMaps(getTopMipMap());
		return mipMaps.getAllMipMapsArray();		
	}
	
	/**
	 * returns the stored MipMaps as {@link ByteBuffer}-Array
	 * @return
	 */
	@Override
	public List<BufferedImage> generateAllMipMaps(){
		MipMaps mipMaps = new MipMaps();
		mipMaps.generateMipMaps(getTopMipMap());
		return mipMaps.getAllMipMaps();
	}

	@Override
	public void loadImageData() throws IOException {
		CompressionType compressionType = 
			PixelFormats.getSquishCompressionFormat(teximage.getPixelFormat());
		this.mipMaps.setMipMap(0, new DXTBufferDecompressor(
				teximage.getEmbeddedMaps(0).getMipMap(0).getData(),
				teximage.getWidth(), 
				teximage.getHeight(), 
				compressionType).getImage());
	}

	@Override
	public BufferedImage getMipMap(final int index) {
		return this.mipMaps.getMipMap(index);
	}
	
}
