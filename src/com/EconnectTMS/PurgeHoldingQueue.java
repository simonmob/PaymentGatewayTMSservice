/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EconnectTMS;

import com.MainFiles.ClassImportantValues;
import com.MainFiles.Functions;
import static com.MainFiles.ISO8583Adaptor.PURGE_QUEUE_EXPIRY;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Collins
 */
public class PurgeHoldingQueue {

    public String purgeHoldingQueue() throws SQLException {

        while (true) {
            try {
                Functions func = new Functions();
                ClassImportantValues cl = new ClassImportantValues();

                Thread.sleep(2000);
                String timestamp = "";
                List<String> list = new ArrayList<String>();
                Map<String, Map<String, String>> IsoFieldsMap = EconnectTMSservice.OuterHoldingQueue;

                for (Map.Entry<String, Map<String, String>> entry : IsoFieldsMap.entrySet()) {

                    // System.out.println(entry.getKey() + ":" + entry.getValue());
                    HashMap<String, String> data = (HashMap) entry.getValue();
                    timestamp = data.get("timestamp");
                    // System.out.println(timestamp);
                    int ageExpiry = Integer.parseInt(PURGE_QUEUE_EXPIRY);
                    String today = anyDate("");
                    Date MessageDate = getAnyDate(timestamp, "");
                    Date ExpiredDate = addMinutes(MessageDate, ageExpiry);
                    Date now = getAnyDate(today, "");
                    // System.out.println("\nExpiredDate = " + ExpiredDate + "\n");
                    if (now.after(ExpiredDate)) {
                        // System.out.println("Please Queue for Archiving");
                        list.add(entry.getKey());
                    } else {
                        //  System.out.println("Don't Archive yet!");
                    }
                }
                //System.out.println(EconnectTMSservice.OuterHoldingQueue);
                Iterator<String> ite = list.iterator();
                while (ite.hasNext()) {
                    ///respond to POS --No response from Switch
                    String value = ite.next();
                    EconnectTMSservice.OuterHoldingQueue.get(value);

                    String strResponse = "";
                    strResponse += "--------------------------------" + "#";
                    strResponse += "                                " + "#";
                    strResponse += "        PIN VERIFICATION        " + "#";
                    strResponse += "                                " + "#";
                    strResponse += "    PIN VERIFICATION FAILED     " + "#";
                    strResponse += "    NO RESPONSE FROM SWITCH     " + "#";
                    strResponse += "                                " + "#";
                    func.SendPOSResponse(strResponse, value);

                    func.log("\n Message archived:: " + value + "\n" + EconnectTMSservice.OuterHoldingQueue.get(value), "Archived");
                    SendTransactionReversal_Switch((HashMap<String, String>) EconnectTMSservice.OuterHoldingQueue.get(value));
                    EconnectTMSservice.OuterHoldingQueue.remove(value);
                    ite.remove();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(PurgeHoldingQueue.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static String anyDate(String format) {
        try {
            if ("".equals(format)) {
                format = "yyyy/MM/dd HH:mm:ss"; // default
            }
            java.util.Date today;
            SimpleDateFormat formatter;

            formatter = new SimpleDateFormat(format);
            today = new java.util.Date();
            return (formatter.format(today));
        } catch (Exception ex) {
            System.out.println(" \n**** anyDate ****\n" + ex.getMessage());
            ex.printStackTrace();
        }
        return "";
    }

    public Date getAnyDate(String date, String dateformat) {
        try {
            if ("".equals(dateformat)) {
                dateformat = "yyyy/MM/dd HH:mm:ss"; // default
            }
            DateFormat format = new SimpleDateFormat(dateformat, Locale.ENGLISH);
            return format.parse(date);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Date addMinutes(Date date, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes); //minus number would decrement the days
        return cal.getTime();
    }

    public void SendTransactionReversal_Switch(HashMap<String, String> LogMap) throws SQLException {
        //Send Transaction Reversal to Switch.
        Functions func = new Functions();
        HashMap<String, String> responseMap = new HashMap<String, String>();
        String referenceNumber = func.generateUniqueReferenceNumber();
        func.PIN_Verify(referenceNumber, LogMap.get("88").toString(), LogMap.get("100").toString(), LogMap.get("4").toString(), LogMap.get("3").toString(), LogMap.get("pin").toString(), LogMap.get("pan").toString(), LogMap.get("expirydate").toString(), LogMap.get("68").toString(), LogMap.get("35").toString(), referenceNumber, LogMap.get("2").toString(), LogMap.get("102").toString(), LogMap.get("103").toString(), LogMap.get("68").toString());
    }

}
