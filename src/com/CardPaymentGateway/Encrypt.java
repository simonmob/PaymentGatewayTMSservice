/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.CardPaymentGateway;

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
import sun.misc.BASE64Encoder;
/**
 *
 * @author smaingi
 */
public class Encrypt {
    Cipher cipher; 
  // Input encrypted String
  //static  String input = "Boni was here..";
  // password for encryption
  final static String strPassword = "!@#$%^&*()~_+}{?";  //password12345678
  // put this as key in AES
  static SecretKeySpec key = new SecretKeySpec(strPassword.getBytes(), "AES");
  
  public String EncrypData(String MyEncrytionData){
      String EncryptedData ="";
            try {
                // Parameter specific algorithm
                AlgorithmParameterSpec paramSpec = new     IvParameterSpec(strPassword.getBytes());
                //Whatever you want to encrypt/decrypt
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                
                // You can use ENCRYPT_MODE (ENCRYPTunderscoreMODE)  or DECRYPT_MODE (DECRYPT underscore MODE)
                cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
                
                // encrypt data
                byte[] ecrypted = cipher.doFinal(MyEncrytionData.getBytes());
                
                // encode data using standard encoder
                 EncryptedData = new BASE64Encoder().encode(ecrypted);
                
                //System.out.println("Orginal tring: " + input);
                //System.out.println("Encripted string: " + output);
              
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Encrypt.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchPaddingException ex) {
                Logger.getLogger(Encrypt.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeyException ex) {
                Logger.getLogger(Encrypt.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidAlgorithmParameterException ex) {
                Logger.getLogger(Encrypt.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalBlockSizeException ex) {
                Logger.getLogger(Encrypt.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BadPaddingException ex) {
                Logger.getLogger(Encrypt.class.getName()).log(Level.SEVERE, null, ex);
            }
        return EncryptedData;
  }
}
