package net.npe.dds.ddsutil;

import java.awt.image.BufferedImage;

import net.npe.dds.model.MipMaps;
import net.npe.dds.model.SingleTextureMap;
import net.npe.dds.model.TextureMap;

/**
 * @author danielsenff
 *
 */
public class TextureFactory {

	/**
	 * @param generateMipMaps
	 * @param sourceImage
	 * @return
	 */
	public static TextureMap createTextureMap(final boolean generateMipMaps, final BufferedImage sourceImage) {
		TextureMap maps;
		if (generateMipMaps) {
			maps = new MipMaps();
			((MipMaps)maps).generateMipMaps(sourceImage);
		} else
			maps = new SingleTextureMap(sourceImage);
		
		return maps;
	}
}
