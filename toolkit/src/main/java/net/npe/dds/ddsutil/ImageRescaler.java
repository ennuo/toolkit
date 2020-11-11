/**
 * 
 */
package net.npe.dds.ddsutil;

import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Java Graphics2D Rescaler
 * @author danielsenff
 *
 */
public class ImageRescaler extends Rescaler {

	/** 
	 * Graphics2D Scale algorithm
	 */
	private int scaleAlgorithm;

	/**
	 * 
	 */
	public ImageRescaler() {
		scaleAlgorithm = Image.SCALE_SMOOTH;
	}

	/**
	 * @param scaleMethod
	 */
	public ImageRescaler(final int scaleMethod) {
		scaleAlgorithm = scaleMethod;
	}

	/**
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	@Override
	public BufferedImage rescaleBI(final BufferedImage originalImage,
			final int newWidth, final int newHeight) {

		Image rescaledImage = originalImage.getScaledInstance(newWidth, newHeight, scaleAlgorithm);
		BufferedImage bi;
		if(rescaledImage instanceof BufferedImage)
			bi = (BufferedImage)rescaledImage;
		else
			bi = BIUtil.convertImageToBufferedImage(rescaledImage, BufferedImage.TYPE_4BYTE_ABGR);
		
		return bi;
	}

	/**
	 * @return
	 */
	public int getScaleAlgorithm() {
		return this.scaleAlgorithm;
	}

	/**
	 * @param scaleAlgorithm
	 */
	public void setScaleAlgorithm(final int scaleAlgorithm) {
		this.scaleAlgorithm = scaleAlgorithm;
	}
}
