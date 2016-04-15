/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.MainFiles;

/**
 *
 * @author Collins
 */
import com.SocketOperations.MySocketHandler;
import com.SocketOperations.ClientSocketHandler;
import com.EconnectTMS.EconnectTMSservice;
import com.EconnectTMS.PurgeHoldingQueue;
import com.Logger.LogsProcessor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.jboss.netty.channel.*;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.util.CharsetUtil;

public class ISO8583Adaptor {

    public static ExecutorService executor;
    public static int port;
    public static int TMS_PORT;
    public static String SWITCH_HOST;
    public static int SWITCH_PORT;
    public static String ESB_HOST;
    public static int ESB_PORT;
    public static String host;
    public static String server;
    public static Boolean isConnected = false;
    public static Boolean isConnectedClient = false;
    static Timer tmr = new Timer();
   
    public static String HOST_TIMEOUT = null;
    public static String logFile = null;
    public static String COUNT_FILE = null;
    public static String logFileFromEC = null;
    public static String logFileFromFlex = null;
    public static String NETWORK_MSG_INTERVAL = null;
    public static int RequestMessages;
    public static int MessagesToESB;
    public static int MessagesFromFlex;
    public static int MessagesToFlex;
    public static String INITIAL_CONTEXT_FACTORY;
    public static String JMS_FACTORY;
    public static String QUEUE;
    public static String PROVIDER_URL;
    public static InitialContext ctx = null;
    public static HashMap HoldingQueue = new HashMap<String, HashMap<String, String>>();
    public static String MACHINE1;
    public static String MACHINE2;
    public static String LOG_DIR;
    public static ClientBootstrap bootstrap;
    //Econnect DB connections
    public static String ECDB;
    public static String ECUSER;
    public static String ECPASSWORD;
    public static String ECSERVER;
    //Etax DB connections
    public static String ETDB;
    public static String ETUSER;
    public static String ETPASSWORD;
    public static String ETSERVER;
    //API connections
    public static String API_URL;
    public static String SOURCE;
    public static String BRANCH_ID;
    public static String API_KEY;
    public static String API_SECRET;
    public static String ERROR_DIR;
    //Terminal Details
    public static String CARD_ACCEPTOR_TERMINAL_ID;
    public static String CARD_ACCEPTOR_ID_CODE;
    public static String CARD_ACCEPTOR_NAME_LOCATION;
    public static String PIN_VERIFICATION_ENCRYPT_KEYS;
    public static String BIN;
    public static String CUSTOMER_DETAILS_URL;
    //ESB TCP/IP 
    public static String SOURCE_ID;
    public static String PURGE_QUEUE_EXPIRY;
    //Switch
    public static String LOCAL_IP;
    public static String PIN_IP;
    public static String PIN_PORT;
    public static String DEBUG_MODE;
    //Billers Information
    public static String DSTV;
    public static String GOTV;
    public static String LUKUPREPAID;
    public static String WATR;
    public static String STTV;
    public static String DAWASCO;
    public static String AZAMTV;
    public static String SMS_URL;
    public static String AGENCY_ADAPTER_URL;
    public static String QUEUE_RESPONSE;
    public static String QUEUE_REQUEST;
    public static String CUSTOMER_DETAILS_USERNAME;
    public static String CUSTOMER_DETAILS_PASSWORD;
    public static String CUSTOMER_DETAILS_SOURCE_ID;
    public static String ESB_TIMEOUT;
    public static String ECHO_TIME;
    public static HashMap<String, String> sysprop = new HashMap();

    public static void main(String[] args) throws Exception {

        Properties prop = new Properties();
        Functions func = new Functions();

        try {
            
           // File jarPath=new File(ISO8583Adaptor.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            //String propertiesPath=jarPath.getParentFile().getAbsolutePath();
            InputStream is=ISO8583Adaptor.class.getResourceAsStream("/config.properties");
            prop.load(is);
        } catch (IOException e) {
            System.out.println("Error on properties file : " + e.getMessage() + func.StackTraceWriter(e));
        }

        executor = Executors.newFixedThreadPool(Integer.parseInt(prop.getProperty("MAX_THREADS")));

        checkInstance();
        LoggerProcessor();

        TMS_PORT = Integer.parseInt(prop.getProperty("TMS_PORT"));
        SWITCH_HOST = prop.getProperty("SWITCH_HOST");
        SWITCH_PORT = Integer.parseInt(prop.getProperty("SWITCH_PORT"));
        host = prop.getProperty("SWITCH_HOST");
        server = prop.getProperty("SERVER");
        HOST_TIMEOUT = prop.getProperty("HOST_TIMEOUT");
        logFile = prop.getProperty("LOG_FILE");
        NETWORK_MSG_INTERVAL = prop.getProperty("NETWORK_MSG_INTERVAL");
        COUNT_FILE = prop.getProperty("COUNT_FILE");
        INITIAL_CONTEXT_FACTORY = prop.getProperty("INITIAL_CONTEXT_FACTORY");
        JMS_FACTORY = prop.getProperty("JMS_FACTORY");
        QUEUE = prop.getProperty("QUEUE_NAME");
        LOG_DIR = prop.getProperty("LOG_DIR");
        PROVIDER_URL = prop.getProperty("PROVIDER_URL");
        MACHINE1 = prop.getProperty("PROVIDER_URL");
        MACHINE2 = prop.getProperty("PROVIDER_URL");
        ECDB = prop.getProperty("ECDB");
        ECUSER = prop.getProperty("ECUSER");
        ECPASSWORD = prop.getProperty("ECPASSWORD");
        ECSERVER = prop.getProperty("ECSERVER");
        SOURCE_ID = prop.getProperty("SOURCE_ID");
        PURGE_QUEUE_EXPIRY = prop.getProperty("PURGE_QUEUE_EXPIRY");
        API_URL = prop.getProperty("API_URL");
        SOURCE = prop.getProperty("SOURCE");
        BRANCH_ID = prop.getProperty("BRANCH_ID");
        API_KEY = prop.getProperty("API_KEY");
        API_SECRET = prop.getProperty("API_SECRET");
        SMS_URL = prop.getProperty("SMS_URL");
        COUNT_FILE = prop.getProperty("COUNT_FILE");
     
        CARD_ACCEPTOR_TERMINAL_ID = prop.getProperty("CARD_ACCEPTOR_TERMINAL_ID");
        CARD_ACCEPTOR_ID_CODE = prop.getProperty("CARD_ACCEPTOR_ID_CODE");
        CARD_ACCEPTOR_NAME_LOCATION = prop.getProperty("CARD_ACCEPTOR_NAME_LOCATION");
        PIN_VERIFICATION_ENCRYPT_KEYS = prop.getProperty("PIN_VERIFICATION_ENCRYPT_KEYS");
        BIN = prop.getProperty("BIN");
        CUSTOMER_DETAILS_URL = prop.getProperty("CUSTOMER_DETAILS_URL");
        AGENCY_ADAPTER_URL = prop.getProperty("AGENCY_ADAPTER_URL");
        QUEUE_RESPONSE = prop.getProperty("QUEUE_RESPONSE");
        QUEUE_REQUEST = prop.getProperty("QUEUE_REQUEST");
        PROVIDER_URL = prop.getProperty("PROVIDER_URL");
        CUSTOMER_DETAILS_USERNAME = prop.getProperty("CUSTOMER_DETAILS_USERNAME");
        CUSTOMER_DETAILS_PASSWORD = prop.getProperty("CUSTOMER_DETAILS_PASSWORD");
        CUSTOMER_DETAILS_SOURCE_ID = prop.getProperty("CUSTOMER_DETAILS_SOURCE_ID");
        ESB_TIMEOUT = prop.getProperty("ESB_TIMEOUT");
        ECHO_TIME = prop.getProperty("ECHO_TIME");
        LOCAL_IP = prop.getProperty("LocalIP");
        PIN_IP = prop.getProperty("PINIP");
        PIN_PORT = prop.getProperty("PINport");
        DEBUG_MODE = prop.getProperty("DEBUG_MODE");
        DSTV = prop.getProperty("DSTV");
        GOTV = prop.getProperty("GOTV");
        LUKUPREPAID = prop.getProperty("LUKUPREPAID");
        WATR = prop.getProperty("WATR");
        STTV = prop.getProperty("STTV");
        AZAMTV = prop.getProperty("AZAMTV");
        DAWASCO = prop.getProperty("DAWASCO");

        ConnectServer();
        //ConnectClient();
    }

    public static boolean ConnectServer() {
        return BindServer();

    }

    public static boolean ConnectClient() {
        return BindClient();

    }

    public static boolean BindClient() {
        //Creating a Connected Channel
        ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

        // Configure the client.
        bootstrap = new ClientBootstrap(factory);
        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline p = Channels.pipeline();
                p.addLast("stringDecoder", new StringDecoder(CharsetUtil.ISO_8859_1));
                p.addLast("stringEncoder", new StringEncoder(CharsetUtil.ISO_8859_1));
                p.addLast("ClientSocketHandler", new ClientSocketHandler());

                return p;
            }
        });
        // Bind and start to accept incoming connections.
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        try {
            System.out.println("Binding to Host " + SWITCH_HOST + " Port " + SWITCH_PORT);
            // Start the connection attempt.
            bootstrap.connect(new InetSocketAddress(SWITCH_HOST.trim(), SWITCH_PORT));
            return true;
        } catch (Exception e) {
            System.out.println("Could Not Bind to Host " + SWITCH_HOST + " + Port " + SWITCH_PORT + " " + e);
            return false;
        }
    }

    public static boolean BindServer() {
        // More terse code to setup the server
        ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

        // Set up the pipeline factory.
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline p = Channels.pipeline();
                p.addLast("stringDecoder", new StringDecoder(CharsetUtil.ISO_8859_1));
                p.addLast("stringEncoder", new StringEncoder(CharsetUtil.ISO_8859_1));
                p.addLast("myHandler", new MySocketHandler());

                return p;

            }
        });

        // Bind and start to accept incoming connections.
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        try {
            System.out.println("Binding to port: " + TMS_PORT);
            bootstrap.bind(new InetSocketAddress(TMS_PORT));
            return true;
        } catch (Exception e) {
            System.out.println("Could Not Bind to Port " + TMS_PORT + " " + e);
            return false;
        }
    }

    public static void checkInstance() {
        Runnable worker = new InstanceChecker();
        ISO8583Adaptor.executor.execute(worker);
    }

    public static void LoggerProcessor() {
        Runnable logprocessor = new LogsProcessor();
        ISO8583Adaptor.executor.execute(logprocessor);
    }

    public static void readPropertyFile() {
        System.out.println("Finding configurations ...");
        Properties prop = new Properties();
        //String targetFormat=null;
        try {
            InputStream fip =ISO8583Adaptor.class.getResourceAsStream("/config.properties");
            prop.load(fip);
            //targetFormat=prop.getProperty("targetFramt");
            System.out.println("Property File Loaded Succesfully");
            Set<String> propertyNames = prop.stringPropertyNames();
            for (String Property : propertyNames) {
              sysprop.put(Property, prop.getProperty(Property));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}

class CheckStatus implements Runnable {

    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                System.out.println("Channel Status: " + ISO8583Adaptor.isConnected);
            } catch (InterruptedException ex) {
                Logger.getLogger(CheckStatus.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
