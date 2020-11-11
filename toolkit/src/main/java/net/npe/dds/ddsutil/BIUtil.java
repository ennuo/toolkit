/**
 * 
 */
package net.npe.dds.ddsutil;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import net.npe.dds.ddsutil.ImageOperations.ChannelMode;


/**
 * @author danielsenff
 *
 */
public class BIUtil {

	private BIUtil() {}
	
	/**
	 * Extracts the specified {@link ChannelMode} from a {@link BufferedImage}
	 * and returns it in a new {@link BufferedImage} 
	 * @param sourceBi
	 * @param channelMode
	 * @return
	 */
	public static BufferedImage getChannel(final BufferedImage sourceBi, final ChannelMode channelMode) {
		
		BufferedImage newBi = new BufferedImage(sourceBi.getWidth(), 
				sourceBi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);		
		
		/*int pixelcount = sourceBi.getWidth() * sourceBi.getHeight();
		for (int y = 0; y < sourceBi.getHeight(); y++) {
			for (int x = 0; x < sourceBi.getWidth(); x++) {
				int pixel = sourceBi.getColorModel().getBlue(p);
				newBi.setRGB(x, y, );
			}
		}*/
		
		
		for (int y = 0; y < sourceBi.getHeight(); y++) {
			for (int x = 0; x < sourceBi.getWidth(); x++) {
				
//				ColorModel color = sourceBi.getColorModel();
				int[] argb = ImageOperations.readPixelARGB(sourceBi.getRGB(x,y)); 

				switch(channelMode){
					case ALPHA:
						newBi.setRGB(x, y, ImageOperations.writePixelRGB(argb[0],argb[0],argb[0]));
						break;
					case RED:
						newBi.setRGB(x, y, ImageOperations.writePixelRGB(argb[1],0,0));
						break;
					case GREEN:
						newBi.setRGB(x, y, ImageOperations.writePixelRGB(0,argb[2],0));
						break;
					case BLUE:
						newBi.setRGB(x, y, ImageOperations.writePixelRGB(0,0, argb[3]));
						break;
					case RGB:
						newBi.setRGB(x,y, ImageOperations.writePixelARGB(255, argb[1],argb[2],argb[3]));
						break;
				}
			}
		}
		
		return newBi;
	}
	
	/**
	 * Get an {@link BufferedImage} from an {@link Image}-Object
	 * @param image
	 * @param type
	 * @return
	 */
	public static BufferedImage convertImageToBufferedImage(final Image image, final int type) {
        BufferedImage result = new BufferedImage(
        		image.getWidth(null), image.getHeight(null), type);
        Graphics g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }
	
	/**
	 * Extracts the Alpha channel from a {@link ChannelMode} 
	 * and returns it in a new BufferedImage
	 * @param sourceBi
	 * @return
	 */
	public static Image getAlphaChannel(final BufferedImage sourceBi) {	
		return getChannel(sourceBi, ChannelMode.ALPHA);
	}
	
}
