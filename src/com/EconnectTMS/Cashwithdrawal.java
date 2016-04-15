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
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author Collins
 */
public class Cashwithdrawal {

    DatabaseConnections connections = new DatabaseConnections();
    ClassImportantValues cl = new ClassImportantValues();
    XMLParser parser = new XMLParser();
    DataConversions convert = new DataConversions();

    Functions func = new Functions();
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
    String strRecievedData[];
    String processingcode = "";
    String strNarration = "";
    String strDebitAccount = "";
    String strCreditAccount = "";
    String strPhoneNumber = "";
    String strProcCode = "";

    public void Run(String IncomingMessage, String intid) {

        try {
            strRecievedData = IncomingMessage.split("#");
            processingcode = "010000";
            strAgencyCashManagement = strRecievedData[1];
            strDeviceid = strRecievedData[2];
            strTrack2Data = strRecievedData[3].replace("Ù", "");
            strTrack2Data = strRecievedData[3].replace("?", "");
            strAgentID = func.fn_RemoveNon_Numeric(strAgentID);
            strPinClear = strRecievedData[5].replace("Ù", "");
            strPinClear = strPinClear.substring(0, 4);
            strAmount = strRecievedData[6].replace("Ù", "");

            strAgentID = strRecievedData[7].replace("Ù", "").trim();

            switch (strAgencyCashManagement) {

                case "AGENCY":
                    if (Double.valueOf(strAmount) < 0) {
                        strResponse = func.strResponseHeader(strDeviceid);
                        strResponse += "--------------------------------" + "#";
                        strResponse += "Amount must be greater than Zero #";
                        strResponse += func.strResponseFooter(strAgentID);
                        func.SendPOSResponse(strResponse, intid);
                        return;
                    }
                    if (Double.valueOf(strAmount) > 1000000) { //TRXN LIMIT
                        strResponse = func.strResponseHeader(strDeviceid);
                        strResponse += "AGENT ID:  " + strAgentID + "#";
                        strResponse += "TRAN NUM:  " + intid + "#";
                        strResponse += "--------------------------------" + "#";
                        strResponse += "                                " + "#";
                        strResponse += "        CASH WITHDRAWAL        " + "#";
                        strResponse += "                                " + "#";
                        strResponse += "    CASH WITHDRAWAL FAILED     " + "#";
                        strResponse += "TRANSACTION AMOUNT EXCEEDS LIMIT#";
                        strResponse += "                                " + "#";
                        strResponse += func.strResponseFooter(strAgentID);
                        func.SendPOSResponse(strResponse, intid);
                        return;
                    }

                    if (strTrack2Data.contains("=")) {

                        strCardInformation = strTrack2Data.split("=");
                        strCardNumber = strCardInformation[0];
                        int strlen = strCardNumber.length();

                        if (strlen < 16) {
                            strResponse = func.strResponseHeader(strDeviceid);
                            strResponse += "--------------------------------" + "#";
                            strResponse += "Invalid PAN #";
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
                            strResponse += func.strResponseFooter(strDeviceid);
                            func.SendPOSResponse(strResponse, intid);
                            return;
                        }

                        strExpiryDate = strCardInformation[1].substring(0, 4);
                        String[] strTrack2Data1 = strCardInformation[1].split("\\?");
                        strField35 = strCardInformation[0] + "=" + strTrack2Data1[0];
                    }

                    strProcCode = "AGENT-CHCW";
                    strCreditAccount = func.fn_getAgentAccountNumber(strAgentID);
                    strDebitAccount = strAccountNumber;
                    strNarration = "CASH WITHDRAWAL FROM ACCOUNT : " + strDebitAccount;

                    strVerifyPin = func.PIN_Verify(strDeviceid, strNarration, strProcCode, strAmount, processingcode, strPinClear, strCardNumber, strExpiryDate, strAgentID, strField35, convert.PadZeros(12, intid), strPhoneNumber, strDebitAccount, strCreditAccount, strDeviceid);
                    return;
                default:
                    break;
            }// end of switch

        } catch (Exception ex) {
            func.log("\nSEVERE CashWithdrawal() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
