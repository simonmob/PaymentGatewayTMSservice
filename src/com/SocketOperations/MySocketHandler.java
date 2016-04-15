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
import com.MainFiles.ClassImportantValues;
import com.MainFiles.Functions;
import com.MainFiles.ISO8583Adaptor;
import com.EconnectTMS.EconnectTMSservice;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

public class MySocketHandler extends SimpleChannelHandler {

    static final ChannelGroup channels = new DefaultChannelGroup();
    final ClientBootstrap bootstrap = null;
    private final Timer timer = null;
    private long startTime = -1;
    String HOST_TIMEOUT = null;
    String logFile = null;
    public static String logFileFromEC = null;
    public static String logFileFromFlex = null;
    public static String NETWORK_MSG_INTERVAL = null;
    String erroLogFile = "error--";
    public static String logFileFromSwtich = "LogFromSwitch";
    public static ChannelStateEvent chEvPOS;
    public static String PendingMessage = "";
    public static String HalfMessage;

    public MySocketHandler() {

        HOST_TIMEOUT = ISO8583Adaptor.HOST_TIMEOUT;
        logFile = "info--";
        logFileFromEC = "logFileFromEC";
        logFileFromFlex = "logFileFromFlex";
        NETWORK_MSG_INTERVAL = ISO8583Adaptor.NETWORK_MSG_INTERVAL;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {

        System.out.println("Connected to/from " + e.getChannel().getRemoteAddress());
        System.out.println("Connection Status: " + e.getState());

        chEvPOS = e;
        ISO8583Adaptor.isConnected = true;
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("Disconnected from " + e.getChannel().getRemoteAddress());
        ISO8583Adaptor.isConnected = false;
        //channels.remove(e.getChannel());

    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
        try {
            if ((ISO8583Adaptor.server).equals("0")) { //This means were are not server but client
                System.out.println("Connection Lost\nSleeping for: " + HOST_TIMEOUT + 's');
                Thread.sleep((Integer.parseInt(HOST_TIMEOUT) * 1000));

                System.out.println("Reconnecting to Server: " + ISO8583Adaptor.host);
                ISO8583Adaptor.BindClient();
            } else {
                //TCPNetty.BindServer();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(MySocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ClassImportantValues cl = new ClassImportantValues();
        Functions func = new Functions();
        //Message from POS Terminal
        ISO8583Adaptor.MessagesFromFlex += 1;
        String IsoMsg = (String) e.getMessage();

        func.log("Message From POS ::" + IsoMsg, "MessageFromPOS");
        System.out.println((new SimpleDateFormat("MMMM dd,yyyy hh:mm:ss.SSS a zzzz")).format(new java.util.Date()) + "\n[RECEIVE: Incoming Message from POS TERMINAL:]");
        //  System.out.println("\n" + IsoMsg);
        Runnable worker = new EconnectTMSservice(IsoMsg);
        ISO8583Adaptor.executor.execute(worker);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        Functions ESBLogger = new Functions();
        ESBLogger.log("SEVERE exceptionCaught() ::" + Thread.currentThread().getStackTrace()[2].getMethodName() + e.getCause().getMessage(), "ERROR");
        Channel ch = e.getChannel();
        ch.close();
    }

    public static String anyDate(String format) {
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
            ex.printStackTrace();
        }
        return "";
    } // end fn anyDate
}
