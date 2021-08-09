package com.ytech;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * For test dll loading
 */
public interface WindowsSDK extends Library {
    WindowsSDK INSTANCE = (WindowsSDK)
            Native.loadLibrary((Platform.isWindows() ? "msvcrt" : "c"),
                    WindowsSDK.class);

    void printf(String format, Object... args);
}


