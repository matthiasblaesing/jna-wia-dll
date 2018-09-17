package net.javajeff.jtwain;

import java.awt.Image;

import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPVOID;
import com.sun.jna.ptr.IntByReference;

import libs.Kernel32;
import libs.Win32Twain;
import libs.Win32Twain.BITMAPINFOHEADER;
import libs.Win32Twain.TW_EVENT;
import libs.Win32Twain.TW_IDENTITY;
import libs.Win32Twain.TW_IMAGEINFO;
import libs.Win32Twain.TW_PENDINGXFERS;
import libs.Win32Twain.TW_STATUS;
import libs.Win32Twain.TW_USERINTERFACE;

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
	private static com.sun.jna.platform.win32.Kernel32 jKernel32 = com.sun.jna.platform.win32.Kernel32.INSTANCE;

	public static boolean init(boolean loadDSM) {
		kernel32 = Native.loadLibrary("kernel32", Kernel32.class);
		if (loadDSM) {
			twain = Native.loadLibrary("TWAINDSM", Win32Twain.class);
		} else {
			if (System.getProperty("sun.arch.data.model").indexOf("64") != -1) {
				twain = Native.loadLibrary("TWAINDSM", Win32Twain.class);
			} else {
				twain = Native.loadLibrary("Twain_32", Win32Twain.class);
			}
		}
		return true;
	}

	private JNATwain() {
	}

	public static Image acquire(boolean showGui, ImageListener imageListener) throws JTwainException {
		HWND hWnd = jUser32.CreateWindowEx(0, "Static", "", 0x80000000, 0x80000000, 0x80000000, 0x80000000, 0x80000000,
				jUser32.GetForegroundWindow(), null,
				jKernel32.GetModuleHandle("")/* Win32TwainLibrary.HMODULE */, null);
		boolean ok = jUser32.SetWindowPos(hWnd, getTophwnd(), 0, 0, 0, 0, 0x0001);
		if (!ok) {
			jUser32.DestroyWindow(hWnd);
			throw new JTwainException("Unable to position private window (select)");
		}
		int nativeHwnd = Math.toIntExact(Pointer.nativeValue(hWnd.getPointer()));
		int stat = OpenDSM(g_AppID, nativeHwnd);
		if (stat != TwainConstants.TWRC_SUCCESS) {
			jUser32.DestroyWindow(hWnd);
			throw new JTwainException("Unable to open DSM");
		}
		// System.out.printf("app.Id: %d%n", app.Id);
		TW_IDENTITY srcID = new TW_IDENTITY();
		stat = GetDefaultSource(g_AppID, srcID);
		if (stat != TwainConstants.TWRC_SUCCESS) {
			CloseDSM(g_AppID, nativeHwnd);
			jUser32.DestroyWindow(hWnd);
			stat = GetConditionCode(g_AppID, srcID);
			throw new JTwainException("Unable to get default: " + stat);
		}
		System.out.printf("Selected M, F, N: %s, %s, %s%n", srcID.getManufacturer(), srcID.getProductFamily(),
				srcID.getProductName());
		stat = OpenDefaultSource(g_AppID, srcID);
		if (stat != TwainConstants.TWRC_SUCCESS) {
			CloseDSM(g_AppID, nativeHwnd);
			jUser32.DestroyWindow(hWnd);
			stat = GetConditionCode(g_AppID, srcID);
			throw new JTwainException("Unable to open default: " + stat);
		}

		TW_USERINTERFACE ui = new TW_USERINTERFACE();
		ui.ShowUI = showGui;
		ui.ModalUI = false;
		ui.hParent = hWnd;

		stat = EnableDefaultSource(g_AppID, srcID, ui);
		if (stat != TwainConstants.TWRC_SUCCESS) {
			CloseDefaultSource(g_AppID, srcID);
			CloseDSM(g_AppID, nativeHwnd);
			jUser32.DestroyWindow(hWnd);
			stat = GetConditionCode(g_AppID, srcID);
			throw new JTwainException("Unable to enable default DS: " + stat);
		}
		TW_EVENT event = new TW_EVENT();
		TW_PENDINGXFERS pxfers = new TW_PENDINGXFERS();
		com.sun.jna.platform.win32.WinUser.MSG msg = new com.sun.jna.platform.win32.WinUser.MSG();
		try {
			while ((jUser32.GetMessage(msg, null, 0, 0)) != 0) {
				event.pEvent = new LPVOID(msg.getPointer());
				event.TWMessage = 0;
				stat = ProcessEvent(g_AppID, srcID, event);
				if (event.TWMessage == TwainConstants.MSG_CLOSEDSREQ) {
					break;
				}
				if (stat == TwainConstants.TWRC_FAILURE) {
					throw new JTwainException("Unable to obtain image information (acquire)");
				}
				if (stat == TwainConstants.TWRC_NOTDSEVENT) {
					jUser32.TranslateMessage(msg);
					jUser32.DispatchMessage(msg);
					continue;
				}
				if (event.TWMessage == TwainConstants.MSG_XFERREADY) {
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
		} finally {
			DisableDefaultSource(g_AppID, srcID, ui);
			CloseDefaultSource(g_AppID, srcID);
			CloseDSM(g_AppID, nativeHwnd);
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
		if (stat == TwainConstants.TWRC_FAILURE) {
			ResetPendingTransfers(g_AppID, srcID, pxfers);
			throw new JTwainException("Unable to obtain image information (acquire)");
		}
		if (ii.Compression != TwainConstants.TWCP_NONE || (ii.BitsPerPixel != 8 && ii.BitsPerPixel != 24)) {
			// Cancel all transfers.
			ResetPendingTransfers(g_AppID, srcID, pxfers);
			throw new JTwainException("Image compressed or not 8-bit/24-bit (acquire)");
		}
		int hdl[] = new int[1];
		stat = PerformImageTransfer(g_AppID, srcID, hdl);
		if (stat != TwainConstants.TWRC_XFERDONE) {
			ResetPendingTransfers(g_AppID, srcID, pxfers);
			throw new JTwainException("User aborted transfer or failure (acquire)");
		}
		Pointer p = kernel32.GlobalLock(hdl[0]);
		if (p != null) {
			System.out.printf("handle: %s%n", p.toString());
			BITMAPINFOHEADER bmih = new BITMAPINFOHEADER(p);
			// dump(bmih);
			if (ii.BitsPerPixel == 8)
				image = xferDIB8toImage(bmih);
			else
				image = xferDIB24toImage(bmih);
			if (image == null)
				throw new JTwainException("Could not transfer DIB to Image (acquire)");
		}
		kernel32.GlobalUnlock(hdl[0]);
		kernel32.GlobalFree(hdl[0]);
		EndTransfers(g_AppID, srcID, pxfers);
		if (imageListener != null) {
			result = imageListener.imageReady(image);
		}
		return result;
	}

	public static int selectSourceAsDefault() throws JTwainException {
		HWND hWnd = jUser32.CreateWindowEx(com.sun.jna.platform.win32.User32.WS_EX_TOPMOST, "Static", "MyWindow1",
				0x80000000, 0x80000000, 0x80000000, 0x80000000, 0x80000000, jUser32.GetForegroundWindow(), null,
				jKernel32.GetModuleHandle(""), null);

		boolean ok = jUser32.SetWindowPos(hWnd, getTophwnd(), 0, 0, 0, 0, 0x0001);
		if (!ok) {
			jUser32.DestroyWindow(hWnd);
			throw new JTwainException("Unable to position private window (select)");
		}
		setupAppId(g_AppID);
		int nativeHwnd = Math.toIntExact(Pointer.nativeValue(hWnd.getPointer()));
		getError();
		int stat = OpenDSM(g_AppID, nativeHwnd);
		if (stat != TwainConstants.TWRC_SUCCESS) {
			jUser32.DestroyWindow(hWnd);
			throw new JTwainException("Unable to open DSM (select)");
		}

		TW_IDENTITY srcID = new TW_IDENTITY(Structure.ALIGN_DEFAULT);
		stat = SelectSource(g_AppID, srcID);
		if (stat != TwainConstants.TWRC_SUCCESS) {
			CloseDSM(g_AppID, nativeHwnd);
			jUser32.DestroyWindow(hWnd);
			if (stat == TwainConstants.TWRC_CANCEL)
				return stat;
			stat = GetConditionCode(g_AppID, srcID);
			throw new JTwainException("Unable to display user interface: " + stat);
		}

		/*
		 * dump(srcID); System.out.printf("ProtocolMajor: %02x%n",
		 * srcID.ProtocolMajor); System.out.printf("ProtocolMinor: %02x%n",
		 * srcID.ProtocolMinor); System.out.printf("SupportedGroups: %04x%n",
		 * srcID.SupportedGroups);
		 */
		System.out.printf("Manufacturer: %s%n", new String(srcID.Manufacturer, 0, 34));
		stat = CloseDSM(g_AppID, nativeHwnd);
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
		// System.out.printf("w: %d, h: %d%n", width, height);
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
		// System.out.printf("NumColors: %d, PadBytes: %d%n", numColors,
		// padBytes);
		int rowBytes = width + padBytes;
		byte bitmap[] = bmih.getPointer().getByteArray(bmih.size() + numColors * 4, height * rowBytes);
		int palette[] = bmih.getPointer().getIntArray(bmih.size(), numColors);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				// Extract color information for pixel and build an equivalent
				// Java pixel for storage in the Java-based integer array.
				byte bitVal = bitmap[rowBytes * row + col];
				// System.out.printf("%02x%s", bitVal, ((col % 16) == 0 && col
				// != 0) ? "\n" : " ");
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
		// System.out.printf("w: %d, h: %d%n", width, height);
		int pixels[] = new int[width * height];
		/*
		 * int numColors; if (bmih.biClrUsed > 0) numColors = bmih.biClrUsed;
		 * else numColors = (1 << bmih.biBitCount);
		 */int padBytes = (3 * width) % 4; // Each pixel occupies 1 byte
		// (palette index)
		// and the number of row bytes is a multiple of
		// 4.
		// System.out.printf("NumColors: %d, PadBytes: %d%n", numColors,
		// padBytes);
		int rowBytes = 3 * width + padBytes;
		byte bitmap[] = bmih.getPointer().getByteArray(bmih.size(), height * rowBytes);
		// int palette[] = bmih.getPointer().getIntArray(bmih.size(),
		// numColors);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				// Obtain pixel index;
				int index = rowBytes * row + col * 3;
				// System.out.printf("%02x%s", bitVal, ((col % 16) == 0 && col
				// != 0) ? "\n" : " ");
				int pixel = 0xff000000 | (bitmap[index + 2] & 0xff) << 16 | (bitmap[index + 1] & 0xff) << 8
						| (bitmap[index] & 0xff);
				// Store the pixel in the array at the appropriate index.
				pixels[width * (height - row - 1) + col] = pixel;
			}
		}
		MemoryImageSource mis = new MemoryImageSource(width, height, pixels, 0, width);
		return Toolkit.getDefaultToolkit().createImage(mis);
	}

	/* Referencehttps://www.twain.org/wp-content/uploads/2016/03/twain1.h */
	private static void setupAppId(TW_IDENTITY appID) {
		appID.Id = 0;
		appID.ProtocolMajor = 2;
		appID.ProtocolMinor = 2;
		appID.SupportedGroups = (TwainConstants.DG_CONTROL | TwainConstants.DG_IMAGE);
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

	private static int OpenDSM(TW_IDENTITY application, int winHdl) {
		int stat = twain.DSM_Entry(application, null, TwainConstants.DG_CONTROL, TwainConstants.DAT_PARENT,
				TwainConstants.MSG_OPENDSM,
				/* winHdl */ // does not work
				/* new int[] {winHdl} */ // works
				new IntByReference(winHdl));
		return stat;
	}

	private static int GetDefaultSource(TW_IDENTITY application, TW_IDENTITY src) {
		int stat = twain.DSM_Entry(application, null, TwainConstants.DG_CONTROL, TwainConstants.DAT_IDENTITY,
				TwainConstants.MSG_GETDEFAULT, src);
		return stat;
	}

	private static int OpenDefaultSource(TW_IDENTITY application, TW_IDENTITY src) {
		int stat = twain.DSM_Entry(application, null, TwainConstants.DG_CONTROL, TwainConstants.DAT_IDENTITY,
				TwainConstants.MSG_OPENDS, src);
		return stat;
	}

	private static int CloseDefaultSource(TW_IDENTITY application, TW_IDENTITY src) {
		int stat = twain.DSM_Entry(application, null, TwainConstants.DG_CONTROL, TwainConstants.DAT_IDENTITY,
				TwainConstants.MSG_CLOSEDS, src);
		return stat;
	}

	private static int SelectSource(TW_IDENTITY application, TW_IDENTITY src) {
		int stat = twain.DSM_Entry(application, null, TwainConstants.DG_CONTROL, TwainConstants.DAT_IDENTITY,
				TwainConstants.MSG_USERSELECT, src);
		return stat;
	}

	private static int EnableDefaultSource(TW_IDENTITY application, TW_IDENTITY src, TW_USERINTERFACE ui) {
		int stat = twain.DSM_Entry(application, src, TwainConstants.DG_CONTROL, TwainConstants.DAT_USERINTERFACE,
				TwainConstants.MSG_ENABLEDS, ui);
		return stat;
	}

	private static int DisableDefaultSource(TW_IDENTITY application, TW_IDENTITY src, TW_USERINTERFACE ui) {
		int stat = twain.DSM_Entry(application, src, TwainConstants.DG_CONTROL, TwainConstants.DAT_USERINTERFACE,
				TwainConstants.MSG_DISABLEDS, ui);
		return stat;
	}

	private static int GetImageInfo(TW_IDENTITY application, TW_IDENTITY src, TW_IMAGEINFO ii) {
		int stat = twain.DSM_Entry(application, src, TwainConstants.DG_IMAGE, TwainConstants.DAT_IMAGEINFO,
				TwainConstants.MSG_GET, ii);
		return stat;
	}

	private static int ResetPendingTransfers(TW_IDENTITY application, TW_IDENTITY src, TW_PENDINGXFERS xfers) {
		int stat = twain.DSM_Entry(application, src, TwainConstants.DG_CONTROL, TwainConstants.DAT_PENDINGXFERS,
				TwainConstants.MSG_RESET, xfers);
		return stat;
	}

	private static int EndTransfers(TW_IDENTITY application, TW_IDENTITY src, TW_PENDINGXFERS xfers) {
		int stat = twain.DSM_Entry(application, src, TwainConstants.DG_CONTROL, TwainConstants.DAT_PENDINGXFERS,
				TwainConstants.MSG_ENDXFER, xfers);
		return stat;
	}

	private static int ProcessEvent(TW_IDENTITY application, TW_IDENTITY src, TW_EVENT event) {
		int stat = twain.DSM_Entry(application, src, TwainConstants.DG_CONTROL, TwainConstants.DAT_EVENT,
				TwainConstants.MSG_PROCESSEVENT, event);
		return stat;
	}

	private static int PerformImageTransfer(TW_IDENTITY application, TW_IDENTITY src, int hdl[]) {
		int stat = twain.DSM_Entry(application, src, TwainConstants.DG_IMAGE, TwainConstants.DAT_IMAGENATIVEXFER,
				TwainConstants.MSG_GET, hdl);
		return stat;
	}

	private static int CloseDSM(TW_IDENTITY application, int winHdl) {
		int stat = twain.DSM_Entry(application, null, TwainConstants.DG_CONTROL, TwainConstants.DAT_PARENT,
				TwainConstants.MSG_CLOSEDSM, winHdl);
		return stat;
	}

	private static int GetConditionCode(TW_IDENTITY application, TW_IDENTITY src) {
		TW_STATUS status = new TW_STATUS();
		int stat = twain.DSM_Entry(application, null, TwainConstants.DG_CONTROL, TwainConstants.DAT_STATUS,
				TwainConstants.MSG_GET, status);
		return (stat == 0) ? status.ConditionCode : ((stat << 16) + status.ConditionCode);
	}

	@SuppressWarnings("unused")
	private static void dump(Structure s) {
		s.write();
		int size = s.size();
		System.out.printf("Structure: %s  Size: %d%n", s.getClass().getSimpleName(), size);
		Pointer p = s.getPointer();
		byte bb[] = p.getByteArray(0, size);
		outer: for (int i = 0;; i += 16) {
			System.out.printf("%03d: ", i);
			for (int j = 0; j < 16; ++j) {
				int k = i + j;
				if (k >= size)
					break;
				System.out.printf(" %02x", bb[k]);
			}
			System.out.println();
			System.out.printf("%03d: ", i);
			for (int j = 0; j < 16; ++j) {
				int k = i + j;
				if (k >= size)
					break outer;
				byte b = bb[k];
				System.out.printf("  %c", (b >= 32 && b < 127) ? b : '.');
			}
			System.out.println();
		}
		System.out.println();
	}

	private static HWND getTophwnd() {
		HWND HWND_TOP = new HWND();
		Pointer p = new Pointer(0);
		HWND_TOP.setPointer(p);
		return HWND_TOP;
	}

	private static int getError() {
		int rc = com.sun.jna.platform.win32.Kernel32.INSTANCE.GetLastError();

		if (rc != 0)
			System.out.println("error: " + rc);

		return rc;
	}

	public static Win32Twain twain = null;
	private static Kernel32 kernel32 = null;
	private static Image image = null;

	private static TW_IDENTITY g_AppID = new TW_IDENTITY();

}
