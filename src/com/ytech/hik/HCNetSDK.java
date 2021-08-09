package com.ytech.hik;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import com.ytech.hik.structure.NET_DVR_DEVICEINFO_V40;
import com.ytech.hik.structure.NET_DVR_USER_LOGIN_INFO;


public interface HCNetSDK extends StdCallLibrary {
    HCNetSDK INSTANCE = (HCNetSDK) Native.loadLibrary(System.getProperty("user.dir") + "\\lib\\HCNetSDK.dll", HCNetSDK.class);

    /*** API函数声明 ***/

    // 初始化SDK，调用其他SDK函数的前提
    boolean NET_DVR_Init();

    // 启用日志文件写入接口
    boolean NET_DVR_SetLogToFile(int bLogEnable, String strLogDir, boolean bAutoDel);

    // 返回最后操作的错误码
    int NET_DVR_GetLastError();

    // 释放SDK资源，在程序结束之前调用
    boolean NET_DVR_Cleanup();

    // 登录接口
    int NET_DVR_Login_V40(NET_DVR_USER_LOGIN_INFO pLoginInfo, NET_DVR_DEVICEINFO_V40 lpDeviceInfo);

    // 用户注销
    boolean NET_DVR_Logout(int lUserID);

    // 回调函数申明
    public static interface FLoginResultCallback extends StdCallCallback {
        // 登录状态回调函数
        // public int invoke(int lUserID, int dwResult, NET_DVR_DEVICEINFO_V30 lpDeviceinfo, Pointer pUser);
    }

}
