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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Collins
 */
public class CardActivation {

    DatabaseConnections connections = new DatabaseConnections();
    ClassImportantValues cl = new ClassImportantValues();
    Functions func = new Functions();
    XMLParser parser = new XMLParser();
    DataConversions convert = new DataConversions();
    HashMap<String, String> AccDetails = new HashMap<String, String>();

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
    String processingcode = "";
    String strResponseXML = "";
    String[] strReceivedData;
    String strSwitchMessage = "";
    String[] strArray;
    String status = "";
    String strMessageToPOS = "";
    String strProcCode = "";
    String strPhoneNumber = "";
    String strDebitAccount = "";
    String strCreditAccount = "";
    String strConfirmPIN = "";
    String strNewPIN = "";
    String strActivationCode = "";

    public void Run(String IncomingMessage, String intid) {

      
        try {

            strReceivedData = IncomingMessage.split("#");
            processingcode = strReceivedData[0].toString();
            strAgencyCashManagement = strReceivedData[1];
            strDeviceid = strReceivedData[2].replace("Ù", "");
            strTrack2Data = strReceivedData[3].replace("Ù", "");
            strTrack2Data = strReceivedData[3].replace("?", "");
            strNewPIN = strReceivedData[5].replace("Ù", "");
            strNewPIN = strNewPIN.substring(0, 4);
            strAgentID = strReceivedData[6].replace("Ù", "").trim();
            strAgentID = func.fn_RemoveNon_Numeric(strAgentID);
            strActivationCode = strReceivedData[7].replace("Ù", "");
            strConfirmPIN = strReceivedData[8].replace("Ù", "");
            strConfirmPIN = strConfirmPIN.substring(0, 4);
            strAccountNumber = strReceivedData[9];

            switch (strAgencyCashManagement) {
                case "AGENCY":
                    //EMV cards look for D in strTrack2Data
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
                        int strlen1 = strCardNumber.length();
                        if (strlen1 < 16) {
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

                    if (!strNewPIN.equals(strConfirmPIN)) {
                        strResponse = func.strResponseHeader(strDeviceid);
                        strResponse += "--------------------------------" + "#";
                        strResponse += " PIN MISMATCH. UNSUCCESSFUL CARD ACTIVATION #";
                        strResponse += func.strResponseFooter(strAgentID);
                        func.SendPOSResponse(strResponse, intid);
                        return;
                    }

                    strNarration = "CARD ACTIVATION FOR ACCOUNT " + strAccountNumber;
                    strProcCode = "AGENT-CDACT";
                    strDebitAccount = strAccountNumber;

                    // Verify Activation Code
                    AccDetails = func.getESBResponse(strAgentID, strCardNumber, processingcode, strAmount, convert.PadZeros(12, intid), strNarration, strProcCode, strAccountNumber, "", strActivationCode, strPhoneNumber,strDeviceid);

                    if (!AccDetails.isEmpty()) {
                        switch (AccDetails.get("39")) {
                            case "00":
                                // Forward PIN to Postillion for Card Activation
                                strResponse = func.strResponseHeader(strDeviceid);
                                strResponse += "--------------------------------" + "#";
                                strResponse += " ACTIVATION CODE SUCCESS#";
                                strResponse += func.strResponseFooter(strAgentID);
                                func.SendPOSResponse(strResponse, intid);
                                break;
                            default:
                                strResponse = func.strResponseHeader(strDeviceid);
                                strResponse += "--------------------------------" + "#";
                                strResponse += "     WRONG ACTIVATION CODE     " + "#";
                                strResponse += func.strResponseFooter(strAgentID);
                                func.SendPOSResponse(strResponse, intid);
                                break;
                        }
                    }
                    break;
                default:
                    strResponse = func.strResponseHeader(strDeviceid);
                    strResponse += "--------------------------------" + "#";
                    strResponse += "     PLEASE TRY AGAIN LATER     " + "#";
                    strResponse += func.strResponseFooter(strAgentID);
                    func.SendPOSResponse(strResponse, intid);
                    break;
            }

        } catch (Exception ex) {
            func.log("\nSEVERE CardActivation() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }

    }
}
