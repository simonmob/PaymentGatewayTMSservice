/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EconnectTMS;


import com.MainFiles.Functions;
import com.Database.DatabaseConnections;
import com.MainFiles.ClassImportantValues;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import com.Utilities.XMLParser;

/**
 *
 * @author Collins
 */
public class CardlessFullfilment {

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
    String strAgentCode = "";
    String strAgentPassword = "";
    String strField60 = "";
    String studentnumber = "";
    String studentname = "";
    String strReceivedData[];
    String strRemittanceCode = "";
    String strPhoneNumber = "";
    String strField100 = "";
    String processingcode = "";
    String strResponseXML = "";

    public void Run(String IncomingMessage, String intid) {
        
        strReceivedData = IncomingMessage.split("#");
        processingcode = strReceivedData[0].toString();

        try {
            strAgencyCashManagement = strReceivedData[1];
            strDeviceid = strReceivedData[2];
            strAgentID = func.fn_RemoveNon_Numeric(strAgentID);
            strRemittanceCode = strReceivedData[3].replace("Ù", "");
            strAmount = strReceivedData[4];
            strPhoneNumber = strReceivedData[5];
            strAgentID = strReceivedData[6].trim();
            strField100 = strReceivedData[7].toUpperCase();

            switch (strAgencyCashManagement) {

                case "AGENCY":
                    strCardNumber = "0000000000000000";
                    strExpiryDate = "0000";
                    if (strField100 == "NMB") {
                        strNarration = " CARDLESS FULLFILMENT BY " + strPhoneNumber;
                        //  strResponseXML = func.getESBResponse(strAgentID,strCardNumber, processingcode, "0", strDeviceid, strNarration, "CARDLESS_FULL", strAccountNumber, "", "", strPhoneNumber);
                    } else {
                        strResponse = func.strResponseHeader(strDeviceid);
                        strResponse += "Auth ID:      " + func.PadZeros(12, intid) + "#";
                        strResponse += "--------------------------------" + "#";
                        strResponse += "   Cardless Fulfilment          " + "#";
                        strResponse += "--------------------------------" + "#";
                        strResponse += strField100 + " Not Enabled      " + "#";
                        strResponse += "#--------------------------------#";
                        strResponse += func.strResponseFooter(strAgentID);
                        func.SendPOSResponse(strResponse, intid);
                        return;
                    }
                    break;
                default:
                    break;
            }

        } catch (Exception ex) {
            func.log("\nSEVERE CardlessFullfilment() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
