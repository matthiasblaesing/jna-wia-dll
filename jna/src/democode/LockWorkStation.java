package democode;

import com.sun.jna.Native;

public class LockWorkStation {
  public static void main(String[] args) {
    User32 user32 = (User32) Native.loadLibrary("user32", User32.class);
    user32.LockWorkStation();
  }
}
