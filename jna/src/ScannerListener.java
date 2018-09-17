import java.awt.Image;

import net.javajeff.jtwain.ImageListener;

/**
 * Listener class for Scanned images
 * @author assu
 *
 */
public class ScannerListener implements ImageListener{

	@Override
	public boolean imageReady(Image newImage) {
		return true; //Add custom processing
	}

}
