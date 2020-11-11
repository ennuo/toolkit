/**
 * 
 */
package net.npe.dds.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import net.npe.dds.jogl.DDSImage;


/**
 * @author danielsenff
 *
 */
public class DDSCubeImageFile extends DDSFile {

	/**
	 * 0 - top
	 * 1 - bottom
	 * 2 - front
	 * 3 - back
	 * 4 - left
	 * 5 - right
	 */
	private BufferedImage[] cubeFaces;
	
	/**
	 * Faces of the Cube
	 *
	 */
	public enum Faces {
		top, bottom, front, back, left, right
	}
	
	/**
	 * @param filename
	 * @throws IOException
	 */
	public DDSCubeImageFile(String filename) throws IOException {
		super(filename);
		this.cubeFaces = new BufferedImage[6];
	}

	/**
	 * @param file
	 * @throws IOException 
	 * @throws IOException
	 */
	public DDSCubeImageFile(File file) throws IOException {
		super(file);

	}

	/**
	 * @param filename
	 * @param bi
	 * @param pixelformat
	 * @param hasmipmaps
	 * @throws IOException
	 */
	public DDSCubeImageFile(File file, BufferedImage bi, int pixelformat,
			boolean hasmipmaps) {
		super(file, bi, pixelformat, hasmipmaps);
	}

	/**
	 * @param file
	 * @param ddsimage
	 */
	public DDSCubeImageFile(File file, DDSImage ddsimage) {
		super(file, ddsimage);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param filename
	 * @param ddsimage
	 */
	public DDSCubeImageFile(String filename, DDSImage ddsimage) {
		super(filename, ddsimage);
	}
	
	
}
