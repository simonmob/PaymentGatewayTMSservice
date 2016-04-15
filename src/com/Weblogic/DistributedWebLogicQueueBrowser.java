/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Weblogic;

/**
 *
 * @author Collins
 */
import com.MainFiles.Functions;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class DistributedWebLogicQueueBrowser {

    // Defines the JNDI context factory.
    public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
    // Defines the JMS connection factory for the queue.
    public final static String JMS_FACTORY = "jms/AgencyConnectionFactory";
    private QueueConnectionFactory qconFactory;
    private QueueConnection qcon;
    private QueueSession qsession;
    private Queue queue;
    private QueueBrowser browser;
    private QueueReceiver receiver;
    private static String JMSCorrelationID;
    private static String url;
    public static String QUEUE;

    public Message browseWebLogicQueue(String JMSCorrelationID, String url, String QUEUE) {
        InitialContext ic = getInitialContext(url);

        Message msg = null;
        try {
            if (ic != null) {
                if (init(ic, QUEUE)) {
                    browser = qsession.createBrowser(queue, "JMSCorrelationID = '" + JMSCorrelationID + "'");
                    Enumeration en = browser.getEnumeration();
                    while (en.hasMoreElements()) {
                        Message message = (Message) en.nextElement();
                        msg = message;
                        receiver = qsession.createReceiver(queue, "JMSCorrelationID = '" + JMSCorrelationID + "'");
                        receiver.receiveNoWait();
                    }
                    browser.close();
                }
            }
        } catch (JMSException ex) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at DistributedWebLogicQueueBrowser browseWebLogicQueue() " + ex.getMessage() + "\n" + ESBLogger.StackTraceWriter(ex), "error--");

        }
        close();
        return msg;
    }

    public boolean init(Context ctx, String queueName) {
        try {
            qconFactory = (QueueConnectionFactory) ctx.lookup(JMS_FACTORY);
            qcon = qconFactory.createQueueConnection();
            qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = (Queue) ctx.lookup(queueName);
            qcon.start();
            return true;

        } catch (NamingException | JMSException ex) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at DistributedWebLogicQueueBrowser init()" + ex.getMessage() + "\n" + ESBLogger.StackTraceWriter(ex), "error--");
            return false;
        }
    }

    private static InitialContext getInitialContext(String url) {
        Hashtable env = new Hashtable();
        InitialContext ic = null;
        try {
            env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
            env.put(Context.PROVIDER_URL, url);
            ic = new InitialContext(env);
        } catch (NamingException ex) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at DistributedWebLogicQueueBrowser getInitialContext()" + ex.getMessage() + "\n" + ESBLogger.StackTraceWriter(ex), "error--");
        }
        return ic;
    }

    public void close() {
        try {
            if (qsession != null) {
                qsession.close();
            }
            if (qcon != null) {
                qcon.close();
            }
            if (receiver != null) {
                receiver.close();
            }
        } catch (JMSException ex) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at DistributedWebLogicQueueBrowser close()" + ex.getMessage() + "\n" + ESBLogger.StackTraceWriter(ex), "error--");

        }
    }
}
