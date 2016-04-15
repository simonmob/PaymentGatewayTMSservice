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

/**
 *
 * @author Collins
 */
public class BillPayments {

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
    String strAgentCode = "";
    String strAgentPassword = "";
    String strField60 = "";
    String studentnumber = "";
    String studentname = "";
    String strField37 = null;
    String strBillNumber = null;
    String strField100 = null;
    String strwalkingcustomerphonenumber = "";
    String strReceivedData[] = null;
    String EcgBalresponse = "";
    String[] strECGResponse;
    String strResponseMessage = "";
    String status = "";
    String customername = "";
    String custbal = "";
    String outstandingdate = "";
    String strResponseXML = "";
    String processingcode = "";
    String strPhoneNumber = "";
    String strDebitAccount = "";
    String strCreditAccount = "";

    public void Run(String IncomingMessage, String intid) {

        try {
            strReceivedData = IncomingMessage.split("#");

            strAgencyCashManagement = strReceivedData[1];
            processingcode = strReceivedData[0].toString();
            strDeviceid = strReceivedData[2];

            switch (strAgencyCashManagement) {
                case "CASH":
                    strAmount = strReceivedData[3];
                    strAgentID = strReceivedData[4].trim();
                    strField100 = strReceivedData[5];
                    strBillNumber = strReceivedData[6];
                    strwalkingcustomerphonenumber = strReceivedData[7];
                    strNarration = "BILL PAYMENT FOR BILL NO " + strBillNumber;
                    strDebitAccount = func.fn_getAgentAccountNumber(strAgentID);
                    func.getESBResponse(strAgentID, strCardNumber, processingcode, strAmount, convert.PadZeros(12, intid), strNarration, strField100, strDebitAccount, strCreditAccount, strBillNumber, strwalkingcustomerphonenumber, strDeviceid);

                    break;
                case "AGENCY":
                    strTrack2Data = strReceivedData[3].replace("Ù", "");
                    strTrack2Data = strReceivedData[3].replace("?", "");
                    strPinClear = strReceivedData[5].replace("Ù", "");
                    strPinClear = strPinClear.substring(0, 4);
                    strAmount = strReceivedData[6];
                    strAgentID = strReceivedData[7].trim();
                    strAgentID = func.fn_RemoveNon_Numeric(strAgentID);
                    strField100 = strReceivedData[8];
                    strBillNumber = strReceivedData[9];
                    strwalkingcustomerphonenumber = strReceivedData[10];
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

                    strNarration = "BILL PAYMENT FOR BILL NO " + strBillNumber;
                    strPhoneNumber = strwalkingcustomerphonenumber;
                    strVerifyPin = func.PIN_Verify(strBillNumber, strNarration, strField100, strAmount, processingcode, strPinClear, strCardNumber, strExpiryDate, strAgentID, strField35, convert.PadZeros(12, intid), strPhoneNumber, strDebitAccount, strCreditAccount, strDeviceid);
                    return;

                default:
                    break;
            }

        } catch (Exception ex) {
            func.log("\nSEVERE BillPayments() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
