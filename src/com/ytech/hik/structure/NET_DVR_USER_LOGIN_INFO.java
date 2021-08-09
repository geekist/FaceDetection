package com.ytech.hik.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.ytech.hik.HCNetSDK;

import java.util.Arrays;
import java.util.List;

import static com.ytech.hik.Constant.NET_DVR_DEV_ADDRESS_MAX_LEN;
import static com.ytech.hik.Constant.NET_DVR_LOGIN_PASSWD_MAX_LEN;
import static com.ytech.hik.Constant.NET_DVR_LOGIN_USERNAME_MAX_LEN;

// 动态库中结构体、接口描述
public class NET_DVR_USER_LOGIN_INFO extends Structure {
    public byte[] sDeviceAddress = new byte[NET_DVR_DEV_ADDRESS_MAX_LEN];
    public byte byUseTransport;
    public short wPort;
    public byte[] sUserName = new byte[NET_DVR_LOGIN_USERNAME_MAX_LEN];
    public byte[] sPassword = new byte[NET_DVR_LOGIN_PASSWD_MAX_LEN];
    public HCNetSDK.FLoginResultCallback cbLoginResult;
    public Pointer pUser;
    public boolean bUseAsynLogin;
    public byte byProxyType;   // 0:不使用代理，1：使用标准代理，2：使用EHome代理
    public byte byUseUTCTime;  // 0-不进行转换，默认,1-接口上输入输出全部使用UTC时间,SDK完成UTC时间与设备时区的转换,2-接口上输入输出全部使用平台本地时间，SDK完成平台本地时间与设备时区的转换
    public byte byLoginMode;   // 0-Private 1-ISAPI 2-自适应
    public byte byHttps;       // 0-不适用tls，1-使用tls 2-自适应
    public int iProxyID;    // 代理服务器序号，添加代理服务器信息时，相对应的服务器数组下表值
    public byte byVerifyMode;  // 认证方式，0-不认证，1-双向认证，2-单向认证；认证仅在使用TLS的时候生效;
    public byte[] byRes2 = new byte[119];

    // 结构体中重写getFieldOrder方法，FieldOrder顺序要和结构体中定义的顺序保持一致
    @Override
    protected List getFieldOrder() {
        return Arrays.asList("sDeviceAddress", "byUseTransport", "wPort", "sUserName", "sPassword", "cbLoginResult", "pUser", "bUseAsynLogin", "byProxyType", "byUseUTCTime",
                "byLoginMode", "byHttps", "iProxyID", "byVerifyMode", "byRes2");
    }

}