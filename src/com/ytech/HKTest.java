package com.ytech;

import com.ytech.core.Admin;
import com.ytech.hikvision.HCNetSDK;

public class HKTest {
    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    static int iErr = 0;

    public static void main(String[] args) throws InterruptedException {
        Admin admin = new Admin();

        // 打印SDK日志
        hCNetSDK.NET_DVR_SetLogToFile(3, ".\\SDKLog\\", false);

        // 初始化
        boolean initSuc = admin.init();
        if (!initSuc) {
            System.out.println("初始化失败");
            iErr = hCNetSDK.NET_DVR_GetLastError();
        }

        // 用户登陆操作
        int userId = admin.login_v40("",(short)8000,"admin","yuya123456");
        if (userId == -1) {
            String errorString = "登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError();
            System.out.println(errorString);
        }else {
            System.out.println("登录成功！");
            // read()后，结构体中才有对应的数据
            // deviceInfo.read();
        }

        /**
         *实现SDK中其余功能模快
         **/

        Thread.sleep(5000);

        //退出
        boolean logoutSuc = admin.logout();
        if(!logoutSuc){
            System.out.println("退出失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
        }else {
            System.out.println("退出成功！");
            // read()后，结构体中才有对应的数据
            // deviceInfo.read();
        }

        //设备注销
        boolean cleanUpSuc = admin.cleanUp();
        if(!cleanUpSuc){
            System.out.println("注销失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
        }else {
            System.out.println("注销成功！");
        }
    }

}


