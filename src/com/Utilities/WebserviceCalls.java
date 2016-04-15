/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Utilities;

import com.MainFiles.Functions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author HESBON
 */
public class WebserviceCalls {

    Functions func = new Functions();

    public String CURLRequest(String data, int... timeout) throws MalformedURLException, IOException {
        String result = "";
        String StrUrl = "http://192.168.114.212:8010/ESBWSConnector";
        URL url = new URL(StrUrl);

        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        if (timeout.length > 0) {
            //conn.setReadTimeout(timeout[0]);
            conn.setConnectTimeout(timeout[0] * 1000);
            conn.setReadTimeout(timeout[0] * 1000);
        }
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        System.out.println("\n\ndata=" + data);
        writer.write(data);
        writer.flush();
        result = getStringFromInputStream(conn.getInputStream());
        System.out.println("\n\nresult=" + result);

//        } catch (Exception ex) {
//            StringWriter sw = new StringWriter();
//            ex.printStackTrace(new PrintWriter(sw));
//            ESBLog el = new ESBLog(sw.toString());
//            el.log();}
        return result;
    }

    public String SendSMSRequest(String data) throws MalformedURLException, IOException {
        String result = "";
        func.log(data, "smsout");

        String StrUrl = "http://192.168.60.232:7010/ESBSmsAPI/ESBSmsAPI";
        URL url = new URL(StrUrl);

        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        //System.out.println("\n\ndata=" + data);
        writer.write(data);
        writer.flush();
        result = getStringFromInputStream(conn.getInputStream());
        //System.out.println("\n\nresult=" + result);
        func.log(result, "smsin");
//        } catch (Exception ex) {
//            StringWriter sw = new StringWriter();
//            ex.printStackTrace(new PrintWriter(sw));
//            ESBLog el = new ESBLog(sw.toString());
//            el.log();}
        return result;
    }

    public String SendEmailRequest(String data) throws MalformedURLException, IOException {
        String result = "";
        ///try {
        func.log(data, "EmailOut");

        String StrUrl = "http://192.168.60.232:7010/ESBEmailAPI/ESBEmailAPI";
        URL url = new URL(StrUrl);

        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        //System.out.println("\n\ndata=" + data);
        writer.write(data);
        writer.flush();
        result = getStringFromInputStream(conn.getInputStream());
        //System.out.println("\n\nresult=" + result);
        func.log(result, "emailin");
//        } catch (Exception ex) {
//            StringWriter sw = new StringWriter();
//            ex.printStackTrace(new PrintWriter(sw));
//            ESBLog el = new ESBLog(sw.toString());
//            el.log();}
        return result;
    }

    private String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

}
