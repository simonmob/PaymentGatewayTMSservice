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
import java.sql.SQLException;

/**
 *
 * @author Collins
 */
public class CashDeposit {

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
    String strReceivedData[];
    String strField37 = "";
    String processingcode = "";
    String strResponseXML = "";
    String strBankCode = "";
    String strRequiredFields = "";
    String strProcCode = "";
    String strPhoneNumber = "";
    String strDebitAccount = "";
    String strCreditAccount = "";

    public void Run(String IncomingMessage, String intid) {

        try {

            strReceivedData = IncomingMessage.split("#");
            processingcode = strReceivedData[0].toString();
            strAgencyCashManagement = strReceivedData[1];
            strDeviceid = strReceivedData[2];
            strAccountNumber = strReceivedData[3].replace("Ù", "");

            switch (strAgencyCashManagement) {

                case "CARDLESS":
                    strAmount = strReceivedData[4];
                    amount = Double.valueOf(strAmount);
                    strAgentID = strReceivedData[5];
                    strAgentID = func.fn_RemoveNon_Numeric(strAgentID);
                    strProcCode = "AGENT-CHDP";
                    strDebitAccount = func.fn_getAgentAccountNumber(strAgentID);
                    strCreditAccount = strAccountNumber;
                    strNarration = "CASH DEPOSIT TO ACCOUNT : " + strCreditAccount;
                    func.getESBResponse(strAgentID, strCardNumber, processingcode, strAmount, convert.PadZeros(12, intid), strNarration, strProcCode, strDebitAccount, strCreditAccount, strDeviceid, strPhoneNumber, strDeviceid);
                    break;

                default:
                    break;
            }

        } catch (Exception ex) {
            func.log("\nSEVERE CashDeposit() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }

    }
}
