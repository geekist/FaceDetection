package com.ytech;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;

public class HCTest {

    public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException, JSONException {
        HCManager hcManager = new HCManager();

        //SDK初始化
        hcManager.init();

        hcManager.setLogFolder(".\\SDKLog\\");



        hcManager.Login("192.168.31.132",(short)8000,"admin","yuya123456");    //登陆
        hcManager.GetEventCount();
        //hcManager.GetUserInfoCount();
      //  hcManager.GetAbility();//获取能力集
        hcManager.SearchUserInfo(); //查询所有人员

        String strEmployeeID = "313";//工号
       // hcManager.DelFaceInfo(strEmployeeID);//删除人脸图片(已经存在的工号关联的人脸图片)
       // hcManager.DelUserInfo(strEmployeeID);//删除人员信息(已经存在的工号)

    //    hcManager.AddUserInfo(strEmployeeID);    //添加人员，工号不能重复
     //   hcManager.AddFaceInfo(strEmployeeID);

      //  hcManager.ModifyUserInfo(strEmployeeID); //修改人员，根据工号修改相关人员信息，必须是已经存在的工号，工号不支持修改（需要删除重新下发）
/*
        strEmployeeID = "11";//工号
        test.SearchFaceInfo(strEmployeeID);  //查询人脸

        //举例批量下发两个工号关联的人脸图片
        String[] strID = {"33", "44"};
        String[] strName = {"张三", "李四"};
        test.AddMultiUserInfo(strID, strName, 2);   //下发人脸需要先下发卡号或者使用已有卡号
        test.AddMultiFace(strID, 2); //下发人脸
*/
        //退出程序时调用注销登录、反初始化接口

        hcManager.logout();

        hcManager.cleanUp();

    }
}
