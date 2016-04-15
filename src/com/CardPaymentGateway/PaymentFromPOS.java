/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.CardPaymentGateway;
import com.EconnectTMS.*;
import com.MainFiles.*;

/**
 *
 * @author smaingi
 */
public class PaymentFromPOS {
    String strCardNumber = "";
    String strDeviceid = "";
    String strExpiryDate = "";
    String strAccountNumber = "";
    String strResponse = "";
    String strAgentID = "";
    String strAmount = "";
    double amount;
    String field24 = "";
    String strTrack2Data = "";
    String strAgencyCashManagement = "";
    String[] strCardInformation;
    String[] strReceinedData;
    String strPhoneNumber="";
    String strCVC="";
    String strRefNo="";
    String strProcessingCode="";
    
    Functions func=new Functions();
    public void run(String incomingMessage, String intid){
        try{
            //strAmount,&CardNo2[1],&CardNo2[2],
			//phoneNumber,strCvc,RefNO
            strReceinedData=incomingMessage.split("#");
            strAmount=strReceinedData[1];
            //strCardNumber=strReceinedData[1];
            //strExpiryDate=strReceinedData[2];
            strPhoneNumber=strReceinedData[3];
            strCVC=strReceinedData[4];
            strRefNo=strReceinedData[5];
            strTrack2Data=strReceinedData[2];
            strProcessingCode=strReceinedData[0];  
            strDeviceid=strRefNo;
            
            
            if (strTrack2Data.contains("=")) {
                        strCardInformation = strTrack2Data.split("=");
                        strCardNumber = strCardInformation[0];
                        int strlen = strCardNumber.length();
                        //if (strlen < 16) {
                            //strResponse = func.strPOSResponseHeader2(strDeviceid);
                            strResponse += "--------------------------------" + "#";
                            strResponse += "INVALID PAN #";
                            //strResponse += func.strPOSResponseFooter("1001");
                            func.SendPOSResponse(strResponse, "BR0003401");
                            //return;
                        //}
                        strExpiryDate = strCardInformation[1].substring(0, 4);
                        String[] strTrack2Data1 = strCardInformation[1].split("\\?");
                        //strField35 = strCardInformation[0] + "=" + strTrack2Data1[0];

                    } else if (strTrack2Data.contains("D")) {

                        strCardInformation = strTrack2Data.split("D");
                        strCardNumber = strCardInformation[0];
                        int strlen = strCardNumber.length();
                        if (strlen < 16) {
                            //strResponse = func.strPOSResponseHeader2(strDeviceid);
                            strResponse += "--------------------------------" + "#";
                            strResponse += "INVALID PAN #";
                            strResponse += func.strPOSResponseFooter(strAgentID);
                            //func.SendPOSResponse(strResponse, intid);
                            return;
                        }
                        strExpiryDate = strCardInformation[1].substring(0, 4);
                        String[] strTrack2Data1 = strCardInformation[1].split("\\?");
                        //strField35 = strCardInformation[0] + "=" + strTrack2Data1[0];
                    }
        }
        catch(Exception ex){
            func.log("\nSEVERE CardPaymentFromPos() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
