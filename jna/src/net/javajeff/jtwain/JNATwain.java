package net.javajeff.jtwain;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.PointerByReference;
import java.awt.image.BufferedImage;

import libs.Kernel32;
import libs.Kernel32.HGLOBAL;
import libs.Win32Twain;
import libs.Win32Twain.BITMAPINFOHEADER;
import libs.Win32Twain.TW_EVENT;
import libs.Win32Twain.TW_IDENTITY;
import libs.Win32Twain.TW_IMAGEINFO;
import libs.Win32Twain.TW_PENDINGXFERS;
import libs.Win32Twain.TW_USERINTERFACE;
import static libs.Win32TwainUtil.CloseDSM;
import static libs.Win32TwainUtil.CloseDefaultSource;
import static libs.Win32TwainUtil.DisableDefaultSource;
import static libs.Win32TwainUtil.EnableDefaultSource;
import static libs.Win32TwainUtil.EndTransfers;
import static libs.Win32TwainUtil.GetConditionCode;
import static libs.Win32TwainUtil.GetDefaultSource;
import static libs.Win32TwainUtil.GetImageInfo;
import static libs.Win32TwainUtil.OpenDSM;
import static libs.Win32TwainUtil.PerformImageTransfer;
import static libs.Win32TwainUtil.ProcessEvent;
import static libs.Win32TwainUtil.ResetPendingTransfers;
import static libs.Win32TwainUtil.SelectSource;
import static libs.Win32TwainUtil.OpenDataSource;

/**
 * The class JNATwain uses JNA mechanism to provide window handle that will act
 * as parent for twain data source. 32/64 bit Twain_32.DLL / TWAINDSM.DLL loading
 * is supported.
 * 
 * @author Ashwini Sutar
 *
 */
public class JNATwain {

	private static User32 jUser32 = com.sun.jna.platform.win32.User32.INSTANCE;
	private static final Logger logger = Logger.getLogger(JNATwain.class.getName());

	private JNATwain() {
	}

	public static Image acquire(boolean showGui, ImageListener imageListener) throws JTwainException {
		HWND hWnd = jUser32.CreateWindowEx(0, "Static", "", 0x80000000, 0x80000000, 0x80000000, 0x80000000, 0x80000000,
				jUser32.GetForegroundWindow(), null,
				kernel32.GetModuleHandle("")/* Win32TwainLibrary.HMODULE */, null);
		boolean ok = jUser32.SetWindowPos(hWnd, getTophwnd(), 0, 0, 0, 0, 0x0001);
		if (!ok) {
			jUser32.DestroyWindow(hWnd);
			throw new JTwainException("Unable to position private window (select)");
		}
		int stat = OpenDSM(g_AppID, hWnd);
		if (stat != Win32Twain.TWRC_SUCCESS) {
			jUser32.DestroyWindow(hWnd);
			throw new JTwainException("Unable to open DSM");
		}
		TW_IDENTITY srcID = new TW_IDENTITY();
		stat = GetDefaultSource(g_AppID, srcID);
		if (stat != Win32Twain.TWRC_SUCCESS) {
			CloseDSM(g_AppID, hWnd);
			jUser32.DestroyWindow(hWnd);
			stat = GetConditionCode(g_AppID, srcID);
			throw new JTwainException("Unable to get default: " + stat);
		}
		logger.log(Level.INFO, "Selected M, F, N: {0} {1} {2} ", new Object[] { srcID.getManufacturer(), srcID.getProductFamily(), srcID.getProductName()});
			
		stat = OpenDataSource(g_AppID, srcID);
		if (stat != Win32Twain.TWRC_SUCCESS) {
			CloseDSM(g_AppID, hWnd);
			jUser32.DestroyWindow(hWnd);
			stat = GetConditionCode(g_AppID, srcID);
			throw new JTwainException("Unable to open default: " + stat);
		}

		TW_USERINTERFACE ui = new TW_USERINTERFACE();
		ui.setShowUI(showGui);
		ui.setModalUI(false);
		ui.hParent = hWnd;

		stat = EnableDefaultSource(g_AppID, srcID, ui);
		if (stat != Win32Twain.TWRC_SUCCESS) {
			CloseDefaultSource(g_AppID, srcID);
			CloseDSM(g_AppID, hWnd);
			jUser32.DestroyWindow(hWnd);
			stat = GetConditionCode(g_AppID, srcID);
			throw new JTwainException("Unable to enable default DS: " + stat);
		}
		TW_EVENT event = new TW_EVENT();
		TW_PENDINGXFERS pxfers = new TW_PENDINGXFERS();
		com.sun.jna.platform.win32.WinUser.MSG msg = new com.sun.jna.platform.win32.WinUser.MSG();
		try {
			while ((jUser32.GetMessage(msg, null, 0, 0)) != 0) {
				event.pEvent = msg.getPointer();
				event.TWMessage = 0;
				stat = ProcessEvent(g_AppID, srcID, event);
				if (event.TWMessage == Win32Twain.MSG_CLOSEDSREQ) {
					break;
				}
				if (stat == Win32Twain.TWRC_FAILURE) {
					throw new JTwainException("Unable to obtain image information (acquire)");
				}
				if (stat == Win32Twain.TWRC_NOTDSEVENT) {
					jUser32.TranslateMessage(msg);
					jUser32.DispatchMessage(msg);
					continue;
				}
                                
				if (event.TWMessage == Win32Twain.MSG_XFERREADY) {
					boolean processNext = processXFERREADY(srcID, pxfers, imageListener);
					if (!processNext) {
						break;
					}
					while ((pxfers.Count == -1 || pxfers.Count > 0)) {
						boolean processImg = processXFERREADY(srcID, pxfers, imageListener);
						if (!processImg) {
							break;
						}
					}
					if (pxfers.Count == 0) {
						break;
					}
				}
			}
                } catch (java.lang.Error ex) {
                    ex.printStackTrace();
                    System.out.println(msg.toString(true));
		} finally {
			DisableDefaultSource(g_AppID, srcID, ui);
			CloseDefaultSource(g_AppID, srcID);
			CloseDSM(g_AppID, hWnd);
			jUser32.DestroyWindow(hWnd);
		}
		return image;
	}

	/**
	 * The method processes the information that describes the image for the
	 * next transfer
	 * 
	 * @param srcID
	 * @param pxfers
	 * @param imageListener
	 * @throws JTwainException
	 */
	private static boolean processXFERREADY(TW_IDENTITY srcID, TW_PENDINGXFERS pxfers, ImageListener imageListener)
			throws JTwainException {
		boolean result = false;
		int stat;
		TW_IMAGEINFO ii = new TW_IMAGEINFO();
		stat = GetImageInfo(g_AppID, srcID, ii);
		if (stat == Win32Twain.TWRC_FAILURE) {
			ResetPendingTransfers(g_AppID, srcID, pxfers);
			throw new JTwainException("Unable to obtain image information (acquire)");
		}
		if (ii.Compression != Win32Twain.TWCP_NONE || (ii.BitsPerPixel != 8 && ii.BitsPerPixel != 24)) {
			// Cancel all transfers.
			ResetPendingTransfers(g_AppID, srcID, pxfers);
			throw new JTwainException("Image compressed or not 8-bit/24-bit (acquire)");
		}
                PointerByReference hdlHolder = new PointerByReference();
		stat = PerformImageTransfer(g_AppID, srcID, hdlHolder);
		if (stat != Win32Twain.TWRC_XFERDONE) {
			ResetPendingTransfers(g_AppID, srcID, pxfers);
			throw new JTwainException("User aborted transfer or failure (acquire)");
		}
                HGLOBAL hdl = new HGLOBAL(hdlHolder.getValue());
		Pointer p = kernel32.GlobalLock(hdl);
		if (p != null) {
			String handle = p.toString();
			logger.log(Level.INFO,"Image handle available: {0}",handle);
			BITMAPINFOHEADER bmih = new BITMAPINFOHEADER(p);
                        bmih.read();
			if (ii.BitsPerPixel == 8)
				image = xferDIB8toImage(bmih);
			else
				image = xferDIB24toImage(bmih);
			if (image == null)
				throw new JTwainException("Could not transfer DIB to Image (acquire)");
		}
		kernel32.GlobalUnlock(hdl);
//		kernel32.GlobalFree(hdl);
		EndTransfers(g_AppID, srcID, pxfers);
		if (imageListener != null) {
			result = imageListener.imageReady(image);
		}
		return result;
	}

	public static int selectSourceAsDefault() throws JTwainException {
		HWND hWnd = jUser32.CreateWindowEx(com.sun.jna.platform.win32.User32.WS_EX_TOPMOST, "Static", "MyWindow1",
				0x80000000, 0x80000000, 0x80000000, 0x80000000, 0x80000000, jUser32.GetForegroundWindow(), null,
				kernel32.GetModuleHandle(""), null);

		boolean ok = jUser32.SetWindowPos(hWnd, getTophwnd(), 0, 0, 0, 0, 0x0001);
		if (!ok) {
			jUser32.DestroyWindow(hWnd);
			throw new JTwainException("Unable to position private window (select)");
		}
		setupAppId(g_AppID);
		int nativeHwnd = Math.toIntExact(Pointer.nativeValue(hWnd.getPointer()));
		int error = getError();
		if (error != 0) {
			logger.log(Level.INFO, "Kernel32.INSTANCE.GetLastError: {0} ", error);
		}
		int stat = OpenDSM(g_AppID, hWnd);
		if (stat != Win32Twain.TWRC_SUCCESS) {
			jUser32.DestroyWindow(hWnd);
			throw new JTwainException("Unable to open DSM (select)");
		}

		TW_IDENTITY srcID = new TW_IDENTITY();
		stat = SelectSource(g_AppID, srcID);
		if (stat != Win32Twain.TWRC_SUCCESS) {
			CloseDSM(g_AppID, hWnd);
			jUser32.DestroyWindow(hWnd);
			if (stat == Win32Twain.TWRC_CANCEL)
				return stat;
			stat = GetConditionCode(g_AppID, srcID);
			throw new JTwainException("Unable to display user interface: " + stat);
		}

		stat = CloseDSM(g_AppID, hWnd);
		if (stat != 0) {
			jUser32.DestroyWindow(hWnd);
			throw new JTwainException("Unable to close DSM");
		}
		jUser32.DestroyWindow(hWnd);
		return stat;
	}

	private static Image xferDIB8toImage(BITMAPINFOHEADER bmih) {
		int width = bmih.biWidth;
		int height = bmih.biHeight; // height < 0 if bitmap is top-down
		if (height < 0)
			height = -height;
		
		int pixels[] = new int[width * height];
		int numColors;
		if (bmih.biClrUsed > 0)
			numColors = bmih.biClrUsed;
		else
			numColors = (1 << bmih.biBitCount);
		int padBytes = (4 - width % 4) % 4; // Each pixel occupies 1 byte
											// (palette index)
		// and the number of row bytes is a multiple of
		// 4.
		
		int rowBytes = width + padBytes;
		byte bitmap[] = bmih.getPointer().getByteArray(bmih.size() + numColors * 4, height * rowBytes);
		int palette[] = bmih.getPointer().getIntArray(bmih.size(), numColors);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				// Extract color information for pixel and build an equivalent
				// Java pixel for storage in the Java-based integer array.
				byte bitVal = bitmap[rowBytes * row + col];
				int pixel = 0xff000000 | palette[bitVal & 0xff];
				// Store the pixel in the array at the appropriate index.
				pixels[width * (height - row - 1) + col] = pixel;
			}
		}
		MemoryImageSource mis = new MemoryImageSource(width, height, pixels, 0, width);
		return Toolkit.getDefaultToolkit().createImage(mis);
	}

	private static Image xferDIB24toImage(BITMAPINFOHEADER bmih) {
		int width = bmih.biWidth;
		int height = bmih.biHeight; // height < 0 if bitmap is top-down
		if (height < 0)
			height = -height;
		int pixels[] = new int[width * height];
		/*
		 * int numColors; if (bmih.biClrUsed > 0) numColors = bmih.biClrUsed;
		 * else numColors = (1 << bmih.biBitCount);
		 */int padBytes = (3 * width) % 4; // Each pixel occupies 1 byte
		// (palette index)
		// and the number of row bytes is a multiple of
		// 4.
		int rowBytes = 3 * width + padBytes;
		byte bitmap[] = bmih.getPointer().getByteArray(bmih.size(), height * rowBytes);
		// int palette[] = bmih.getPointer().getIntArray(bmih.size(),
		// numColors);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				// Obtain pixel index;
				int index = rowBytes * row + col * 3;
				int pixel = 0xff000000 | (bitmap[index + 2] & 0xff) << 16 | (bitmap[index + 1] & 0xff) << 8
						| (bitmap[index] & 0xff);
				// Store the pixel in the array at the appropriate index.
				pixels[width * (height - row - 1) + col] = pixel;
			}
		}
		MemoryImageSource mis = new MemoryImageSource(width, height, pixels, 0, width);
		return Toolkit.getDefaultToolkit().createImage(mis);
	}

	/* Reference https://www.twain.org/wp-content/uploads/2016/03/twain1.h */
	public static void setupAppId(TW_IDENTITY appID) {
		appID.Id = 0;
		appID.ProtocolMajor = 2;
		appID.ProtocolMinor = 1;
		appID.SupportedGroups = (Win32Twain.DG_CONTROL | Win32Twain.DG_IMAGE);
		appID.setManufacturer("Swiss AviationSoftware");
		appID.setProductFamily("MRO Software");
		appID.setProductName("AMOS Client");
		appID.Version.MajorNum = 1;
		appID.Version.MinorNum = 0;
		appID.Version.Language = 13; // TWLG_ENGLISH_USA
		appID.Version.Country = 1; // TWCY_USA
		appID.Version.setInfo("JNA-JTwain 2.0");
		// dump(appID);
	}



	public static HWND getTophwnd() {
		HWND HWND_TOP = new HWND();
		Pointer p = new Pointer(0);
		HWND_TOP.setPointer(p);
		return HWND_TOP;
	}

	private static int getError() {
		return com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError();
	}

	private static Kernel32 kernel32 = Kernel32.INSTANCE;
	private static Image image = null;

	private static TW_IDENTITY g_AppID = new TW_IDENTITY();

}
