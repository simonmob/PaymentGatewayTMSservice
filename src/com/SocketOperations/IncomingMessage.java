/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.SocketOperations;

/**
 *
 * @author Collins
 */
import com.MainFiles.DataConversions;
import com.MainFiles.Functions;
import com.MainFiles.ISO8583Adaptor;
import org.jboss.netty.channel.MessageEvent;

public class IncomingMessage {

    String erroLogFile = "error--";
    public static String logFileFromSwtich = "LogFromSwitch";
    String RamainingMessages = "";
    boolean proceed = false;

    public void GetMessagesVer2(String data, MessageEvent e) {

        RamainingMessages = data;
        try {

            do {
                SplitMessages(RamainingMessages, e);
            } while (RamainingMessages.length() != 0 && !proceed);

            MySocketHandler.PendingMessage = RamainingMessages;

        } catch (NumberFormatException ex) {
            //ex.printStackTrace();
            Functions ESBLogger = new Functions();
            ESBLogger.log("\n\nError at GetMessagesVer2: " + data + " - " + ex.getMessage(), "error--");
            //return;
        }
    }

    public void getResponseFromSocket(String data, MessageEvent e) {

        RamainingMessages = data;
        try {

            do {
                SplitMessages(RamainingMessages, e);
            } while (RamainingMessages.length() != 0 && !proceed);

            ClientSocketHandler.PendingMessage = RamainingMessages;

        } catch (NumberFormatException ex) {
            //ex.printStackTrace();
            Functions ESBLogger = new Functions();
            ESBLogger.log("\n\nError at getResponseFromSocket: " + data + " - " + ex.getMessage(), "error--");
            //return;
        }
    }

    public String SplitMessages(String data, MessageEvent e) {
        //System.out.println("SendMessages="+data);
        DataConversions dc = new DataConversions();
        int headerLength = 4;
        String HeaderEncoding = "ASC";
        String SingleMessage = "";
        int HeaderL = 0;
        String HexL = "";
        String L = "";
        try {

            if (data.length() == 1) {
                proceed = true;
            } else {
                if (HeaderEncoding.equalsIgnoreCase("ASC")) {
                    L = data.substring(0, ((headerLength / 2)));
                    HexL = dc.AsciiToHEX(L);
                    HeaderL = Integer.parseInt(HexL, 16);
                    //Add to list
                    if (data.length() >= HeaderL + headerLength / 2) {
                        SingleMessage = data.substring((headerLength / 2), HeaderL + headerLength / 2);

                        Runnable worker = new ParseISOInput(SingleMessage, e);
                        ISO8583Adaptor.executor.execute(worker);
                        RamainingMessages = data.substring((headerLength / 2) + HeaderL);
                        if (RamainingMessages.trim().length() == 0) {
                            proceed = true;
                        }

                    } else {
                        //RamainingMessages = "";
                        proceed = true;

                        //SingleMessage = data.substring((headerLength / 2), HeaderL + headerLength / 2);
                        // Runnable worker = new ParseISOInput(data, e);
                        //ISO8583Adaptor.executor.execute(worker);
                    }
                } else if (HeaderEncoding.equalsIgnoreCase("HEX")) {
                    L = data.substring(0, ((headerLength)));
                    HexL = L;
                    HeaderL = Integer.parseInt(HexL, 16);
                    //Add to list
                    if (data.length() >= HeaderL + headerLength) {
                        SingleMessage = data.substring((headerLength), HeaderL + headerLength);

                        Runnable worker = new ParseISOInput(SingleMessage, e);
                        ISO8583Adaptor.executor.execute(worker);
                        RamainingMessages = data.substring((headerLength) + HeaderL);
                        if (RamainingMessages.trim().length() == 0) {
                            proceed = true;
                        }

                    } else {
                        //RamainingMessages = "";
                        proceed = true;

                        //SingleMessage = data.substring((headerLength / 2), HeaderL + headerLength / 2);
                        // Runnable worker = new ParseISOInput(data, e);
                        //ISO8583Adaptor.executor.execute(worker);
                    }
                } else {

                }

            }

        } catch (NumberFormatException ex) {
            RamainingMessages = "";
            Functions ESBLogger = new Functions();
            System.out.println("\n\nError Short Message at SendMessages L=" + L + " HexL=" + HexL + " HeaderL=" + HeaderL + "; " + data + " - " + ex.getMessage() + ESBLogger.StackTraceWriter(ex));

            ESBLogger.log("\n\nError Splitting Message at SendMessages L=" + L + " HexL=" + HexL + " HeaderL=" + HeaderL + "; " + data + " - " + ex.getMessage() + ESBLogger.StackTraceWriter(ex), "IncomingLog");
            //ex.printStackTrace();
        }

        return RamainingMessages.trim();
    }

   
}
