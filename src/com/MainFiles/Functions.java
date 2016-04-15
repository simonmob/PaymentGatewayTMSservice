/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.MainFiles;

import com.Database.DatabaseConnections;
import static com.EconnectTMS.EconnectTMSservice.OuterHoldingQueue;
import com.Logger.Logging;
import static com.MainFiles.ISO8583Adaptor.AGENCY_ADAPTER_URL;
import static com.MainFiles.ISO8583Adaptor.AZAMTV;
import static com.MainFiles.ISO8583Adaptor.COUNT_FILE;
import static com.MainFiles.ISO8583Adaptor.CUSTOMER_DETAILS_PASSWORD;
import static com.MainFiles.ISO8583Adaptor.CUSTOMER_DETAILS_SOURCE_ID;
import static com.MainFiles.ISO8583Adaptor.CUSTOMER_DETAILS_URL;
import static com.MainFiles.ISO8583Adaptor.CUSTOMER_DETAILS_USERNAME;
import static com.MainFiles.ISO8583Adaptor.DAWASCO;
import static com.MainFiles.ISO8583Adaptor.ECDB;
import static com.MainFiles.ISO8583Adaptor.ECPASSWORD;
import static com.MainFiles.ISO8583Adaptor.ECSERVER;
import static com.MainFiles.ISO8583Adaptor.ECUSER;
import static com.MainFiles.ISO8583Adaptor.LUKUPREPAID;
import static com.MainFiles.ISO8583Adaptor.PIN_VERIFICATION_ENCRYPT_KEYS;
import static com.MainFiles.ISO8583Adaptor.PROVIDER_URL;
import static com.MainFiles.ISO8583Adaptor.QUEUE_REQUEST;
import static com.MainFiles.ISO8583Adaptor.QUEUE_RESPONSE;
import static com.MainFiles.ISO8583Adaptor.SOURCE_ID;
import com.SocketOperations.ClientSocketHandler;
import com.SocketOperations.MySocketHandler;
import com.Utilities.EsbFormatter;
import com.Utilities.PinBlock;
import com.Utilities.SMSCall;
import com.Utilities.Util;
import static com.Utilities.Util.HexString2Bytes;
import com.Weblogic.DistributedWebLogicQueueBrowser;
import isojpos.MyLogger;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.sql.CallableStatement;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

/**
 *
 * @author Collins
 */
public class Functions {

    private FileHandler fh = null;
    private Logger logger = null;
    private static final char DEFAULT_TRIM_WHITESPACE = ' ';

    DatabaseConnections connections = new DatabaseConnections();
    ClassImportantValues cl = new ClassImportantValues();
    Logging logging = new Logging();
    ISO8583 iso = new ISO8583();
    Util generatePINBlock = new Util();

    public static String StackTraceWriter(Exception exception) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        exception.printStackTrace(printWriter);
        String s = writer.toString();
        return s;
    }

    public static String trimLeftString(String string) {
        return trimLeft(string, DEFAULT_TRIM_WHITESPACE);
    }

    /**
     * Trims the character given from the given string and returns the result.
     *
     * @param string The string to trim, cannot be null.
     * @param trimChar The character to trim from the left of the given string.
     * @return A string with the given character trimmed from the string given.
     */
    public static String trimLeft(final String string, final char trimChar) {
        final int stringLength = string.length();
        int i;

        for (i = 0; i < stringLength && string.charAt(i) == trimChar; i++) {
            /* increment i until it is at the location of the first char that
             * does not match the trimChar given. */
        }

        if (i == 0) {
            return string;
        } else {
            return string.substring(i);
        }
    }

    public String anyDate(String format) {
        try {
            if ("".equals(format)) {
                // format = "yyyy-MM-dd HH:mm:ss"; // default
                format = "yyyy/MM/dd HH:mm:ss"; // default
            }
            java.util.Date today;
            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat(format);
            today = new java.util.Date();
            return (formatter.format(today));
        } catch (Exception ex) {
            this.log("\nINFO anyDate() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }
        return "";
    }

    public String generateUniqueReferenceNumber() throws SQLException {
        String strReferenceNumber = "";
        Connection dbConnection = connections.getDBConnection(ECSERVER, ECDB, ECUSER, ECPASSWORD);
        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "tz"));
            String strStoredProcedure = "{call SP_GET_AGENCY_NEXT_SEQ(?)}";
            CallableStatement callableStatement = dbConnection.prepareCall(strStoredProcedure);
            callableStatement.registerOutParameter(1, java.sql.Types.VARCHAR);
            callableStatement.executeUpdate();
            String response = callableStatement.getString(1);
            strReferenceNumber = "POS" + response;
        } catch (Exception ex) {
            this.log("INFO :: Error on generateUniqueReferenceNumber " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        } finally {
            dbConnection.close();
        }
        return strReferenceNumber;
    }

    public String ddmmyy() {
        //Creating Date in java with today's date.
        java.util.Date dateNow = new java.util.Date();

//converting  date into ddMMyyyy format example "14092011"
        SimpleDateFormat dateformatddMMyyyy = new SimpleDateFormat("ddMMyyyy");
        String date_to_string = dateformatddMMyyyy.format(dateNow);
        return date_to_string;
    }

    public String MMyyhhmmss() {
        //Creating Date in java with today's date.
        java.util.Date dateNow = new java.util.Date();

        //converting  date into ddMMyyyy format example "14092011"
        SimpleDateFormat dateformatddMMyyyy = new SimpleDateFormat("MMddhhmmss");
        String date_to_string = dateformatddMMyyyy.format(dateNow);
        return date_to_string;
    }

    public String DateTime() {
        java.util.Date today = new java.util.Date();

        //Date format
        DateFormat df = DateFormat.getDateTimeInstance();                              //Date and time
        String strDate = df.format(today);
        return strDate;
    }

    public String hhmmss() {
        //Creating Date in java with today's date.
        java.util.Date dateNow = new java.util.Date();

        SimpleDateFormat dateformatddMMyyyy = new SimpleDateFormat("hhmmss");
        String date_to_string = dateformatddMMyyyy.format(dateNow);
        return date_to_string;
    }

    public String yymmdd(java.util.Date date) {
        SimpleDateFormat dateformatddMMyyyy = new SimpleDateFormat("yyMMdd");
        String date_to_string = dateformatddMMyyyy.format(date);
        return date_to_string;
    }

    public String strResponseFooter(String strAgentID) throws SQLException {
        String strResponse = "";

        try {

            if (strAgentID != null && !strAgentID.isEmpty()) {
                String strLogin = "SELECT AGENTNAMES FROM TBAGENTS WHERE AGENTID ='" + strAgentID + "'";
                String AgentName = connections.ExecuteQueryStringValue(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB, "", "AGENTNAMES");
                strResponse += "Served by:" + stringBuilder(AgentName) + "#";
            }

            strResponse += "--------------------------------" + "#";
            strResponse += " THANK YOU FOR USING NMB WAKALA " + "#";
            strResponse += "   AGENCY HELPDESK: 0800112233" + "#";
            strResponse += "              +255 22 216 1888" + "#";
            strResponse += " " + "#";
            strResponse += " " + "#";
            strResponse += " " + "#";
            strResponse += " " + "#";
            strResponse += " " + "#";
            strResponse += " " + "#";

        } catch (Exception ex) {
            this.log("INFO strResponseFooter() ::" + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }
        return strResponse;
    }
//Card payment gateway Response Footer
    public String strPOSResponseFooter(String strDeviceId) throws SQLException {
        String strResponse = "";

        try {

//            if (strAgentID != null && !strAgentID.isEmpty()) {
//                String strLogin = "SELECT AGENTNAMES FROM TBAGENTS WHERE AGENTID ='" + strAgentID + "'";
//                String AgentName = connections.ExecuteQueryStringValue(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB, "", "AGENTNAMES");
//                strResponse += "Served by:" + stringBuilder(AgentName) + "#";
            //}

            strResponse += "--------------------------------" + "#";
            strResponse += "     THANK YOU FOR USING      " + "#";
            strResponse += "     CARD PAYMENT GATEWAY     " + "#";
            strResponse += "     HELPDESK: 0800112233" + "#";
            strResponse += "              +254 22 216 1888" + "#";
            strResponse += " " + "#";
            strResponse += " " + "#";
            strResponse += " " + "#";
            strResponse += " " + "#";
            strResponse += " " + "#";
            strResponse += " " + "#";

        } catch (Exception ex) {
            this.log("INFO strPOSResponseFooter() ::" + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }
        return strResponse;
    }
    
    //encrypt PIN function
    public String encryptPin(String username, String plainPassword, String strKey) {
        String plainString = username + plainPassword;
        byte[] byteArray = Base64.encodeBase64(plainString.getBytes());
        String encodedString = new String(byteArray);
        String HMAC_SHA512 = "HmacSHA512";
        String DEFAULT_ENCODING = "UTF-8";
        byte[] result = null;

        //Hash Algorithm
        try {
            SecretKeySpec keySpec = new SecretKeySpec(strKey.getBytes(DEFAULT_ENCODING), HMAC_SHA512);
            Mac mac = Mac.getInstance(HMAC_SHA512);
            mac.init(keySpec);
            result = mac.doFinal(encodedString.getBytes(DEFAULT_ENCODING));

        } catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException ex) {
            this.log("INFO strResponseFooter() ::" + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }

    public void updateTrials(String username, String deviceID) {
        String Query = "";
        String strTrials = "";
        String strResponse = "";
        String strField39 = "";
        String strField48 = "";
        boolean Success = true;
        try {
            Query = "SELECT A.TRIALS FROM TBAGENTDEVICES A INNER JOIN TBAGENTS B ON A.agentid = b.AGENTID where b.AGENTID = '" + username + "'";
            Query += " AND  A.deviceimei ='" + deviceID + "'";

            strTrials = connections.ExecuteQueryStringValue(ECSERVER, Query, ECPASSWORD, ECUSER, ECDB, "", "TRIALS");
            int trial = Integer.parseInt(strTrials) + 1;

            if (trial >= 3) {
                String strInMsg = "UPDATE TBAGENTDEVICES SET TRIALS='" + trial + "' , ACTIVE ='0'  WHERE DEVICEIMEI ='" + deviceID + "'";
                Success = connections.ExecuteUpdate(ECSERVER, strInMsg, ECPASSWORD, ECUSER, ECDB);
            } else {
                String strInMsg = "UPDATE TBAGENTDEVICES SET TRIALS='" + trial + "' , LOGGEDIN='0' WHERE DEVICEIMEI ='" + deviceID + "'";
                Success = connections.ExecuteUpdate(ECSERVER, strInMsg, ECPASSWORD, ECUSER, ECDB);
            }

        } catch (Exception ex) {
            this.log("INFO updateTrials()  :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }

    }

    public void clearTrials(String deviceID) {
        String strInMsg = "UPDATE TBAGENTDEVICES SET TRIALS ='0', ACTIVE ='1'  WHERE DEVICEIMEI ='" + deviceID + "'";
        connections.ExecuteUpdate(ECSERVER, strInMsg, ECPASSWORD, ECUSER, ECDB);
    }

    public String fn_verify_login_details(String strUsername, String strPassword, String serialNumber) {
        String strStatus = "";
        String strUserType = "";
        String MyPassword = "";
        String strLogin = "";

        try {
            MyPassword = this.encryptPin(strUsername, strPassword, "!Eclectic%IsThe BomB%Limited!??hehehe");

            strLogin = "SELECT A.ACTIVE || '|' ||B.AGENTTYPE || '|' || A.TRIALS || '|' || A.LOGGEDIN || '|' || A.FIRSTLOGIN  || '|' || B.ACTIVE  AS PROFILE ";
            strLogin += " FROM TBAGENTDEVICES A INNER JOIN TBAGENTS B ON A.agentid = b.AGENTID WHERE B.AGENTID ='" + strUsername + "' ";
            strLogin += " AND A.PIN ='" + MyPassword.toLowerCase() + "'  AND A.DEVICEIMEI ='" + serialNumber + "'";

            strStatus = connections.ExecuteQueryStringValue(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB, "", "PROFILE");

            if (strStatus == "" || strStatus == null || strStatus.isEmpty()) {
                strStatus = "0|0|0|0|0|0";
                updateTrials(strUsername, serialNumber);
            }

            System.out.println(strStatus + "\n\n" + strLogin);
        } catch (Exception ex) {
            this.log("INFO fn_verify_login_details()  :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }

        return strStatus;
    }

    public String PadZeros(int width, String value) {

        String z = "";
        for (int i = 0; i < width - value.length(); i++) {
            z += "0";
        }
        return z + value;
    }

    public String PadSpaces(int width, String value, String bool) {

        String z = "";
        String result = "";
        for (int i = 0; i < width - value.length(); i++) {
            z += " ";
        }
        if (bool.equalsIgnoreCase("LEFT")) {
            result = z + value;
        } else if (bool.equalsIgnoreCase("RIGHT")) {
            result = value + z;
        }
        return result;
    }

    public boolean fn_terminal_allowed(String strTerminalSerialNumber) {
        try {

            String str_query = "select ASSIGNED from tbagentdevices where deviceIMEI ='" + strTerminalSerialNumber.trim() + "'";
            String blnActive = connections.ExecuteQueryStringValue(ECSERVER, str_query, ECPASSWORD, ECUSER, ECDB, "", "ASSIGNED");

            switch (blnActive) {
                case "True":
                case "1":
                    return true;
                default:
                    return false;
            }
        } catch (Exception ex) {
            this.log("Error on Function fn_terminal_allowed" + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "fn_terminal_allowed");
            return false;
        }
    }

    public String fn_getAgentLocation(String strAgentID) {
        String strLocation = "";
        String strLogin = "";
        try {
            strLogin = "SELECT ADDRESS FROM TBAGENTREGISTRATION WHERE AGENTNO ='" + strAgentID + "'";
            strLocation = connections.ExecuteQueryStringValue(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB, "", "ADDRESS");
            return strLocation;
        } catch (Exception ex) {
            this.log("\nINFO fn_getAgentLocation() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }

    }

    public String getSetting(String strParam) {
        String schoolid = "";
        String SQL = "";

        try {
            SQL = "select ITEMVALUE FROM tbGENERALPARAMS WHERE ITEMNAME='" + strParam + "'";
            schoolid = connections.ExecuteQueryStringValue(ECSERVER, SQL, ECPASSWORD, ECUSER, ECDB, "", "ITEMVALUE");
            return schoolid;
        } catch (Exception ex) {
            this.log("\nINFO getSetting() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }
    }

    public String fn_RemoveNon_Numeric(String strExpression) {

        String strtocheck = strExpression.trim();

        char[] myChars = strtocheck.toCharArray();
        String strNumeic = "";

        try {
            // Loop through the array testing if each is a digit

            for (char ch : myChars) {
                if (Character.isDigit(ch)) {
                    strNumeic = strNumeic + ch;
                }
            }
            return strNumeic;
        } catch (Exception ex) {
            this.log("\nINFO fn_RemoveNon_Numeric() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }
    }

    public String SendPOSResponse(String outmessage, String DeviceId) {

        //System.out.println("[SENT: Message Sent To POS                -:]" + intid + "\n" + outmessage);
        try {
            this.log("Message To POS :: \n " + outmessage, "MessageToPOS ");
            if (ISO8583Adaptor.isConnected) {
                MySocketHandler.chEvPOS.getChannel().write(outmessage);
            } else {
                this.log("Error on function SendPOSResponse could not connect to Host ", "MessageToPOS");
            }
        } catch (Exception ex) {
            this.log("\nINFO anyDate() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }
        return null;
    }

    public String fn_getAgentAccountNumber(String strAgentID) {
        String strLogin = "";

        try {
            strLogin = "SELECT FLOATACCOUNT FROM TBAGENTS WHERE AGENTID ='" + strAgentID + "'";
            String rsReader = connections.ExecuteQueryStringValue(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB, "", "FLOATACCOUNT");
            return rsReader;
        } catch (Exception ex) {
            this.log("\nINFO fn_getAgentAccountNumber() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }
    }

    public HashMap<String, String> getTrxnDetails(String strReferenceNumber) {
        String strQuery = "";
        HashMap<String, String> parseFields = new HashMap<String, String>();

        try {
            strQuery = "SELECT FIELD_2,FIELD_3,FIELD_4,FIELD_37,FIELD_65,FIELD_66,FIELD_68,FIELD_88,FIELD_100,FIELD_102,FIELD_103,FIELD_104 FROM TBINCOMINGTRANSACTIONS WHERE FIELD_37 ='" + strReferenceNumber + "'";
            ResultSet rsrecordQuery = connections.ExecuteQueryReturnString(ECSERVER, strQuery, ECPASSWORD, ECUSER, ECDB);

            while (rsrecordQuery.next()) {

                parseFields.put("2", rsrecordQuery.getString("FIELD_2"));
                parseFields.put("3", rsrecordQuery.getString("FIELD_3"));
                parseFields.put("4", rsrecordQuery.getString("FIELD_4"));
                parseFields.put("37", rsrecordQuery.getString("FIELD_37"));
                parseFields.put("65", rsrecordQuery.getString("FIELD_65"));
                parseFields.put("66", rsrecordQuery.getString("FIELD_66"));
                parseFields.put("68", rsrecordQuery.getString("FIELD_68"));
                parseFields.put("88", rsrecordQuery.getString("FIELD_88"));
                parseFields.put("100", rsrecordQuery.getString("FIELD_100"));
                parseFields.put("102", rsrecordQuery.getString("FIELD_102"));
                parseFields.put("103", rsrecordQuery.getString("FIELD_103"));
                parseFields.put("104", rsrecordQuery.getString("FIELD_104"));
            }

        } catch (Exception ex) {
            this.log("\nINFO getTrxnDetails() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }
        return parseFields;
    }

    public String getMerchantAccount(String strAgentID) {
        String strLogin = "";

        try {
            strLogin = "SELECT ACCOUNTNUMBER FROM TBMERCHANTS WHERE AGENTID ='" + strAgentID + "'";
            String rsReader = connections.ExecuteQueryStringValue(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB, "", "ACCOUNTNUMBER");;
            return rsReader;
        } catch (Exception ex) {
            this.log("\nINFO getMerchantAccount() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }
    }

    public int createStan() {
        int x = 0;

        String filename = COUNT_FILE;
        File inwrite = new File(filename);

        // Get a file channel for the file
        try {
            FileChannel channel = new RandomAccessFile(inwrite, "rw").getChannel();
            // Use the file channel to create a lock on the file.
            // This method blocks until it can retrieve the lock.
            FileLock lock = channel.lock();
            //  if(!inwrite.exists()) {
            String s = "";
            try {
                int fileSize = (int) channel.size();
                //    System.out.println("int is" + fileSize);
                ByteBuffer bafa = ByteBuffer.allocate(fileSize);
                int numRead = 0;
                while (numRead >= 0) {
                    numRead = channel.read(bafa);
                    bafa.rewind();
                    for (int i = 0; i < numRead; i++) {
                        int b = (int) bafa.get();
                        char c = (char) b;
                        s = s + c;
                    }
                }

                x = Integer.parseInt(s);
                if (x > 999999) {
                    x = 100000;
                } else if (x < 100000) {
                    x = 100000;
                }
                x = ++x;
                String xx = String.valueOf(x);
                byte[] yy = xx.getBytes();
                channel.truncate(0);
                channel.write(ByteBuffer.wrap(yy));
                // channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            lock.release();
            // Close the file
            channel.close();
        } catch (FileNotFoundException e) {
            String message = "The file " + inwrite.getName() + " does not exist. So no input can be written on it";
            System.out.println(message);
            e.printStackTrace();
            //log to error file
        } catch (IOException e) {
            System.out.println("Problem writing to the logfile " + inwrite.getName());

        }

        filename = "";
        return x;
    } // end fn createstan

    public String generateCorelationID() {
        int min = 100000000;
        int max = 999999999;
        Random randomGenerator = new Random();
        String CorelationID = "" + (randomGenerator.nextInt(max - min + 1) + min);

        int rand = 100000 + (int) (Math.random() * ((999999999 - 100000000) + 1));
        String intid = "POS" + rand;

        return intid;
    }

    public String generateID() {
        int min = 100000;
        int max = 999999;
        Random randomGenerator = new Random();
        String CorelationID = "" + (randomGenerator.nextInt(max - min + 1) + min);//+""+(randomGenerator.nextInt(max - min + 1) + min);

        int rand = 100000 + (int) (Math.random() * ((999999 - 100000) + 1));
        String intid = CorelationID;
        return intid;
    }

    public String padEqual(String str) {
        int length;
        int eitherLength;
        int spaces;
        String response = "";
        length = str.length();

        if (length <= 32) {
            spaces = 32 - length;
            if ((spaces % 2) == 0) {
                eitherLength = (spaces / 2) + length;
                response = PadSpaces(eitherLength, str.toUpperCase(), "LEFT");
            } else {
                eitherLength = (spaces - 1) / 2;

                eitherLength = ((spaces - 1) / 2) + length;
                response = PadSpaces(eitherLength, str.toUpperCase(), "LEFT");
            }

        } else {

        }
        return response;
    }

    public String strResponseHeader(String strTerminalid) throws SQLException {

        try {
            String strResponse = "";
            String strLogin = "";
            String strAgentName = "";
            String strAgentLocation = "";
            String strAgentNumber = "";
            String TerminalID = "";
            String str = "";
            String deviceInfo[];

            strLogin = "SELECT A.AGENTNAMES || '|' || A.LOCATION || '|' || A.AGENTID || '|' || B.TERMINALID|| '|' || B.DEVICEIMEI AS DEVICEINFO  FROM TBAGENTS A INNER JOIN TBAGENTDEVICES B ON A.AGENTID = B.AGENTID ";
            strLogin += " WHERE B.DEVICEIMEI='" + strTerminalid + "'";

            str = connections.ExecuteQueryStringValue(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB, "", "DEVICEINFO");

            if (!str.isEmpty()) {
                deviceInfo = str.split("\\|");
                strAgentName = deviceInfo[0];
                strAgentLocation = deviceInfo[1];
                strAgentNumber = deviceInfo[2];
                TerminalID = deviceInfo[3];
            }
            String strTime = this.anyDate("HH:mm");
            String strDate = this.anyDate("dd/MM/yyyy");
            strResponse = "              NMB               " + "#";
            strResponse += padEqual(strAgentLocation) + "#";
            strResponse += " " + "#";
            strResponse += " DATE       TIME    TERMINAL ID  " + "#";
            strResponse += strDate + " " + strTime + "     " + TerminalID + "#";
            strResponse += "                               " + "#";
            return strResponse;
        } catch (Exception ex) {
            this.log("INFO strResponse() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        } finally {
            connections.con.close();
        }
    }

    public String strResponseHeader2(String strAgentID) {

        try {
            String strResponse = "";
            String strLogin = "";
            String strAgentName = "";
            String strAgentLocation = "";
            String strAgentNumber = "";
            String TerminalID = "";

            strLogin = "SELECT A.AGENTNAMES,A.LOCATION,A.AGENTID,B.TERMINALID,B.DEVICEIMEI FROM TBAGENTS A INNER JOIN TBAGENTDEVICES B ON A.AGENTID = B.AGENTID ";
            strLogin += " WHERE A.AGENTID='" + strAgentID + "'";
            ResultSet rslt = connections.ExecuteQueryReturnString(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB);

            while (rslt.next()) {
                strAgentName = rslt.getString("AGENTNAMES").toString();
                strAgentLocation = rslt.getString("LOCATION").toString();
                strAgentNumber = rslt.getString("AGENTID").toString();
                TerminalID = rslt.getString("TERMINALID").toString();
            }

            String strTime = this.anyDate("HH:mm");
            String strDate = this.anyDate("dd/MM/yyyy");
            strResponse = "              NMB               " + "#";
            strResponse += "   " + strAgentLocation.toUpperCase() + "  BR#";
            strResponse += " " + "#";
            strResponse += " DATE       TIME    POS SERIAL  " + "#";
            strResponse += strDate + " " + strTime + " " + TerminalID + "#";
            strResponse += "                               " + "#";
            return strResponse;
        } catch (Exception ex) {
            this.log("INFO strResponseHeader2() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }
    }
    
    //Response Header for POS Card Payment gateway
    public String strPOSResponseHeader2(String refNo) {

        try {
            String strResponse = "";
//            String strLogin = "";
//            String strAgentName = "";
//            String strAgentLocation = "";
//            String strAgentNumber = "";
//            String TerminalID = "";
//
//            strLogin = "SELECT A.AGENTNAMES,A.LOCATION,A.AGENTID,B.TERMINALID,B.DEVICEIMEI FROM TBAGENTS A INNER JOIN TBAGENTDEVICES B ON A.AGENTID = B.AGENTID ";
//            strLogin += " WHERE A.AGENTID='" + strAgentID + "'";
//            ResultSet rslt = connections.ExecuteQueryReturnString(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB);
//
//            while (rslt.next()) {
//                strAgentName = rslt.getString("AGENTNAMES").toString();
//                strAgentLocation = rslt.getString("LOCATION").toString();
//                strAgentNumber = rslt.getString("AGENTID").toString();
//                TerminalID = rslt.getString("TERMINALID").toString();
//            }

            String strTime = this.anyDate("HH:mm");
            String strDate = this.anyDate("dd/MM/yyyy");
            strResponse = "      PAYMENT GATEWAY      " + "#";
            //strResponse += "   " + strAgentLocation.toUpperCase() + "  BR#";
            strResponse += " " + "#";
            strResponse += " DATE       TIME    POS SERIAL  " + "#";
            strResponse += strDate + " " + strTime + " " + refNo + "#";
            strResponse += "                               " + "#";
            return strResponse;
        } catch (Exception ex) {
            this.log("INFO strPOSResponseHeader2() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }
    }

    public void spInsertPOSTransaction(Map<String, String> Fields) {

        try {

            String Field_0 = Fields.get("0") == null ? "" : Fields.get("0").toString();
            String Field_2 = Fields.get("2") == null ? "" : Fields.get("2").toString();
            String Field_3 = Fields.get("3") == null ? "" : Fields.get("3").toString();
            String Field_4 = Fields.get("4") == null ? "" : Fields.get("4").toString();
            String Field_7 = Fields.get("7") == null ? "" : Fields.get("7").toString();
            String Field_11 = Fields.get("11") == null ? "" : Fields.get("11").toString();
            String Field_32 = Fields.get("32") == null ? "" : Fields.get("32").toString();
            String Field_37 = Fields.get("37") == null ? "" : Fields.get("37").toString();
            String Field_39 = Fields.get("39") == null ? "" : Fields.get("39").toString();
            String Field_48 = Fields.get("48") == null ? "" : Fields.get("48").toString();
            String Field_54 = Fields.get("54") == null ? "" : Fields.get("54").toString();
            String Field_65 = Fields.get("65") == null ? "" : Fields.get("65").toString();
            String Field_88 = Fields.get("88") == null ? "" : Fields.get("88").toString();
            String Field_100 = Fields.get("100") == null ? "" : Fields.get("100").toString();
            String Field_102 = Fields.get("102") == null ? "" : Fields.get("102").toString();
            String Field_103 = Fields.get("103") == null ? "" : Fields.get("103").toString();
            String Field_104 = Fields.get("104") == null ? "" : Fields.get("104").toString();
            String strDate = this.anyDate("yyyy-mm-dd");//to_char(some_date, 'yyyy-mm-dd hh24:mi:ss') 
            String strTime = this.anyDate("HH:mm:ss");
            String strchannel = "POS";
            String strRequesttoEconnect = Fields.get("strRequesttoEconnect") == null ? "" : Fields.get("strRequesttoEconnect").toString();
            String strResponseFromEconnect = Fields.get("strResponseFromEconnect") == null ? "" : Fields.get("strResponseFromEconnect").toString();
            String POSReceipt = Fields.get("POSReceipt") == null ? "" : Fields.get("POSReceipt").toString();

            String sql = " INSERT INTO TBINCOMINGTRANSACTIONS (FIELD_0, FIELD_2, FIELD_3, FIELD_4,FIELD_7,FIELD_11,FIELD_32,FIELD_37,FIELD_39,"
                    + "  FIELD_54,FIELD_65,FIELD_88,FIELD_100,FIELD_102,FIELD_103,FIELD_104,REQUEST_TO_ECONNECT,"
                    + "  RESPONSE_FROM_ECONNECT,POS_RECEIPT,CHANNEL)"
                    + "  VALUES ('" + Field_0 + "','" + Field_2 + "','" + Field_3 + "','" + Field_4 + "','" + Field_7 + "','" + Field_11 + "','" + Field_32 + "','" + Field_37 + "','" + Field_39 + "','" + Field_54 + "','" + Field_65 + "',"
                    + "  '" + Field_88 + "','" + Field_100 + "','" + Field_102 + "','" + Field_103 + "','" + Field_104 + "','" + strRequesttoEconnect.replace("'", "\"") + "','" + strResponseFromEconnect.replace("'", "\"") + "','" + POSReceipt.replace("'", "\"") + "','" + strchannel + "')";

            connections.ExecuteUpdate(ECSERVER, sql, ECPASSWORD, ECUSER, ECDB);

        } catch (Exception ex) {
            this.log("INFO spInsertPOSTransaction() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }

    }

    public String fn_getAgentPhonenumber(String strAgentID) {

        String strMobile = "";
        String strLogin = "";
        try {
            strLogin = "Select DEVICEMOBILENUMBER from tbagentdevices  where AGENTID =" + strAgentID + "'";
            strMobile = connections.ExecuteQueryStringValue(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB, "", "DEVICEMOBILENUMBER");
            return strMobile;
        } catch (Exception ex) {
            this.log("\nINFO fn_getAgentPhonenumber() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }
    }

    public String fn_getAgentPhonenumber2(String TerminalID) {

        String strMobile = "";
        String strLogin = "";
        try {
            strLogin = "Select DEVICEMOBILENUMBER from tbagentdevices  where DEVICEIMEI = '" + TerminalID + "'";
            strMobile = connections.ExecuteQueryStringValue(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB, "", "DEVICEMOBILENUMBER");
            return strMobile;
        } catch (Exception ex) {
            this.log("\nINFO fn_getAgentPhonenumber() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }
    }

    public Boolean updateTransactionResponse(Map<String, String> map, String CORR_ID, String POS_Receipt) {

        boolean Success = false;
        String field120 = "";
        try {
            if (map.containsKey("120")) {
                field120 = map.get("120").toString();
            }

            String strInMsg = "update tbincomingtransactions set FIELD_120 = '" + field120 + "',POS_receipt='" + POS_Receipt + "' where FIELD_37='" + map.get("37").toString() + "'";
            Success = connections.ExecuteUpdate(ECSERVER, strInMsg, ECPASSWORD, ECUSER, ECDB);
        } catch (Exception ex) {
            this.log("\nINFO updateTransactionResponse() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");

        }
        return Success;
    }

    public Boolean fn_Updateagentpassword(String StrAgentID, String StrPassword, String strDeviceid) {

        boolean Success = false;
        try {
            String SQL, MyPassword = "";
            MyPassword = this.encryptPin(StrAgentID, StrPassword, "!Eclectic%IsThe BomB%Limited!??hehehe");
            String strInMsg = "update TBAGENTDEVICES set PIN='" + MyPassword.toLowerCase() + "',firstlogin = 0 where DEVICEIMEI = '" + strDeviceid + "'";
            Success = connections.ExecuteUpdate(ECSERVER, strInMsg, ECPASSWORD, ECUSER, ECDB);

            if (Success == true) {
                String stragentphonenumber = this.fn_getAgentPhonenumber2(strDeviceid);
                String srAgentname = this.fn_getAgentName(StrAgentID);
                String strmessage = "Dear " + srAgentname + ",  agent ID." + StrAgentID + " your new POS login password is " + StrPassword + ".Your login Id remains " + StrAgentID;
                this.sendSMS(strmessage, stragentphonenumber);
            }
            return Success;
        } catch (Exception ex) {
            this.log("\nINFO fn_Updateagentpassword() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return false;
        }
    }

    //Reversal
    public String generateReversal(HashMap<String, String> ISOdetails) throws IOException, InterruptedException {
        String response = "";
        String strResponse = "";
        try {
            // int attempts = Integer.parseInt(ISOdetails.get("trials"));
            String CORR_ID = this.generateCorelationID();
            ISOdetails.remove("CorrelationID");

            ISOdetails.put("0", "0420");
            ISOdetails.put("7", this.anyDate("MMddHHmmss"));
            ISOdetails.put("11", this.anyDate("MMddHHmmss"));
            ISOdetails.put("32", SOURCE_ID);
            ISOdetails.put("CorrelationID", CORR_ID);

            this.log("Reversal" + ISOdetails.get("CorrelationID"), "REVERSE");

            boolean sentToWebLogicAgain = false;
            HashMap ParamsFromAdapterAgain;
            ParamsFromAdapterAgain = new HashMap();

            QueueWriter queueWriter = new QueueWriter(QUEUE_REQUEST, PROVIDER_URL);

            int trials = 0;
            do {
                sentToWebLogicAgain = queueWriter.sendObject((HashMap) ISOdetails, CORR_ID);
                trials++;
            } while (sentToWebLogicAgain == false & trials < 3);

            if (sentToWebLogicAgain) {
                System.out.println("[SENT: Reversal Transaction Sent to ESB   -:]" + ISOdetails.get("CorrelationID"));
                long Start = System.currentTimeMillis();
                long Stop = Start + (20 * 1000);
                do {
                    Thread.currentThread().sleep(100);
                    ParamsFromAdapterAgain = this.getWeblogicMessageFromQueue(ISOdetails.get("CorrelationID"));
                } while (ParamsFromAdapterAgain.isEmpty() && System.currentTimeMillis() < Stop);

                if (ParamsFromAdapterAgain.isEmpty()) {

                    System.out.println("[SENT: Transaction Responses Failed from ESB-:] " + ISOdetails.get("37"));

                    //Send Failed Response to POS
                    String TransactionType = getTransactionType(ISOdetails.get("3").toString());

                    strResponse += this.strResponseHeader(ISOdetails.get("68").toString()) + "#";
                    strResponse += "AGENT ID:  " + ISOdetails.get("104").toString() + "#";
                    strResponse += "TRAN NUM:  " + ISOdetails.get("37").toString() + "#";
                    strResponse += "--------------------------------" + "#";
                    strResponse += "                                " + "#";
                    strResponse += padEqual(TransactionType.toUpperCase()) + "#";
                    strResponse += "                                " + "#";
                    strResponse += "   NO RESPONSE FROM ESB GATEWAY " + "#";
                    strResponse += " " + "#";
                    strResponse += this.strResponseFooter(ISOdetails.get("104").toString()) + "#";
                    SendPOSResponse(strResponse, ISOdetails.get("37").toString());

                } else {
                    response = this.genHashDelimiterString(ParamsFromAdapterAgain, ISOdetails.get("CorrelationID").toString());
                }
            }
        } catch (Exception ex) {
            this.log("\nINFO fn_Updateagentpassword() :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }
        return response;
    }

    public static String GetSequenceNumber() {
        ClassImportantValues cl = new ClassImportantValues();

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
            System.out.println("Error GetSequenceNumber : " + ex.getMessage() + StackTraceWriter(ex));
            return null;
        }

    } // end fn GetSequenceNumber

    public String PIN_Verify(String strReferenceNo, String strNarration, String strProcCode, String strAmount, String strProcessingCode,
            String strClearPIN, String strPAN, String strExpiryDate, String strTerminalSerial, String strField35, String strField37, String strPhoneNumber,
            String strDebitAccount, String strCreditAccount, String strField68) {

        try {
            PinBlock pin = new PinBlock();
            String response;
            String STAN = "" + GetSequenceNumber();
            String strPINBlock = pin.pinProcess(strClearPIN, trimLeftString(strPAN), PIN_VERIFICATION_ENCRYPT_KEYS);
            // String strPINBlock = generatePINBlock.DESEncrypt(strClearPIN, trimLeftString(strPAN), PIN_VERIFICATION_ENCRYPT_KEYS);
            String Track2Data = trimLeftString(strField35);
            String cardSequenceNumber = null;
            if (Track2Data != null && Track2Data.length() >= 3) {
                cardSequenceNumber = Track2Data.substring(Track2Data.length() - 3);
            }

            HashMap<String, String> data = new HashMap<String, String>();
            data.put("MTI", "0200"); // Message Type
            data.put("2", trimLeftString(strPAN)); // Primary Account Number (PAN)
            data.put("3", "320000");//Processing code
            data.put("4", "000000000000");//Amount
            data.put("7", (new SimpleDateFormat("MMddHHmmss")).format(new java.util.Date())); // Valuedate 
            data.put("11", STAN); // Transaction Unique Reference (STAN)
            data.put("12", (new SimpleDateFormat("hhmmss")).format(new java.util.Date())); // Time, local transaction
            data.put("13", (new SimpleDateFormat("MMdd")).format(new java.util.Date())); //Local Transaction Date
            data.put("14", strExpiryDate);// Card Expiry date
            data.put("15", (new SimpleDateFormat("MMdd")).format(new java.util.Date()));//Settlement date
            data.put("22", "902");//POS Entry Mode
            data.put("23", cardSequenceNumber);//Card Sequence Number
            data.put("25", "00");//POS Condition Mode
            data.put("26", "12");//POS Entry Mode
            data.put("28", "C00000000");//Amount, Transaction Fee
            data.put("30", "C00000000");//Amount, Transaction Processing Fee
            data.put("32", "639673");//Acquiring institution code(BIN)
            data.put("35", trimLeftString(strField35));//TRACK2 DATA
            data.put("37", strField37);//Retrieval Reference Number
            data.put("41", "22310001");//Card Acceptor terminal ID
            data.put("42", "TPDF           ");// Card Acceptor ID code
            data.put("43", "NMB House  Test        Dar Es Salaam  TZ");// Card Acceptor terminal ID
            data.put("49", "834");// Currency Code
            data.put("52", strPINBlock);// PIN BLOCK
            data.put("56", "1510");// Message reason code
            data.put("59", PadZeros(10, STAN));// Echo Data
            data.put("123", "21010121014C101");// POS Data Code

            String requestISO = iso.CreateISO(data);
            requestISO = requestISO.replace(strPINBlock, new String(HexString2Bytes(strPINBlock), "ISO8859_1"));
            String header = (new DataConversions()).IntegertoASCII(requestISO.length());
            requestISO = header + requestISO;

            if (ISO8583Adaptor.isConnectedClient) {
                ClientSocketHandler.chEv.getChannel().write(requestISO);
                //  Mask the PAN [replace PAN,Pin Block data during logging]
                String strnewpan = strPAN;
                strnewpan = strnewpan.replace(strPAN.substring(6, 14), "xxxxxxxxxxxxxx");
                String strField_35 = strField35.replace(strField35.substring(6, 32), "xxxxxxxxxxxxxxxxxxxxxxxxx");
                data.remove("2");
                data.remove("52");
                data.remove("35");
                data.put("2", strnewpan);
                data.put("35", strField_35);
                data.put("52", "*********************************");

                // Save map details on the OuterHashMap Collection
                data.put("88", strNarration);
                data.remove("3");
                data.put("3", strProcessingCode);
                data.put("4", strAmount);
                data.put("88", strNarration);
                data.put("65", strReferenceNo);
                data.put("68", strField68);
                data.put("100", strProcCode);
                data.put("102", strDebitAccount);
                data.put("103", strCreditAccount);
                data.put("104", strTerminalSerial);
                data.put("PhoneNumber", strPhoneNumber);
                data.put("timestamp", this.anyDate(""));

                OuterHoldingQueue.put(strField37, data);

            } else {
                String strMessageToPOS = "";
                System.out.println("Message Failed to be Sent Postilion");
                String strnewpan = strPAN;
                strnewpan = strnewpan.replace(strPAN.substring(6, 14), "xxxxxxxxxxxxxx");
                String strField_35 = strField35.replace(strField35.substring(6, 32), "xxxxxxxxxxxxxxxxxxxxxxxxx");
                data.remove("2");
                data.remove("52");
                data.remove("35");
                data.put("2", strnewpan);
                data.put("39", "Failed Connecting to Postilion");
                data.put("52", "*********************************");
                data.put("35", strField_35);
                data.put("timestamp", this.anyDate(""));

                strMessageToPOS += this.strResponseHeader(strField68) + "#";
                strMessageToPOS += "AGENT ID:  " + strTerminalSerial.toString() + "#";
                strMessageToPOS += "TRAN NUM:  " + strField37 + "#";
                strMessageToPOS += "--------------------------------" + "#";
                strMessageToPOS += "                                " + "#";
                strMessageToPOS += "        PIN VERIFICATION        " + "#";
                strMessageToPOS += "                                " + "#";
                strMessageToPOS += " FAILED CONNECTING TO POSTILION " + "#";
                strMessageToPOS += " " + "#";
                strMessageToPOS += this.strResponseFooter(strTerminalSerial.toString()) + "#";
                SendPOSResponse(strMessageToPOS, strField37);
            }
            /*
             data.put("39", "00");

             if (strDebitAccount.isEmpty() || strDebitAccount == null) {
             strDebitAccount = "20201200003";
             }
             data.put("88", strNarration);
             data.remove("3");
             data.put("3", strProcessingCode);
             data.put("4", strAmount);
             data.put("88", strNarration);
             data.put("65", strReferenceNo);
             data.put("100", strProcCode);
             data.put("102", strDebitAccount);
             data.put("103", strCreditAccount);
             data.put("104", strTerminalSerial);
             data.put("PhoneNumber", strPhoneNumber);
             data.put("timestamp", this.anyDate(""));
             OuterHoldingQueue.put(strField37, data);
             getSwitchResponse(data);
             */
            this.log(data.toString() + "\n\n", "PinVerification");

        } catch (Exception ex) {
            this.log("Error on Function PIN_Verify() " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }
        return "";
    }

    public HashMap<String, String> getSwitchResponse(HashMap<String, String> Fields) throws InterruptedException {

        String strResponse = "";
        String strAccount = "";
        String strCardno = "";
        String strMessageToPOS = "";
        HashMap<String, String> OriginalFieldsToSwitch = new HashMap<String, String>();
        try {
            if (Fields != null || !Fields.isEmpty()) {

                String Field_2 = Fields.get("2") == null ? "" : Fields.get("2").toString();
                String Field_11 = Fields.get("11") == null ? "" : Fields.get("11").toString();;
                String Field_39 = Fields.get("39") == null ? "" : Fields.get("39").toString();
                String Field_37 = Fields.get("37") == null ? "" : Fields.get("37").toString();
                String Field_102 = Fields.get("102") == null ? "" : Fields.get("102").toString();

                switch (Field_39) {
                    case "00":
                        // 1. GET ALL THE CONTENTS FROM THE ORIGINAL HASHMAP USING THE REFERENCE_NUMBER
                        OriginalFieldsToSwitch = (HashMap<String, String>) OuterHoldingQueue.get(Field_37.trim());
                        OuterHoldingQueue.remove(Field_37);

                        //2. ADD ACCOUNT NUMBER FROM SWITCH TO ORIGINAL HASHMAP THEN SEND TO ESB
                        OriginalFieldsToSwitch.put("102", Field_102);

                        //3. SEND THE NEW HASHMAP COLLECTION TO ECONNECT
                        String strcardNumber = OriginalFieldsToSwitch.get("2") == null ? "" : OriginalFieldsToSwitch.get("2").toString();
                        String strProcessingCode = OriginalFieldsToSwitch.get("3") == null ? "" : OriginalFieldsToSwitch.get("3").toString();
                        String strAmount = OriginalFieldsToSwitch.get("4") == null ? "" : OriginalFieldsToSwitch.get("4").toString();
                        String strReferenceNumber = OriginalFieldsToSwitch.get("37") == null ? "" : OriginalFieldsToSwitch.get("37").toString();
                        String strField65 = OriginalFieldsToSwitch.get("65") == null ? "" : OriginalFieldsToSwitch.get("65").toString();
                        String strField68 = OriginalFieldsToSwitch.get("68") == null ? "" : OriginalFieldsToSwitch.get("68").toString();
                        String strNarration = OriginalFieldsToSwitch.get("88") == null ? "" : OriginalFieldsToSwitch.get("88").toString();
                        String strTransactionIdentifier = OriginalFieldsToSwitch.get("100") == null ? "" : OriginalFieldsToSwitch.get("100").toString();
                        String strAccountNumber = OriginalFieldsToSwitch.get("102") == null ? "" : OriginalFieldsToSwitch.get("102").toString();
                        String strCreditAccount = OriginalFieldsToSwitch.get("103") == null ? "" : OriginalFieldsToSwitch.get("103").toString();
                        String strAgentID = OriginalFieldsToSwitch.get("104") == null ? "" : OriginalFieldsToSwitch.get("104").toString();
                        String strPhoneNumber = OriginalFieldsToSwitch.get("PhoneNumber") == null ? "" : OriginalFieldsToSwitch.get("PhoneNumber").toString();

                        this.getESBResponse(strAgentID, strcardNumber, strProcessingCode, strAmount, strReferenceNumber, strNarration, strTransactionIdentifier, strAccountNumber, strCreditAccount, strField65, strPhoneNumber, strField68);
                        break;

                    default:
                        String ResponseDescription = getResponsePostillion(Field_39);
                        // 1. GENERATE MESSAGE TO SEND TO POS
                        OriginalFieldsToSwitch = (HashMap<String, String>) OuterHoldingQueue.get(Field_37.trim());

                        strMessageToPOS += this.strResponseHeader(OriginalFieldsToSwitch.get("68").toString()) + "#";
                        strMessageToPOS += "AGENT ID:  " + OriginalFieldsToSwitch.get("104").toString() + "#";
                        strMessageToPOS += "TRAN NUM:  " + OriginalFieldsToSwitch.get("37").toString() + "#";
                        strMessageToPOS += "--------------------------------" + "#";
                        strMessageToPOS += "                                " + "#";
                        strMessageToPOS += "        PIN VERIFICATION        " + "#";
                        strMessageToPOS += "                                " + "#";
                        strMessageToPOS += "    PIN VERIFICATION FAILED     " + "#";
                        strMessageToPOS += padEqual(ResponseDescription) + " #";
                        strMessageToPOS += " " + "#";
                        strMessageToPOS += this.strResponseFooter(OriginalFieldsToSwitch.get("104").toString()) + "#";
                        SendPOSResponse(strMessageToPOS, Field_37);

                        // 2. REMOVE THE HASHMAP FROM THE OUTERMAP COLLECTION MAP
                        OuterHoldingQueue.remove(Field_37);
                        break;

                }
            }
        } catch (Exception ex) {
            this.log("Error on getSwitchResponse() " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }
        return OriginalFieldsToSwitch;
    }

    public void log(String content, String filename) {
        logging.log(content, filename);
    }

    //
    public String fn_getAgentName(String strAgentID) {

        String AGENTNAMES = "";
        String strLogin = "";

        try {
            strLogin = "Select AGENTNAMES FROM TBAGENTS where AGENTID ='" + strAgentID + "'";
            AGENTNAMES = connections.ExecuteQueryStringValue(ECSERVER, strLogin, ECPASSWORD, ECUSER, ECDB, "", "AGENTNAMES");

        } catch (Exception ex) {
            this.log("INFO : Function fn_getAgentName()" + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }
        return AGENTNAMES;
    }

    public Boolean GetEnabledServices(String strServiceCode) {

        Boolean Strstatus = false;
        //' 1 MEANS ENABLED 0 MEANS DISABLED
        try {
            switch (strServiceCode) {
                case "LUKUPREPAID":
                    if (Integer.parseInt(LUKUPREPAID) == 1) {
                        Strstatus = true;
                    } else {
                        Strstatus = false;
                    }
                    break;
                case "AZAMTV":
                    if (Integer.parseInt(AZAMTV) == 1) {
                        Strstatus = true;
                    } else {
                        Strstatus = false;
                    }
                    break;
                case "DAWASCO":
                    if (Integer.parseInt(DAWASCO) == 1) {
                        Strstatus = true;
                    } else {
                        Strstatus = false;
                    }
                    break;
            }
            return Strstatus;
        } catch (Exception ex) {
            return false;
        }
    }

    public void sendSMS(String strMessage, String strPhonenumber) {

        strPhonenumber = "255" + strPhonenumber.substring(strPhonenumber.length() - 9);

        String strXML = "<?xml version= '1.0' encoding= 'utf-8'?>\n"
                + "<message>\n"
                + "<authHeader sourceid='SIMULATOR' password='SIMULATOR123'/>\n"
                + "<isomsg direction='request'>\n"
                + "<field id='65' value='" + strPhonenumber + "' />\n"
                + "<field id='104' value='" + strMessage + "' />\n </isomsg>\n</message>";

        try {
            SMSCall url = new SMSCall();
            String responseXml = url.connectToESBWS(strXML);
            this.log(strXML, "ESB_Request");
            //Break the Response XML
            this.log(responseXml, "ESB_Response");

        } catch (Exception e) {

        }
    }

    public String fn_getAgentEODTransactions(String strAgentID, String strTerminalID, String intID) throws SQLException {
        String strResponse = "";
        String SQL, sqlQuery = "";
        String strTotalcredit = "";
        String strdate = "";
        double amount = 0;
        String strStatementPrinting = "";
        String strTotaldebits = "";
        int i = 1;
        Connection dbConnection = connections.getDBConnection(ECSERVER, ECDB, ECUSER, ECPASSWORD);
        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "tz"));
            String strStoredProcedure = "{call SP_DAILY_TRANSACTIONS(?,?)}";
            CallableStatement callableStatement = dbConnection.prepareCall(strStoredProcedure);

            callableStatement.setString(1, strTerminalID);
            callableStatement.registerOutParameter(2, java.sql.Types.VARCHAR);
            callableStatement.executeUpdate();

            String strStatus = callableStatement.getString(2);

            if (strStatus.equals("1")) {
                String query = "select transactiontype,transactioncount,amount from tbagentsummary where serialnumber='" + strTerminalID + "'";
                ResultSet rsrecordQuery = connections.ExecuteQueryReturnString(ECSERVER, query, ECPASSWORD, ECUSER, ECDB);

                while (rsrecordQuery.next()) {
                    String strAmount = NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(rsrecordQuery.getString("AMOUNT").toString()));
                    strStatementPrinting += " ";
                    strStatementPrinting += PadSpaces(15, rsrecordQuery.getString("TRANSACTIONTYPE").toString(), "RIGHT");
                    strStatementPrinting += PadSpaces(4, rsrecordQuery.getString("TRANSACTIONCOUNT").toString(), "LEFT");
                    strStatementPrinting += PadSpaces(12, strAmount.replace("TZS", ""), "LEFT");
                    strStatementPrinting += "#";
                }

            }
            String StrMerchantFloatAccount = fn_getAgentAccountNumber(strAgentID);
            strResponse += "AGENT ID:    " + strAgentID + "#";
            strResponse += "TRAN NUM:    " + intID + "#";
            strResponse += "ACCOUNT NUM: " + StrMerchantFloatAccount + "#";
            strResponse += "--------------------------------" + "#";
            strResponse += "                                " + "#";
            strResponse += "       EOD SUMMARY REPORT       " + "#";
            strResponse += "                                " + "#";
            strResponse += "    TRANS.       COUNT    AMOUNT" + "#";
            strResponse += "                                " + "#";
            strResponse += strStatementPrinting;
            strResponse += "                                " + "#";
            strResponse += "                                " + "#";

            return strResponse;
        } catch (Exception ex) {
            this.log("INFO :: Error on fn_getAgentEODTransactions " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        } finally {
            dbConnection.close();
        }

        return null;
    }

    //Generate XML
    public String genXML(Map<String, String> ISOdetails) {
        String strXML = "<?xml version= '1.0' encoding= 'utf-8'?>\n"
                + "<message>\n"
                + "<authHeader sourceid='SIMULATOR' password='SIMULATOR123'/>\n"
                + "<isomsg direction='request'>\n"
                + "<field id='0' value='" + ISOdetails.get("0") + "' />\n"
                + "<field id='2' value='" + ISOdetails.get("2") + "' />\n"
                + "<field id='3' value='" + ISOdetails.get("3") + "' />\n"
                + "<field id='4' value='" + ISOdetails.get("4") + "' />\n"
                + "<field id='7' value='" + ISOdetails.get("7") + "' />\n"
                + "<field id='11' value='" + ISOdetails.get("11") + "' />\n"
                + "<field id='32' value='" + ISOdetails.get("32") + "' />\n"
                + "<field id='37' value='" + ISOdetails.get("37") + "' />\n"
                + "<field id='65' value='" + ISOdetails.get("65") + "' />\n"
                + "<field id='88' value='" + ISOdetails.get("88") + "' />\n"
                + "<field id='100' value='" + ISOdetails.get("100") + "' />\n"
                + "<field id='102' value='" + ISOdetails.get("102") + "' />\n"
                + "<field id='103' value='" + ISOdetails.get("103") + "' />\n"
                + "<field id='104' value='" + ISOdetails.get("104") + "' />\n"
                + "</isomsg>\n"
                + "</message>\n";

        return strXML;
    }

    public String getCustomerDetails(String strAccountNumber) throws IOException {
        String[] strCustomerNameArray;
        String strCustomerName = "";
        String fname = "";
        String mname = "";
        String lname = "";
        try {
            URL url = new URL(CUSTOMER_DETAILS_URL);
            Map<String, String> params = new LinkedHashMap<>();

            params.put("username", CUSTOMER_DETAILS_USERNAME);
            params.put("password", CUSTOMER_DETAILS_PASSWORD);
            params.put("source", CUSTOMER_DETAILS_SOURCE_ID);
            params.put("account", strAccountNumber);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            String urlParameters = postData.toString();
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(urlParameters);
            writer.flush();

            String result = "";
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = reader.readLine()) != null) {
                result += line;
            }
            writer.close();
            reader.close();

            JSONObject respobj = new JSONObject(result);
            if (respobj.has("FSTNAME") || respobj.has("MIDNAME") || respobj.has("LSTNAME")) {
                if (respobj.has("FSTNAME")) {
                    fname = respobj.get("FSTNAME").toString().toUpperCase() + ' ';
                }
                if (respobj.has("MIDNAME")) {
                    mname = respobj.get("MIDNAME").toString().toUpperCase() + ' ';
                }
                if (respobj.has("LSTNAME")) {
                    lname = respobj.get("LSTNAME").toString().toUpperCase() + ' ';
                }
                strCustomerName = fname + mname + lname;
            } else {
                strCustomerName = "N/A";
            }

        } catch (Exception ex) {
            this.log("\nINFO : Function getCustomerDetails() " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }

        // System.out.println(strCustomerName);
        return strCustomerName;
    }

    public String[] getBalance(String strAccountNo, String strAgentID, String strTerminalID) throws IOException {
        String[] strBalance = null;
        try {
            String referenceNumber = this.PadZeros(12, this.generateCorelationID());
            Map<String, String> params = new HashMap<>();

            params.put("0", "0200");
            params.put("2", "0000000000000");
            params.put("3", "310000");
            params.put("4", "0");
            params.put("7", this.anyDate("MMddHHmmss"));
            params.put("11", this.anyDate("MMddHHmmss"));
            params.put("32", SOURCE_ID);
            params.put("37", referenceNumber);
            params.put("65", "");
            params.put("66", getTerminalID(strTerminalID));
            params.put("68", strTerminalID);
            params.put("88", "BALANCE ENQUIRY FOR ACCOUNT: " + strAccountNo);
            params.put("100", "BI");
            params.put("102", strAccountNo);
            params.put("103", "");
            params.put("104", strAgentID);

            boolean sentToWebLogic = false;
            HashMap ParamsFromAdapter = new HashMap();
            QueueWriter queueWriter = new QueueWriter(QUEUE_REQUEST, PROVIDER_URL);

            int trials = 0;
            do {
                sentToWebLogic = queueWriter.sendObject((HashMap) params, referenceNumber);
                trials++;
            } while (sentToWebLogic == false & trials < 3);

            if (sentToWebLogic) {

                long Start = System.currentTimeMillis();
                long Stop = Start + (Config.flexTimeOut * 1000);
                do {
                    Thread.currentThread().sleep(100);
                    ParamsFromAdapter = this.getWeblogicMessageFromQueue(referenceNumber);
                } while (ParamsFromAdapter.isEmpty() && System.currentTimeMillis() < Stop);

                if (ParamsFromAdapter.isEmpty()) {
                    params.put("39", "999");
                    params.put("48", "No response from Flex");
                    this.log("No Response " + referenceNumber + ":" + params.toString(), "ERROR");

                } else {
                    strBalance = ParamsFromAdapter.get("54").toString().split("\\|");
                }

            }
        } catch (Exception ex) {
            this.log("INFO : Function getBalance()  " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }
        return strBalance;
    }

    public String getTerminalID(String terminalID) {
        String strResponse = "";
        String strQuery = "";
        String TerminalID = "";

        try {
            strQuery = "SELECT TERMINALID FROM TBAGENTDEVICES WHERE DEVICEIMEI='" + terminalID + "'";
            TerminalID = connections.ExecuteQueryStringValue(ECSERVER, strQuery, ECPASSWORD, ECUSER, ECDB, "", "TERMINALID");
            //System.out.println(strQuery);
        } catch (Exception ex) {
            this.log("INFO : Function getTerminalID()" + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
            return "";
        }
        return TerminalID;
    }

    //Get Response from ESB
    public HashMap getESBResponse(String strAgentID, String cardNumber, String processingCode, String amount, String referenceNumber, String narration, String transactionIdentifier, String strAccountNumber, String creditAccount, String strField65, String Phonenumber, String strField68) throws InterruptedException {
        String response = "";
        String strField_02 = "";
        String strField_00 = "";
        String strFieldPRD = "";
        String strProcessingCode = "";
        String strMessageToPOS = "";
        String STAN = "" + GetSequenceNumber();

        try {

            if (Phonenumber.toString().trim().equals("") || Phonenumber.toString().trim() == null) {
                strField_02 = cardNumber;
            } else {
                strField_02 = "255" + Phonenumber.substring(Phonenumber.length() - 9);
            }

            switch (processingCode) {
                case "120000": // MERCHANT PAYMENTS
                    strProcessingCode = "400000";
                    strField_00 = "0200";
                    strFieldPRD = "AGMP";
                    narration = "PAYMENT OF GOODS AND SERVICES FOR " + strAccountNumber;
                    break;
                case "310000":// BALANCE ENQUIRY
                    strProcessingCode = processingCode;
                    strField_00 = "0200";
                    narration = "BALANCE ENQUIRY FOR ACCOUNT " + strAccountNumber;
                    break;
                case "300000": // AGENT FLOAT (we do BI for Agent float)
                    strProcessingCode = "310000";
                    strField_00 = "0200";
                    strFieldPRD = "FLBI";
                    narration = "AGENT FLOAT FOR ACCOUNT " + strAccountNumber;
                    break;
                case "380000": //MINI STATEMENT
                    strProcessingCode = processingCode;
                    strField_00 = "0200";
                    narration = "MINI STATEMENT FOR ACCOUNT " + strAccountNumber;
                    break;
                case "340000": // CARD ACTIVATION
                    strProcessingCode = processingCode;
                    strField_00 = "0100";
                    break;
                case "010000": // CASH WITHDRAWAL
                    strProcessingCode = processingCode;
                    strFieldPRD = "CHWL";
                    strField_00 = "0200";
                    narration = "CASH WITHDRAWAL FOR ACCOUNT " + strAccountNumber;
                    break;
                case "500000": // BILL PAYMENTS
                    strProcessingCode = processingCode;
                    strFieldPRD = "";
                    strField_00 = "0200";
                    break;
                case "400000": // FUNDS TRANSFER
                    strProcessingCode = processingCode;
                    strFieldPRD = "AGFT";
                    strField_00 = "0200";
                    narration = "FUNDS TRANSFER FOR ACCOUNT " + strAccountNumber;
                    break;
                case "210000": // CASH DEPOSIT
                    strProcessingCode = processingCode;
                    strFieldPRD = "CHDP";
                    strField_00 = "0200";
                    break;
                case "420000": // TOPUPS
                    strField_00 = "0200";
                    strField65 = strField_02;
                    strProcessingCode = processingCode;
                    break;
                default:
                    strField_00 = "0200";
                    strProcessingCode = processingCode;
                    break;
            }

            Map<String, String> ISOdetails = new HashMap<>();

            ISOdetails.put("0", strField_00);
            ISOdetails.put("2", strField_02);
            ISOdetails.put("3", strProcessingCode);
            ISOdetails.put("4", amount);
            ISOdetails.put("7", this.anyDate("MMddHHmmss"));
            ISOdetails.put("11", STAN);
            ISOdetails.put("32", SOURCE_ID);
            ISOdetails.put("37", referenceNumber);
            ISOdetails.put("65", strField65);
            ISOdetails.put("66", getTerminalID(strField68));
            ISOdetails.put("68", strField68);
            ISOdetails.put("88", narration);
            ISOdetails.put("100", transactionIdentifier);
            ISOdetails.put("102", strAccountNumber);
            ISOdetails.put("103", creditAccount);
            ISOdetails.put("104", strAgentID);
            ISOdetails.put("CorrelationID", referenceNumber);
            ISOdetails.put("PRD", strFieldPRD);
            ISOdetails.put("HASH", sendTransactionHash(strAgentID, strField68).toLowerCase());
            this.log("REQUEST :: " + referenceNumber + "\n" + ISOdetails.toString() + "\n\n", "ESB_Request");

            boolean sentToWebLogic = false;
            HashMap ParamsFromAdapter = new HashMap();
            QueueWriter queueWriter = new QueueWriter(QUEUE_REQUEST, PROVIDER_URL);

            int trials = 0;
            do {
                sentToWebLogic = queueWriter.sendObject((HashMap) ISOdetails, referenceNumber);
                trials++;
            } while (sentToWebLogic == false & trials < 3);

            if (sentToWebLogic) {
                long Start = System.currentTimeMillis();
                long Stop = Start + (Integer.parseInt(ISO8583Adaptor.ESB_TIMEOUT) * 1000);
                do {
                    Thread.currentThread().sleep(100);
                    ParamsFromAdapter = this.getWeblogicMessageFromQueue(referenceNumber);
                } while (ParamsFromAdapter.isEmpty() && System.currentTimeMillis() < Stop);

                if (ParamsFromAdapter.isEmpty()) {
                    //Excempt for processing code 340000
                    if (!ISOdetails.get("3").equals("340000")) {
                        //printMsg:No reponse from ESB
                        System.out.println("===================== ");
                        System.out.println("===================== ");
                        System.out.println("ESB Timeout Response at " + (new SimpleDateFormat("MMMM dd,yyyy hh:mm:ss.SSS a zzzz")).format(new java.util.Date()));
                        System.out.println("LoggedError:CorrelationID:" + referenceNumber + "");
                        System.out.println("LoggedError:StatusCode:999");
                        System.out.println("LoggedError:Status Description:Response timeout from ESB Gateway");

                        //Send Failed Response to POS
                        String TransactionType = getTransactionType(ISOdetails.get("3").toString());

                        strMessageToPOS += this.strResponseHeader(strField68) + "#";
                        strMessageToPOS += "AGENT ID:  " + ISOdetails.get("104").toString() + "#";
                        strMessageToPOS += "TRAN NUM:  " + ISOdetails.get("37").toString() + "#";
                        strMessageToPOS += "--------------------------------" + "#";
                        strMessageToPOS += "                                " + "#";
                        strMessageToPOS += padEqual(TransactionType.toUpperCase()) + "#";
                        strMessageToPOS += "                                " + "#";
                        strMessageToPOS += "   NO RESPONSE FROM ESB GATEWAY " + "#";
                        strMessageToPOS += " " + "#";
                        strMessageToPOS += this.strResponseFooter(ISOdetails.get("104").toString()) + "#";
                        SendPOSResponse(strMessageToPOS, ISOdetails.get("37").toString());
                    }
                } else {

                    switch (processingCode) {
                        case "340000":// For Card Activation Return Array
                            return ParamsFromAdapter;
                        case "300000"://AgentFloat
                            ParamsFromAdapter.remove("3");
                            ParamsFromAdapter.put("3", "300000");
                            break;
                        case "120000":
                            ParamsFromAdapter.remove("3");
                            ParamsFromAdapter.put("3", "120000");
                            break;
                        default:
                            break;
                    }
                    printScreenMessage(ParamsFromAdapter);
                    response = this.genHashDelimiterString(ParamsFromAdapter, referenceNumber);
                }
            }
        } catch (Exception ex) {
            this.log("Error on getESBResponse " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "getESBResponse");
        }
        return null;
    }

    public void printScreenMessage(HashMap<String, String> FieldsMap) {
        try {
            String strlines = "\n==============================";
            String strResponseDescription = "";
            String responseDescription = "";
            String TransactionType = "";
            String CorrelationID = "";
            String Field48 = "";

            if (FieldsMap.containsKey("3")) {
                TransactionType = getTransactionType(FieldsMap.get("3").toString());
            }
            if (FieldsMap.containsKey("39")) {
                if (FieldsMap.containsKey("37")) {
                    CorrelationID = FieldsMap.get("37").toString();
                }
                if (FieldsMap.containsKey("48")) {
                    Field48 = FieldsMap.get("48").toString();
                }
                responseDescription = strlines + strlines;
                responseDescription += "\n" + TransactionType + " Response at " + (new SimpleDateFormat("MMMM dd,yyyy hh:mm:ss.SSS a zzzz")).format(new java.util.Date());
                responseDescription += "\nLogFromESB:CorrelationID: " + CorrelationID;
                responseDescription += "\nLogFromESB:StatusCode: " + FieldsMap.get("39").toString();
                responseDescription += "\nLogFromESB:StatusType: RESPONSE";
                responseDescription += "\nLogFromESB:StatusDescription: " + Field48;
            }
            System.out.println(responseDescription);
        } catch (Exception ex) {
            this.log("Exception", ex.getMessage() + "  " + ex.getStackTrace());
        }
    }

    public HashMap getWeblogicMessageFromQueue(String JMSCorrelationID) {

        String url = AGENCY_ADAPTER_URL;
        String QUEUE = QUEUE_RESPONSE;
        Message msg = null;
        Object ResponseMessage = null;
        HashMap fields = new HashMap();
        int loops = 1;
        try {
            while (true) {
                if (loops > 5) {
                    break;
                }

                DistributedWebLogicQueueBrowser distrWebLogic = new DistributedWebLogicQueueBrowser();
                msg = distrWebLogic.browseWebLogicQueue(JMSCorrelationID, url, QUEUE);
                if (msg instanceof ObjectMessage) {
                    ResponseMessage = ((ObjectMessage) msg).getObject();
                    fields = (HashMap) ResponseMessage;
                    this.log("RESPONSE ::  " + JMSCorrelationID + "\n" + fields.toString() + "\n\n", "ESB_Response");
                    break;
                }
                distrWebLogic = null;
                loops++;
            }
        } catch (JMSException ex) {
            this.log("Error getWeblogicMessageFromUDQueue() " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "error--");
        }
        return fields;
    }

    public String CustomerCopy() {
        String strResponse = "";
        strResponse = "********* CUSTOMER COPY ********" + "#";
        strResponse += " " + "#";
        return strResponse;
    }

    public String AgentCopy() {
        String strResponse = "";
        strResponse = "********** AGENT COPY **********" + "#";
        strResponse += "  " + "#";
        return strResponse;
    }

    public String stringBuilder(String s) {
        StringBuilder sb = new StringBuilder(s);

        int i = 0;
        while ((i = sb.indexOf(" ", i + 30)) != -1) {
            sb.replace(i, i + 1, "#");
        }
        return sb.toString();
    }

    public String InterpretResponse(String f48) {
        String MSGSTAT = "";
        String details[] = f48.split("%");
        if (details.length >= 3) {
            MSGSTAT += details[2];
        } else {
            MSGSTAT += f48;
        }
        return MSGSTAT;
    }

    public String genHashDelimiterString(Map<String, String> hashDelimiterdetails, String intid) throws IOException, SQLException {

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "tz"));
        String hashDelimiterString = "";
        String agentTeller;
        String strMessage;
        String strMessage1;
        String strReasonDescription;
        String strAvailableBalance = "";
        String strActualBalance = "";
        String cardlesssmsmsg;
        String strCustomername = "";
        String[] strBalance = null;
        String[] dataArray;
        String strAccountNo = "";
        String strStatementPrinting = "";
        String Status = "";
        String Amount = "";
        String Currency = "";
        String CustomerName = "";
        String response = "";
        String invoiceNo = "";

        if (hashDelimiterdetails.get("39").equals("00")) {
            hashDelimiterString += CustomerCopy();
        } else {
            hashDelimiterString += AgentCopy();
        }

        hashDelimiterString += this.strResponseHeader(hashDelimiterdetails.get("68"));
        hashDelimiterString += "AGENT ID:  " + hashDelimiterdetails.get("104") + "#";

        //Get Processing Code
        switch (hashDelimiterdetails.get("3")) {

            case "010000":
                //Cash Withdrawal
                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));

                switch (hashDelimiterdetails.get("39")) {
                    case "00":
                        if (hashDelimiterdetails.get("48").equals("SUCCESS")) {
                            try {
                                strAccountNo = hashDelimiterdetails.get("102").toString();
                                String amountString = formatter.format(Double.valueOf(hashDelimiterdetails.get("4"))).replace("TZS", "");

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "         CASH WITHDRAWAL        " + "#";
                                hashDelimiterString += "    CASH WITHDRAWAL SUCCESSFUL  " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "ACCOUNT NUM:  " + strAccountNo + "#";
                                hashDelimiterString += "AMOUNT:   TZS " + amountString + "#";
                                hashDelimiterString += "                                " + "#";

                            } catch (Exception ex) {
                                this.log("INFO : Function genHashDelimiterString   " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
                            }

                        } else {
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "         CASH WITHDRAWAL        " + "#";
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "     CASH WITHDRAWAL FAILED    " + "#";
                            hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                            hashDelimiterString += "                                " + "#";
                        }
                        break;
                    default:
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "         CASH WITHDRAWAL        " + "#";
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "     CASH WITHDRAWAL FAILED    " + "#";
                        hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                        hashDelimiterString += "                                " + "#";
                        break;
                }

                hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                if (hashDelimiterdetails.get("39").equals("00")) {
                    String strReplace = hashDelimiterString.replace("CUSTOMER COPY", "AGENT COPY");
                    hashDelimiterString += strReplace;
                }
                this.SendPOSResponse(hashDelimiterString, intid);

                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;

            case "120000":
                //Merchant Payments

                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));

                switch (hashDelimiterdetails.get("39")) {
                    case "00":
                        if (hashDelimiterdetails.get("48").equals("SUCCESS")) {
                            try {
                                strAccountNo = hashDelimiterdetails.get("102").toString();
                                String amountString = formatter.format(Double.valueOf(hashDelimiterdetails.get("4"))).replace("TZS", "");

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "         MERCHANT PAYMENT        " + "#";
                                hashDelimiterString += "    MERCHANT PAYMENT SUCCESSFUL  " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "ACCOUNT NUM:  " + strAccountNo + "#";
                                hashDelimiterString += "AMOUNT:   TZS " + amountString + "#";
                                hashDelimiterString += "                                " + "#";

                            } catch (Exception ex) {
                                this.log("INFO : Function genHashDelimiterString   " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
                            }

                        } else {
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "        MERCHANT PAYMENT        " + "#";
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "     MERCHANT PAYMENT FAILED    " + "#";
                            hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                            hashDelimiterString += "                                " + "#";

                        }
                        break;
                    default:
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "        MERCHANT PAYMENT       " + "#";
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "     MERCHANT PAYMENT FAILED    " + "#";
                        hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                        hashDelimiterString += "                                " + "#";

                        break;
                }

                hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                if (hashDelimiterdetails.get("39").equals("00")) {
                    String strReplace = hashDelimiterString.replace("CUSTOMER COPY", "AGENT COPY");
                    hashDelimiterString += strReplace;
                }
                this.SendPOSResponse(hashDelimiterString, intid);
                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;

            case "340000":
                // Card Activation
                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));

                switch (hashDelimiterdetails.get("39")) {
                    case "00":
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "         CARD ACTIVATION        " + "#";
                        hashDelimiterString += "   CARD ACTIVATION SUCCESSFUL   " + "#";
                        hashDelimiterString += "                                " + "#";
                        break;
                    default:
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "         CARD ACTIVATION        " + "#";
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "      CARD ACTIVATION FAILED    " + "#";
                        hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                        hashDelimiterString += "                                " + "#";

                        break;
                }
                hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                this.SendPOSResponse(hashDelimiterString, intid);
                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;
            case "320000":
                // Bill presentment
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("48"));
                switch (hashDelimiterdetails.get("39")) {
                    case "00":
                        switch (hashDelimiterdetails.get("100").toUpperCase()) {
                            case "DSTV_INQ":
                                response = getDSTVresponse(strReasonDescription);
                                break;
                            case "TRA_INQ":
                                break;
                            case "BRELA_INQ":
                                dataArray = strReasonDescription.split("|");
                                Status = dataArray[1];
                                Amount = dataArray[3];
                                Currency = dataArray[4];
                                CustomerName = dataArray[5];
                                response = "STATUS: " + Status + " AMOUNT: " + Currency + " " + Amount + " CUSTOMER NAME: " + CustomerName;
                                break;
                            case "TPA_INQ":
                                dataArray = strReasonDescription.split("~");
                                Status = dataArray[1];
                                Amount = dataArray[3];
                                Currency = dataArray[4];
                                CustomerName = dataArray[5];
                                response = "STATUS: " + Status + " AMOUNT: " + Currency + " " + Amount + " CUSTOMER NAME: " + CustomerName;
                                break;
                        }
                        break;
                    default:
                        break;
                }
                this.SendPOSResponse(hashDelimiterString, intid);
                break;

            case "210000":
                // Cash Deposit

                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));
                String amountString = formatter.format(Double.valueOf(hashDelimiterdetails.get("4"))).replace("TZS", "");

                switch (hashDelimiterdetails.get("39")) {
                    case "00":
                        if (hashDelimiterdetails.get("48").toUpperCase().equals("SUCCESS")) {

                            try {
                                String strAccountNumber = hashDelimiterdetails.get("103").toString();
                                String RecipientName = "";
                                if (hashDelimiterdetails.containsKey("89")) {
                                    RecipientName = "ACC NAME:" + hashDelimiterdetails.get("89");
                                } else {
                                    RecipientName = "ACC NAME:" + getCustomerDetails((hashDelimiterdetails.get("103")));
                                }

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "          CASH DEPOSIT          " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "     CASH DEPOSIT SUCCESSFUL    " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "AMOUNT:      TZS " + amountString + "#";
                                hashDelimiterString += "TO ACCOUNT:      " + (hashDelimiterdetails.get("103")) + "#";
                                hashDelimiterString += stringBuilder(RecipientName) + "#";

                                hashDelimiterString += "                                " + "#";

                            } catch (Exception ex) {
                                this.log("INFO : Function genHashDelimiterString   " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
                            }
                        } else {
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "          CASH DEPOSIT          " + "#";
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "        CASH DEPOSIT FAILED    " + "#";
                            hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                            hashDelimiterString += "                                " + "#";

                        }
                        break;

                    default:
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "          CASH DEPOSIT          " + "#";
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "        CASH DEPOSIT FAILED    " + "#";
                        hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                        hashDelimiterString += "                                " + "#";

                        break;

                }
                hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                if (hashDelimiterdetails.get("39").equals("00")) {
                    String strReplace = hashDelimiterString.replace("CUSTOMER COPY", "AGENT COPY");
                    hashDelimiterString += strReplace;
                }
                this.SendPOSResponse(hashDelimiterString, intid);
                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;

            case "310000":
                //Balance Inquiry
                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));
                switch (hashDelimiterdetails.get("39")) {
                    case "00":

                        try {
                            String[] strBal = hashDelimiterdetails.get("54").split("\\|");
                            String strAccountNumber = hashDelimiterdetails.get("102").toString();
                            strAvailableBalance = formatter.format(Double.valueOf(strBal[0])).replace("TZS", "");
                            strActualBalance = formatter.format(Double.valueOf(strBal[1])).replace("TZS", "");

                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "        BALANCE ENQUIRY         " + "#";
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "ACCOUNT NUM:       " + (hashDelimiterdetails.get("102")) + "#";
                            hashDelimiterString += "AVAIL BALANCE: TZS " + strAvailableBalance + "#";
                            hashDelimiterString += "ACTUAL BALANCE:TZS " + strActualBalance + "#";
                            hashDelimiterString += "                                " + "#";

                        } catch (Exception ex) {
                            this.log("INFO : Function genHashDelimiterString   " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
                        }
                        break;
                    default:
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "        BALANCE ENQUIRY         " + "#";
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "       BALANCE ENQUIRY FAILED   " + "#";
                        hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                        hashDelimiterString += "                                " + "#";

                        break;

                }
                hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                this.SendPOSResponse(hashDelimiterString, intid);
                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;

            case "300000":
                // Agent balances
                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));
                switch (hashDelimiterdetails.get("39")) {
                    case "00":
                        try {
                            String[] strBal = hashDelimiterdetails.get("54").split("\\|");
                            String strAccountNumber = hashDelimiterdetails.get("102").toString();
                            strAvailableBalance = formatter.format(Double.valueOf(strBal[0])).replace("TZS", "");
                            strActualBalance = formatter.format(Double.valueOf(strBal[1])).replace("TZS", "");

                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "     AGENT BALANCE ENQUIRY      " + "#";
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "ACCOUNT NUM:       " + hashDelimiterdetails.get("102").toString() + "#";
                            hashDelimiterString += "AVAIL BALANCE:  TZS" + strAvailableBalance + "#";
                            hashDelimiterString += "ACTUAL BALANCE: TZS" + strActualBalance + "#";
                            hashDelimiterString += "                                " + "#";

                        } catch (Exception ex) {
                            this.log("INFO : Function genHashDelimiterString   " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
                        }
                        break;
                    default:
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "     AGENT BALANCE ENQUIRY      " + "#";
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "     AGENT BALANCE FAILED      " + "#";
                        hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                        hashDelimiterString += "                                " + "#";
                        break;
                }
                hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                this.SendPOSResponse(hashDelimiterString, intid);
                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;

            case "380000":
                // Mini Statement
                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";
                try {
                    if (hashDelimiterdetails.get("48").contains("~")) {
                        String[] strStatementData = hashDelimiterdetails.get("48").split("~");

                        for (int i = 0; i < strStatementData.length; i++) {
                            strStatementPrinting += strStatementData[i] + "#";
                        }
                    }

                    switch (hashDelimiterdetails.get("39")) {
                        case "00":
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "      MINISTATEMENT ENQUIRY     " + "#";
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "ACCOUNT NUM:       " + (hashDelimiterdetails.get("102")) + "#";
                            hashDelimiterString += strStatementPrinting + "#";
                            hashDelimiterString += "                                " + "#";
                            break;

                        default:
                            strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "      MINISTATEMENT ENQUIRY     " + "#";
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "   MINISTATEMENT ENQUIRY FAILED " + "#";
                            hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                            hashDelimiterString += "                                " + "#";
                            break;
                    }
                    hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                    this.SendPOSResponse(hashDelimiterString, intid);
                    updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                    break;

                } catch (Exception ex) {
                    this.log("INFO : Function genHashDelimiterString   " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
                }

                break;
            case "400000":
                // FUNDS TRANSFER

                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));
                String StramountString = formatter.format(Double.valueOf(hashDelimiterdetails.get("4"))).replace("TZS", "");

                switch (hashDelimiterdetails.get("39")) {
                    case "00":
                        if (hashDelimiterdetails.get("48").toUpperCase().equals("SUCCESS")) {

                            try {
                                String strAccountNumber = hashDelimiterdetails.get("103").toString();
                                String SendersName = "ACC NAME:" + getCustomerDetails((hashDelimiterdetails.get("102")));
                                String RecipientsName = "ACC NAME:" + getCustomerDetails((hashDelimiterdetails.get("103")));

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "          FUNDS TRANSFER          " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "     FUNDS TRANSFER SUCCESSFUL    " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "AMOUNT:       TZS " + StramountString + "#";
                                hashDelimiterString += "FROM ACCOUNT:     " + (hashDelimiterdetails.get("102")) + "#";
                                hashDelimiterString += stringBuilder(SendersName) + "#";
                                hashDelimiterString += "TO ACCOUNT:       " + (hashDelimiterdetails.get("103")) + "#";
                                hashDelimiterString += stringBuilder(RecipientsName) + "#";

                                hashDelimiterString += "                                " + "#";

                            } catch (Exception ex) {
                                this.log("INFO : Function genHashDelimiterString   " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
                            }
                        } else {
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "          FUNDS TRANSFER          " + "#";
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "       FUNDS TRANSFER FAILED    " + "#";
                            hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                            hashDelimiterString += "                                " + "#";
                        }
                        break;

                    default:
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "          FUNDS TRANSFER          " + "#";
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "       FUNDS TRANSFER FAILED    " + "#";
                        hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                        hashDelimiterString += "                                " + "#";
                        break;

                }
                hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                if (hashDelimiterdetails.get("39").equals("00")) {
                    String strReplace = hashDelimiterString.replace("CUSTOMER COPY", "AGENT COPY");
                    hashDelimiterString += strReplace;
                }
                this.SendPOSResponse(hashDelimiterString, intid);
                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;

            case "420000":
                // Airtime Topup
                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));

                switch (hashDelimiterdetails.get("39")) {
                    case "00":
                        try {
                            String strAccountNumber = hashDelimiterdetails.get("102").toString();
                            String strAmount = formatter.format(Double.valueOf(hashDelimiterdetails.get("4"))).replace("TZS", "");

                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "         AIRTIME TOPUP          " + "#";
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "    AIRTIME TOPUP SUCCESSFUL    " + "#";
                            hashDelimiterString += "ACCOUNT NUM:     " + strAccountNumber + "#";
                            hashDelimiterString += "AMOUNT:      TZS " + strAmount + "#";
                            hashDelimiterString += "                                " + "#";

                        } catch (Exception ex) {
                            this.log("INFO : Function genHashDelimiterString   " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
                        }
                        break;
                    default:
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "         AIRTIME TOPUP          " + "#";
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "      AIRTIME TOPUP FAILED      " + "#";
                        hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                        hashDelimiterString += "                                " + "#";
                        break;
                }

                hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));

                if (hashDelimiterdetails.get("39").equals("00")) {
                    String strReplace = hashDelimiterString.replace("CUSTOMER COPY", "AGENT COPY");
                    hashDelimiterString += strReplace;
                }
                this.SendPOSResponse(hashDelimiterString, intid);
                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;

            case "500000":
                //Bills Payments
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));
                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";

                switch (hashDelimiterdetails.get("39")) {
                    case "00":
                        if (hashDelimiterdetails.get("48").toString().trim().equalsIgnoreCase("NOPURCHASE")) {
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "          BILL PAYMENT          " + "#";
                            hashDelimiterString += stringBuilder(hashDelimiterdetails.get("48")) + "#";
                            hashDelimiterString += "                                " + "#";
                        } else {
                            // LUKU PREPAID
                            String strAmount = formatter.format(Double.valueOf(hashDelimiterdetails.get("4"))).replace("TZS", "");
                            String strToken = "";
                            if (hashDelimiterdetails.get("100").equals("LUKUPREPAID")) {

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "          BILL PAYMENT          " + "#";
                                hashDelimiterString += "     LUKU PAYMENT SUCCESSFUL    " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += " METER NUM: " + hashDelimiterdetails.get("65") + "#";
                                hashDelimiterString += " TOKEN NUM: " + stringBuilder(hashDelimiterdetails.get("48")) + "#";
                                hashDelimiterString += " AMOUNT PAID:TZS " + strAmount + "#";
                                hashDelimiterString += "                                " + "#";
                            }

                            //AZAMTV
                            if (hashDelimiterdetails.get("100").equals("AZAMTV")) {

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "          BILL PAYMENT          " + "#";
                                hashDelimiterString += "   AZAM TV PAYMENT SUCCESSFUL   " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += " METER NUM: " + hashDelimiterdetails.get("65") + "#";
                                hashDelimiterString += getAZAMTVResponse(hashDelimiterdetails.get("48"));
                                hashDelimiterString += "                                " + "#";

                            }

                            //STARTIMES TV
                            if (hashDelimiterdetails.get("100").equals("STARTIMESTV")) {

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "          BILL PAYMENT          " + "#";
                                hashDelimiterString += " STARTIMES TV PAYMENT SUCCESSFUL" + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += " METER NUM:      " + hashDelimiterdetails.get("65") + "#";
                                hashDelimiterString += getStartimesResponse(hashDelimiterdetails.get("48"));
                                hashDelimiterString += "                                " + "#";
                            }

                            //DAWASCO PAYMENT  
                            if (hashDelimiterdetails.get("100").equals("DAWASCO")) {

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "          BILL PAYMENT          " + "#";
                                hashDelimiterString += "   DAWASCO PAYMENT SUCCESSFUL   " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += " METER NUM:      " + hashDelimiterdetails.get("65") + "#";
                                hashDelimiterString += " TOKEN NUM:      " + stringBuilder(hashDelimiterdetails.get("48")) + "#";
                                hashDelimiterString += " AMOUNT PAID:    " + strAmount + "#";
                                hashDelimiterString += "                                " + "#";
                            }

                            //ZUKU PAYMENT
                            if (hashDelimiterdetails.get("100").equals("ZUKU")) {

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "          BILL PAYMENT          " + "#";
                                hashDelimiterString += "   ZUKU PAYMENT SUCCESSFUL   " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += " METER NUM:      " + hashDelimiterdetails.get("65") + "#";
                                hashDelimiterString += getZukuresponse(hashDelimiterdetails.get("48"));
                                hashDelimiterString += "                                " + "#";
                            }

                            //NHC PAYMENT
                            if (hashDelimiterdetails.get("100").equals("NHC")) {

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "          BILL PAYMENT          " + "#";
                                hashDelimiterString += "    NHC PAYMENT SUCCESSFUL   " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += " METER NUM:      " + hashDelimiterdetails.get("65") + "#";
                                hashDelimiterString += " TOKEN NUM:      " + stringBuilder(hashDelimiterdetails.get("48"));
                                hashDelimiterString += " AMOUNT PAID:    " + strAmount + "#";
                                hashDelimiterString += "                                " + "#";
                            }

                            //PRECISIONAIR PAYMENT
                            if (hashDelimiterdetails.get("100").equals("PRECISIONAIR")) {

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "          BILL PAYMENT          " + "#";
                                hashDelimiterString += "PRECISION AIR PAYMENT SUCCESSFUL" + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += " METER NUM:      " + hashDelimiterdetails.get("65") + "#";
                                hashDelimiterString += " TICKET NUM:     " + stringBuilder(hashDelimiterdetails.get("48")) + "#";
                                hashDelimiterString += " AMOUNT PAID:    " + strAmount + "#";
                                hashDelimiterString += "                                " + "#";
                            }
                        }
                        break;
                    default:
                        hashDelimiterString += "         BILL PAYMENT           " + "#";
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "      BILL PAYMENT FAILED   " + "#";
                        hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                        hashDelimiterString += "                                " + "#";
                        break;

                }

                hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                if (hashDelimiterdetails.get("39").equals("00")) {
                    String strReplace = hashDelimiterString.replace("CUSTOMER COPY", "AGENT COPY");
                    hashDelimiterString += strReplace;
                }
                this.SendPOSResponse(hashDelimiterString, intid);
                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;
            case "620000":
                //Cardless Origination
                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));
                switch (hashDelimiterdetails.get("39")) {
                    case "00":
                        String[] strBal = hashDelimiterdetails.get("54").split("|");
                        try {
                            strAvailableBalance = strBal[0];
                        } catch (Exception ex) {
                            this.log("INFO : Function genHashDelimiterString   " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
                        }
                        try {
                            strActualBalance = strBal[1];
                        } catch (Exception ex) {
                            this.log("INFO : Function genHashDelimiterString   " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
                        }

                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "      CARDLESS ORIGINATION      " + "#";
                        hashDelimiterString += "  CARDLESS TRANSFER SUCCESSFUL  " + "#";
                        hashDelimiterString += "                                " + "#";

                        if (hashDelimiterdetails.get("80").isEmpty()) {
                        } else {
                            hashDelimiterString += "    Your 4 digit code is " + hashDelimiterdetails.get("80").substring(0, 4) + "#";
                        }

                        hashDelimiterString += "Please send it to the Receipient" + "#";
                        hashDelimiterString += "--------------------------------" + "#";
                        hashDelimiterString += "Amount .      " + NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(hashDelimiterdetails.get("4"))) + "#";
                        hashDelimiterString += "--------------------------------" + "#";
                        hashDelimiterString += "Avail.      " + NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(strAvailableBalance)) + "#";
                        hashDelimiterString += "Actual.     " + NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(strActualBalance)) + "#";

                        cardlesssmsmsg = "Dear Customer, Your 4 digit code is " + hashDelimiterdetails.get("80").substring(0, 4) + ", kindly send this to the recipient";
                        break;
                    default:

                        strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "      CARDLESS ORIGINATION      " + "#";
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "  CARDLESS ORIGINATION FAILED   " + "#";
                        hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                        hashDelimiterString += "                                " + "#";
                        break;
                }

                hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                this.SendPOSResponse(hashDelimiterString, intid);
                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;

            case "630000":
                //Cardless Fullfillment
                try {
                    hashDelimiterString += "Ref No:      " + hashDelimiterdetails.get("37") + "#";
                    strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));
                    switch (hashDelimiterdetails.get("39")) {
                        case "00":
                            String strAmount = formatter.format(Double.valueOf(hashDelimiterdetails.get("4"))).replace("TZS", "");
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "      CARDLESS FULLFILMENT      " + "#";
                            hashDelimiterString += "CARDLESS FULLFILLMENT SUCCESSFUL" + "#";
                            hashDelimiterString += "AMOUNT:      " + strAmount + "#";
                            hashDelimiterString += "                                " + "#";
                            break;
                        default:
                            strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "      CARDLESS FULLFILMENT      " + "#";
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "   CARDLESS FULLFILMENT FAILED  " + "#";
                            hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                            hashDelimiterString += "                                " + "#";
                            break;
                    }

                    hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                    this.SendPOSResponse(hashDelimiterString, intid);
                    updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                } catch (Exception ex) {
                    this.log("\nINFO genHashDelimeterString() :: " + ex.getMessage() + "\n" + StackTraceWriter(ex), "ERROR");
                }
                break;

            case "720000":
                //Reversal Request
                hashDelimiterString += "TRAN NUM:  " + hashDelimiterdetails.get("37") + "#";
                hashDelimiterString += "--------------------------------" + "#";
                hashDelimiterString += "                                " + "#";
                strReasonDescription = this.getResponseCode(hashDelimiterdetails.get("39"));

                switch (hashDelimiterdetails.get("39")) {
                    case "00":
                        if (hashDelimiterdetails.get("48").equals("SUCCESS")) {
                            try {

                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "        REVERSAL REQUEST        " + "#";
                                hashDelimiterString += "    REVERSAL REQUEST SUCCESSFUL " + "#";
                                hashDelimiterString += "                                " + "#";
                                hashDelimiterString += "                                " + "#";

                            } catch (Exception ex) {
                                this.log("INFO : Function genHashDelimiterString   " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
                            }

                        } else {
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "        REVERSAL REQUEST        " + "#";
                            hashDelimiterString += "                                " + "#";
                            hashDelimiterString += "    REVERSAL REQUEST FAILED     " + "#";
                            hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                            hashDelimiterString += "                                " + "#";

                        }
                        break;
                    default:
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "         REVERSAL REQUEST        " + "#";
                        hashDelimiterString += "                                " + "#";
                        hashDelimiterString += "     REVERSAL REQUEST FAILED    " + "#";
                        hashDelimiterString += " " + stringBuilder(InterpretResponse(hashDelimiterdetails.get("48").toString())) + "#";
                        hashDelimiterString += "                                " + "#";

                        break;
                }

                hashDelimiterString += this.strResponseFooter(hashDelimiterdetails.get("104"));
                if (hashDelimiterdetails.get("39").equals("00")) {
                    String strReplace = hashDelimiterString.replace("CUSTOMER COPY", "AGENT COPY");
                    hashDelimiterString += strReplace;
                }
                this.SendPOSResponse(hashDelimiterString, intid);

                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;

            default:
                hashDelimiterString = "TRANSACTION CODE NOT DEFINED#--------------------------------#";
                hashDelimiterString += "";
                this.SendPOSResponse(hashDelimiterString, intid);
                updateTransactionResponse(hashDelimiterdetails, intid, hashDelimiterString);
                break;
        }

        return hashDelimiterString;
    }

    public String getTransactionType(String transactionCode) {
        String TransactionType = "";

        switch (transactionCode) {
            case "010000":
                TransactionType = "Cash Withdrawal";
                break;
            case "300000":
                TransactionType = "Agent Float";
                break;
            case "310000":
                TransactionType = "Balance Inquiry";
                break;
            case "320000":
                TransactionType = "Bill Presentment";
                break;
            case "380000":
                TransactionType = "Mini-Statement";
                break;
            case "400000":
                TransactionType = "Funds Transfer";
                break;
            case "420000":
                TransactionType = "Mobile Topup";
                break;
            case "500000":
                TransactionType = "Bill Payment";
                break;
            case "720000":
                TransactionType = "Reversal Request";
                break;
        }

        return TransactionType;
    }

    //Response Code on field 39
    public String getResponseCode(String f39) {
        String responseCode = "";

        switch (f39) {
            case "00":
                responseCode = "Transaction completed successfully.";
                break;
            case "01":
                responseCode = "NO PURCHASE";
                break;
            case "26":
                responseCode = "Paid Invoice";
                break;
            case "30":
                responseCode = "Payment reference number is missing";
                break;
            case "79":
                responseCode = "Transaction failed , invalid Currency";
                break;
            case "51":
                responseCode = "Transaction failed , insufficient funds.";
                break;
            case "57":
                responseCode = "Transaction not honoured.";
                break;
            case "72":
                responseCode = "Payment reference number is incorrect";
                break;
            case "73":
                responseCode = "Cancelled Invoice";
                break;
            case "91":
                responseCode = "Issuer not Available";
                break;

        }

        return responseCode;
    }

    public String getResponsePostillion(String respcode) {
        String resp = "";
        switch (respcode) {
            case "00":
                resp = "APPROVED OR COMPLETED SUCCESSFULLY ";
                break;
            case "01":
                resp = "REFER TO CARD ISSUER ";
                break;
            case "02":
                resp = "REFER TO CARD ISSUER SPECIAL CONDITION ";
                break;
            case "03":
                resp = "INVALID MERCHANT ";
                break;
            case "04":
                resp = "PICK UP CARD ";
                break;
            case "05":
                resp = "DO NOT HONOR ";
                break;
            case "06":
                resp = "ERROR ";
                break;
            case "07":
                resp = "PICK UP CARD SPECIAL CONDITION ";
                break;
            case "08":
                resp = "HONOR WITH IDENTIFICATION ";
                break;
            case "09":
                resp = "REQUEST IN PROGRESS ";
                break;
            case "10":
                resp = "APPROVED PARTIAL ";
                break;
            case "11":
                resp = "APPROVED VIP ";
                break;
            case "12":
                resp = "INVALID TRANSACTION ";
                break;
            case "13":
                resp = "INVALID AMOUNT ";
                break;
            case "14":
                resp = "INVALID CARD NUMBER ";
                break;
            case "15":
                resp = "NO SUCH ISSUER ";
                break;
            case "16":
                resp = "APPROVED UPDATE TRACK 3 ";
                break;
            case "17":
                resp = "CUSTOMER CANCELLATION ";
                break;
            case "18":
                resp = "CUSTOMER DISPUTE ";
                break;
            case "19":
                resp = "RE ENTER TRANSACTION ";
                break;
            case "20":
                resp = "INVALID RESPONSE ";
                break;
            case "21":
                resp = "NO ACTION TAKEN ";
                break;
            case "22":
                resp = "SUSPECTED MALFUNCTION ";
                break;
            case "23":
                resp = "UNACCEPTABLE TRANSACTION FEE ";
                break;
            case "24":
                resp = "FILE UPDATE NOT SUPPORTED ";
                break;
            case "25":
                resp = "UNABLE TO LOCATE RECORD ";
                break;
            case "26":
                resp = "DUPLICATE RECORD ";
                break;
            case "27":
                resp = "FILE UPDATE FIELD EDIT ERROR ";
                break;
            case "28":
                resp = "FILE UPDATE FILE LOCKED ";
                break;
            case "29":
                resp = "FILE UPDATE FAILED ";
                break;
            case "30":
                resp = "FORMAT ERROR ";
                break;
            case "31":
                resp = "BANK NOT SUPPORTED ";
                break;
            case "32":
                resp = "COMPLETED PARTIALLY ";
                break;
            case "33":
                resp = "EXPIRED CARD PICK UP ";
                break;
            case "34":
                resp = "SUSPECTED FRAUD PICK UP ";
                break;
            case "35":
                resp = "CONTACT ACQUIRER PICK UP ";
                break;
            case "36":
                resp = "RESTRICTED CARD PICK UP ";
                break;
            case "37":
                resp = "CALL ACQUIRER SECURITY PICK UP ";
                break;
            case "38":
                resp = "PIN TRIES EXCEEDED PICK UP ";
                break;
            case "39":
                resp = "NO CREDIT ACCOUNT ";
                break;
            case "40":
                resp = "FUNCTION NOT SUPPORTED ";
                break;
            case "41":
                resp = "LOST CARD PICK UP ";
                break;
            case "42":
                resp = "NO UNIVERSAL ACCOUNT ";
                break;
            case "43":
                resp = "STOLEN CARD PICK UP ";
                break;
            case "44":
                resp = "NO INVESTMENT ACCOUNT ";
                break;
            case "45":
                resp = "ACCOUNT CLOSED ";
                break;
            case "46":
                resp = "IDENTIFICATION REQUIRED ";
                break;
            case "47":
                resp = "IDENTIFICATION CROSS CHECK REQUIRED ";
                break;
            case "48":
                resp = "TO 50 RESERVED FOR FUTURE POSTILION USE ";
                break;
            case "51":
                resp = "NOT SUFFICIENT FUNDS ";
                break;
            case "52":
                resp = "NO CHECK ACCOUNT ";
                break;
            case "53":
                resp = "NO SAVINGS ACCOUNT ";
                break;
            case "54":
                resp = "EXPIRED CARD ";
                break;
            case "55":
                resp = "INCORRECT PIN ";
                break;
            case "56":
                resp = "NO CARD RECORD ";
                break;
            case "57":
                resp = "TRANSACTION NOT PERMITTED TO CARDHOLDER ";
                break;
            case "58":
                resp = "TRANSACTION NOT PERMITTED ON TERMINAL ";
                break;
            case "59":
                resp = "SUSPECTED FRAUD ";
                break;
            case "60":
                resp = "CONTACT ACQUIRER ";
                break;
            case "61":
                resp = "EXCEEDS WITHDRAWAL LIMIT ";
                break;
            case "62":
                resp = "RESTRICTED CARD ";
                break;
            case "63":
                resp = "SECURITY VIOLATION ";
                break;
            case "64":
                resp = "ORIGINAL AMOUNT INCORRECT ";
                break;
            case "65":
                resp = "EXCEEDS WITHDRAWAL FREQUENCY ";
                break;
            case "66":
                resp = "CALL ACQUIRER SECURITY ";
                break;
            case "67":
                resp = "HARD CAPTURE ";
                break;
            case "68":
                resp = "RESPONSE RECEIVED TOO LATE ";
                break;
            case "69":
                resp = "ADVICE RECEIVED TOO LATE ";
                break;
            case "70":
                resp = "TO 74 RESERVED FOR FUTURE POSTILION USE ";
                break;
            case "75":
                resp = "PIN TRIES EXCEEDED ";
                break;
            case "76":
                resp = "RESERVED FOR FUTURE POSTILION USE ";
                break;
            case "77":
                resp = "INTERVENE BANK APPROVAL REQUIRED ";
                break;
            case "79":
                resp = "TO 89 RESERVED FOR CLIENT SPECIFIC USE ";
                break;
            case "90":
                resp = "CUT OFF IN PROGRESS ";
                break;
            case "91":
                resp = "ISSUER OR SWITCH INOPERATIVE ";
                break;
            case "92":
                resp = "ROUTING ERROR ";
                break;
            case "93":
                resp = "VIOLATION OF LAW ";
                break;
            case "94":
                resp = "DUPLICATE TRANSACTION ";
                break;
            case "95":
                resp = "RECONCILE ERROR ";
                break;
            case "96":
                resp = "SYSTEM MALFUNCTION ";
                break;
            case "98":
                resp = "EXCEEDS CASH LIMIT ";
                break;
            case "A1":
                resp = "ATC NOT INCREMENTED ";
                break;
            case "A2":
                resp = "ATC LIMIT EXCEEDED ";
                break;
            case "A3":
                resp = "ATC CONFIGURATION ERROR ";
                break;
            case "A4":
                resp = "CVR CHECK FAILURE ";
                break;
            case "A5":
                resp = "CVR CONFIGURATION ERROR ";
                break;
            case "A6":
                resp = "TVR CHECK FAILURE ";
                break;
            case "A7":
                resp = "TVR CONFIGURATION ERROR ";
                break;
            case "A8":
                resp = "TO BZ RESERVED FOR FUTURE POSTILION USE ";
                break;
            case "C":
                resp = "ZERO UNACCEPTABLE PIN ";
                break;
            case "C1":
                resp = "PIN CHANGE FAILED ";
                break;
            case "C2":
                resp = "PIN UNBLOCK FAILED ";
                break;
            case "C3":
                resp = "TO D ZERO RESERVED FOR FUTURE POSTILION USE ";
                break;
            case "D1":
                resp = "MAC ERROR ";
                break;
            case "D2":
                resp = "TO E ZERO RESERVED FOR FUTURE POSTILION USE ";
                break;
            case "E1":
                resp = "PREPAY ERROR ";
                break;
            case "E2":
                resp = "TO MZ RESERVED FOR FUTURE POSTILION USE ";
                break;
            case "N":
                resp = "ZERO TO ZZ RESERVED FOR CLIENT SPECIFIC USE ";
                break;
            default:
                resp = "DO NOT HONOUR ";
        }
        return resp;
    }

    public String sendTransactionHash(String AgentID, String SerialNumber) {
        String hashedResult = "";
        boolean response = false;
        String firstResult = "";
        String secondResult = "";
        String query = "";
        String data = "";
        String[] dataArray;

        try {

            query = "SELECT A.TERMINALID ||'|' || B.FLOATACCOUNT || '|' || B.COMMISSIONACCOUNT  || '|' || A.PIN || '|' || A.HASH AS PROFILE ";
            query += " FROM TBAGENTDEVICES A INNER JOIN TBAGENTS B ON A.AGENTID = B.AGENTID WHERE B.AGENTID ='" + AgentID + "' ";
            query += " AND A.DEVICEIMEI ='" + SerialNumber + "'";

            data = connections.ExecuteQueryStringValue(ECSERVER, query, ECPASSWORD, ECUSER, ECDB, "", "PROFILE");

            if (data == "" || data == null || data.isEmpty()) {
                // 
            } else {
                dataArray = data.split("\\|");
                firstResult = dataArray[0] + dataArray[1];
                secondResult = dataArray[2] + dataArray[3];
                hashedResult = encryptPin(firstResult, secondResult, "!Eclectic%IsThe BomB%Limited!??hehehe");
            }
        } catch (Exception ex) {
            this.log("INFO checkValidTransaction()  :: " + ex.getMessage() + "\n" + this.StackTraceWriter(ex), "ERROR");
        }

        return hashedResult;
    }

    public String getDSTVresponse(String inputStream) {
        String[] dataArray;
        String productCode = "";
        String productName = "";
        String Amount = "";
        String Currency = "";
        String[] billerDetails;
        String[] customerDetails;
        String response = "";
        int i;

        dataArray = inputStream.split("\\~");
        customerDetails = dataArray[0].split("\\|");
        response = customerDetails[0] + " : " + customerDetails[1] + "\n";
        for (i = 1; i < 5; i++) {
            billerDetails = dataArray[i].split("\\|");
            productCode = billerDetails[0];
            productName = billerDetails[1];
            Amount = billerDetails[2];
            Currency = billerDetails[3];
            response += i + " " + productCode + " " + productName + " " + Currency + " " + Amount + "\n";
        }
        return response;
    }

    public String getZukuresponse(String str) {
        String response = "";
        String request = "";
        String[] splits = str.split("ZUKUVendor|Account|Amount|Trans ID|Reference|Confirmation ID|Powered by selcommobile");
        for (String s : splits) {
            request += s + "-";
        }
        String[] dataArray = request.split("-");
        response = " ACCOUNT:         " + dataArray[2] + "#";
        response += " AMOUNT:         " + dataArray[3] + "#";
        response += " TRANSACTION ID: " + dataArray[4] + "#";
        response += " REFERENCE NUM:  " + dataArray[5] + "#";
        response += " CONFIRMATION ID:" + dataArray[6] + "#";

        return response;
    }

    public String getStartimesResponse(String str) {
        String response = "";
        String request = "";
        String[] splits = str.split("StarTimes|Account|Amount|TransID|Reference|Receipt");
        for (String s : splits) {
            request += s + "-";
        }
        String[] dataArray = request.split("-");
        response = " CUSTOMER NAME: " + dataArray[1] + "#";
        response += " ACCOUNT:        " + dataArray[2] + "#";
        response += " AMOUNT:         " + dataArray[3] + "#";
        response += " TRANSACTION ID: " + dataArray[4] + "#";
        response += " REFERENCE NUM:  " + dataArray[5] + "#";
        response += " RECIEPT NO:" + dataArray[6] + "#";

        return response;
    }

    public String getAZAMTVResponse(String str) {
        String response = "";
        String request = "";
        String[] splits = str.split("Azam Pay TV|Account|Amount|TransID|Reference|Receipt");
        for (String s : splits) {
            request += s + "-";
        }
        String[] dataArray = request.split("-");
        response = " CUSTOMER NAME: " + dataArray[1] + "#";
        response += " ACCOUNT:        " + dataArray[2] + "#";
        response += " AMOUNT:         " + dataArray[3] + "#";
        response += " TRANSACTION ID: " + dataArray[4] + "#";
        response += " REFERENCE NUM:  " + dataArray[5] + "#";
        response += " RECIEPT NO:" + dataArray[6] + "#";

        return response;
    }
}
