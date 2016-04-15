package com.CardPaymentGateway;

///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.CardPayment;
////import com.sun.xml.internal.messaging.saaj.util.Base64;
//import com.sun.org.apache.xml.internal.security.utils.Base64;
//import java.io.UnsupportedEncodingException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.json.*;
///**
// *
// * @author smaingi
// */
//public class CardPaymentRequest {
//
////    private static String authentication(java.lang.String tranDetails, java.lang.String authKey) {
////        com.CardPayment.Request service = new com.CardPayment.Request();
////        com.CardPayment.IncomingTransaction port = service.getIncomingTransactionPort();
////        return port.authentication(tranDetails, authKey);
////    }
////    
//    
//    public void sendRequest()
//    {
//        //Auth key Variables
//        String uniqueKey="!Eclectic%IsThe BomB%Limited!??hehehe";
//        String amount="100";
//        String channel = "POS";
//        String cardNo = "5173350005532618";
//        String expiryDate = "122019";
//        String CVC = "654";
//        String refNo = "A234GHJ4567945";
//        String phoneNumber="254708003472";
//        
//        //Transaction Details Json Data
//        JSONObject object=new JSONObject();
//        object.put("Amount", "100");
//        object.put("CardNo", "5173350005532618");
//        object.put("Channel", "POS");
//        object.put("Organization", "Eclectics");
//        object.put("ExpiryDate", "122019");
//        object.put("CVC", "654");
//        object.put("RefNo", "A234GHJ4567945");
//        
//        System.out.println("JSON DATA= "+object);
//        
//        String jsonString=object.toString();
//        Encrypt encrypt=new Encrypt();
//        
//        String testEncrypt=encrypt.EncrypData("Boni was here..");
//        System.out.println("TEST ENCRYPT STRING= "+testEncrypt);
//        
//        String encryptedString= encrypt.EncrypData(jsonString);
//        String tobase64String=Base64.getEncoder().encodeToString(jsonString.getBytes());
//        System.out.println("TO BASE64 STRING= "+tobase64String);
//        
//        //Auth key
//        String authKeyString = uniqueKey+phoneNumber;
//        //base64 of aythKey
//        String base64AuthString=Base64.getEncoder().encodeToString(authKeyString.getBytes());
//        //generate authKey sha512
//        String sha512AuthKey=null;
//        try {
//            
//            MessageDigest md=MessageDigest.getInstance("SHA-512");
//            byte[] hash=md.digest(base64AuthString.getBytes());
//            sha512AuthKey=convertToHex(hash);
//        } catch (NoSuchAlgorithmException ex) {
//            Logger.getLogger(CardPaymentRequest.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println("SHA512 AUTH KEY= "+sha512AuthKey);
//        //send request
//        String response=authentication(tobase64String,sha512AuthKey);
//        System.out.println(response);
//        
//        //decode response from base 64
//       // byte[] fromBase64Bytes=Base64.getDecoder().decode(response.getBytes());
//        //String fromBase64String=new String(fromBase64Bytes);
//        System.out.println("====================");
//        //System.out.println(fromBase64String);
//        
//        //decrypt to get the final response
//       // Decrypt decrypt=new Decrypt();
//       // String decryptedResponse=decrypt.DecrypData(fromBase64String);
//        //System.out.println(decryptedResponse);
//        
//    }
//    
///**
//* Converts the given byte[] to a hex string.
//* @param raw the byte[] to convert
//* @return the string the given byte[] represents
//*/
//private String convertToHex(byte[] raw) {
//    StringBuffer sb = new StringBuffer();
//    for (int i = 0; i < raw.length; i++) {
//        sb.append(Integer.toString((raw[i] & 0xff) + 0x100, 16).substring(1));
//    }
//    return sb.toString();
//}
//
////public static void main(String[] args){
////    CardPaymentRequest request=new CardPaymentRequest();
////    request.sendRequest();
////}
//
//    private static String authentication(java.lang.String tranDetails, java.lang.String authKey) {
//        com.CardPayment.Request service = new com.CardPayment.Request();
//        com.CardPayment.IncomingTransaction port = service.getIncomingTransactionPort();
//        return port.authentication(tranDetails, authKey);
//    }
//}
