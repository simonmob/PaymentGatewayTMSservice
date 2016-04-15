/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.CardPaymentGateway;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
//import sun.misc.BASE64Decoder;
import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Decoder;

/**
 *
 * @author smaingi
 */
public class Decrypt {
    Cipher cipher; 
 
    // Input encrypted String
    //static  String input = "fFDVvIphyRA+4gUWgYdxSg==";
 
   // password to decrypt 16 bit
    final static String strPassword = "!@#$%^&*()~_+}{?";
       // put this as key in AES
   static SecretKeySpec key = new SecretKeySpec(strPassword.getBytes(), "AES");
   
     public String DecrypData(String MyDecrypData){
         String DecryptedData ="";
              try {
                  AlgorithmParameterSpec paramSpec = new IvParameterSpec(strPassword.getBytes());
                  //Whatever you want to encrypt/decrypt using AES /CBC padding
                  Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                  
                  //You can use ENCRYPT_MODE or DECRYPT_MODE
                  cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
                  
                  //decode data using standard decoder
                  byte[] output = new BASE64Decoder().decodeBuffer(MyDecrypData);
                  
                  // Decrypt the data
                  byte[] decrypted = cipher.doFinal(output);
                  
//                  System.out.println("Original string: " +
//                          new String(input));
//                  
                  // decryptedData .;
                  //System.out.println("Decrypted string: " + new String(decrypted));
                  DecryptedData = new String(decrypted);
                  
              } catch (NoSuchAlgorithmException ex) {
                  Logger.getLogger(Decrypt.class.getName()).log(Level.SEVERE, null, ex);
              } catch (NoSuchPaddingException ex) {
                  Logger.getLogger(Decrypt.class.getName()).log(Level.SEVERE, null, ex);
              } catch (InvalidKeyException ex) {
                  Logger.getLogger(Decrypt.class.getName()).log(Level.SEVERE, null, ex);
              } catch (InvalidAlgorithmParameterException ex) {
                  Logger.getLogger(Decrypt.class.getName()).log(Level.SEVERE, null, ex);
              } catch (IllegalBlockSizeException ex) {
                  Logger.getLogger(Decrypt.class.getName()).log(Level.SEVERE, null, ex);
              } catch (BadPaddingException ex) {
                  Logger.getLogger(Decrypt.class.getName()).log(Level.SEVERE, null, ex);
              } catch (IOException ex) {
            Logger.getLogger(Decrypt.class.getName()).log(Level.SEVERE, null, ex);
        }
         return DecryptedData;
     }
}
