/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.CardPaymentGateway;

import java.util.ArrayList;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author smaingi
 */
public class Testing {
    
    public static void main(String[] args) {
        String jsonString="{Status=002, StatusMessage=Parameters are Missing, country=kenya, Biller=KPLC,Name=me}";
        //JSONObject jSONObject=new JSONObject(jsonString);
        //System.out.println(jSONObject);
        //JSONObject status=jSONObject.getJSONObject("status");
        String noCurly=jsonString.replaceAll("[{}]", "");
        System.out.println(noCurly);
        
        String[] formatString=noCurly.split(",");
        String response=null;
        
        for(int i=0;i<formatString.length;i++)
        {
            if(formatString[i].contains("status"))
            {
                response+=formatString[i];
            }
        }
        System.out.println(response);
        
        //Map<String, JSONObject> map = (Map<String,JSONObject>)status.getMap();

        //ArrayList<String> list = new ArrayList<String>(map.keySet());

       // System.out.println(list);
    }
    
}
