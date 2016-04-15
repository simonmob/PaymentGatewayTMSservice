/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Utilities;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author ken
 */
public class PinBlock {

    public String pinProcess(String PIN, String PAN, String key) {

        try {
            Security.addProvider(new BouncyCastleProvider());

//         String PIN = "1007";
//        String PAN = "6396730416041961";
//        String key = "26EA89DCA810CB8CBC19BA4C26C7943426EA89DCA810CB8C";
//
            PinBlock p = new PinBlock();

            byte[] plain = p.processPinPan(PIN, PAN);

            byte[] keyBytes = HexString2Bytes(key);

            SecretKey keySpec = new SecretKeySpec(keyBytes, "DESede");

            IvParameterSpec iv = new IvParameterSpec(new byte[8]);

            Cipher e_cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding", "BC");

            e_cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

            byte[] cipherText = e_cipher.doFinal(plain);
          //  System.out.println("Ciphertext: " + Bytes2HexString(cipherText).substring(0, 16));
            return Bytes2HexString(cipherText).substring(0, 16);
        } // end main
        catch (InvalidKeyException ex) {
            Logger.getLogger(PinBlock.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(PinBlock.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(PinBlock.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(PinBlock.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PinBlock.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(PinBlock.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(PinBlock.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    public static void printHexString(String hint, byte[] b) {
       // System.out.print(hint);
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
          //  System.out.print(hex.toUpperCase() + " ");
        }
        System.out.println("");
    }

    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{
            src0
        }))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{
            src1
        }))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    // ken  24 bytes for tripleDES change to 8 for DES
    public static byte[] HexString2Bytes(String src) {

        byte[] ret = new byte[24];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < 24; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    public byte[] processPinPan(String pin, String pan) {

        byte arrPAN[] = getHexPAN(pan);
        byte arrPin[] = getHexPin(pin);
        byte arrRet[] = new byte[8];
        for (int i = 0; i < 8; i++) {
            arrRet[i] = (byte) (arrPin[i] ^ arrPAN[i]);
        }
        //    printHexString("Clear PIN Block：", arrRet);
        return arrRet;
    }

    private byte[] getHexPin(String pin) {
        byte arrPin[] = pin.getBytes();
        byte encode[] = new byte[8];

        encode[0] = (byte) 0x04;
        encode[1] = (byte) uniteBytes(arrPin[0], arrPin[1]);
        encode[2] = (byte) uniteBytes(arrPin[2], arrPin[3]);
        encode[3] = (byte) 0xFF;
        encode[4] = (byte) 0xFF;
        encode[5] = (byte) 0xFF;
        encode[6] = (byte) 0xFF;
        encode[7] = (byte) 0xFF;
        printHexString("encoded pin：", encode);
        return encode;
    }

    private byte[] getHexPAN(String pan) {

        int len = pan.length();
        byte arrTemp[] = pan.substring(len < 13 ? 0 : len - 13, len - 1).getBytes();
        byte arrAccno[] = new byte[12];
        for (int i = 0; i < 12; i++) {
            arrAccno[i] = (i <= arrTemp.length ? arrTemp[i] : (byte) 0x00);
        }
        byte encode[] = new byte[8];
        encode[0] = (byte) 0x00;
        encode[1] = (byte) 0x00;
        encode[2] = (byte) uniteBytes(arrAccno[0], arrAccno[1]);
        encode[3] = (byte) uniteBytes(arrAccno[2], arrAccno[3]);
        encode[4] = (byte) uniteBytes(arrAccno[4], arrAccno[5]);
        encode[5] = (byte) uniteBytes(arrAccno[6], arrAccno[7]);
        encode[6] = (byte) uniteBytes(arrAccno[8], arrAccno[9]);
        encode[7] = (byte) uniteBytes(arrAccno[10], arrAccno[11]);
        printHexString("encoded pan：", encode);
        return encode;
    }
}
