package net.npe.dds.compression;

import java.awt.Dimension;
import java.nio.ByteBuffer;

public class ARGBBufferDecompressor extends BufferDecompressor{

	int pixelformat;

	/**
	 * @param compressedBuffer
	 * @param width
	 * @param height
	 * @param pixelformat
	 */
	public ARGBBufferDecompressor(final ByteBuffer compressedBuffer, 
			final int width, final int height, int pixelformat) {
		this(compressedBuffer, new Dimension(width, height), pixelformat);
	}
	
	
	/**
	 * @param compressedData
	 * @param width
	 * @param height
	 * @param pixelformat
	 */
	public ARGBBufferDecompressor(byte[] compressedData, int width, int height,
			int pixelformat) {
		this(ByteBuffer.wrap(compressedData), new Dimension(width, height), pixelformat);
	}
	
	/**
	 * @param databuffer
	 * @param dimension
	 * @param type 
	 */
	public ARGBBufferDecompressor(final ByteBuffer databuffer, 
			final Dimension dimension, int pixelformat) {
		this.uncompressedBuffer = 
			decompressBuffer(databuffer, dimension.width, dimension.height, pixelformat);
		this.dimension = dimension;
		this.pixelformat = pixelformat;
	}


	private ByteBuffer decompressBuffer(ByteBuffer dataBuffer, int width,
			int height, Object pix) {
		
		return dataBuffer;
	}

}
