package net.javajeff.jtwain;

public interface TwainConstants {
	
	
	public static final short MSG_GET = 1;
	
	public static final short MSG_GETDEFAULT = 3;
	
	public static final short MSG_RESET = 7;
	
	public static final short MSG_CLOSEDSM = 0x0302;
	
	public static final short MSG_OPENDSM = 0x0301;
	
	public static final short MSG_OPENDS = 0x0401;
	
	public static final short MSG_CLOSEDS = 0x0402;
	
	public static final short MSG_USERSELECT = 0x0403;
	
	public static final short MSG_ENABLEDS = 0x0502;
	
	public static final short MSG_DISABLEDS = 0x0501;
	
	public static final short MSG_PROCESSEVENT = 0x0601;
	
	public static final short MSG_ENDXFER = 0x0701;
	
	public static final short DAT_EVENT = 2;
	
	public static final short DAT_IDENTITY = 3;
	
	public static final short DAT_PARENT = 4;
	
	public static final short DAT_PENDINGXFERS = 5;
	
	public static final short DAT_STATUS = 8;
	
	public static final short DAT_USERINTERFACE = 9;
	
	public static final short DAT_IMAGEINFO = 0x0101;
	
	public static final short DAT_IMAGENATIVEXFER = 0x0104;
	
	public static final int MSG_CLOSEDSREQ = 0x0102;
	
	public static final int MSG_XFERREADY = 0x0101;
	
	public static final int TWCP_NONE = 0;
	
	public static final short DG_CONTROL = 1;
	
	public static final short DG_IMAGE = 2;
	
	public static final short G_AUDIO = 4;
	
	public static final int TWRC_SUCCESS = 0;
	
	public static final int TWRC_FAILURE = 1;
	
	public static final int twrc_checkstatus = 2;
	
	public static final int TWRC_CANCEL = 3;
	
	public static final int TWRC_NOTDSEVENT = 5;
	
	public static final int TWRC_XFERDONE = 6;
	
	public static final int HWND_DESKTOP = 0x10014;
	
	public static final int WS_POPUPWINDOW = 0x80000000 | 0x00800000 | 0x00080000;
	
	public static final int CW_USEDEFAULT = 0x80000000;
	
	public static final int HWND_TOPMOST = -1;
	
	public static final int HWND_TOP = 0;
	
	public static final int SWP_NOSIZE = 1;

}
