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
import java.sql.SQLException;
import java.util.HashMap;

/**
 *
 * @author Collins
 */
public class Login {

    DatabaseConnections connections = new DatabaseConnections();
    ClassImportantValues cl = new ClassImportantValues();
    Functions func = new Functions();
    XMLParser parser = new XMLParser();

    String strAgentCode = "";
    String strAgentPassword = "";
    String strDeviceid = "";
    String strResponse = "";
    String strField39 = "";
    String[] strLoginStatus;
    String strField37 = "";
    String processingcode = "";
    String[] strRecievedData;
    String strField48 = "";
    String strUserType = "";
    String strActiveDevice = "";
    String strTrials = "";
    String strLoggedin = "";
    String strActiveAgent = "";
    String strFirstLogin = "";

    public void Run(String IncomingMessage, String intID) {

        String strRecievedData[] = IncomingMessage.split("#");
        processingcode = strRecievedData[0].toString();
        try {
            strDeviceid = strRecievedData[1].replace("Ù", "");
            strAgentCode = strRecievedData[2].replace("Ù", "");
            strAgentPassword = strRecievedData[3].replace("Ù", "");
            strLoginStatus = func.fn_verify_login_details(strAgentCode, strAgentPassword, strDeviceid).split("\\|");

            strActiveDevice = strLoginStatus[0];
            strUserType = strLoginStatus[1];
            strTrials = strLoginStatus[2];
            strLoggedin = strLoginStatus[3];
            strFirstLogin = strLoginStatus[4];
            strActiveAgent = strLoginStatus[5];

            strAgentCode = func.fn_RemoveNon_Numeric(strAgentCode);

            if (strActiveDevice.equals("1")) {
                if (strActiveAgent.equals("1")) {
                    strResponse = "11#";
                    strField39 = "00";
                    strField48 = "Successful";
                } else {
                    strResponse = "55#";
                    strField39 = "99";
                    strField48 = "Agent is Inactive";
                }
            } else {
                if (strActiveDevice.equals("0") && strUserType.equals("0") && strActiveAgent.equals("0") && strFirstLogin.equals("0")) {
                    strResponse = "57#";
                    strField39 = "99";
                    strField48 = "Invalid Credentials";
                } else {
                    strResponse = "56#";
                    strField39 = "99";
                    strField48 = "Terminal is blocked";
                }
            }

            HashMap<String, String> map = new HashMap<String, String>();
            String strNarration = "USER LOGIN " + strAgentCode;

            map.put("0", "0200");
            map.put("2", "0000000000000000");
            map.put("3", processingcode);
            map.put("4", "0");
            map.put("7", func.anyDate("MMDDHHMMSS"));
            map.put("11", strDeviceid);
            map.put("32", SOURCE_ID);
            map.put("37", strField37);
            map.put("39", strField39);
            map.put("48", strField48);
            map.put("65", strAgentCode);
            map.put("88", strNarration);
            map.put("100", "LOGIN");
            map.put("102", strAgentCode);
            map.put("103", "");
            map.put("104", "");
            map.put("ReferenceNumber", intID);
            String strXML_Request = parser.WriteXML(map);
            map.put("strRequesttoEconnect", strXML_Request);
            map.put("strResponseFromEconnect", "");

            func.spInsertPOSTransaction(map);

            func.SendPOSResponse(strResponse, intID);
            System.out.println(strField48);

        } catch (Exception ex) {
            func.log("\nSEVERE Login() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
