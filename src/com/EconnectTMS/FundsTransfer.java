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
public class FundsTransfer {

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
    String strAccountNumber2 = "";
    String studentnumber = "";
    String studentname = "";
    String[] strReceivedData;
    String processingcode = "";
    String strDebitAccount = "";
    String strCreditAccount = "";
    String strPhoneNumber = "";
    String strProcCode = "";

    public void Run(String IncomingMessage, String intid) {

        strReceivedData = IncomingMessage.split("#");

        try {
            strReceivedData = IncomingMessage.split("#");
            processingcode = strReceivedData[0].toString();
            strAgencyCashManagement = strReceivedData[1];
            strDeviceid = strReceivedData[2];
            strAccountNumber = strReceivedData[3].replace("Ù", "");
            strTrack2Data = strReceivedData[4].replace("Ù", "");
            strTrack2Data = strReceivedData[4].replace("?", "");
            strAmount = strReceivedData[6];
            amount = Double.valueOf(strAmount);
            strAgentID = strReceivedData[7];
            strAgentID = func.fn_RemoveNon_Numeric(strAgentID);
            strPinClear = strReceivedData[5].replace("Ù", "");
            strPinClear = strPinClear.substring(0, 4);

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

                    if (amount < 0) {
                        strResponse = func.strResponseHeader(strDeviceid);
                        strResponse += "--------------------------------" + "#";
                        strResponse += "Amount must be greater than Zero #";
                        strResponse += func.strResponseFooter(strAgentID);
                        func.SendPOSResponse(strResponse, intid);
                        return;
                    }

                    strProcCode = "AGFT";
                    strDebitAccount = func.fn_getAgentAccountNumber(strAgentID);
                    strCreditAccount = strAccountNumber;
                    strNarration = "FUNDS TRANSFER FROM ACCOUNT " + strDebitAccount + " TO ACCOUNT " + strCreditAccount;
                    strVerifyPin = func.PIN_Verify(strDeviceid, strNarration, strProcCode, strAmount, processingcode, strPinClear, strCardNumber, strExpiryDate, strAgentID, strField35, convert.PadZeros(12, intid), strPhoneNumber, strDebitAccount, strCreditAccount, strDeviceid);
                    return;

            }

        } catch (Exception ex) {
            func.log("\nSEVERE FundsTransfer() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
