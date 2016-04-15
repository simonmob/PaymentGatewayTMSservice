package com.EconnectTMS;

import com.MainFiles.Functions;
import com.Database.DatabaseConnections;
import com.MainFiles.ClassImportantValues;
import com.MainFiles.DataConversions;
import static com.MainFiles.ISO8583Adaptor.ECDB;
import static com.MainFiles.ISO8583Adaptor.ECPASSWORD;
import static com.MainFiles.ISO8583Adaptor.ECSERVER;
import static com.MainFiles.ISO8583Adaptor.ECUSER;
import static com.MainFiles.ISO8583Adaptor.SOURCE_ID;
import com.Utilities.XMLParser;
import java.util.HashMap;

public class ReversalRequest {

    DatabaseConnections connections = new DatabaseConnections();
    ClassImportantValues cl = new ClassImportantValues();
    Functions func = new Functions();
    XMLParser parser = new XMLParser();
    DataConversions convert = new DataConversions();
    HashMap<String,String> parseFields = new HashMap<String,String>();

    String strDeviceid = "";
    String strAgentID = "";
    String strAgencyCashManagement = "";
    String[] strCardInformation;
    String processingcode = "";
    String[] strReceivedData;
    String strReferenceNumber = "";
    String strProcCode = "";
    String strCardNumber = "";
    String strAmount = "";
    String strNarration = "";
    String strAccountNumber = "";
    String strPhoneNumber = "";
    String strResponse = "";

    public void Run(String IncomingMessage, String intid) {
        strReceivedData = IncomingMessage.split("#");
        try {
            processingcode = strReceivedData[0].toString();
            strAgencyCashManagement = strReceivedData[1].toString();
            strDeviceid = strReceivedData[2].toString();
            strReferenceNumber = strReceivedData[3].toString();
            strAgentID = strReceivedData[4].toString().replace("Ù", "");
            parseFields = func.getTrxnDetails(strReferenceNumber);
            
            //Allow only reversal request for Merchant Payments
            if (!parseFields.isEmpty()) {
                func.generateReversal(parseFields);
            } else {

                strResponse = func.strResponseHeader(strDeviceid);
                strResponse += "AGENT ID:  " + strAgentID + "#";
                strResponse += "TRAN NUM:  " + intid + "#";
                strResponse += "--------------------------------" + "#";
                strResponse += "                                " + "#";
                strResponse += "        REVERSAL REQUEST        " + "#";
                strResponse += "                                " + "#";
                strResponse += "    REVERSAL REQUEST FAILED     " + "#";
                strResponse += "TRANS. NOT PERMITED FOR REVERSAL#";
                strResponse += "                                " + "#";
                strResponse += func.strResponseFooter(strAgentID);
                func.SendPOSResponse(strResponse, intid);
            }

        } catch (Exception ex) {
            func.log("\nSEVERE ReversalRequest() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
