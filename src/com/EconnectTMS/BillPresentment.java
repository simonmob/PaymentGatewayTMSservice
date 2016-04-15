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
public class BillPresentment {

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
            processingcode = strReceivedData[0].toString();
            strAgencyCashManagement = strReceivedData[1];
            strDeviceid = strReceivedData[2];

            switch (strAgencyCashManagement) {
                case "INQUIRY":
                    strAgentID = strReceivedData[3].trim();
                    strField100 = strReceivedData[4] + "_INQ";
                    strBillNumber = strReceivedData[5];
                    strwalkingcustomerphonenumber = strReceivedData[6];
                    strNarration = strReceivedData[4] + " BILL PRESENTMENT FOR BILL NO " + strBillNumber;
                    strDebitAccount = func.fn_getAgentAccountNumber(strAgentID);
                    func.getESBResponse(strAgentID, strCardNumber, processingcode, strAmount, convert.PadZeros(12, intid), strNarration, strField100, strDebitAccount, strCreditAccount, strBillNumber, strwalkingcustomerphonenumber, strDeviceid);
                    break;
                default:
                    break;
            }

        } catch (Exception ex) {
            func.log("\nSEVERE BillPresentment() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
