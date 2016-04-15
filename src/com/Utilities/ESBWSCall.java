//Create a connection to the ESB Web Service and make a call, sending a request to ESB.
package com.Utilities;

//@author: Joy

import com.MainFiles.Config;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ESBWSCall 
{
    //Open a connection to the ESB Web Service and send request
    public String connectToESBWS(String request) throws MalformedURLException, IOException
    {
        String response;
        
        String StrESBWSUrl = Config.ESBWSUrl;
        URL urlESBWS = new URL(StrESBWSUrl);
        URLConnection conn = urlESBWS.openConnection();
        conn.setDoOutput(true);
        
        try(OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream()))
        {
          //  System.out.println("\n\nRequest=" + request);
            writer.write(request);
            writer.flush();
            
            response = getResponseFromInputStream(conn.getInputStream());
        }
       
        return response;
    }
    
    //Get response from ESB Web Service
    public String getResponseFromInputStream (InputStream is)
    {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        
        try
        {
           br = new BufferedReader(new InputStreamReader(is)); 
           while ((line = br.readLine()) != null) 
           {
                sb.append(line);
           }
        }
        catch(IOException e)
        {
           e.printStackTrace();
        }
        finally
        {
            if (br != null) 
            {
                try 
                {
                    br.close();
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
        
        return sb.toString();
    }
    
    
}
