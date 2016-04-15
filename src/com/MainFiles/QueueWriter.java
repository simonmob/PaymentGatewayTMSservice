/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.MainFiles;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class QueueWriter {

    // Defines the JNDI context factory.
    private final String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
    // Defines the JMS context factory.
    public final static String JMS_FACTORY = "jms/AgencyConnectionFactory";
    // Defines the queue.
    private String QUEUE;
    private QueueConnectionFactory qconFactory;
    private QueueConnection qcon;
    private QueueSession qsession;
    private QueueSender qsender;
    private Queue queue;
    private TextMessage msg;
    private ObjectMessage objmsg;
    private MapMessage mapmsg;
    private String PROVIDER_URL;

    public QueueWriter() {
    }

    public QueueWriter(String QUEUE, String PROVIDER_URL) {
        this.PROVIDER_URL = PROVIDER_URL;
        this.QUEUE = QUEUE;
        InitialContext ic = getInitialContext();
        init(ic, QUEUE);
    }

    public void init(Context ctx, String queueName) {
        try {
            qconFactory = (QueueConnectionFactory) ctx.lookup(JMS_FACTORY);
            qcon = qconFactory.createQueueConnection();
            qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = (Queue) ctx.lookup(queueName);
            qsender = qsession.createSender(queue);
            msg = qsession.createTextMessage();
            objmsg = qsession.createObjectMessage();
            mapmsg = qsession.createMapMessage();
            qcon.start();
        } catch (Exception e) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at init " + e.getMessage() + "\n" + ESBLogger.StackTraceWriter(e), "ERROR");
        }
    }

    private InitialContext getInitialContext() {
        Hashtable env = new Hashtable();
        InitialContext ic = null;
        try {
            env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
            env.put(Context.PROVIDER_URL, PROVIDER_URL);
            ic = new InitialContext(env);
        } catch (NamingException e) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at getInitialContext() " + e.getMessage() + "\n" + ESBLogger.StackTraceWriter(e), "ERROR");

        }
        return ic;
    }

    public void close() {
        try {
            if (qsender != null) {
                qsender.close();
            }

            if (qsession != null) {
                qsession.close();
            }

            if (qcon != null) {
                qcon.close();
            }
        } catch (JMSException e) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at close " + e.getMessage() + "\n" + ESBLogger.StackTraceWriter(e), "ERROR");
        }
    }

    public boolean send(String message, String CorrelationID) {
        boolean sent = false;
        try {
            msg.setText(message);
            if (CorrelationID.length() > 0) {
                msg.setJMSCorrelationID(CorrelationID);
            }
            qsender.send(msg);
            sent = true;
        } catch (JMSException e) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at send  " + e.getMessage() + "\n" + ESBLogger.StackTraceWriter(e), "ERROR");
        }
        close();
        return sent;
    }

    public boolean sendObject(HashMap message, String CorrelationID) {
        boolean sent = false;
        try {
            objmsg.setJMSCorrelationID(CorrelationID);
            objmsg.setObject(message);
            qsender.send(objmsg);
            sent = true;
        } catch (JMSException e) {
            Functions ESBLogger = new Functions();
            ESBLogger.log("Error at send  " + e.getMessage() + "\n" + ESBLogger.StackTraceWriter(e), "ERROR");
        }
        close();
        return sent;
    }

}
