package com.ytech.core;

import com.ytech.hik.HCNetSDK;
import com.ytech.hik.structure.NET_DVR_DEVICEINFO_V40;
import com.ytech.hik.structure.NET_DVR_USER_LOGIN_INFO;

import static com.ytech.hik.Constant.NET_DVR_DEV_ADDRESS_MAX_LEN;
import static com.ytech.hik.Constant.NET_DVR_LOGIN_PASSWD_MAX_LEN;
import static com.ytech.hik.Constant.NET_DVR_LOGIN_USERNAME_MAX_LEN;

public class Admin {
    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    private int userId = -1;

    public boolean init() {
        return hCNetSDK.NET_DVR_Init();
    }

    public int login_v40(String deviceIP, short port, String userName, String password) {
        // 登录信息
        NET_DVR_USER_LOGIN_INFO loginInfo = new NET_DVR_USER_LOGIN_INFO();
        loginInfo.sDeviceAddress = new byte[NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(deviceIP.getBytes(), 0, loginInfo.sDeviceAddress, 0, deviceIP.length());
        loginInfo.wPort = port;
        loginInfo.sUserName = new byte[NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(userName.getBytes(), 0, loginInfo.sUserName, 0, userName.length());
        loginInfo.sPassword = new byte[NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(password.getBytes(), 0, loginInfo.sPassword, 0, password.length());
        loginInfo.bUseAsynLogin = false; // 是否异步登录：false- 否，true- 是
        loginInfo.write();// write()调用后数据才写入到内存中

        // 设备信息
        NET_DVR_DEVICEINFO_V40 deviceInfo = new NET_DVR_DEVICEINFO_V40();

        userId = hCNetSDK.NET_DVR_Login_V40(loginInfo, deviceInfo);
        return userId;
    }

    public boolean logout() {
        if (userId >= 0) {
            if (!hCNetSDK.NET_DVR_Logout(userId)) {
                return false;
            } else {
                System.out.println("退出成功");
                return true;
            }
        } else {
            System.out.println("设备未登录");
            return false;
        }
    }

    public boolean cleanUp() {
        return hCNetSDK.NET_DVR_Cleanup();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
