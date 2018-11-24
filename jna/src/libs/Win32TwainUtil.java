package libs;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

public final class Win32TwainUtil {

    private Win32TwainUtil() {
    }

    public static int OpenDSM(Win32Twain.TW_IDENTITY application) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_PARENT,
                Win32Twain.MSG_OPENDSM,
                (Pointer) null);
        return stat;
    }

    public static int OpenDSM(Win32Twain.TW_IDENTITY application, WinNT.HANDLE data) {
        WinNT.HANDLEByReference byRef = new WinNT.HANDLEByReference(data);
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_PARENT,
                Win32Twain.MSG_OPENDSM,
                byRef);
        return stat;
    }

    public static int GetDefaultSource(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY src) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_IDENTITY,
                Win32Twain.MSG_GETDEFAULT, src);
        return stat;
    }

    public static int OpenDataSource(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY src) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_IDENTITY,
                Win32Twain.MSG_OPENDS, src);
        return stat;
    }

    public static int CloseDefaultSource(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY src) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_IDENTITY,
                Win32Twain.MSG_CLOSEDS, src);
        return stat;
    }

    public static int SelectSource(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY data) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_IDENTITY,
                Win32Twain.MSG_USERSELECT, data);
        return stat;
    }

    public static int EnableDefaultSource(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY src, Win32Twain.TW_USERINTERFACE ui) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, src, Win32Twain.DG_CONTROL, Win32Twain.DAT_USERINTERFACE,
                Win32Twain.MSG_ENABLEDS, ui);
        return stat;
    }

    public static int DisableDefaultSource(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY src, Win32Twain.TW_USERINTERFACE ui) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, src, Win32Twain.DG_CONTROL, Win32Twain.DAT_USERINTERFACE,
                Win32Twain.MSG_DISABLEDS, ui);
        return stat;
    }

    public static int GetImageInfo(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY src, Win32Twain.TW_IMAGEINFO ii) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, src, Win32Twain.DG_IMAGE, Win32Twain.DAT_IMAGEINFO,
                Win32Twain.MSG_GET, ii);
        return stat;
    }

    public static int ResetPendingTransfers(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY src, Win32Twain.TW_PENDINGXFERS xfers) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, src, Win32Twain.DG_CONTROL, Win32Twain.DAT_PENDINGXFERS,
                Win32Twain.MSG_RESET, xfers);
        return stat;
    }

    public static int EndTransfers(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY src, Win32Twain.TW_PENDINGXFERS xfers) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, src, Win32Twain.DG_CONTROL, Win32Twain.DAT_PENDINGXFERS,
                Win32Twain.MSG_ENDXFER, xfers);
        return stat;
    }

    public static int ProcessEvent(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY src, Win32Twain.TW_EVENT event) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, src, Win32Twain.DG_CONTROL, Win32Twain.DAT_EVENT,
                Win32Twain.MSG_PROCESSEVENT, event);
        return stat;
    }

    public static int PerformImageTransfer(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY src, PointerByReference pbr) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, src, Win32Twain.DG_IMAGE, Win32Twain.DAT_IMAGENATIVEXFER,
                Win32Twain.MSG_GET, pbr);
        return stat;
    }

    public static int CloseDSM(Win32Twain.TW_IDENTITY application) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_PARENT,
                Win32Twain.MSG_CLOSEDSM, (Pointer) null);
        return stat;
    }

    public static int CloseDSM(Win32Twain.TW_IDENTITY application, WinNT.HANDLE winHdl) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_PARENT,
                Win32Twain.MSG_CLOSEDSM, winHdl);
        return stat;
    }

    public static int GetConditionCode(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY src) {
        Win32Twain.TW_STATUS status = new Win32Twain.TW_STATUS();
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_STATUS,
                Win32Twain.MSG_GET, status);
        return (stat == 0) ? status.ConditionCode : ((stat << 16) + status.ConditionCode);
    }

    public static int GetEntryPoint(Win32Twain.TW_IDENTITY application, Win32Twain.TW_ENTRYPOINT entryPoint) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_ENTRYPOINT,
                Win32Twain.MSG_GET, entryPoint);
        return stat;
    }

    public static int GetFirstDatasource(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY info) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_IDENTITY,
                Win32Twain.MSG_GETFIRST, info);
        return stat;
    }

    public static int GetNextDatasource(Win32Twain.TW_IDENTITY application, Win32Twain.TW_IDENTITY info) {
        int stat = Win32Twain.INSTANCE.DSM_Entry(application, null, Win32Twain.DG_CONTROL, Win32Twain.DAT_IDENTITY,
                Win32Twain.MSG_GETNEXT, info);
        return stat;
    }
}
