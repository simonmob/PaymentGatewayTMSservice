/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EconnectTMS;

import com.MainFiles.Functions;
import com.Database.DatabaseConnections;
import com.MainFiles.ClassImportantValues;
import static com.MainFiles.ISO8583Adaptor.ECDB;
import static com.MainFiles.ISO8583Adaptor.ECPASSWORD;
import static com.MainFiles.ISO8583Adaptor.ECSERVER;
import static com.MainFiles.ISO8583Adaptor.ECUSER;
import static com.MainFiles.ISO8583Adaptor.SOURCE_ID;
import com.Utilities.XMLParser;
import java.util.HashMap;

/**
 *
 * @author Collins
 */
public class Reprint {

    DatabaseConnections connections = new DatabaseConnections();
    ClassImportantValues cl = new ClassImportantValues();
    Functions func = new Functions();
    XMLParser parser = new XMLParser();

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
    String strField35 = "";
    String strPinClear = "";
    String strVerifyPin = "";
    String strNarration = "";
    String strField37 = "";
    String strField39 = "";
    String str514narration = "";
    String strPhoneNumber = "";
    String strField100 = "";
    String strBillNumber = "";
    String strRemittanceCode = "";
    String strAgentPassword = "";
    String strAgentCode = "";
    String processingcode = "";
    String[] strReceivedData;
    String[] strLoginStatus;
    String strStatus;
    String strUserType;
    String strTrials;
    String strLoggedin;
    String strFirstLogin = "";
    String strActiveAgent = "";

    public void Run(String IncomingMessage, String intid) {//999999#AGENCY##1001#1234#1001#

        strReceivedData = IncomingMessage.split("#");
        processingcode = strReceivedData[0].toString();
        try {
            strAgentCode = strReceivedData[3].replace("Ù", "");
            strAgentCode = func.fn_RemoveNon_Numeric(strAgentCode);
            strAgentPassword = strReceivedData[4].replace("Ù", "");
            strAgentPassword = func.fn_RemoveNon_Numeric(strAgentPassword);

            strCardNumber = "0000000000000000";
            strDeviceid = strReceivedData[2];
            strExpiryDate = "0000";
            strAgentID = strReceivedData[5].trim();
            strAgentID = func.fn_RemoveNon_Numeric(strAgentID);

            strLoginStatus = func.fn_verify_login_details(strAgentCode, strAgentPassword, strDeviceid).split("\\|");
            strStatus = strLoginStatus[0];
            strUserType = strLoginStatus[1];
            strTrials = strLoginStatus[2];
            strLoggedin = strLoginStatus[3];
            strFirstLogin = strLoginStatus[4];
            strActiveAgent = strLoginStatus[5];

            switch (Integer.parseInt(strStatus)) {
                case 1:
                    switch (Integer.parseInt(strUserType)) {
                        case 11:
                            strResponse = connections.ExecuteQueryStringValue(ECSERVER, "SELECT * FROM (select POS_RECEIPT from tbincomingtransactions where field_3 <> '000000' and field_3 <> '999999' and field_104='" + strAgentID + "' AND POS_RECEIPT IS NOT NULL ORDER BY ID DESC) where rownum <= 1  ", ECPASSWORD, ECUSER, ECDB, "", "POS_RECEIPT");
                            break;
                        default:
                            strResponse = func.strResponseHeader(strReceivedData[2]);
                            strResponse += "Ref No    :" + func.PadZeros(12, intid) + "#";
                            strResponse += "--------------------------------" + "#";
                            strResponse += "   WRONG CREDENTIALS ENTERED   " + "#";
                            strResponse += "--------------------------------" + "#";
                            strResponse += func.strResponseFooter(strAgentID);
                            break;
                    }
                default:
                    strResponse = func.strResponseHeader(strReceivedData[2]);
                    strResponse += "Ref No    :" + func.PadZeros(12, intid) + "#";
                    strResponse += "--------------------------------" + "#";
                    strResponse += "   WRONG CREDENTIALS ENTERED   " + "#";
                    strResponse += "--------------------------------" + "#";
                    strResponse += func.strResponseFooter(strAgentID);
                    break;

            }

            func.SendPOSResponse(strResponse, intid);

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("0", "0200");
            map.put("2", "0000000000000000");
            map.put("3", processingcode);
            map.put("4", strAmount);
            map.put("7", func.anyDate("MMDDHHMMSS"));
            map.put("11", strDeviceid);
            map.put("32", SOURCE_ID);
            map.put("37", strField37);
            map.put("65", strAgentID);
            map.put("88", strNarration);
            map.put("100", "BI");
            map.put("102", strAccountNumber);
            map.put("103", "");
            map.put("104", "");
            map.put("POSReceipt", strResponse);

            func.spInsertPOSTransaction(map);

        } catch (Exception ex) {
            func.log("\nSEVERE Reprint() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
