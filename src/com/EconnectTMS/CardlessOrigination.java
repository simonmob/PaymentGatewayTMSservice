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
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author Collins
 */
public class CardlessOrigination {

    DatabaseConnections connections = new DatabaseConnections();
    ClassImportantValues cl = new ClassImportantValues();
    Functions func = new Functions();
    XMLParser parser = new XMLParser();
    DataConversions convert = new DataConversions();

    String strCardNumber = null;
    String str514narration = "";
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
    String strManagerName = "";
    String strManagerEmail = "";
    String strAgentPhone = "";
    String strAgentFloatAccount = "";
    String AgentName = "";
    String Address = "";
    String strPhoneNumberToAlert = "";
    String strPhoneNumber = "";
    String strMessage = "";
    String strField100 = "";
    String processingcode = "";
    String strResponseXML = "";
    String[] strReceivedData;
    String strDebitAccount = "";
    String strCreditAccount = "";

    public void Run(String IncomingMessage, String intid) {

        strReceivedData = IncomingMessage.split("#");
        processingcode = strReceivedData[0].toString();
        strAgencyCashManagement = strReceivedData[1];
        strDeviceid = strReceivedData[2];
        strTrack2Data = strReceivedData[3].replace("Ù", "");
        strTrack2Data = strReceivedData[3].replace("?", "");
        strPinClear = strReceivedData[5].replace("Ù", "");
        strPinClear = strPinClear.substring(0, 4);
        strAmount = strReceivedData[6];
        strPhoneNumber = strReceivedData[7].trim();
        strAgentID = strReceivedData[8].trim();
        strAgentID = func.fn_RemoveNon_Numeric(strAgentID);
        try {
            switch (strAgencyCashManagement) {
                case "AGENCY":
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
                        String[] strTrack2Data1 = strCardInformation[1].split("?");
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
                        String[] strTrack2Data1 = strCardInformation[1].split("?");
                        strField35 = strCardInformation[0] + "=" + strTrack2Data1[0];

                    }
                    strNarration = "CARDLESS REMITTANCE TO: " + strAccountNumber;
                    String strProcCode = "AGENT-CDOR";
                    strVerifyPin = func.PIN_Verify(strDeviceid, strNarration, strProcCode, strAmount, processingcode, strPinClear, strCardNumber, strExpiryDate, strAgentID, strField35, convert.PadZeros(12, intid), strPhoneNumber, strDebitAccount, strCreditAccount, strDeviceid);
                    break;
                default:
                    break;

            }//end of switch
        } catch (Exception ex) {
            func.log("\nSEVERE CardlessRemittance() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
