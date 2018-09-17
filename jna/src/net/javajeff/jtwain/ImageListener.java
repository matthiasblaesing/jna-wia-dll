package net.javajeff.jtwain;

import java.awt.Image;
/**
 * ImageListener is used by the JTwain acquire method for image processing.
 * @author assu
 *
 */
public interface ImageListener {

	/**
	 * This method is called whenever a new image is aquired and ready for further processing.
	 * @param newImage  the scanned image
	 * @return true if you wish to continue with further images
	 */
	  public boolean imageReady(Image newImage);
}
