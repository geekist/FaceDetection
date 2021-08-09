package com.ytech;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;


public class HIKTest2 {

    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    static int lUserID = -1;//用户句柄

    static int dwState = -1; //人员信息状态
    static int dwFaceState = -1; //下发人脸数据状态

    static int iCharEncodeType = 0;//设备字符集

    /**
     * @param args
     * @throws UnsupportedEncodingException
     * @throws InterruptedException
     * @throws JSONException
     */
    public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException, JSONException {
        HIKTest2 test = new HIKTest2();

        //SDK初始化
        hCNetSDK.NET_DVR_Init();

        //SDK启用写日志
        hCNetSDK.NET_DVR_SetLogToFile(3, "C:/JavaDemoLog", false);

        test.Login();    //登陆
        test.GetAbility();//获取能力集
        test.SearchUserInfo(); //查询所有人员

        String strEmployeeID = "22";//工号
        test.DelFaceInfo(strEmployeeID);//删除人脸图片(已经存在的工号关联的人脸图片)
        test.DelUserInfo(strEmployeeID);//删除人员信息(已经存在的工号)

        test.AddUserInfo(strEmployeeID);    //添加人员，工号不能重复
        test.ModifyUserInfo(strEmployeeID); //修改人员，根据工号修改相关人员信息，必须是已经存在的工号，工号不支持修改（需要删除重新下发）

        strEmployeeID = "11";//工号
        test.SearchFaceInfo(strEmployeeID);  //查询人脸

        //举例批量下发两个工号关联的人脸图片
        String[] strID = {"33", "44"};
        String[] strName = {"张三", "李四"};
        test.AddMultiUserInfo(strID, strName, 2);   //下发人脸需要先下发卡号或者使用已有卡号
        test.AddMultiFace(strID, 2); //下发人脸

        //退出程序时调用注销登录、反初始化接口
        hCNetSDK.NET_DVR_Logout(lUserID);
        hCNetSDK.NET_DVR_Cleanup();
    }

    public void Login() {
        //注册
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息

        String m_sDeviceIP = "10.17.36.2";//设备ip地址
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());

        String m_sUsername = "admin";//设备用户名
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());

        String m_sPassword = "hik12345";//设备密码
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());

        m_strLoginInfo.wPort = 8000;
        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
        m_strLoginInfo.write();

        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息
        lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (lUserID == -1) {
            System.out.println("登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {
            System.out.println("登录成功！");
            iCharEncodeType = m_strDeviceInfo.byCharEncodeType;
        }
    }

    public static final int ISAPI_DATA_LEN = 1024 * 1024;
    public static final int ISAPI_STATUS_LEN = 4 * 4096;
    public static final int BYTE_ARRAY_LEN = 1024;

    public void GetAbility() {
        String strURL = "GET /ISAPI/AccessControl/UserInfo/capabilities?format=json";
        HCNetSDK.BYTE_ARRAY ptrUrl = new HCNetSDK.BYTE_ARRAY(BYTE_ARRAY_LEN);
        System.arraycopy(strURL.getBytes(), 0, ptrUrl.byValue, 0, strURL.length());
        ptrUrl.write();

        //获取能力集时输入参数为空即可
        /*HCNetSDK.BYTE_ARRAY ptrInBuffer = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrInBuffer.read();
        String strInbuffer = "";
        ptrInBuffer.byValue = strInbuffer.getBytes();
        ptrInBuffer.write();
        */

        HCNetSDK.NET_DVR_XML_CONFIG_INPUT struXMLInput = new HCNetSDK.NET_DVR_XML_CONFIG_INPUT();
        struXMLInput.read();
        struXMLInput.dwSize = struXMLInput.size();
        struXMLInput.lpRequestUrl = ptrUrl.getPointer();
        struXMLInput.dwRequestUrlLen = ptrUrl.byValue.length;
        struXMLInput.lpInBuffer = null;//ptrInBuffer.getPointer();
        struXMLInput.dwInBufferSize = 0;//ptrInBuffer.byValue.length;
        struXMLInput.write();

        HCNetSDK.BYTE_ARRAY ptrStatusByte = new HCNetSDK.BYTE_ARRAY(ISAPI_STATUS_LEN);
        ptrStatusByte.read();

        HCNetSDK.BYTE_ARRAY ptrOutByte = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrOutByte.read();

        HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT struXMLOutput = new HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT();
        struXMLOutput.read();
        struXMLOutput.dwSize = struXMLOutput.size();
        struXMLOutput.lpOutBuffer = ptrOutByte.getPointer();
        struXMLOutput.dwOutBufferSize = ptrOutByte.size();
        struXMLOutput.lpStatusBuffer = ptrStatusByte.getPointer();
        struXMLOutput.dwStatusSize = ptrStatusByte.size();
        struXMLOutput.write();

        if (!hCNetSDK.NET_DVR_STDXMLConfig(lUserID, struXMLInput, struXMLOutput)) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("NET_DVR_STDXMLConfig失败，错误号：" + iErr);
            return;

        } else {
            struXMLOutput.read();
            ptrOutByte.read();
            ptrStatusByte.read();
            String strOutXML = new String(ptrOutByte.byValue).trim();
            System.out.println("获取设备能力集输出结果:" + strOutXML);
            String strStatus = new String(ptrStatusByte.byValue).trim();
            System.out.println("获取设备能力集返回状态：" + strStatus);
        }
    }

    public void SearchUserInfo() throws JSONException {
        HCNetSDK.BYTE_ARRAY ptrByteArray = new HCNetSDK.BYTE_ARRAY(1024);    //数组
        String strInBuffer = "POST /ISAPI/AccessControl/UserInfo/Search?format=json";
        System.arraycopy(strInBuffer.getBytes(), 0, ptrByteArray.byValue, 0, strInBuffer.length());//字符串拷贝到数组中
        ptrByteArray.write();

        int lHandler = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, 2550/*NET_DVR_JSON_CONFIG*/, ptrByteArray.getPointer(), strInBuffer.length(), null, null);
        if (lHandler < 0) {
            System.out.println("SearchUserInfo NET_DVR_StartRemoteConfig 失败,错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {
            //组装查询的JSON报文，这边查询的是所有的卡
            JSONObject jsonObject = new JSONObject();
            JSONObject jsonSearchCond = new JSONObject();

            //如果需要查询指定的工号人员信息，把下面注释的内容去除掉即可
//			JSONArray EmployeeNoList = new JSONArray();
//			JSONObject employeeNo1 = new JSONObject();
//			employeeNo1.put("employeeNo", "1");
//			JSONObject employeeNo2 = new JSONObject();
//			employeeNo2.put("employeeNo", "2");
//			EmployeeNoList.put(employeeNo1);
//			EmployeeNoList.put(employeeNo2);
//			jsonSearchCond.put("EmployeeNoList", EmployeeNoList);

            jsonSearchCond.put("searchID", "123e4567-e89b-12d3-a456-426655440000");
            jsonSearchCond.put("searchResultPosition", 0);
            jsonSearchCond.put("maxResults", 10);
            jsonObject.put("UserInfoSearchCond", jsonSearchCond);

            String strInbuff = jsonObject.toString();
            System.out.println("查询人员的json报文:" + strInbuff);

            //把string传递到Byte数组中，后续用.getPointer()方法传入指针地址中。
            HCNetSDK.BYTE_ARRAY ptrInbuff = new HCNetSDK.BYTE_ARRAY(strInbuff.length());
            System.arraycopy(strInbuff.getBytes(), 0, ptrInbuff.byValue, 0, strInbuff.length());
            ptrInbuff.write();

            //定义接收结果的结构体
            HCNetSDK.BYTE_ARRAY ptrOutuff = new HCNetSDK.BYTE_ARRAY(10 * 1024);

            IntByReference pInt = new IntByReference(0);

            while (true) {
                dwState = hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(lHandler, ptrInbuff.getPointer(), strInbuff.length(), ptrOutuff.getPointer(), 10 * 1024, pInt);
                System.out.println(dwState);
                if (dwState == -1) {
                    System.out.println("NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_NEEDWAIT) {
                    System.out.println("配置等待");
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    continue;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    System.out.println("查询人员失败");
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    System.out.println("查询人员异常");
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                    ptrOutuff.read();
                    System.out.println("查询人员成功, json:" + new String(ptrOutuff.byValue).trim());
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    System.out.println("获取人员完成");
                    break;
                }
            }

            if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandler)) {
                System.out.println("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                System.out.println("NET_DVR_StopRemoteConfig接口成功");
                lHandler = -1;
            }
        }


    }

    public void AddUserInfo(String strEmployeeID) throws UnsupportedEncodingException, InterruptedException, JSONException {
        HCNetSDK.BYTE_ARRAY ptrByteArray = new HCNetSDK.BYTE_ARRAY(1024);    //数组
        String strInBuffer = "POST /ISAPI/AccessControl/UserInfo/Record?format=json";
        System.arraycopy(strInBuffer.getBytes(), 0, ptrByteArray.byValue, 0, strInBuffer.length());//字符串拷贝到数组中
        ptrByteArray.write();

        int lHandler = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, 2550/*NET_DVR_JSON_CONFIG*/, ptrByteArray.getPointer(), strInBuffer.length(), null, null);
        if (lHandler < 0) {
            System.out.println("AddUserInfo NET_DVR_StartRemoteConfig 失败,错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {
            System.out.println("AddUserInfo NET_DVR_StartRemoteConfig 成功!");

            byte[] Name = "张三".getBytes("utf-8"); //根据iCharEncodeType判断，如果iCharEncodeType返回6，则是UTF-8编码。
            //如果是0或者1或者2，则是GBK编码

            //将中文字符编码之后用数组拷贝的方式，避免因为编码导致的长度问题
            String strInBuffer1 = "{\"UserInfo\":{\"Valid\":{\"beginTime\":\"2017-08-01T17:30:08\",\"enable\":true,\"endTime\":\"2030-08-01T17:30:08\"},\"checkUser\":false,\"doorRight\":\"1\",\"RightPlan\":[{\"doorNo\": 1,\"planTemplateNo\": \"1,3,5\"}],\"employeeNo\":\""
                    + strEmployeeID + "\",\"floorNumber\":1,\"maxOpenDoorTime\":0,\"name\":\"";
            String strInBuffer2 = "\",\"openDelayEnabled\":false,\"password\":\"123456\",\"roomNumber\":1,\"userType\":\"normal\"}}";
            int iStringSize = Name.length + strInBuffer1.length() + strInBuffer2.length();

            HCNetSDK.BYTE_ARRAY ptrByte = new HCNetSDK.BYTE_ARRAY(iStringSize);
            System.arraycopy(strInBuffer1.getBytes(), 0, ptrByte.byValue, 0, strInBuffer1.length());
            System.arraycopy(Name, 0, ptrByte.byValue, strInBuffer1.length(), Name.length);
            System.arraycopy(strInBuffer2.getBytes(), 0, ptrByte.byValue, strInBuffer1.length() + Name.length, strInBuffer2.length());
            ptrByte.write();

            System.out.println(new String(ptrByte.byValue));

            HCNetSDK.BYTE_ARRAY ptrOutuff = new HCNetSDK.BYTE_ARRAY(1024);

            IntByReference pInt = new IntByReference(0);
            while (true) {
                dwState = hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(lHandler, ptrByte.getPointer(), iStringSize, ptrOutuff.getPointer(), 1024, pInt);
                //读取返回的json并解析
                ptrOutuff.read();
                String strResult = new String(ptrOutuff.byValue).trim();
                System.out.println("dwState:" + dwState + ",strResult:" + strResult);

                JSONObject jsonResult = new JSONObject(strResult);
                int statusCode = jsonResult.getInt("statusCode");
                String statusString = jsonResult.getString("statusString");


                if (dwState == -1) {
                    System.out.println("NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_NEEDWAIT) {
                    System.out.println("配置等待");
                    Thread.sleep(10);
                    continue;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    System.out.println("下发人员失败, json retun:" + jsonResult.toString());
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    System.out.println("下发人员异常, json retun:" + jsonResult.toString());
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {//返回NET_SDK_CONFIG_STATUS_SUCCESS代表流程走通了，但并不代表下发成功，比如有些设备可能因为人员已存在等原因下发失败，所以需要解析Json报文
                    if (statusCode != 1) {
                        System.out.println("下发人员成功,但是有异常情况:" + jsonResult.toString());
                    } else {
                        System.out.println("下发人员成功: json retun:" + jsonResult.toString());
                    }
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    //下发人员时：dwState其实不会走到这里，因为设备不知道我们会下发多少个人，所以长连接需要我们主动关闭
                    System.out.println("下发人员完成");
                    break;
                }
            }
            if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandler)) {
                System.out.println("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                System.out.println("NET_DVR_StopRemoteConfig接口成功");
            }
        }
    }

    public void ModifyUserInfo(String strEmployeeID) throws UnsupportedEncodingException, InterruptedException, JSONException {
        HCNetSDK.BYTE_ARRAY ptrByteArray = new HCNetSDK.BYTE_ARRAY(1024);    //数组
        String strInBuffer = "PUT /ISAPI/AccessControl/UserInfo/Modify?format=json";
        System.arraycopy(strInBuffer.getBytes(), 0, ptrByteArray.byValue, 0, strInBuffer.length());//字符串拷贝到数组中
        ptrByteArray.write();

        int lHandler = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, 2550/*NET_DVR_JSON_CONFIG*/, ptrByteArray.getPointer(), strInBuffer.length(), null, null);
        if (lHandler < 0) {
            System.out.println("AddUserInfo NET_DVR_StartRemoteConfig 失败,错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {

            System.out.println("AddUserInfo NET_DVR_StartRemoteConfig 成功!");
            byte[] Name = "张三".getBytes("utf-8"); //根据iCharEncodeType判断，如果iCharEncodeType返回6，则是UTF-8编码。
            //如果是0或者1或者2，则是GBK编码

            //将中文字符编码之后用数组拷贝的方式，避免因为编码导致的长度问题
            String strInBuffer1 = "{\"UserInfo\":{\"Valid\":{\"beginTime\":\"2017-08-01T17:30:08\",\"enable\":true,\"endTime\":\"2030-08-01T17:30:08\"},\"checkUser\":false,\"doorRight\":\"1\",\"RightPlan\":[{\"doorNo\": 1,\"planTemplateNo\": \"1,3,5\"}],\"employeeNo\":\"" +
                    strEmployeeID + "\",\"floorNumber\":1,\"maxOpenDoorTime\":0,\"name\":\"";
            String strInBuffer2 = "\",\"openDelayEnabled\":false,\"password\":\"123456\",\"roomNumber\":1,\"userType\":\"normal\"}}";
            int iStringSize = Name.length + strInBuffer1.length() + strInBuffer2.length();

            HCNetSDK.BYTE_ARRAY ptrByte = new HCNetSDK.BYTE_ARRAY(iStringSize);
            System.arraycopy(strInBuffer1.getBytes(), 0, ptrByte.byValue, 0, strInBuffer1.length());
            System.arraycopy(Name, 0, ptrByte.byValue, strInBuffer1.length(), Name.length);
            System.arraycopy(strInBuffer2.getBytes(), 0, ptrByte.byValue, strInBuffer1.length() + Name.length, strInBuffer2.length());
            ptrByte.write();

            System.out.println("修改人员JSON数据：" + new String(ptrByte.byValue));

            HCNetSDK.BYTE_ARRAY ptrOutuff = new HCNetSDK.BYTE_ARRAY(1024);

            IntByReference pInt = new IntByReference(0);
            while (true) {
                dwState = hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(lHandler, ptrByte.getPointer(), iStringSize, ptrOutuff.getPointer(), 1024, pInt);
                //读取返回的json并解析
                ptrOutuff.read();
                String strResult = new String(ptrOutuff.byValue).trim();
                System.out.println("dwState:" + dwState + ",strResult:" + strResult);

                JSONObject jsonResult = new JSONObject(strResult);
                int statusCode = jsonResult.getInt("statusCode");
                String statusString = jsonResult.getString("statusString");


                if (dwState == -1) {
                    System.out.println("NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_NEEDWAIT) {
                    System.out.println("配置等待");
                    Thread.sleep(10);
                    continue;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    System.out.println("修改人员失败, json retun:" + jsonResult.toString());
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    System.out.println("修改人员异常, json retun:" + jsonResult.toString());
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {//返回NET_SDK_CONFIG_STATUS_SUCCESS代表流程走通了，但并不代表下发成功，比如有些设备可能因为人员已存在等原因下发失败，所以需要解析Json报文
                    if (statusCode != 1) {
                        System.out.println("修改人员成功,但是有异常情况:" + jsonResult.toString());
                    } else {
                        System.out.println("修改人员成功: json retun:" + jsonResult.toString());
                    }
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    //下发人员时：dwState其实不会走到这里，因为设备不知道我们会下发多少个人，所以长连接需要我们主动关闭
                    System.out.println("修改人员完成");
                    break;
                }
            }
            if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandler)) {
                System.out.println("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                System.out.println("NET_DVR_StopRemoteConfig接口成功");
            }
        }
    }

    public void AddMultiUserInfo(String[] strEmployeeID, String[] strUserName, int iNum) throws UnsupportedEncodingException, InterruptedException, JSONException {
        HCNetSDK.BYTE_ARRAY ptrByteArray = new HCNetSDK.BYTE_ARRAY(1024);    //数组
        String strInBuffer = "POST /ISAPI/AccessControl/UserInfo/Record?format=json";
        System.arraycopy(strInBuffer.getBytes(), 0, ptrByteArray.byValue, 0, strInBuffer.length());//字符串拷贝到数组中
        ptrByteArray.write();

        int lHandler = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, 2550/*NET_DVR_JSON_CONFIG*/, ptrByteArray.getPointer(), strInBuffer.length(), null, null);
        if (lHandler < 0) {
            System.out.println("AddUserInfo NET_DVR_StartRemoteConfig 失败,错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {
            System.out.println("AddUserInfo NET_DVR_StartRemoteConfig 成功!");

            int iSend = 0;
            while (iSend < iNum) {
                byte[] Name = strUserName[iSend].getBytes("utf-8"); //根据iCharEncodeType判断，如果iCharEncodeType返回6，则是UTF-8编码。
                //如果是0或者1或者2，则是GBK编码

                //将中文字符编码之后用数组拷贝的方式，避免因为编码导致的长度问题
                String strInBuffer1 = "{\"UserInfo\":{\"Valid\":{\"beginTime\":\"2017-08-01T17:30:08\",\"enable\":true,\"endTime\":\"2030-08-01T17:30:08\"},\"checkUser\":false,\"doorRight\":\"1\",\"RightPlan\":[{\"doorNo\": 1,\"planTemplateNo\": \"1\"}],\"employeeNo\":\""
                        + strEmployeeID[iSend] + "\",\"floorNumber\":1,\"maxOpenDoorTime\":0,\"name\":\"";
                String strInBuffer2 = "\",\"openDelayEnabled\":false,\"password\":\"123456\",\"roomNumber\":1,\"userType\":\"normal\"}}";
                int iStringSize = Name.length + strInBuffer1.length() + strInBuffer2.length();

                HCNetSDK.BYTE_ARRAY ptrByte = new HCNetSDK.BYTE_ARRAY(iStringSize);
                System.arraycopy(strInBuffer1.getBytes(), 0, ptrByte.byValue, 0, strInBuffer1.length());
                System.arraycopy(Name, 0, ptrByte.byValue, strInBuffer1.length(), Name.length);
                System.arraycopy(strInBuffer2.getBytes(), 0, ptrByte.byValue, strInBuffer1.length() + Name.length, strInBuffer2.length());
                ptrByte.write();

                System.out.println(new String(ptrByte.byValue));

                HCNetSDK.BYTE_ARRAY ptrOutuff = new HCNetSDK.BYTE_ARRAY(1024);

                IntByReference pInt = new IntByReference(0);
                dwState = hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(lHandler, ptrByte.getPointer(), iStringSize, ptrOutuff.getPointer(), 1024, pInt);
                //读取返回的json并解析
                ptrOutuff.read();
                String strResult = new String(ptrOutuff.byValue).trim();
                System.out.println("dwState:" + dwState + ",strResult:" + strResult);

                JSONObject jsonResult = new JSONObject(strResult);
                int statusCode = jsonResult.getInt("statusCode");
                String statusString = jsonResult.getString("statusString");

                if (dwState == -1) {
                    System.out.println("NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_NEEDWAIT) {
                    System.out.println("配置等待");
                    Thread.sleep(10);
                    continue;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    System.out.println("下发人员失败, json retun:" + jsonResult.toString());
                    iSend++;//下发下一个
                    continue;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    System.out.println("下发人员异常, json retun:" + jsonResult.toString());
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {//返回NET_SDK_CONFIG_STATUS_SUCCESS代表流程走通了，但并不代表下发成功，比如有些设备可能因为人员已存在等原因下发失败，所以需要解析Json报文
                    if (statusCode != 1) {
                        System.out.println("下发人员成功,但是有异常情况:" + jsonResult.toString());
                    } else {
                        System.out.println("下发人员成功: json retun:" + jsonResult.toString());
                    }
                    iSend++;//下发下一个
                    continue;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    //下发人员时：dwState其实不会走到这里，因为设备不知道我们会下发多少个人，所以长连接需要我们主动关闭
                    System.out.println("下发人员完成");
                    break;
                }
            }

            if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandler)) {
                System.out.println("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                System.out.println("NET_DVR_StopRemoteConfig接口成功");
            }
        }
    }

    public void SearchFaceInfo(String strEmployeeID) throws JSONException {
        HCNetSDK.BYTE_ARRAY ptrByteArray = new HCNetSDK.BYTE_ARRAY(1024);    //数组
        String strInBuffer = "POST /ISAPI/Intelligent/FDLib/FDSearch?format=json";
        System.arraycopy(strInBuffer.getBytes(), 0, ptrByteArray.byValue, 0, strInBuffer.length());//字符串拷贝到数组中
        ptrByteArray.write();

        int lHandler = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, 2552/*NET_DVR_FACE_DATA_SEARCH*/, ptrByteArray.getPointer(), strInBuffer.length(), null, null);
        if (lHandler < 0) {
            System.out.println("SearchFaceInfo NET_DVR_StartRemoteConfig 失败,错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {
            System.out.println("SearchFaceInfo NET_DVR_StartRemoteConfig成功!");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("searchResultPosition", 0);
            jsonObject.put("maxResults", 1);
            jsonObject.put("faceLibType", "blackFD");
            jsonObject.put("FDID", "1");
            jsonObject.put("FPID", strEmployeeID);//人脸关联的工号，同下发人员时的employeeNo字段

            String strInbuff = jsonObject.toString();
            System.out.println("查询人脸的json报文:" + strInbuff);

            //把string传递到Byte数组中，后续用.getPointer()方法传入指针地址中。
            HCNetSDK.BYTE_ARRAY ptrInbuff = new HCNetSDK.BYTE_ARRAY(strInbuff.length());
            System.arraycopy(strInbuff.getBytes(), 0, ptrInbuff.byValue, 0, strInbuff.length());
            ptrInbuff.write();

            HCNetSDK.NET_DVR_JSON_DATA_CFG m_struJsonData = new HCNetSDK.NET_DVR_JSON_DATA_CFG();
            m_struJsonData.write();

            IntByReference pInt = new IntByReference(0);

            while (true) {
                dwState = hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(lHandler, ptrInbuff.getPointer(), strInbuff.length(), m_struJsonData.getPointer(), m_struJsonData.size(), pInt);
                m_struJsonData.read();
                System.out.println(dwState);
                if (dwState == -1) {
                    System.out.println("NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_NEEDWAIT) {
                    System.out.println("配置等待");
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    continue;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    System.out.println("查询人脸失败");
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    System.out.println("查询人脸异常");
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                    System.out.println("查询人脸成功");

                    //解析JSON字符串
                    HCNetSDK.BYTE_ARRAY pJsonData = new HCNetSDK.BYTE_ARRAY(m_struJsonData.dwJsonDataSize);
                    pJsonData.write();
                    Pointer pPlateInfo = pJsonData.getPointer();
                    pPlateInfo.write(0, m_struJsonData.lpJsonData.getByteArray(0, pJsonData.size()), 0, pJsonData.size());
                    pJsonData.read();
                    String strResult = new String(pJsonData.byValue).trim();
                    System.out.println("strResult:" + strResult);
                    JSONObject jsonResult = new JSONObject(strResult);

                    int numOfMatches = jsonResult.getInt("numOfMatches");
                    if (numOfMatches != 0) {//确认有人脸
                        JSONArray MatchList = jsonResult.getJSONArray("MatchList");
                        JSONObject MatchList_1 = MatchList.optJSONObject(0);
                        String FPID = MatchList_1.getString("FPID"); //获取json中人脸关联的工号

                        FileOutputStream fout;
                        try {
                            fout = new FileOutputStream(".\\lib\\FPID_[" + FPID + "]_FacePic.jpg");
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = m_struJsonData.lpPicData.getByteBuffer(offset, m_struJsonData.dwPicDataSize);
                            byte[] bytes = new byte[m_struJsonData.dwPicDataSize];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    break;
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    System.out.println("获取人脸完成");
                    break;
                }
            }
            if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandler)) {
                System.out.println("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                System.out.println("NET_DVR_StopRemoteConfig接口成功");
                lHandler = -1;
            }

        }

    }

    public void AddMultiFace(String[] strFPID, int iNum) throws JSONException, InterruptedException {
        HCNetSDK.BYTE_ARRAY ptrByteArray = new HCNetSDK.BYTE_ARRAY(1024);    //数组
        String strInBuffer = "POST /ISAPI/Intelligent/FDLib/FaceDataRecord?format=json ";
        System.arraycopy(strInBuffer.getBytes(), 0, ptrByteArray.byValue, 0, strInBuffer.length());//字符串拷贝到数组中
        ptrByteArray.write();

        int lHandler = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, 2551/*NET_DVR_FACE_DATA_RECORD*/, ptrByteArray.getPointer(), strInBuffer.length(), null, null);
        if (lHandler < 0) {
            System.out.println("Addface NET_DVR_StartRemoteConfig 失败,错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {
            System.out.println("Addface NET_DVR_StartRemoteConfig 成功!");

            //批量下发多个人脸（不同工号）
            HCNetSDK.NET_DVR_JSON_DATA_CFG[] struAddFaceDataCfg = (HCNetSDK.NET_DVR_JSON_DATA_CFG[]) new HCNetSDK.NET_DVR_JSON_DATA_CFG().toArray(iNum);

            //下发的人脸图片
            String[] strFilePath = new String[iNum];
            //这里举例下发两个人脸图片
            strFilePath[0] = System.getProperty("user.dir") + "\\lib\\pic\\face1.jpg";
            strFilePath[1] = System.getProperty("user.dir") + "\\lib\\pic\\face2.jpg";

            for (int i = 0; i < iNum; i++) {
                struAddFaceDataCfg[i].read();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("faceLibType", "blackFD");
                jsonObject.put("FDID", "1");
                jsonObject.put("FPID", strFPID[i]);//人脸下发关联的工号

                String strJsonData = jsonObject.toString();
                System.out.println("下发人脸的json报文:" + strJsonData);

                System.arraycopy(strJsonData.getBytes(), 0, ptrByteArray.byValue, 0, strJsonData.length());//字符串拷贝到数组中
                ptrByteArray.write();

                struAddFaceDataCfg[i].dwSize = struAddFaceDataCfg[i].size();
                struAddFaceDataCfg[i].lpJsonData = ptrByteArray.getPointer();
                struAddFaceDataCfg[i].dwJsonDataSize = strJsonData.length();

                /*****************************************
                 * 从本地文件里面读取JPEG图片二进制数据
                 *****************************************/
                FileInputStream picfile = null;
                int picdataLength = 0;
                try {
                    picfile = new FileInputStream(new File(strFilePath[i]));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    picdataLength = picfile.available();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if (picdataLength < 0) {
                    System.out.println("input file dataSize < 0");
                    return;
                }

                HCNetSDK.BYTE_ARRAY ptrpicByte = new HCNetSDK.BYTE_ARRAY(picdataLength);
                try {
                    picfile.read(ptrpicByte.byValue);
                    picfile.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                ptrpicByte.write();
                struAddFaceDataCfg[i].dwPicDataSize = picdataLength;
                struAddFaceDataCfg[i].lpPicData = ptrpicByte.getPointer();
                struAddFaceDataCfg[i].write();

                HCNetSDK.BYTE_ARRAY ptrOutuff = new HCNetSDK.BYTE_ARRAY(1024);
                IntByReference pInt = new IntByReference(0);

                dwState = hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(lHandler, struAddFaceDataCfg[i].getPointer(), struAddFaceDataCfg[i].dwSize, ptrOutuff.getPointer(), ptrOutuff.size(), pInt);
                //读取返回的json并解析
                ptrOutuff.read();
                String strResult = new String(ptrOutuff.byValue).trim();
                System.out.println("dwState:" + dwState + ",strResult:" + strResult);

                if (strResult.isEmpty()) {
                    return;
                }
                JSONObject jsonResult = new JSONObject(strResult);
                int statusCode = jsonResult.getInt("statusCode");
                //String statusString = jsonResult.getString("statusString");

                if (dwState == -1) {
                    System.out.println("NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    System.out.println("下发人脸失败, json retun:" + jsonResult.toString());
                    //可以继续下发下一个
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    System.out.println("下发人脸异常, json retun:" + jsonResult.toString());
                    break;
                    //异常是长连接异常，不能继续下发后面的数据，需要重新建立长连接
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                    //返回NET_SDK_CONFIG_STATUS_SUCCESS代表流程走通了，但并不代表下发成功，比如人脸图片不符合设备规范等原因，所以需要解析Json报文
                    if (statusCode != 1) {
                        System.out.println("下发人脸成功,但是有异常情况:" + jsonResult.toString());
                    } else {
                        System.out.println("下发人脸成功,  json retun:" + jsonResult.toString());
                    }
                    //可以继续下发下一个
                } else if (dwState == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    //下发人脸时：dwState其实不会走到这里，因为设备不知道我们会下发多少个人，所以长连接需要我们主动关闭
                    System.out.println("下发人脸完成");
                } else {
                    System.out.println("下发人脸识别，其他状态：" + dwState);
                }
            }

            if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandler)) {
                System.out.println("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                System.out.println("NET_DVR_StopRemoteConfig接口成功");
            }
        }
    }

    public void DelFaceInfo(String strEmployeeID) {
        String strURL = "PUT /ISAPI/Intelligent/FDLib/FDSearch/Delete?format=json&FDID=1&faceLibType=blackFD";
        HCNetSDK.BYTE_ARRAY ptrUrl = new HCNetSDK.BYTE_ARRAY(BYTE_ARRAY_LEN);
        System.arraycopy(strURL.getBytes(), 0, ptrUrl.byValue, 0, strURL.length());
        ptrUrl.write();

        //输入删除条件
        HCNetSDK.BYTE_ARRAY ptrInBuffer = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrInBuffer.read();
        String strInbuffer = "{\"FPID\":[{\"value\":\"" + strEmployeeID + "\"}]}";
        ptrInBuffer.byValue = strInbuffer.getBytes();
        ptrInBuffer.write();

        HCNetSDK.NET_DVR_XML_CONFIG_INPUT struXMLInput = new HCNetSDK.NET_DVR_XML_CONFIG_INPUT();
        struXMLInput.read();
        struXMLInput.dwSize = struXMLInput.size();
        struXMLInput.lpRequestUrl = ptrUrl.getPointer();
        struXMLInput.dwRequestUrlLen = ptrUrl.byValue.length;
        struXMLInput.lpInBuffer = ptrInBuffer.getPointer();
        struXMLInput.dwInBufferSize = ptrInBuffer.byValue.length;
        struXMLInput.write();

        HCNetSDK.BYTE_ARRAY ptrStatusByte = new HCNetSDK.BYTE_ARRAY(ISAPI_STATUS_LEN);
        ptrStatusByte.read();

        HCNetSDK.BYTE_ARRAY ptrOutByte = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrOutByte.read();

        HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT struXMLOutput = new HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT();
        struXMLOutput.read();
        struXMLOutput.dwSize = struXMLOutput.size();
        struXMLOutput.lpOutBuffer = ptrOutByte.getPointer();
        struXMLOutput.dwOutBufferSize = ptrOutByte.size();
        struXMLOutput.lpStatusBuffer = ptrStatusByte.getPointer();
        struXMLOutput.dwStatusSize = ptrStatusByte.size();
        struXMLOutput.write();

        if (!hCNetSDK.NET_DVR_STDXMLConfig(lUserID, struXMLInput, struXMLOutput)) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("NET_DVR_STDXMLConfig失败，错误号：" + iErr);
            return;

        } else {
            struXMLOutput.read();
            ptrOutByte.read();
            ptrStatusByte.read();
            String strOutXML = new String(ptrOutByte.byValue).trim();
            System.out.println("删除人脸输出结果:" + strOutXML);
            String strStatus = new String(ptrStatusByte.byValue).trim();
            System.out.println("删除人脸返回状态：" + strStatus);
        }
    }

    public void DelUserInfo(String strEmployeeID) {
        String strURL = "PUT /ISAPI/AccessControl/UserInfo/Delete?format=json";
        HCNetSDK.BYTE_ARRAY ptrUrl = new HCNetSDK.BYTE_ARRAY(BYTE_ARRAY_LEN);
        System.arraycopy(strURL.getBytes(), 0, ptrUrl.byValue, 0, strURL.length());
        ptrUrl.write();

        //输入删除条件
        HCNetSDK.BYTE_ARRAY ptrInBuffer = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrInBuffer.read();
        String strInbuffer = "{\"UserInfoDelCond\":{\"EmployeeNoList\":[{\"employeeNo\":\"" + strEmployeeID + "\"}]}}";
        ptrInBuffer.byValue = strInbuffer.getBytes();
        ptrInBuffer.write();

        HCNetSDK.NET_DVR_XML_CONFIG_INPUT struXMLInput = new HCNetSDK.NET_DVR_XML_CONFIG_INPUT();
        struXMLInput.read();
        struXMLInput.dwSize = struXMLInput.size();
        struXMLInput.lpRequestUrl = ptrUrl.getPointer();
        struXMLInput.dwRequestUrlLen = ptrUrl.byValue.length;
        struXMLInput.lpInBuffer = ptrInBuffer.getPointer();
        struXMLInput.dwInBufferSize = ptrInBuffer.byValue.length;
        struXMLInput.write();

        HCNetSDK.BYTE_ARRAY ptrStatusByte = new HCNetSDK.BYTE_ARRAY(ISAPI_STATUS_LEN);
        ptrStatusByte.read();

        HCNetSDK.BYTE_ARRAY ptrOutByte = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrOutByte.read();

        HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT struXMLOutput = new HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT();
        struXMLOutput.read();
        struXMLOutput.dwSize = struXMLOutput.size();
        struXMLOutput.lpOutBuffer = ptrOutByte.getPointer();
        struXMLOutput.dwOutBufferSize = ptrOutByte.size();
        struXMLOutput.lpStatusBuffer = ptrStatusByte.getPointer();
        struXMLOutput.dwStatusSize = ptrStatusByte.size();
        struXMLOutput.write();

        if (!hCNetSDK.NET_DVR_STDXMLConfig(lUserID, struXMLInput, struXMLOutput)) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("NET_DVR_STDXMLConfig失败，错误号：" + iErr);
            return;

        } else {
            struXMLOutput.read();
            ptrOutByte.read();
            ptrStatusByte.read();
            String strOutXML = new String(ptrOutByte.byValue).trim();
            System.out.println("删除人员输出结果:" + strOutXML);
            String strStatus = new String(ptrStatusByte.byValue).trim();
            System.out.println("删除人员返回状态：" + strStatus);
        }
    }
}//Test1  Class结束