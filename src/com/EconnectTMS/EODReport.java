/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EconnectTMS;

import com.MainFiles.Functions;
import com.Database.DatabaseConnections;
import com.MainFiles.ClassImportantValues;
import static com.MainFiles.ISO8583Adaptor.SOURCE_ID;
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
public class EODReport {

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
    String strRemittanceCode = "";
    String str514narration = "";
    String strAgentCode = "";
    String strAgentPassword = "";
    String[] strReceivedData;
    String processingcode = "";

    public void Run(String IncomingMessage, String intid) {

        strReceivedData = IncomingMessage.split("#");
        processingcode = strReceivedData[0].toString();
        try {
            strDeviceid = strReceivedData[2];
            strAgentCode = strReceivedData[3].replace("Ù", "");
            strAgentPassword = strReceivedData[4].replace("Ù", "");
            strAgentID = strReceivedData[5].trim();
            strAgentPassword = func.fn_RemoveNon_Numeric(strAgentPassword);
            strCardNumber = "0000000000000000";
            strExpiryDate = "0000";
            strAgentID = func.fn_RemoveNon_Numeric(strAgentID);
            strNarration = "EOD Report";

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

            strResponse = func.strResponseHeader(strDeviceid);
            strResponse = strResponse + func.fn_getAgentEODTransactions(strAgentID, strDeviceid, intid);
            strResponse = strResponse + func.strResponseFooter(strAgentID);

            func.SendPOSResponse(strResponse, intid);
        } catch (Exception ex) {
            func.log("Error on EOD Report " + ex.getMessage(), " EODReport");
        }
    }
}
