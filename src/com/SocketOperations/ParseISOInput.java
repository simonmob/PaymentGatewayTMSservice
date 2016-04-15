/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.SocketOperations;

import com.MainFiles.Functions;
import com.MainFiles.ISO8583;
import com.EconnectTMS.EconnectTMSservice;
import java.util.HashMap;
import java.util.Queue;
import org.jboss.netty.channel.MessageEvent;

/**
 *
 * @author Collins
 */
public class ParseISOInput implements Runnable {

    private static String countFile;
    private static String logFile;
    private static String logFileToEC;
    private static String logFileFromEC;
    public static String logFileToFlex;
    public static String logFileFromFlex;
    private static int flexTimeOut;
    public static int hostDelay;
    private static int jCount;
    private Queue qList;
    String IsoMsg;
    MessageEvent e;

    public ParseISOInput(String Iso, MessageEvent chEv) {
        IsoMsg = Iso;
        e = chEv;

        logFile = "Info--";
        logFileToEC = "logFileToEC";
        logFileFromEC = "logFileFromEC";
        logFileToFlex = "logFileToFlex";
        logFileFromFlex = "logFileFromFlex";
        flexTimeOut = 20;

    }

    @Override
    public void run() {
        processInput(IsoMsg, e);
    }

    public void processInput(String IsoMsg, MessageEvent e) {
        try {
            Functions func = new Functions();
            ISO8583 Iso = new ISO8583();
            HashMap<String, String> fields = new HashMap(); // key value pairs
            
            fields = Iso.ReadISO(IsoMsg);
            
            fields.put("RawMessage", IsoMsg);
            String MTI = fields.get("MTI");
 
            switch (MTI) {
                case "0800":
                    //System.out.println(MTI + " Processing");
                    e.getChannel().write(processNetworkMessage(fields));
                    //Reply to sender and transact now
                    break;

                case "0810":
                    //System.out.println(MTI + " Processing");
                    NetworkMessageSender.SignonReceived = true;
                    //Transact now
                    break;

                case "0200":  //Bal inq Rqst
                    System.out.println(MTI + " Processing");
                   // process0200Message(fields);

                    break;

                case "0210":  //Bal inq response
                    //System.out.println(MTI + " Processing");
                    NetworkMessageSender.SignonReceived = true;
                    //Iso.printScreenMessage(fields);
                    func.getSwitchResponse(fields);

                    break;

                case "0221":
                    System.out.println(MTI + " Processing");
                    NetworkMessageSender.SignonReceived = true;
                    //processTransactionMessage(fields);
                    // breakmsg.MessageIncomingFromPOS(fields);

                    break;
                case "0230":
                case "0231":
                    System.out.println(MTI + " Processing");
                    NetworkMessageSender.SignonReceived = true;
                    //processTransactionMessage(fields);

                    break;

                case "0430":
                case "0431":
                    System.out.println(MTI + " Processing");
                    NetworkMessageSender.SignonReceived = true;
                    processTransactionMessage(fields);

                    break;

                default:
                    System.out.println(MTI + " no such transaction type configured");
                    break;
            }

        } catch (Exception ex) {
            System.out.println("Error : " + ex);
            Functions ESBLogger = new Functions();
            ESBLogger.log(ex.getMessage(), logFile);

        }
    } // end fn processInput()

    public void process0200Message(HashMap<String, String> fields) {
    }

    public HashMap processTransactionMessage(HashMap<String, String> fields) {
//        System.out.println("----ISO MESSAGE-----");
//        for (Map.Entry<String, String> entry : fields.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue();
//
//            System.out.printf("  Field-%s : %s\n", key, value);
//        }
//        System.out.println("--------------------");

        //FlexQueueListener flx = new FlexQueueListener();
        //flx.ResponseFromFlex(fields);
        // EconnectTMSservice incomingMessage = new EconnectTMSservice();
        //incomingMessage.MessageIncomingFromPOS(fields);
        return fields;
    }

    public String processNetworkMessage(HashMap<String, String> fields) {
        String IsoMsg = "";
        ISO8583 Iso = new ISO8583();
        if (fields.get("70").equals("001")) {
            IsoMsg = Iso.CreateEchoNetworkISO("signon", "ack", fields);
        } else {
            IsoMsg = Iso.CreateEchoNetworkISO("echo", "ack", fields);
        }
      return IsoMsg;
    }
}
