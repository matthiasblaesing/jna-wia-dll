
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import libs.Win32Twain;
import libs.Win32Twain.TW_ENTRYPOINT;
import libs.Win32Twain.TW_IDENTITY;
import libs.Win32TwainUtil;
import static libs.Win32TwainUtil.EnableDefaultSource;


public class TwainSample {
    public static void main(String[] args) throws Exception {
        MessageLoopThread mlt = new MessageLoopThread();

        mlt.start();

        int res;

        WinDef.HWND hWnd = mlt.runOnThread(() -> 
            User32.INSTANCE.CreateWindowEx(User32.WS_EX_TOPMOST, "Static", "MyWindow1",
                0x80000000, 0x80000000, 0x80000000, 0x80000000, 0x80000000, User32.INSTANCE.GetForegroundWindow(), null,
                Kernel32.INSTANCE.GetModuleHandle(""), null)
        );

        final TW_IDENTITY identity = new TW_IDENTITY();
        identity.Version.MajorNum = 2;
        identity.Version.MinorNum = 0;
        identity.Version.Language = Win32Twain.TWLG_ENGLISH_CANADIAN;
        identity.Version.Country = Win32Twain.TWCY_CANADA;
        identity.Version.setInfo("2.0.9");
        identity.ProtocolMajor = 2;
        identity.ProtocolMinor = 2;
        identity.SupportedGroups = Win32Twain.DF_APP2 | Win32Twain.DG_IMAGE | Win32Twain.DG_CONTROL;
        identity.setManufacturer("App's Manufacturer");
        identity.setProductFamily("App's Product Family");
        identity.setProductName("Specific App Product Name");
        identity.write();

        res = mlt.runOnThread(() -> Win32TwainUtil.OpenDSM(identity, hWnd));

        System.out.println("OpenDSM: " + res);

        System.out.println("DF_DSM2: " + (identity.SupportedGroups & Win32Twain.DF_DSM2));

        TW_ENTRYPOINT entryPoint = new Win32Twain.TW_ENTRYPOINT();

        res = mlt.runOnThread(() -> Win32TwainUtil.GetEntryPoint(identity, entryPoint));

        System.out.println("GetEntryPoint: " + res);

        List<TW_IDENTITY> sources = mlt.runOnThread(() -> getDatasources(identity));

        System.out.println("First Source: " + sources.get(0).getManufacturer() + " - " + sources.get(0).getProductFamily() + " - " + sources.get(0).getProductName());

        res = mlt.runOnThread(() -> Win32TwainUtil.OpenDataSource(identity, sources.get(0)));

        System.out.println("Selected source: " + res);

        Win32Twain.TW_USERINTERFACE ui = new Win32Twain.TW_USERINTERFACE();
        ui.setShowUI(true);
        ui.setModalUI(true);
        ui.hParent = hWnd;

        System.out.println(ui);

        res = mlt.runOnThread(() -> EnableDefaultSource(identity, sources.get(0), ui));
        
        System.out.println("EnableDefaultSource: " + res);

        MessageLoopThread.EventInterceptor ei = new MessageLoopThread.EventInterceptor() {
            @Override
            public boolean intercept(WinUser.MSG msg) {
                Win32Twain.TW_EVENT event = new Win32Twain.TW_EVENT();
                event.pEvent = msg.getPointer();
                event.TWMessage = 0;
                int res = Win32TwainUtil.ProcessEvent(identity, sources.get(0), event);
                boolean handled = res != Win32Twain.TWRC_NOTDSEVENT;
                if(handled) {
                    System.out.println(event);
                }
                return handled;
            }
        };

        mlt.addEventInterceptor(ei);

        Thread.sleep(5 * 1000);

        mlt.exit();
    }

    private static List<TW_IDENTITY> getDatasources(TW_IDENTITY identity) {
        List<TW_IDENTITY> result = new ArrayList<>();

        int res;

        {
            TW_IDENTITY entry = new TW_IDENTITY();
            res = Win32TwainUtil.GetFirstDatasource(identity, entry);

            if (res == Win32Twain.TWRC_SUCCESS) {
                result.add(entry);
            } else if (res == Win32Twain.TWRC_ENDOFLIST) {
                return result;
            } else {
                throw new RuntimeException("Unknown error code: " + res);
            }
        }

        while(true) {
            TW_IDENTITY entry = new TW_IDENTITY();
            res = Win32TwainUtil.GetNextDatasource(identity, entry);

            if (res == Win32Twain.TWRC_SUCCESS) {
                result.add(entry);
            } else if (res == Win32Twain.TWRC_ENDOFLIST) {
                return result;
            } else {
                throw new RuntimeException("Unknown error code: " + res);
            }
        }
    }


    /**
     * Helper class, that runs a windows message loop as a seperate thread.
     *
     * This is intended to be used in conjunction with APIs, that need a
     * spinning message loop. One example for this are the DDE functions, that
     * can only be used if a message loop is present.
     *
     * To enable interaction with the mainloop the MessageLoopThread allows to
     * dispatch callables into the mainloop and let these Callables be invoked
     * on the message thread.
     *
     * This implies, that the Callables should block the loop as short as possible.
     */
    public static class MessageLoopThread extends Thread {

        public class Handler implements InvocationHandler {

            private final Object delegate;

            public Handler(Object delegate) {
                this.delegate = delegate;
            }

            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                try {
                    return MessageLoopThread.this.runOnThread(new Callable<Object>() {
                        public Object call() throws Exception {
                            return method.invoke(delegate, args);
                        }
                    });
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof Exception) {
                        StackTraceElement[] hiddenStack = cause.getStackTrace();
                        cause.fillInStackTrace();
                        StackTraceElement[] currentStack = cause.getStackTrace();
                        StackTraceElement[] fullStack = new StackTraceElement[currentStack.length + hiddenStack.length];
                        System.arraycopy(hiddenStack, 0, fullStack, 0, hiddenStack.length);
                        System.arraycopy(currentStack, 0, fullStack, hiddenStack.length, currentStack.length);
                        cause.setStackTrace(fullStack);
                        throw (Exception) cause;
                    } else {
                        throw ex;
                    }
                }
            }
        }

        private volatile int nativeThreadId = 0;
        private volatile long javaThreadId = 0;
        private final List<FutureTask> workQueue = Collections.synchronizedList(new ArrayList<FutureTask>());
        private static long messageLoopId = 0;
        private CopyOnWriteArrayList<EventInterceptor> eventInterceptor = new CopyOnWriteArrayList<>();

        public void addEventInterceptor(EventInterceptor ei) {
            eventInterceptor.add(ei);
        }

        public void removeEventInterceptor(EventInterceptor ei) {
            eventInterceptor.remove(ei);
        }

        public MessageLoopThread() {
            setName("JNA User32 MessageLoop " + (++messageLoopId));
        }

        @Override
        public void run() {
            WinUser.MSG msg = new WinUser.MSG();

            // Make sure message loop is prepared
            User32.INSTANCE.PeekMessage(msg, null, 0, 0, 0);
            javaThreadId = Thread.currentThread().getId();
            nativeThreadId = Kernel32.INSTANCE.GetCurrentThreadId();

            int getMessageReturn;
            LOOP: while ((getMessageReturn = User32.INSTANCE.GetMessage(msg, null, 0, 0)) != 0) {
                if (getMessageReturn != -1) {
                    // Normal processing
                    while (!workQueue.isEmpty()) {
                        try {
                            FutureTask ft = workQueue.remove(0);
                            ft.run();
                        } catch (IndexOutOfBoundsException ex) {
                            break;
                        }
                    }
                    for(EventInterceptor ei: eventInterceptor) {
                        if(ei.intercept(msg)) {
                            continue LOOP;
                        }
                    }
                    User32.INSTANCE.TranslateMessage(msg);
                    User32.INSTANCE.DispatchMessage(msg);
                } else {
                    // Error case
                    if(getMessageFailed()) {
                        break;
                    }
                }
            }

            while (!workQueue.isEmpty()) {
                workQueue.remove(0).cancel(false);
            }
        }

        public <V> Future<V> runAsync(Callable<V> command) {
            while(nativeThreadId == 0) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MessageLoopThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            FutureTask<V> futureTask = new FutureTask<V>(command);
            workQueue.add(futureTask);
            User32.INSTANCE.PostThreadMessage(nativeThreadId, WinUser.WM_USER, null, null);
            return futureTask;
        }

        public <V> V runOnThread(Callable<V> callable) throws Exception {
            while (javaThreadId == 0) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MessageLoopThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if(javaThreadId == Thread.currentThread().getId()) {
                return callable.call();
            } else {

                Future<V> ft = runAsync(callable);
                try {
                    return ft.get();
                } catch (InterruptedException ex) {
                    throw ex;
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof Exception) {
                        throw (Exception) cause;
                    } else {
                        throw ex;
                    }
                }
            }
        }

        public void exit() {
            User32.INSTANCE.PostThreadMessage(nativeThreadId, WinUser.WM_QUIT, null, null);
        }

        /**
         * The method is called from the thread, that run the message dispatcher,
         * when the call to {@link com.sun.jna.platform.win32.User32#GetMessage}
         * fails (returns {@code -1}).
         *
         * <p>If the method returns {@code true}, the MainLoop is exitted, if it
         * returns {@code false} the mainloop is resumed.</p>
         *
         * <p>Default behavior: The error code is logged to the
         * com.sun.jna.platform.win32.User32Util.MessageLoopThread logger and
         * the main loop exists.
         * </p>
         *
         * @return true if MainLoop should exit, false it it should resume
         */
        protected boolean getMessageFailed() {
            int lastError = Kernel32.INSTANCE.GetLastError();
            Logger.getLogger("com.sun.jna.platform.win32.User32Util.MessageLoopThread")
                    .log(Level.WARNING,
                            "Message loop was interrupted by an error. [lastError: {0}]",
                            lastError);
            return true;
        }

        public static interface EventInterceptor {
            boolean intercept(WinUser.MSG msg);
        }
    }
}
