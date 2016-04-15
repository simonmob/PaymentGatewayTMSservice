/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.MainFiles;

import static com.MainFiles.ISO8583Adaptor.COUNT_FILE;
import static com.MainFiles.ISO8583Adaptor.LOG_DIR;
import isojpos.BuildISOMessage;
import isojpos.BuildISOMessageVer2;
import isojpos.ParseISOMessage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;

/**
 *
 * @author Collins
 */
public class ISO8583 {

    private static final String countFile = ISO8583Adaptor.COUNT_FILE;
    private static final String InfoFile = "Info--";

    public ISO8583() {
    }

    public HashMap ReadISO(String IsoData) {
        HashMap<String, String> FieldsMap = new HashMap<>();
        ClassImportantValues cl = new ClassImportantValues();
        try {
            GenericPackager packager = new GenericPackager("basic.xml");
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.setISOConstantPresent(false);
            isoMsg.setHeaderPresent(false);
            isoMsg.setHeaderLength("4");
            isoMsg.setHeaderEcoding(isojpos.DataEncoding.ASC);
            isoMsg.setBitMapEncoding(isojpos.DataEncoding.ASC);
            isoMsg.setLoggerActive(false);
            isoMsg.setLogFile(LOG_DIR);
            isoMsg.setIsoMessage(IsoData);
            ParseISOMessage pp = new ParseISOMessage();
            FieldsMap = pp.getFields(isoMsg);

            //System.out.println(FieldsMap);
            printScreenMessage(FieldsMap);
            //isoMsg.setPackager(null);
            ISO8583 log = new ISO8583();
            log.logISOMsg(isoMsg, "Fields_IN");   //Log message
        } catch (ISOException ex) {
            //ex.printStackTrace();
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at ReadISO" + ex.getMessage() + "\n" + ESBLogger.StackTraceWriter(ex), "error--");
        }

        return FieldsMap;
    }

    public String CreateISO(HashMap<String, String> IsoFieldsMap) {
        String ISOMesage = "";
        ClassImportantValues cl = new ClassImportantValues();
        try {
            GenericPackager packager = new GenericPackager("basic.xml");
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.setISOConstantPresent(false);
            isoMsg.setHeaderPresent(false);
            isoMsg.setHeaderLength("4");
            isoMsg.setHeaderEcoding(isojpos.DataEncoding.ASC);
            isoMsg.setBitMapEncoding(isojpos.DataEncoding.ASC);
            isoMsg.setLoggerActive(false);
            isoMsg.setLogFile(LOG_DIR);
            isoMsg.setMTI(IsoFieldsMap.get("MTI"));
            //isoMsg.setMTI("0210");
            // System.out.println(IsoFieldsMap);

            printScreenMessage(IsoFieldsMap);

            for (Map.Entry<String, String> entry : IsoFieldsMap.entrySet()) {
                if (entry.getValue() != null) {
                    if (tryParseInt(entry.getKey()) && entry.getValue().length() != 0) {
                        //System.out.println(entry.getKey() +":"+ entry.getValue());
                        isoMsg.set(Integer.parseInt(entry.getKey()), entry.getValue());
                    }
                }

            }

            BuildISOMessage bb = new BuildISOMessage();
            ISOMesage = bb.returnISO(isoMsg);
            //System.out.println("ISO Message :: " + ISOMesage);
            logISOMsg(isoMsg, "Fields_OUT");  //log message fields
        } catch (ISOException ex) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at CreateISO" + ex.getMessage() + "\n" + ESBLogger.StackTraceWriter(ex), "error--");
        }

        return ISOMesage;
    }

    public void printScreenMessage(HashMap<String, String> FieldsMap) {
        String strlines = "\n==============================";
        String strResponseDescription = "";
        String responseDescription = "";
        String MTI = FieldsMap.get("MTI");
        String strField70 = FieldsMap.get("70");

        switch (MTI) {
            case "0810":
                switch (strField70) {
                    case "301":
                        if (!FieldsMap.get("39").toString().isEmpty()) {
                            if (FieldsMap.get("39").equals("00")) {
                                strResponseDescription = "SUCCESS";
                            } else {
                                strResponseDescription = "FAILED";
                            }
                        }
                        responseDescription = strlines + strlines;
                        responseDescription += "\nEcho Network Message Response at " + (new SimpleDateFormat("MMMM dd,yyyy hh:mm:ss.SSS a zzzz")).format(new java.util.Date());
                        responseDescription += "\nLogFromPostilion:STAN: " + FieldsMap.get("11").toString();
                        responseDescription += "\nLogFromPostilion:StatusCode: " + FieldsMap.get("39").toString();
                        responseDescription += "\nLogFromPostilion:StatusType: RESPONSE";
                        responseDescription += "\nLogFromPostilion:StatusDescription: " + strResponseDescription;
                        break;
                    case "001":
                        if (!FieldsMap.get("39").toString().isEmpty()) {
                            if (FieldsMap.get("39").equals("00")) {
                                strResponseDescription = "SUCCESS";
                            } else {
                                strResponseDescription = "FAILED";
                            }
                        }
                        responseDescription = strlines + strlines;
                        responseDescription += "\nSignon Network Message Response at " + (new SimpleDateFormat("MMMM dd,yyyy hh:mm:ss.SSS a zzzz")).format(new java.util.Date());
                        responseDescription += "\nLogFromPostilion:STAN: " + FieldsMap.get("11").toString();
                        responseDescription += "\nLogFromPostilion:StatusCode: " + FieldsMap.get("39").toString();
                        responseDescription += "\nLogFromPostilion:StatusType: RESPONSE";
                        responseDescription += "\nLogFromPostilion:StatusDescription: " + strResponseDescription;
                        break;
                    default:
                        responseDescription = "";
                        break;
                }
                break;
            case "0210":
                Functions func = new Functions();
                strResponseDescription = func.getResponseCode(FieldsMap.get("39").toString());
                
                responseDescription = strlines + strlines;
                responseDescription += "\nFinancial Transaction Network Response at " + (new SimpleDateFormat("MMMM dd,yyyy hh:mm:ss.SSS a zzzz")).format(new java.util.Date());
                responseDescription += "\nLogFromPostilion:CorrelationID: " + FieldsMap.get("37").toString();
                responseDescription += "\nLogFromPostilion:StatusCode: " + FieldsMap.get("39").toString();
                responseDescription += "\nLogFromPostilion:StatusType: RESPONSE";
                responseDescription += "\nLogFromPostilion:StatusDescription: " + strResponseDescription;
                break;
            case "0800":
                switch (strField70) {
                    case "301":
                        responseDescription = strlines + strlines;
                        responseDescription += "\nEcho Network Message Request at " + (new SimpleDateFormat("MMMM dd,yyyy hh:mm:ss.SSS a zzzz")).format(new java.util.Date());
                        responseDescription += "\nLogToPostilion:STAN: " + FieldsMap.get("11").toString();
                        responseDescription += "\nLogToPostilion:StatusCode: 00";
                        responseDescription += "\nLogToPostilion:StatusType: REQUEST";
                        responseDescription += "\nLogToPostilion:StatusDescription: SUCCESS";
                        break;
                    case "001":
                        responseDescription = strlines + strlines;
                        responseDescription += "\nSignon Network Message Request at " + (new SimpleDateFormat("MMMM dd,yyyy hh:mm:ss.SSS a zzzz")).format(new java.util.Date());
                        responseDescription += "\nLogToPostilion:STAN: " + FieldsMap.get("11").toString();
                        responseDescription += "\nLogToPostilion:StatusCode: 00";
                        responseDescription += "\nLogToPostilion:StatusType: REQUEST";
                        responseDescription += "\nLogToPostilion:StatusDescription: SUCCESS";
                        break;
                }
                break;
            case "0200":
                responseDescription = strlines + strlines;
                responseDescription += "\nFinancial Transaction Network Request at " + (new SimpleDateFormat("MMMM dd,yyyy hh:mm:ss.SSS a zzzz")).format(new java.util.Date());
                responseDescription += "\nLogToPostilion:STAN: " + FieldsMap.get("11").toString();
                responseDescription += "\nLogToPostilion:StatusCode: 00";
                responseDescription += "\nLogToPostilion:StatusType: REQUEST";
                responseDescription += "\nLogToPostilion:StatusDescription: SUCCESS";
                break;
            default:
                responseDescription = "";
                break;
        }
        System.out.println(responseDescription);
    }

    private static void logISOMsg(ISOMsg msg, String filename) {
        ClassImportantValues cl = new ClassImportantValues();
        try {
            String fields = "";
            String mti = "----ISO FIELDS MESSAGE-----\n" + "  <MTI>" + msg.getMTI() + "<MTI>";
            //System.out.println("");
            for (int i = 1; i <= msg.getMaxField(); i++) {
                if (msg.hasField(i)) {
                    if (fields.isEmpty()) {
                        fields = fields + "<Field_" + i + ">" + msg.getString(i) + "</Field_" + i + ">\n";
                    } else {
                        fields = fields + "<Field_" + i + ">" + msg.getString(i) + "</Field_" + i + ">\n";
                    }
                }
            }

            cl.logs(filename, mti + "\n" + fields); //log my message to file
        } catch (ISOException e) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error LogISOmsg :: " + e.getMessage() + "\n" + ESBLogger.StackTraceWriter(e), "error--");
        } finally {
            //System.out.println("\n==============================");
        }

    }

    public String CreateEchoNetworkISO(String MsgType, String ReqType, HashMap<String, String>... OriginalFieldsMap) {

        ClassImportantValues cl = new ClassImportantValues();
        String ISOMesage = "";
        try {
            GenericPackager packager = new GenericPackager("basic.xml");
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.setISOConstantPresent(false);
            isoMsg.setHeaderPresent(true);
            isoMsg.setHeaderLength("4");
            isoMsg.setHeaderEcoding("ASC");
            isoMsg.setBitMapEncoding("ASC");
            isoMsg.setLoggerActive(false);
            isoMsg.setLogFile(LOG_DIR);

            String trn_tran_date = anyDate("MMdd"); //MMdd
            String trn_tran_time = anyDate("HHmmss"); //HHmmss
            String field7 = trn_tran_date + trn_tran_time; //MMddHHmmss 
            String field11 = "";  //STAN

            if (OriginalFieldsMap.length > 0) {
                field11 = OriginalFieldsMap[0].get("11"); //Get Original STAN
            } else {
                field11 = GetSequenceNumber(); //New STAN
            }

            String field12 = trn_tran_time;  //hhmmss
            String field13 = trn_tran_date; //MMDD
            String field70 = "001"; //Network Management Information Code: 001 or 002
            String field128 = "2020202032202020";

            switch (MsgType) {
                case "signon":
                    isoMsg.set(70, field70);
                    if (ReqType.equals("ack")) {
                        isoMsg.set(39, "00");
                        isoMsg.setMTI("0810");
                        isoMsg.set(7, OriginalFieldsMap[0].get("7"));
                        isoMsg.set(11, OriginalFieldsMap[0].get("11"));
                        isoMsg.set(12, OriginalFieldsMap[0].get("12"));
                        isoMsg.set(13, OriginalFieldsMap[0].get("13"));
                    } else {
                        isoMsg.setMTI("0800");
                        isoMsg.set(7, field7);
                        isoMsg.set(11, field11);
                        isoMsg.set(12, field12);
                        isoMsg.set(13, field13);
                        isoMsg.set(70, field70);
                    }

                    break;

                case "echo":
                    isoMsg.set(70, "301");

                    if (ReqType.equals("ack")) {
                        isoMsg.set(39, "00");
                        isoMsg.setMTI("0810");
                        isoMsg.set(7, OriginalFieldsMap[0].get("7"));
                        isoMsg.set(11, OriginalFieldsMap[0].get("11"));
                        isoMsg.set(12, OriginalFieldsMap[0].get("12"));
                        isoMsg.set(13, OriginalFieldsMap[0].get("13"));
                    } else {
                        isoMsg.setMTI("0800");
                        isoMsg.set(7, field7);
                        isoMsg.set(11, field11);
                        isoMsg.set(12, field12);
                        isoMsg.set(13, field13);
                    }

                default:
                    break;

            }

            BuildISOMessage bb = new BuildISOMessage();
            ISOMesage = bb.returnISO(isoMsg);
            ISO8583 log = new ISO8583();
            cl.logs("NETWORKISO", MsgType + ":  " + ISOMesage);
            log.logISOMsg(isoMsg, "ISOOutNetFields");
            return ISOMesage;
        } catch (ISOException ex) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at CreateEchoNetworkISO" + ex.getMessage() + "\n" + ESBLogger.StackTraceWriter(ex), "error--");
        }

        return ISOMesage;
    }

    public String GetField(HashMap fields, int id) {
        try {
            return fields.get(String.valueOf(id)).toString();
        } catch (Exception ex) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Field " + id + " missing in Processing Code " + fields.get("3"), "Missing_Fields--");
            return "";
        }

    }

    boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public String anyDate(String format) {
        try {
            if ("".equals(format)) {
                format = "yyyy-MM-dd HH:mm:ss"; // default
            }
            java.util.Date today;
            SimpleDateFormat formatter;

            formatter = new SimpleDateFormat(format);
            today = new java.util.Date();
            return (formatter.format(today));
        } catch (Exception ex) {
            System.out.println(" \n**** anyDate ****\n" + ex.getMessage());
        }
        return "";
    } // end fn anyDate

    public String CreateField127ISO(HashMap<String, String> IsoFieldsMap) {
        String ISOMesage = "";
        ClassImportantValues cl = new ClassImportantValues();
        try {
            GenericPackager packager = new GenericPackager("postillionconfig.xml");
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.setISOConstantPresent(false);
            isoMsg.setHeaderPresent(false);
            isoMsg.setMTIPresent(false);
            isoMsg.setHeaderLength("4");
            isoMsg.setHeaderEcoding(DataEncoding.ASC);
            isoMsg.setBitMapEncoding(DataEncoding.ASC);
            isoMsg.setLoggerActive(false);
            isoMsg.setLogFile(LOG_DIR);
            isoMsg.setMTI(IsoFieldsMap.get("MTI"));

            for (Map.Entry<String, String> entry : IsoFieldsMap.entrySet()) {
                if (tryParseInt(entry.getKey()) && entry.getValue().length() != 0) {
                    //System.out.println(entry.getKey() +":"+ entry.getValue());
                    isoMsg.set(Integer.parseInt(entry.getKey()), entry.getValue());
                }

            }
            BuildISOMessageVer2 bb = new BuildISOMessageVer2();
            ISOMesage = bb.returnISO(isoMsg);
            logISOMsg(isoMsg, "ISOoutFields");  //log message fields
        } catch (ISOException ex) {
            //Logger.getLogger(BuildISOMessage.class.getName()).log(Level.SEVERE, null, ex);
            //ex.printStackTrace();

            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at CreateField127ISO" + ex.getMessage() + "\n" + ESBLogger.StackTraceWriter(ex), "ERROR");

        }

        return ISOMesage;
    }

    public static String GetSequenceNumber() {
        ClassImportantValues cl = new ClassImportantValues();
        Functions func = new Functions();
        String strcountFile = COUNT_FILE;
        File file = new File(strcountFile);
        FileLock flock = null;
        String strPaddedNumber = "";
        try {

            if (file.exists()) {
                FileInputStream fstream = new FileInputStream(strcountFile);

                // Read the File Containing the Next Sequence.. Then
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine = "";
                strLine = br.readLine();
                in.close();

                int intCurrentNum = Integer.parseInt(strLine);
                intCurrentNum += 1;

                strPaddedNumber = String.format("%06d", intCurrentNum);

                //Now OverWrite the File
                FileOutputStream fos = new FileOutputStream(file, false);
                fos.write(strPaddedNumber.getBytes());
                fos.close();

            } // end if file.exists()
            else { // create the file if it doesn't exist

                file.createNewFile();
                // write 1234 to the file to begin the counter
                try (FileOutputStream fos = new FileOutputStream(file, false)) {
                    // write 1234 to the file to begin the counter
                    fos.write("1234".getBytes());
                }
                strPaddedNumber = String.format("%06d", 1);
            }

            return strPaddedNumber;

        } catch (IOException | NumberFormatException ex) {

            System.out.println("Error GetSequenceNumber : " + ex + func.StackTraceWriter(ex));
            return null;

        }

    } // end fn GetSequenceNumber
}
