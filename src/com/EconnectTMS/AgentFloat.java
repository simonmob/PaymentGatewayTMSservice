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
public class AgentFloat {

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
    String[] strReceivedData;
    String processingcode = "";
    String strResponseXML = "";
    String strProcCode = "";
    String strPhoneNumber = "";

    public void Run(String IncomingMessage, String intid) {
        strReceivedData = IncomingMessage.split("#");
        try {
            processingcode = strReceivedData[0].toString();
            strAgencyCashManagement = strReceivedData[1];
            strDeviceid = strReceivedData[2].replace("Ù", "");
            strAgentID = strReceivedData[5].replace("Ù", "").trim();

            switch (strAgencyCashManagement) {
                case "AGENCY":
                    strAccountNumber = "";
                    strNarration = "AGENT FLOAT :" + strAgentID;
                    strProcCode = "BI";
                    strAmount = "0";
                    strAccountNumber = func.fn_getAgentAccountNumber(strAgentID);
                    strPhoneNumber = func.fn_getAgentPhonenumber2(strDeviceid);
                    func.getESBResponse(strAgentID, strCardNumber, processingcode, strAmount, convert.PadZeros(12, intid), strNarration, strProcCode, strAccountNumber, "", strDeviceid, strPhoneNumber, strDeviceid);
                    break;
                default:
                    break;
            }

        } catch (Exception ex) {
            func.log("\nSEVERE AgentFloat() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
