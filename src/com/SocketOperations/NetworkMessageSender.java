/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.SocketOperations;

import com.MainFiles.ClassImportantValues;
import com.MainFiles.Functions;
import static com.MainFiles.Functions.StackTraceWriter;
import com.MainFiles.ISO8583;
import static com.MainFiles.ISO8583Adaptor.ECHO_TIME;
import java.text.SimpleDateFormat;
import org.jboss.netty.channel.ChannelStateEvent;

/**
 *
 * @author HESBON
 */
public class NetworkMessageSender implements Runnable {

    public static ChannelStateEvent ChEvt;
    public static boolean exit = false;
    public static boolean SignonSent = false;
    public static boolean SignonReceived = false;

    public NetworkMessageSender(ChannelStateEvent e) {
        ChEvt = e;
        //WoisheHandler.NetworkMessage(ChEvt); //Start Immediately

    }

    @Override
    public void run() {
        try {
            exit = false;
            if (!SignonSent) {
                SignOnMessage(ChEvt);
                SignonSent = true;
            }
            ClassImportantValues cl = new ClassImportantValues();

            while (!exit) {
                try {
                    int timer = Integer.parseInt(ECHO_TIME);
                    Thread.sleep(timer * 1000);
                    NetworkMessage(ChEvt);
                } catch (InterruptedException | NumberFormatException ex) {
                    Functions ESBLogger = new Functions();
                    ESBLogger.log(ex.getMessage(), "error--");
                }
            }
            exit = false;
        } catch (NumberFormatException ex) {
            Functions ESBLogger = new Functions();
            ESBLogger.log(ex.getMessage(), "error--");
        }
    }

    public void SignOnMessage(ChannelStateEvent e) {
        String SignOnMsg = "";
        ISO8583 Iso = new ISO8583();
        SignOnMsg = Iso.CreateEchoNetworkISO("signon", "req");

        e.getChannel().write(SignOnMsg);
        
        long now = System.currentTimeMillis();
        long tEnd = now + 60000;
        while (!SignonReceived) {
            try {
                //System.out.println("Signon not acknowledged.");
                System.out.print("");
                if (System.currentTimeMillis() > tEnd) {
                    if (!SignonReceived) {
                        e.getChannel().disconnect();
                        e.getChannel().close();
                        System.out.println("Network message not acknowledged. Disconnecting Now");
                        exit = true;
                        Functions ESBLogger = new Functions();
                        ESBLogger.log("Network message not acknowledged. Disconnecting Now", "connectionlost--");
                        break;
                    }
                    {
                        System.out.println((new SimpleDateFormat("MMMM dd,yyyy hh:mm:ss.SSS a zzzz")).format(new java.util.Date()) + "\n[RECEIVE: Network message acknowledged.   -:]");
                        String NetworkMsg = Iso.CreateEchoNetworkISO("echo", "req");
                        e.getChannel().write(NetworkMsg);

                    }
                }
            } catch (Exception ex) {

            }
        }
        if (SignonReceived) {
            System.out.println("Network message acknowledged. Transact Now");
        }
    }

    public static void NetworkMessage(ChannelStateEvent e) {
        String NetworkMsg = "";
        ISO8583 Iso = new ISO8583();
        NetworkMsg = Iso.CreateEchoNetworkISO("echo", "req");
        SignonReceived = false;
        e.getChannel().write(NetworkMsg);
       
        long now = System.currentTimeMillis();
        // long tEnd = now + 60000;
        long tEnd = now + 1000;
        while (true && !SignonReceived) {
            try {
                //System.out.println("Signon not acknowledged.");
                System.out.print("");
                if (System.currentTimeMillis() > tEnd) {
                    if (!SignonReceived) {
                        e.getChannel().disconnect();
                        e.getChannel().close();
                        System.out.println("Echo message not acknowledged. Disconnecting Now");
                        exit = true;
                        break;

                    }
                    {
                        System.out.println("Echo message acknowledged. Transact Now");
                    }
                }
            } catch (Exception ex) {
                Functions ESBLogger = new Functions();
                ESBLogger.log("Error GetSequenceNumber : " + ex.getMessage() + ESBLogger.StackTraceWriter(ex), "connectionlost");

            }
        }
        if (SignonReceived) {
            //System.out.println("Echo message acknowledged.");
        }
    }
}
