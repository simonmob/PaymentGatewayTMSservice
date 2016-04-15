/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Logger;

import static com.EconnectTMS.EconnectTMSservice.Logsdataqueue;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.SocketHandler;

public class Logging {

    public String StackTraceWriter(Exception exception) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        exception.printStackTrace(printWriter);
        String s = writer.toString();
        return s;
    }

    public boolean log(String txt, String lFile) {
        Map<String, String> onerequest = new HashMap();
        onerequest.put("lFile", lFile);
        onerequest.put("txt", txt);
        Logsdataqueue.offer(onerequest);
        return true;
    } // end fn log

}
