/*
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package net.npe.dds.jogl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Vector;

import net.npe.dds.jogl.TEXImage.Header.EmbeddedBuffer;


/** A reader and writer for Tex (.tex) files, which are
    used to describe textures. These files can contain multiple mipmap
    levels in one file. This class is currently minimal and does not
    support all of the possible file formats. 
    http://www.realgpx.com/partage/texformat.htm*/

public class TEXImage {



	private FileInputStream fis;
	private FileChannel     chan;
	private ByteBuffer buf;
	private Header header;
	private Vector<DDSImage> embeddedMap;



	// Known pixel formats
	public static final int D3DFMT_UNKNOWN   =  0;
	public static final int D3DFMT_R8G8B8    =  20;
	public static final int D3DFMT_A8R8G8B8  =  21;
	public static final int D3DFMT_X8R8G8B8  =  22;
	// The following are also valid FourCC codes
	public static final int D3DFMT_DXT1      =  0x31545844;
	public static final int D3DFMT_DXT2      =  0x32545844;
	public static final int D3DFMT_DXT3      =  0x33545844;
	public static final int D3DFMT_DXT4      =  0x34545844;
	public static final int D3DFMT_DXT5      =  0x35545844;

	/** Reads a DirectDraw surface from the specified file name,
      returning the resulting DDSImage.

      @param filename File name
      @return DDS image object
      @throws java.io.IOException if an I/O exception occurred
	 */
	public static TEXImage read(String filename) throws IOException {
		return read(new File(filename));
	}

	/** Reads a DirectDraw surface from the specified file, returning
      the resulting DDSImage.

      @param file File object
      @return DDS image object
      @throws java.io.IOException if an I/O exception occurred
	 */
	public static TEXImage read(File file) throws IOException {
		TEXImage image = new TEXImage();
		image.readFromFile(file);
		return image;
	}

	/** Reads a DirectDraw surface from the specified ByteBuffer, returning
      the resulting DDSImage.

      @param buf Input data
      @return DDS image object
      @throws java.io.IOException if an I/O exception occurred
	 */
	public static TEXImage read(ByteBuffer buf) throws IOException {
		TEXImage image = new TEXImage();
		image.readFromBuffer(buf);
		return image;
	}

	/** Closes open files and resources associated with the open
      DDSImage. No other methods may be called on this object once
      this is called. */
	public void close() {
		try {
			if (chan != null) {
				chan.close();
				chan = null;
			}
			if (fis != null) {
				fis.close();
				fis = null;
			}
			buf = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Creates a new DDSImage from data supplied by the user. The
	 * resulting DDSImage can be written to disk using the write()
	 * method.
	 *
	 * @param d3dFormat the D3DFMT_ constant describing the data; it is
	 *                  assumed that it is packed tightly
	 * @param width  the width in pixels of the topmost mipmap image
	 * @param height the height in pixels of the topmost mipmap image
	 * @param mipmapData the data for each mipmap level of the resulting
	 *                   DDSImage; either only one mipmap level should
	 *                   be specified, or they all must be
	 * @throws IllegalArgumentException if the data does not match the
	 *   specified arguments
	 * @return DDS image object
	 */
	public static TEXImage createFromData(int d3dFormat,
			int width,
			int height,
			ByteBuffer[] mipmapData) throws IllegalArgumentException {
		TEXImage image = new TEXImage();
		image.initFromData(d3dFormat, width, height, mipmapData);
		return image;
	}

	/** Determines from the magic number whether the given InputStream
      points to a DDS image. The given InputStream must return true
      from markSupported() and support a minimum of four bytes of
      read-ahead.

      @param in Stream to check
      @return true if input stream is DDS image or false otherwise
      @throws java.io.IOException if an I/O exception occurred
	 */
	public static boolean isTEXImage(InputStream in) throws IOException {
		if (!(in instanceof BufferedInputStream)) {
			in = new BufferedInputStream(in);
		}
		if (!in.markSupported()) {
			throw new IOException("Can not test non-destructively whether given InputStream is a TEX image");
		}
		in.mark(4);
		int magic = 0;
		for (int i = 0; i < 4; i++) {
			int tmp = in.read();
			if (tmp < 0) {
				in.reset();
				return false;
			}
			magic = ((magic >>> 8) | (tmp << 24));
		}
		in.reset();
		return (magic == MAGIC);
	}

	/**
	 * Writes this DDSImage to the specified file name.
	 * @param filename File name to write to
	 * @throws java.io.IOException if an I/O exception occurred
	 */
	public void write(String filename) throws IOException {
		write(new File(filename));
	}

	/**
	 * Writes this TEXImage to the specified file name.
	 * @param file File object to write to
	 * @throws java.io.IOException if an I/O exception occurred
	 */
	public void write(File file) throws IOException {
		FileOutputStream stream = new FileOutputStream(file);
		FileChannel chan = stream.getChannel();
		// Create ByteBuffer for header in case the start of our
		// ByteBuffer isn't actually memory-mapped
		ByteBuffer hdr = ByteBuffer.allocate(Header.writtenSize());
		hdr.order(ByteOrder.LITTLE_ENDIAN);
		header.write(hdr);
		hdr.rewind();
		chan.write(hdr);
		buf.position(Header.writtenSize());
		chan.write(buf);
		chan.force(true);
		chan.close();
		stream.close();
	}


	/** Gets the pixel format of this texture (D3DFMT_*) based on some
      heuristics. Returns D3DFMT_UNKNOWN if could not recognize the
      pixel format. */
	public int getPixelFormat() {
		return embeddedMap.get(0).getPixelFormat();
	}


	/** Indicates whether this texture is compressed. */
	public boolean isCompressed() {
		return embeddedMap.get(0).isCompressed();
	}

	/** If this surface is compressed, returns the kind of compression
      used (DXT1..DXT5). */
	public int getCompressionFormat() {
		return embeddedMap.get(0).getCompressionFormat();
	}

	/** Width of the texture (or the top-most mipmap if mipmaps are
      present) */
	public int getWidth() {
		return header.width;
	}

	/** Height of the texture (or the top-most mipmap if mipmaps are
      present) */
	public int getHeight() {
		return header.height;
	}

	/** Number of mip maps in the texture */
	public int getNumMipMaps() {
		return header.mipMapCountOrAux;
	}


	/** Converts e.g. DXT1 compression format constant (see {@link
      #getCompressionFormat}) into "DXT1".
      @param compressionFormat Compression format constant
      @return String format code
	 */
	public static String getCompressionFormatName(int compressionFormat) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			char c = (char) (compressionFormat & 0xFF);
			buf.append(c);
			compressionFormat = compressionFormat >> 8;
		}
		return buf.toString();
	}

	public void debugPrint() {
		/*PrintStream tty = System.err;
    tty.println("Compressed texture: " + isCompressed());
    if (isCompressed()) {
      int fmt = getCompressionFormat();
      String name = getCompressionFormatName(fmt);
      tty.println("Compression format: 0x" + Integer.toHexString(fmt) + " (" + name + ")");
    }
    tty.println("Width: " + header.width + " Height: " + header.height);
    tty.println("header.pitchOrLinearSize: " + header.pitchOrLinearSize);
    tty.println("header.pfRBitMask: 0x" + Integer.toHexString(header.pfRBitMask));
    tty.println("header.pfGBitMask: 0x" + Integer.toHexString(header.pfGBitMask));
    tty.println("header.pfBBitMask: 0x" + Integer.toHexString(header.pfBBitMask));
    tty.println("SurfaceDesc flags:");
    boolean recognizedAny = false;
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_CAPS, "DDSD_CAPS");
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_HEIGHT, "DDSD_HEIGHT");
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_WIDTH, "DDSD_WIDTH");
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_PITCH, "DDSD_PITCH");
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_BACKBUFFERCOUNT, "DDSD_BACKBUFFERCOUNT");
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_ZBUFFERBITDEPTH, "DDSD_ZBUFFERBITDEPTH");
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_ALPHABITDEPTH, "DDSD_ALPHABITDEPTH");
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_LPSURFACE, "DDSD_LPSURFACE");
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_PIXELFORMAT, "DDSD_PIXELFORMAT");
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_MIPMAPCOUNT, "DDSD_MIPMAPCOUNT");
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_LINEARSIZE, "DDSD_LINEARSIZE");
    recognizedAny |= printIfRecognized(tty, header.flags, DDSD_DEPTH, "DDSD_DEPTH");
    if (!recognizedAny) {
      tty.println("(none)");
    }
    tty.println("Raw SurfaceDesc flags: 0x" + Integer.toHexString(header.flags));
    tty.println("Pixel format flags:");
    recognizedAny = false;
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_ALPHAPIXELS, "DDPF_ALPHAPIXELS");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_ALPHA, "DDPF_ALPHA");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_FOURCC, "DDPF_FOURCC");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_PALETTEINDEXED4, "DDPF_PALETTEINDEXED4");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_PALETTEINDEXEDTO8, "DDPF_PALETTEINDEXEDTO8");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_PALETTEINDEXED8, "DDPF_PALETTEINDEXED8");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_RGB, "DDPF_RGB");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_COMPRESSED, "DDPF_COMPRESSED");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_RGBTOYUV, "DDPF_RGBTOYUV");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_YUV, "DDPF_YUV");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_ZBUFFER, "DDPF_ZBUFFER");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_PALETTEINDEXED1, "DDPF_PALETTEINDEXED1");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_PALETTEINDEXED2, "DDPF_PALETTEINDEXED2");
    recognizedAny |= printIfRecognized(tty, header.pfFlags, DDPF_ZPIXELS, "DDPF_ZPIXELS");
    if (!recognizedAny) {
      tty.println("(none)");
    }
    tty.println("Raw pixel format flags: 0x" + Integer.toHexString(header.pfFlags));
    tty.println("Depth: " + getDepth());
    tty.println("Number of mip maps: " + getNumMipMaps());
    int fmt = getPixelFormat();
    tty.print("Pixel format: ");
    switch (fmt) {
    case D3DFMT_R8G8B8:   tty.println("D3DFMT_R8G8B8"); break;
    case D3DFMT_A8R8G8B8: tty.println("D3DFMT_A8R8G8B8"); break;
    case D3DFMT_X8R8G8B8: tty.println("D3DFMT_X8R8G8B8"); break;
    case D3DFMT_DXT1:     tty.println("D3DFMT_DXT1"); break;
    case D3DFMT_DXT2:     tty.println("D3DFMT_DXT2"); break;
    case D3DFMT_DXT3:     tty.println("D3DFMT_DXT3"); break;
    case D3DFMT_DXT4:     tty.println("D3DFMT_DXT4"); break;
    case D3DFMT_DXT5:     tty.println("D3DFMT_DXT5"); break;
    case D3DFMT_UNKNOWN:  tty.println("D3DFMT_UNKNOWN"); break;
    default:              tty.println("(unknown pixel format " + fmt + ")"); break;
    }*/
	}

	//----------------------------------------------------------------------
	// Internals only below this point
	//

	//  private static final int MAGIC = 0x20534444;
	private static final int MAGIC = 0x54455800;

	static class Header {
		int height;               // height of surface to be created
		int width;                // width of input surface
		int mipMapCountOrAux;     // number of mip-map levels requested (in this context), range of 1 to 8
		int alphaBitDepth;        // depth of alpha buffer requested
		Vector<EmbeddedBuffer> embeddedMap;

		void read(ByteBuffer buf) throws IOException {
			int magic                     = buf.getInt();
			if (magic != MAGIC) {
				throw new IOException("Incorrect magic number 0x" +
						Integer.toHexString(magic) +
						" (expected " + MAGIC + ")");
			}

			width                         = buf.getInt();
			height                        = buf.getInt();
			alphaBitDepth                 = buf.getInt();
			mipMapCountOrAux              = buf.getInt();

			embeddedMap = readHeaderTable(buf); // header data for embedded dds

//			for (EmbeddedBuffer embBuffer : embeddedMap) {
//				DDSImage image = DDSImage.read(embBuffer.buffer);
//			}
		}

		private Vector<EmbeddedBuffer> readHeaderTable(ByteBuffer buf) {
			int offset, size, currentPos;
			Vector<EmbeddedBuffer> embeddedBuffer = new Vector<EmbeddedBuffer>();
			/*
			 *  iterate over 5 tables, mapping pixelformat
			 *  Table #1 : r8g8b8, x8r8g8b8, r5g6b5, x1r5g5b5 
			 *  Table #2 : a8r8g8b8, a4r4g4b4, DXT2, DXT3, DXT4 
			 *	Table #3 : a1r5g5b5 
			 * 	Table #4 : DXT1 
			 *  Table #5 : DXT5
			 */
			for (int t = 0; t < 5; t++) {
				// iterate over 8 maps
//				System.out.println("Table: "+t);
				for (int i = 0; i < 8; i++) {
					offset = buf.getInt();
					size = buf.getInt();
//					System.out.println("offset="+offset+" size="+size);
					EmbeddedBuffer embBuffer = new EmbeddedBuffer();
					if((offset != -1) && (size != -1)) { // if not blank
						currentPos = buf.position();
						byte[] ddsbuffer = new byte[size];
						buf.position(offset);
						buf.get(ddsbuffer);
						embBuffer.buffer = ByteBuffer.wrap(ddsbuffer);
						embBuffer.size = size;
						embBuffer.offset = offset;
						embeddedBuffer.add(embBuffer);
						buf.position(currentPos);
					}
				}
			}
			for (EmbeddedBuffer embeddedBuffer2 : embeddedBuffer) {
				embeddedBuffer2.buffer.rewind();
				embeddedBuffer2.buffer.order(ByteOrder.LITTLE_ENDIAN);
			}
			
			return embeddedBuffer;
		}

		class EmbeddedBuffer {
			ByteBuffer buffer;
			int size;
			int offset;
		}

		// buf must be in little-endian byte order
		void write(ByteBuffer buf) {
			buf.putInt(MAGIC);
			buf.putInt(width);
			buf.putInt(height);
			buf.putInt(alphaBitDepth);
			buf.putInt(mipMapCountOrAux);

			// header table


			// embedded buffer

		}

		private static int size() {
			return 340;
		}

		private static int pfSize() {
			return 32;
		}

		private static int writtenSize() {
			return 340;
		}
	}

	private TEXImage() {
		embeddedMap = new Vector<DDSImage>();
	}

	private void readFromFile(File file) throws IOException {
		fis = new FileInputStream(file);
		chan = fis.getChannel();
		ByteBuffer buf = chan.map(FileChannel.MapMode.READ_ONLY,
				0, (int) file.length());
		readFromBuffer(buf);
	}

	private void readFromBuffer(ByteBuffer buf) throws IOException {
		this.buf = buf;
		buf.order(ByteOrder.LITTLE_ENDIAN);
		header = new Header();
		header.read(buf);
		for (EmbeddedBuffer embBuffer : header.embeddedMap) {
			embBuffer.buffer.rewind();
			embeddedMap.add(DDSImage.read(embBuffer.buffer));
		}
	}

	private void initFromData(int d3dFormat,
			int width,
			int height,
			ByteBuffer[] mipmapData) throws IllegalArgumentException {
		// Check size of mipmap data compared against format, width and
		// height
		int topmostMipmapSize = width * height;
		boolean isCompressed = false;
		
		for (int i = 0; i < mipmapData.length; i++) {
			DDSImage image = DDSImage.createFromData(d3dFormat, width, height, mipmapData);
		}
		
		
		
		
		// Allocate and initialize a Header
		header = new Header();
		if (mipmapData.length > 1) {
			header.mipMapCountOrAux = mipmapData.length;
		}
		header.width = width;
		header.height = height;
	}

	/**
	 * Get a Vector of all DDSImages embedded in this TEX-file.
	 * @return
	 */
	public Vector<DDSImage> getAllEmbeddedMaps() {
		return embeddedMap;
	}

	/**
	 * Get a {@link DDSImage} embedded in this TEX-file.
	 * @param index
	 * @return
	 */
	public DDSImage getEmbeddedMaps(int index) {
		return embeddedMap.get(index);
	}

	public int getDepth() {
		return embeddedMap.get(0).getDepth();
	}
}
