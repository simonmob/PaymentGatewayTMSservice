/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.CardPaymentGateway;

import com.MainFiles.Functions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import sun.misc.BASE64Encoder;

/**
 *
 * @author Simon Maingi
 */
public class GatewayAirtimeTopUp {
    
    String strCardNumber = "";
    String strDeviceid = "";
    String strExpiryDate = "";
    String strAccountNumber = "";
    String strResponse = "";
    String strAgentID = "";
    String strAmount = "";
    double amount;
    String strTrack2Data = "";
    String strAgencyCashManagement = "";
    String[] strCardInformation;
    String[] strReceivedData;
    String strPhoneNumber="";
    String strRefNo="";
    String strProcessingCode="";
    String strpin="";
    String strBillNumber="";
    
    Functions func=new Functions();
    
    
    
    public void run(String incomingMessage, String intid){
        try{
            strReceivedData=incomingMessage.split("#");
            strProcessingCode=strReceivedData[0]; 
            strAmount=strReceivedData[1];
            strTrack2Data=strReceivedData[2];
            strpin=strReceivedData[3];
            strPhoneNumber=strReceivedData[4];
            //strBillNumber=strReceivedData[5];
            strRefNo=strReceivedData[5];
            
             
            strDeviceid=strRefNo;
            
            
            if (strTrack2Data.contains("=")) {
                    strCardInformation = strTrack2Data.split("=");
                    strCardNumber = strCardInformation[0];
                    //strExpiryDate = strCardInformation[1].substring(0, 4);
                    int strlen = strCardNumber.length();
                    if (strlen < 16) {
                        strResponse = func.strPOSResponseHeader2(strDeviceid);
                        strResponse += "--------------------------------" + "#";
                        strResponse += "INVALID PAN #";
                        //strResponse += "CARDNO: "+strCardNumber+ "#";
                        //strResponse += "ExPiryDate: "+strExpiryDate+ "#";
                        strResponse += func.strPOSResponseFooter(strDeviceid);
                        func.SendPOSResponse(strResponse, intid);
                        return;
                    }
                    strExpiryDate = strCardInformation[1].substring(0, 4);
                    //String[] strTrack2Data1 = strCardInformation[1].split("\\?");
                    //strField35 = strCardInformation[0] + "=" + strTrack2Data1[0];

                } else if (strTrack2Data.contains("D")) {

                    strCardInformation = strTrack2Data.split("D");
                    strCardNumber = strCardInformation[0];
                    int strlen = strCardNumber.length();
                    if (strlen < 16) {
                        strResponse = func.strPOSResponseHeader2(strDeviceid);
                        strResponse += "--------------------------------" + "#";
                        strResponse += "INVALID PAN #";
                        strResponse += func.strPOSResponseFooter(strDeviceid);
                        func.SendPOSResponse(strResponse, strDeviceid);
                        return;
                    }
                    strExpiryDate = strCardInformation[1].substring(0, 4);
                    //String[] strTrack2Data1 = strCardInformation[1].split("\\?");
                    //strField35 = strCardInformation[0] + "=" + strTrack2Data1[0];
                }

            //send transactionDetails from pos to PaymentGateway
            //Auth key Variables
            String uniqueKey="!Eclectic%IsThe BomB%Limited!??hehehe";
            String phoneNumber=strPhoneNumber;

            //Transaction Details Json Data
            JSONObject object=new JSONObject();
            object.put("Amount", strAmount);
            object.put("CardNo", strCardNumber);
            object.put("Channel", "POS");
            object.put("Organization", "Eclectics");
            object.put("ExpiryDate", strExpiryDate);
            object.put("RefNo", strRefNo);
            object.put("PhoneNumber", strPhoneNumber);
            object.put("MessageType", "0000");
            object.put("Country", "Kenya");
            object.put("Pin", strpin);
            object.put("ProcessingCode", strProcessingCode);

            System.out.println("JSON DATA= "+object);

            String jsonString=object.toString().replaceAll("\"", "");
            
            String tobase64String=new  BASE64Encoder().encode(jsonString.getBytes());//. Base64.getEncoder().encodeToString(jsonString.getBytes());
            //System.out.println("TO BASE64 STRING= "+tobase64String);

            //Auth key
            String authKeyString = uniqueKey+phoneNumber;
            //base64 of authKey
            String base64AuthString= new BASE64Encoder().encode(authKeyString.getBytes());// ++Base64.getEncoder().encodeToString(authKeyString.getBytes());
            //generate authKey sha512
            String sha512AuthKey=null;
            try {

                MessageDigest md=MessageDigest.getInstance("SHA-512");
                byte[] hash=md.digest(base64AuthString.getBytes());
                sha512AuthKey=convertToHex(hash);
            } catch (NoSuchAlgorithmException ex) {
                func.log("\nSEVERE GatewayAirtimeTopUp() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");   
            }
            System.out.println("SHA512 AUTH KEY= "+sha512AuthKey);
            //send request
            String response=authentication(tobase64String,sha512AuthKey);
            System.out.println(response);
            
            String noCurly=response.replaceAll("[{}]", "");
            String formatResponse[]=noCurly.split(",");
            
            
            strResponse = func.strPOSResponseHeader2(strDeviceid);
            strResponse += "--------------------------------" + "#";
            strResponse += "  AIRTIME TOP UP #";
            for(int i=0;i<formatResponse.length;i++)
            {
               strResponse += "SUCCESS:  "  + formatResponse[i]+ "#";
            }
            
            strResponse += func.strPOSResponseFooter(strDeviceid);
            func.SendPOSResponse(strResponse, strDeviceid);

        }
        catch(Exception ex){
            func.log("\nSEVERE GatewayAirtimeTopUp() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
    
    /**
    * Converts the given byte[] to a hex string.
    * @param raw the byte[] to convert
    * @return the string the given byte[] represents
    */
    private String convertToHex(byte[] raw) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length; i++) {
            sb.append(Integer.toString((raw[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    //invoking the web service client reference 

    private static String authentication(java.lang.String tranDetails, java.lang.String authKey) {
        com.CardPaymentGateway.Request service = new com.CardPaymentGateway.Request();
        com.CardPaymentGateway.IncomingTransaction port = service.getIncomingTransactionPort();
        return port.authentication(tranDetails, authKey);
    }
    
    
}
