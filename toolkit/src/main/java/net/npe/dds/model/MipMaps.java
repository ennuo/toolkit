/**
 * 
 */
package net.npe.dds.model;

import gr.zdimensions.jsquish.Squish;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.npe.dds.jogl.DDSImage;
import net.npe.dds.ddsutil.ByteBufferedImage;
import net.npe.dds.ddsutil.ImageRescaler;
import net.npe.dds.ddsutil.MipMapsUtil;
import net.npe.dds.ddsutil.NonCubicDimensionException;
import net.npe.dds.ddsutil.Rescaler;


/**
 * MipMap Texture contains several layers of MipMaps, each is 1/4 the size of the one above.
 * @author Daniel Senff
 *
 */
public class MipMaps extends AbstractTextureMap implements Iterable<BufferedImage> {
	
	/**
	 * Topmost MipMap Index 
	 */
	public static final int TOP_MOST_MIP_MAP = 0;
	
	List<BufferedImage> mipmaps;
	/**
	 * {@link Rescaler} providing the scaling algorithm.
	 */
	protected Rescaler rescaler;

	private int numMipMaps;
	
	/**
	 * @param topmost 
	 * 
	 */
	public MipMaps() {
		this(0);
	}
	
	public MipMaps(final int numMipMaps) {
		this.numMipMaps = numMipMaps;
		this.rescaler = new ImageRescaler();
		this.mipmaps = new Vector<BufferedImage>(numMipMaps);
	}
	
	/**
	 * Populate this MipMap-Object based on the given topmost Map.
	 * @param topmost 
	 */
	public void generateMipMaps(BufferedImage topmost) {
		addMipMap(topmost);
		System.out.println("Generate Mipmaps");
		
		if(!DDSFile.isPowerOfTwo(topmost.getWidth()) 
				&& !DDSFile.isPowerOfTwo(topmost.getHeight())) 
			throw new NonCubicDimensionException();
		
		generateMipMapArray();	
	}

	private void generateMipMapArray() {
		BufferedImage topmost = getMipMaps().get(0);
		// dimensions of first map
		int mipmapWidth = topmost.getWidth(); 
		int mipmapHeight = topmost.getHeight();
		this.numMipMaps = MipMapsUtil.calculateMaxNumberOfMipMaps(mipmapWidth, mipmapHeight);
		
		BufferedImage previousMap = topmost;
		BufferedImage mipMapBi;
		for (int i = 1; i < this.numMipMaps; i++) {
			// calculation for next map
			mipmapWidth = MipMaps.calculateMipMapSize(mipmapWidth);
			mipmapHeight = MipMaps.calculateMipMapSize(mipmapHeight);
			
			mipMapBi = rescaler.rescaleBI(previousMap, mipmapWidth, mipmapHeight);
			addMipMap(mipMapBi);
			// by using this map in the next MipMap generation step, we increase
			// performance, since we don't always scale from the biggest image.
			// however this might also increase errors over generations
			previousMap = mipMapBi;
		}
	}

	/**
	 * Returns the highest MipMap in the original resolution. 
	 * @return
	 */
	public BufferedImage getTopMostMipMap() {
		return getMipMap(TOP_MOST_MIP_MAP);
	}
	
	/**
	 * @return
	 */
	public int getNumMipMaps() {
		return this.numMipMaps;
	}
	
	@Override
	public int getHeight() {
		return getMipMap(TOP_MOST_MIP_MAP).getHeight();
	}
	

	@Override
	public int getWidth() {
		return getMipMap(TOP_MOST_MIP_MAP).getWidth();
	}

	/**
	 * Returns a Map of the given level.
	 * @param index
	 * @return
	 */
	public BufferedImage getMipMap(final int index) {
		return getMipMaps().get(index);
	}
	
	/**
	 * Set the given {@link BufferedImage} as MipMap in the index.
	 * @param mipmapIndex
	 * @param image
	 */
	public void setMipMap(int mipmapIndex, BufferedImage image) {
		if(getMipMaps().size() == mipmapIndex)
			addMipMap(mipmapIndex, image);
		else
			getMipMaps().set(mipmapIndex, image);
	}

	private List<BufferedImage> getMipMaps() {
		return this.mipmaps;
	}
	
	/**
	 * @param image
	 */
	public void addMipMap(final BufferedImage image) {
		getMipMaps().add(image);
	}
	
	private void addMipMap(final int mipmapIndex, final BufferedImage image) {
		getMipMaps().add(mipmapIndex, image);
	}
	
	/**
	 * All contained MipMaps compressed with DXT in {@link ByteBuffer}
	 * Squishes each mipmap and store in a {@link DDSImage} compatible {@link ByteBuffer}-Array.
	 * @param compressionType
	 * @return
	 */
	@Override
	public ByteBuffer[] getDXTCompressedBuffer(final Squish.CompressionType compressionType) {
		ByteBuffer[] mipmapBuffer = new ByteBuffer[this.numMipMaps];
		
		for (int j = 0; j < this.numMipMaps; j++) {
			System.out.println("compress mipmap " + j);
			mipmapBuffer[j] = compress(getMipMap(j), compressionType);
		}
		return mipmapBuffer;
	}
	
	/**
	 * Returns a Vector with all MipMaps
	 * @return
	 */
	public List<BufferedImage> getAllMipMaps() {
		return getMipMaps();
	}
	
	/**
	 * Returns an Array of {@link BufferedImage}s of MipMaps.
	 * @return
	 */
	public BufferedImage[] getAllMipMapsArray() {
		return (BufferedImage[]) getMipMaps().toArray();
	}

	/* (non-Javadoc)
	 * @see DDSUtil.AbstractTextureMap#getUncompressedBuffer()
	 */
	public ByteBuffer[] getUncompressedBuffer() {
		ByteBuffer[] mipmapBuffer = new ByteBuffer[numMipMaps];
		for (int i = 0; i < numMipMaps; i++) {
			mipmapBuffer[i] = ByteBuffer.wrap(ByteBufferedImage.convertBIintoARGBArray(getMipMap(i)));
		}
		return mipmapBuffer;
	}

	/**
	 * @param topmost
	 * @param mipmapWidth
	 * @param mipmapHeight
	 * @param mipmapBI
	 * @return
	 */
	/*public static BufferedImage[] generateMipMaps(final BufferedImage topmost, 
			int mipmapWidth,
			int mipmapHeight, 
			final BufferedImage[] mipmapBI) {
		int i = 0; // cause the first already is set
		ImageRescaler rescaler = new ImageRescaler();
		while(true) {
			
			mipmapBI[i] = rescaler.rescaleBI(mipmapBI[i], mipmapWidth, mipmapHeight);
			
			if (mipmapWidth == 1 || mipmapHeight == 1) 
				break;
			
			i++;
			mipmapWidth = calculateMipMapSize(mipmapWidth);
			mipmapHeight = calculateMipMapSize(mipmapHeight);
		}
		return mipmapBI;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<BufferedImage> iterator() {
		return new Iterator<BufferedImage>() {
			int count=0;
			
			@Override
			public boolean hasNext() {
				boolean b = count++ < mipmaps.size()-1;
				return b;
			}

			@Override
			public BufferedImage next() {
				return mipmaps.get(count);
			}

			@Override
			public void remove() {
				 throw new UnsupportedOperationException();
			}
			
		};
	}

	/**
	 * Get the {@link Rescaler}.
	 * @return
	 */
	public Rescaler getRescaler() {
		return this.rescaler;
	}

	/**
	 * Set the {@link Rescaler}.
	 * @param rescaler
	 */
	public void setRescaler(Rescaler rescaler) {
		this.rescaler = rescaler;
	}

	/**
	 * returns the new size for the next iteration of a generated MipMap
	 * Usually half the current value, unless current value is 1
	 * @param currentValue 
	 * @return
	 */
	public static int calculateMipMapSize(final int currentValue) {
		return (currentValue > 1) ? currentValue/2 : 1;
	}

	/**
	 * Returns the size of the MipMap at the requested index based on the original value
	 * @param targetIndex
	 * @param original size at index 0
	 * @return
	 */
	public static int getMipMapSizeAtIndex(final int targetIndex, final int original) {
		int newValue = original;
		for (int i = 0; i < targetIndex; i++) {
			newValue = MipMaps.calculateMipMapSize(newValue);
		}
		return newValue;
	}
	
	/**
	 * Width of the MipMap on the specified index.
	 * @param index
	 * @return
	 */
	public int getMipMapWidth(int index) {
		return getMipMap(index).getWidth();
	}
	
	/**
	 * Width of the MipMap on the specified index.
	 * @param index
	 * @return
	 */
	public int getMipMapHeight(int index) {
		return getMipMap(index).getHeight();
	}
	
	/**
	 * Returns the {@link Dimension} of the MipMap at the specified index.
	 * @param index
	 * @return
	 */
	public Dimension getMipMapDimension(final int index) {
		return new Dimension(getMipMapWidth(index),	getMipMapHeight(index));
	}
	
}
