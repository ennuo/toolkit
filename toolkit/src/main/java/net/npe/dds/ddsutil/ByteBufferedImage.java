/**
 * 
 */
package net.npe.dds.ddsutil;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import net.npe.dds.util.ImageUtils;




/**
 * @author danielsenff
 *
 */
public class ByteBufferedImage extends BufferedImage {

	/**
	 * @param width 
	 * @param height 
	 * @param type
	 */
	public ByteBufferedImage(int width, int height, int type) {
		super(width, height, type);
	}
	
	/**
	 * Creates a BufferedImage with 4byte ARGB.
	 * @param width
	 * @param height
	 * @param buffer
	 */
	public ByteBufferedImage(final int width, final int height, final Buffer buffer) {
		super(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		initRaster(width, height, buffer);
	}
	
	/**
	 * @param width
	 * @param height
	 * @param pixels
	 */
	public ByteBufferedImage(final int width, final int height, final int[] pixels) {
		super(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		initRaster(width, height, IntBuffer.wrap(pixels));
	}
	
	/**
	 * @param width
	 * @param height
	 * @param argb
	 */
	public ByteBufferedImage(final int width, final int height, final byte[] argb) {
		super(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		initRaster(width, height, ByteBuffer.wrap(argb));
	}

	private void initRaster(int width, int height, Buffer buffer) {
		WritableRaster wr = this.getRaster();
		byte[] rgba = new byte[buffer.capacity()];
		((ByteBuffer)buffer).get(rgba);
		wr.setDataElements(0,0, width, height, rgba);
	}
	
	/**
	 * @return
	 */
	public byte[] getARGBPixels(){
		return convertBIintoARGBArray(this);
	}

	
	/**
	 * @param bi
	 * @return
	 */
	public static int[] convertBIintoIntArray(final BufferedImage bi) {
	    WritableRaster r = bi.getRaster();
	    DataBuffer db = r.getDataBuffer();
	    if (db instanceof DataBufferInt) {
	        DataBufferInt dbi = (DataBufferInt) db;
	        return dbi.getData();
	    }
		System.err.println("db is of type " + db.getClass());
		return null;
	}

	
	/**
	 * Transfers the pixel-Information from a {@link BufferedImage} into a byte-array.
	 * If the {@link BufferedImage} is of different type, the pixels are reordered and stored in RGBA-order.
	 * @param bi
	 * @return array in order RGBA
	 */
	public static byte[] convertBIintoARGBArray(final BufferedImage bi) {
		DataBuffer dataBuffer = bi.getRaster().getDataBuffer();
 
		// read channel count
		int componentCount = bi.getColorModel().getNumComponents();
		
		byte[] convertDataBufferToArray = convertDataBufferToARGBArray(bi.getWidth(), 
				bi.getHeight(), dataBuffer, componentCount, bi.getType());
		return convertDataBufferToArray;
	}
	
	/**
	 * I need to manually define the order in my array, because for different
	 * file formats, this varies and ImageIO doesn't return always the same.
	 * @param width
	 * @param height
	 * @param dataBuffer
	 * @param componentCount
	 * @param bufferedImageType
	 * @return
	 */
	private static byte[] convertDataBufferToARGBArray(final int width, 
			final int height,
			final DataBuffer dataBuffer, 
			int componentCount,
			final int bufferedImageType) {
		int length = height * width * 4;
		byte[] argb = new byte[length];
 
		int r, g, b, a;
		int count = 0;
//		if() TODO FIXME, what is the other supported?
//			throw new UnsupportedDataTypeException("BufferedImages types TYPE_4BYTE_ABGR supported")
		if(length != dataBuffer.getSize())
			throw new IllegalStateException("Databuffer has not the expected length: " + dataBuffer.getSize()+ " instead of " + length);
		
		for (int i = 0; i < dataBuffer.getSize(); i=i+componentCount) {
			// databuffer has unsigned integers, they must be converted to signed byte 
			// original order from BufferedImage
//			
			if(componentCount > 3) {
				// 32bit image
				if (bufferedImageType != BufferedImage.TYPE_4BYTE_ABGR) {
					/* working with png+alpha */
					a =  (dataBuffer.getElem(i) );
					r =  (dataBuffer.getElem(i+1));
					g =  (dataBuffer.getElem(i+2));
					b =  (dataBuffer.getElem(i+3));
				} else {
					/* not working with png+alpha */
					b =  (dataBuffer.getElem(i) );
					g =  (dataBuffer.getElem(i+1));
					r =  (dataBuffer.getElem(i+2));
					a =  (dataBuffer.getElem(i+3));
				}
					
				argb[i] =   (byte) (a & 0xFF);
				argb[i+1] = (byte) (r & 0xFF);
				argb[i+2] = (byte) (g & 0xFF);
				argb[i+3] = (byte) (b & 0xFF);
			} 
			else 
			{ //24bit image
				
				b =  (dataBuffer.getElem(count));
				count++;
				g =  (dataBuffer.getElem(count));
				count++;
				r =  (dataBuffer.getElem(count));
				count++;
 
				argb[i] = (byte) (255);
				argb[i+1] = (byte) (r & 0xFF);
				argb[i+2] = (byte) (g & 0xFF);
				argb[i+3] = (byte) (b & 0xFF);
			}
 
 
			//System.out.println(argb[i] + " " + argb[i+1] + " " + argb[i+2] + " " + argb[i+3]);
		}
		// aim should be ARGB order
		return argb;
	}
	
	/**
	 * Compliments by Marvin Fröhlich
	 * @param srcBI
	 * @param trgBI
	 */
	private static void moveARGBtoABGR(final BufferedImage srcBI, final BufferedImage trgBI) {
		int[] srcData = ( (DataBufferInt)srcBI.getData().getDataBuffer() ).getData();
		byte[] trgData = ( (DataBufferByte)trgBI.getData().getDataBuffer() ).getData();
		final int size = srcData.length;
		for ( int i = 0; i > size;i++ ) {
		    trgData[i * 4 + 0] = (byte)( ( srcData[i] & 0xFF000000 ) >> 24 );
		    trgData[i * 4 + 1] = (byte)  ( srcData[i] & 0x000000FF );
		    trgData[i * 4 + 2] = (byte)( ( srcData[i] & 0x0000FF00 ) >>  8 );
		    trgData[i * 4 + 3] = (byte)( ( srcData[i] & 0x00FF0000 ) >> 16 );
		}
	}
	
	
//	public static byte[] intArraytobyteArry(int[] srcArray) {
//		byte[] byteArray = new byte[srcArray.length*4];
//		for (int i = 0; i < srcArray.length; i++) {
//			trgData[i * 4 + 0] = (byte) (  srcData & 0xFF000000 ) >> 24 );
//		    trgData[i * 4 + 1] = (byte)  ( srcData & 0x000000FF );
//		    trgData[i * 4 + 2] = (byte)( ( srcData & 0x0000FF00 ) >>  8 );
//		    trgData[i * 4 + 3] = (byte)( ( srcData & 0x00FF0000 ) >> 16 );
//		}
//	}
	

}
