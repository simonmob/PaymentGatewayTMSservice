/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Utilities;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Collins
 */
public class XMLParser {

    public String WriteXML(HashMap<String, String> fields) {
        String strXML = "";
        strXML = "<?xml version= '1.0'   encoding= 'utf-8'?>"
                + "<message>"
                + "<isomsg direction='" + fields.get("direction") + "'>";
        fields.remove("direction");
        // loop thru the hashmap tp get the values and map to the new keys
        try {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                String id = entry.getKey();
                String value = entry.getValue();
                strXML += "<field id='" + id + "' value='" + value + "' />";
            }

            strXML += " </isomsg></message>";
         //   System.out.println(strXML);
        } catch (Exception e) {
            System.out.println("Error in xmlToEC" + e);
            return null;
        }

        return strXML;
    }

    public HashMap ParseXml(String xmlString) {
        HashMap<String, String> fields = new HashMap<>();
        try {
            DocumentBuilderFactory dbf
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));

            Document doc = db.parse(is);
            NodeList nodes = doc.getElementsByTagName("message");

            for (int k = 0; k < nodes.getLength(); k++) {

                NodeList nodelt = doc.getElementsByTagName("field");

                // iterate the nodes
                for (int i = 0; i < nodelt.getLength(); i++) {
                    Element m = (Element) nodelt.item(i);
                    String id = m.getAttribute("id");
                    String value = m.getAttribute("value");

                    // populate the hashmap
                    fields.put(id, value);

                    //System.out.println(" id : " + id);
                    //System.out.println(" value : " + value);
                } // end for i=0, nodelt
            } // end for k=0 ,nodes
        } catch (Exception ex) {
        }
        return fields;
    }

    private HashMap ReadXML(String xmlString) {
        final HashMap<String, String> FieldsMap = new HashMap();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            DefaultHandler handler = new DefaultHandler() {
                public void startElement(String uri, String localName, String qName,
                        Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase("field")) {
                        FieldsMap.put(attributes.getValue(0), attributes.getValue(1));
                        //System.out.println(attributes.getValue(0) + " : " + attributes.getValue(1));
                    }
                }
            };

            //saxParser.parse(new InputSource(new StringReader(xmlString)), handler);
            //System.out.println(xmlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FieldsMap;
    }
}
