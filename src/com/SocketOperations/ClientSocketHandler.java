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
import com.MainFiles.Functions;
import com.MainFiles.ISO8583Adaptor;
import static com.MainFiles.ISO8583Adaptor.SWITCH_HOST;
import static com.MainFiles.ISO8583Adaptor.SWITCH_PORT;
import static com.MainFiles.ISO8583Adaptor.bootstrap;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Timer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

public class ClientSocketHandler extends SimpleChannelHandler {

    static final ChannelGroup channels = new DefaultChannelGroup();
    private final Timer timer = null;
    private long startTime = -1;
    String HOST_TIMEOUT = null;
    String logFile = null;
    public static String logFileFromEC = null;
    public static String logFileFromFlex = null;
    public static String NETWORK_MSG_INTERVAL = null;
    String erroLogFile = "error--";
    public static String logFileFromSwtich = "LogFromSwitch";
    public static ChannelStateEvent chEv;
    public static String PendingMessage = "";
    public static String HalfMessage;

    public ClientSocketHandler() {

        HOST_TIMEOUT = ISO8583Adaptor.HOST_TIMEOUT;
        logFile = "info--";
        logFileFromEC = "logFileFromEC";
        logFileFromFlex = "logFileFromFlex";
        NETWORK_MSG_INTERVAL = ISO8583Adaptor.NETWORK_MSG_INTERVAL;

    }

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {

        System.out.println("\nConnected to/from " + e.getChannel().getRemoteAddress());
        System.out.println("Connection Status: " + e.getState());

        chEv = e;
        ISO8583Adaptor.isConnectedClient = true;
        Runnable worker = new NetworkMessageSender(e);
        ISO8583Adaptor.executor.execute(worker);
    }

    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("Disconnected from " + e.getChannel().getRemoteAddress());
        ISO8583Adaptor.isConnectedClient = false;
        //channels.remove(e.getChannel());
        chEv = e;

    }

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
        try {
            System.out.println("Connection Lost\nSleeping for: " + HOST_TIMEOUT + 's');
            Thread.sleep((Integer.parseInt(HOST_TIMEOUT) * 1000));
            System.out.println("Reconnecting to Server: " + ISO8583Adaptor.host);
            //ISO8583Adaptor.BindClient();
            bootstrap.connect(new InetSocketAddress(SWITCH_HOST.trim(), SWITCH_PORT));
            chEv = e;

        } catch (InterruptedException ex) {
            //Logger.getLogger(MySocketHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
    }

    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        String IsoMsg = (String) e.getMessage();
       // System.out.println((new SimpleDateFormat("MMMM dd,yyyy hh:mm:ss.SSS a zzzz")).format(new java.util.Date()) + "\n[RECEIVE: Incoming Message from Postilion: :]=" + IsoMsg);
        IncomingMessage rqstQ = new IncomingMessage();
        rqstQ.getResponseFromSocket(PendingMessage + IsoMsg, e);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        System.out.println(e.getCause().getMessage());
        Channel ch = e.getChannel();
        ch.close();
        System.out.println(e.getCause());
        Functions ESBLogger = new Functions();
        ESBLogger.log("SEVERE exceptionCaught() ::" + Thread.currentThread().getStackTrace()[2].getMethodName() + e.getCause().getMessage(), "ERROR");
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
