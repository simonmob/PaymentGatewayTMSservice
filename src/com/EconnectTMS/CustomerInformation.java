/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EconnectTMS;

import com.Database.DatabaseConnections;
import com.MainFiles.ClassImportantValues;
import com.MainFiles.DataConversions;
import com.MainFiles.Functions;
import static com.MainFiles.ISO8583Adaptor.CUSTOMER_DETAILS_PASSWORD;
import static com.MainFiles.ISO8583Adaptor.CUSTOMER_DETAILS_SOURCE_ID;
import static com.MainFiles.ISO8583Adaptor.CUSTOMER_DETAILS_URL;
import static com.MainFiles.ISO8583Adaptor.CUSTOMER_DETAILS_USERNAME;
import com.Utilities.XMLParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author Collins
 */
public class CustomerInformation {

    DatabaseConnections connections = new DatabaseConnections();
    ClassImportantValues cl = new ClassImportantValues();
    Functions func = new Functions();
    XMLParser parser = new XMLParser();
    DataConversions convert = new DataConversions();

    String[] strCustomerNameArray;
    String strCustomerName = "";
    String strRecievedData[];
    String AccountNumber = "";
    String urlParameters = "";
    String result = "";
    String line;
    StringBuilder postData;
    URLConnection conn;
    String processingcode = "";
    String fname = "";
    String mname = "";
    String lname = "";

    public void Run(String IncomingMessage, String intid) throws MalformedURLException, UnsupportedEncodingException, IOException {

        try {
            strRecievedData = IncomingMessage.split("#");
            processingcode = strRecievedData[0];
            switch (processingcode) {
                case "800000":
                    AccountNumber = strRecievedData[1];
                    URL url = new URL(CUSTOMER_DETAILS_URL);
                    Map<String, String> params = new LinkedHashMap<>();

                    params.put("username", CUSTOMER_DETAILS_USERNAME);
                    params.put("password", CUSTOMER_DETAILS_PASSWORD);
                    params.put("source", CUSTOMER_DETAILS_SOURCE_ID);
                    params.put("account", AccountNumber);

                    postData = new StringBuilder();
                    for (Map.Entry<String, String> param : params.entrySet()) {
                        if (postData.length() != 0) {
                            postData.append('&');
                        }
                        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                        postData.append('=');
                        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                    }
                    urlParameters = postData.toString();
                    conn = url.openConnection();
                    conn.setDoOutput(true);

                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(urlParameters);
                    writer.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    while ((line = reader.readLine()) != null) {
                        result += line;
                    }
                    writer.close();
                    reader.close();

                    JSONObject respobj = new JSONObject(result);
                    if (respobj.has("FSTNAME") || respobj.has("MIDNAME") || respobj.has("LSTNAME")) {
                        if (respobj.has("FSTNAME")) {
                            fname = respobj.get("FSTNAME").toString().toUpperCase() + ' ';
                        }
                        if (respobj.has("MIDNAME")) {
                            mname = respobj.get("MIDNAME").toString().toUpperCase() + ' ';
                        }
                        if (respobj.has("LSTNAME")) {
                            lname = respobj.get("LSTNAME").toString().toUpperCase() + ' ';
                        }
                        strCustomerName = fname + mname + lname;
                    } else {
                        strCustomerName = " ";
                    }
                    System.out.println("result:" + result);
                    func.SendPOSResponse(strCustomerName + "#", intid);
                    return;
                default:
                    break;
            }
        } catch (Exception ex) {
            strCustomerName = "";
            func.log("\nSEVERE CustomerInformation() :: " + ex.getMessage() + "\n" + func.StackTraceWriter(ex), "ERROR");
        }
    }
}
