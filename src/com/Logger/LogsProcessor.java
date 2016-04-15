/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Logger;

import static com.EconnectTMS.EconnectTMSservice.Logsdataqueue;
import static com.MainFiles.ISO8583Adaptor.DEBUG_MODE;
import static com.MainFiles.ISO8583Adaptor.LOG_DIR;
import com.Utilities.EsbFormatter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HESBON
 */
public class LogsProcessor implements Runnable {

    private final String logFile = LOG_DIR;
    private static final Logger LOGGER = Logger.getLogger(LogsProcessor.class.getName());
    static private FileHandler fhandler;
    static private SimpleDateFormat today;
    static private Date date;
    static int TransactionsLogged = 0;

    @Override
    public void run() {
        System.out.println("Logger Thread started...");
        while (true) {
            try {
                Thread.currentThread().sleep(50);
                if (Logsdataqueue.peek() != null) {
                    Map<String, String> onerequest = Logsdataqueue.poll();
                    log(onerequest);
                    //TransactionsLogged += 1;
                    //System.out.println("LOGGED TRANSACTIONS: " + TransactionsLogged);
                    //System.out.println("POPPELOG: " + onerequest);
                    //System.out.println("QUEUE SIZE AFTER POP: " + VodacomTopUpMainClass.Logsdataqueue.size());
                }
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    void log(Map<String, String> onerequest) {
        try {
            String lFile = onerequest.get("lFile");
            String txt = onerequest.get("txt");
            date = new Date();
            today = new SimpleDateFormat("yyyy-MM-dd");
            String fileName = makeDirectory(LOG_DIR, lFile, today.format(date));

            fhandler = new FileHandler(fileName + lFile + "_" + "%g.log", 26000000, 20, true);
            //LOGGER.setLevel(Level.ALL);
            fhandler.setFormatter(new EsbFormatter());
            LOGGER.addHandler(fhandler);
            if (DEBUG_MODE.equals("1")) {
                LOGGER.setLevel(Level.ALL);
                LOGGER.info(txt);
            } else {
                LOGGER.setLevel(Level.FINE);
                LOGGER.fine(txt);
            }
            fhandler.flush();
            fhandler.close();
        } catch (IOException | SecurityException ex) {
            LOGGER.log(Level.INFO, "Error in log {0}", ex.getMessage());
        }

    }

    private String makeDirectory(String dir, String lFile, String today) {
        String flname = dir + today;
        try {
            boolean success = (new File(flname)).mkdirs();
            if (!success) {
                // Directory creation failed
                //return dir;
            }
        } catch (Exception ex) {

        }

        return flname + "/";

    }
}
