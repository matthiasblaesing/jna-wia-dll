package libs;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Native;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {
        Kernel32 INSTANCE = Native.loadLibrary("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);
    
	//http://msdn.microsoft.com/en-us/library/ms679277(VS.85).aspx
	boolean Beep(int freq, int duration);
	
	int GetCurrentDirectoryA(int bufLen, byte buffer[]);
	boolean SetCurrentDirectoryA(String dir);

	int LoadLibraryA(String lib);
	Pointer GlobalLock(HGLOBAL hdl);
	boolean GlobalUnlock(HGLOBAL hdl);
	int GlobalFree(HGLOBAL hdl);
	int GetLogicalDrives();
	int GetLogicalDriveStringsA(int bufLen, byte buf[]);
	//http://msdn.microsoft.com/en-us/library/aa364993(VS.85).aspx
	boolean GetVolumeInformationA(String lpRootPathName, byte lpVolumeNameBuffer[], 
			int nVolumeNameSize, int lpVolumeSerialNumber[], int lpMaximumComponentLength[], 
			int lpFileSystemFlags[], byte lpFileSystemNameBuffer[], int nFileSystemNameSize);
	int GetDriveTypeA(String drive);
	
/*	public static class SYSTEMTIME extends Structure {
		public int wYear;
		public int wMonth;
		public int wDayOfWeek;
		public int wDay;
		public int wHour;
		public int wMinute;
		public int wSecond;
		public int wMilliseconds;
	}
*/
	public static class SYSTEMTIME extends Structure {
		public short wYear;
		public short wMonth;
		public short wDayOfWeek;
		public short wDay;
		public short wHour;
		public short wMinute;
		public short wSecond;
		public short wMilliseconds;
		@Override
		protected List<String> getFieldOrder() {
			 return Arrays.asList(new String[] { "wYear", "wMonth", "wDayOfWeek", "wDay", "wHour", "wMinute", "wSecond", "wMilliseconds" });
		}
	}

	void GetSystemTime(SYSTEMTIME st);
	void GetLocalTime(SYSTEMTIME st);
        void GetComputerName();

	public static class FILETIME extends Structure {
		public int dwLowDateTime;
		public int dwHighDateTime;
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(new String[] { "dwLowDateTime", "dwHighDateTime" });
		}
	}
	
	boolean GetProcessTimes(HANDLE processHdl, FILETIME creation, FILETIME exit, FILETIME kernel, FILETIME user);
	boolean GetSystemTimes(FILETIME idle, FILETIME kernel, FILETIME user);
	
	public static class SECURITY_ATTRIBUTES extends Structure {
		int nLength;
		Pointer lpSecurityDescriptor;
		boolean bInheritHandle;
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(new String[] { "dwLength", "lpSecurityDescriptor", "bInheritHandle" });
		}
	}

	int CreateFileA(String file, int access, int mode, SECURITY_ATTRIBUTES secAttrs,
			int disposition, int flagsAndAttribs, int hdlTemplate);

	public static class OVERLAPPED extends Structure {
		int Internal;
		int InternalHigh;
		// union begins ...
		// embedded struct members ...
		int Offset;
		int OffsetHigh;
		// union member PVOID Pointer;
		int hEvent;
		@Override
		protected List<String> getFieldOrder() {
			 return Arrays.asList(new String[] { "Internal", "InternalHigh", "Offset", "OffsetHigh", "hEvent" });
		}
	}
	
	//http://msdn.microsoft.com/en-us/library/aa363411(VS.85).aspx
	//http://msdn.microsoft.com/en-us/library/aa363226(VS.85).aspx	
	boolean DeviceIoControl(int hdl, int opCode, byte inBuf[], int inBufSize, 
			byte outBuf[], int outBufSize, int bytesReturned[], OVERLAPPED ol);

	static class SYSTEM_POWER_STATUS extends Structure {
		public byte ACLineStatus;
		public byte BatteryFlag;
		public byte BatteryLifePercent;
		public byte Reserved1;
		public int BatteryLifeTime;
		public int BatteryFullLifeTime;
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("ACLineStatus", "BatteryFlag","BatteryLifePercent","Reserved1","BatteryLifeTime","BatteryFullLifeTime");
		}
	}
	boolean GetSystemPowerStatus(SYSTEM_POWER_STATUS sps);

	boolean GetDiskFreeSpaceA(String s, IntByReference r1, IntByReference r2, IntByReference r3, IntByReference r4);
	boolean GetDiskFreeSpaceEx(String s, IntByReference r1, IntByReference r2, IntByReference r3);

        public static class HGLOBAL extends HANDLE {

                public HGLOBAL() {
                }

                public HGLOBAL(Pointer p) {
                    super(p);
                }

                @Override
                public Object fromNative(Object nativeValue, FromNativeContext context) {
                    return new HGLOBAL((Pointer) nativeValue);
                }

        }
}
