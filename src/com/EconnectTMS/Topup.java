/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EconnectTMS;

import com.MainFiles.Functions;
import com.Database.DatabaseConnections;
import com.MainFiles.ClassImportantValues;
import com.MainFiles.DataConversions;
import com.Utilities.XMLParser;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author Collins
 */
public class Topup {

    DatabaseConnections connections = new DatabaseConnections();
    ClassImportantValues cl = new ClassImportantValues();
    Functions func = new Functions();
    XMLParser parser = new XMLParser();
    DataConversions convert = new DataConversions();

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
    String processingcode = "";
    String[] strReceivedData;
    String strBillNumber = "";
    String strwalkingcustomerphonenumber = "";
    String strDebitAccount = "";
    String strCreditAccount = "";

    public void Run(String IncomingMessage, String intid) {

        strReceivedData = IncomingMessage.split("#");
        try {

            strReceivedData = IncomingMessage.split("#");

            processingcode = strReceivedData[0].toString();
            strAgencyCashManagement = strReceivedData[1];
            strDeviceid = strReceivedData[2];

            switch (strAgencyCashManagement) {
                case "CASH":
                    strAmount = strReceivedData[3];
                    strAgentID = strReceivedData[4].trim();
                    strField100 = strReceivedData[5];
                    strwalkingcustomerphonenumber = strReceivedData[6];
                    strNarration = "AIRTIME TOPUP FOR MOBILE NO: " + strwalkingcustomerphonenumber;
                    strDebitAccount = func.fn_getAgentAccountNumber(strAgentID);
                    func.getESBResponse(strAgentID, strCardNumber, processingcode, strAmount, convert.PadZeros(12, intid), strNarration, strField100, strDebitAccount, strCreditAccount, strBillNumber, strwalkingcustomerphonenumber, strDeviceid);
                    break;
                case "AGENCY":
                    strTrack2Data = strReceivedData[3].replace("Ù", "");
                    strTrack2Data = strReceivedData[3].replace("?", "");
                    strPinClear = strReceivedData[5].replace("Ù", "");
                    strAmount = strReceivedData[6];
                    strAgentID = strReceivedData[7].trim();
                    strField100 = strReceivedData[8];
                    strwalkingcustomerphonenumber = strReceivedData[9];
                    if (strTrack2Data.contains("=")) {
                        strCardInformation = strTrack2Data.split("=");
                        strCardNumber = strCardInformation[0];
                        int strlen = strCardNumber.length();
                        if (strlen < 16) {
                            strResponse = func.strResponseHeader(strDeviceid);
                            strResponse += "--------------------------------" + "#";
                            strResponse += "INVALID PAN #";
                            strResponse += func.strResponseFooter(strAgentID);
                            func.SendPOSResponse(strResponse, intid);
                            return;
                        }
                        strExpiryDate = strCardInformation[1].substring(0, 4);
                        String[] strTrack2Data1 = strCardInformation[1].split("\\?");
                        strField35 = strCardInformation[0] + "=" + strTrack2Data1[0];
                    } else if (strTrack2Data.contains("D")) {
                        strCardInformation = strTrack2Data.split("D");
                        strCardNumber = strCardInformation[0];
                        int strlen = strCardNumber.length();
                        if (strlen < 16) {
                            strResponse = func.strResponseHeader(strDeviceid);
                            strResponse += "--------------------------------" + "#";
                            strResponse += "INVALID PAN #";
                            strResponse += func.strResponseFooter(strAgentID);
                            func.SendPOSResponse(strResponse, intid);
                            return;
                        }
                        strExpiryDate = strCardInformation[1].substring(0, 4);
                        String[] strTrack2Data1 = strCardInformation[1].split("\\?");
                        strField35 = strCardInformation[0] + "=" + strTrack2Data1[0];
                    }

                    strNarration = "AIRTIME TOPUP FOR MOBILE NO: " + strwalkingcustomerphonenumber;
                    strPhoneNumber = strwalkingcustomerphonenumber;
                    strVerifyPin = func.PIN_Verify(strBillNumber, strNarration, strField100, strAmount, processingcode, strPinClear, strCardNumber, strExpiryDate, strAgentID, strField35, convert.PadZeros(12, intid), strPhoneNumber, strDebitAccount, strCreditAccount, strDeviceid);
                    return;

            }

        } catch (Exception ex) {
            func.log("\nSEVERE AirtimeTopup() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }

    }
}
