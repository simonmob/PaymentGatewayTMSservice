package com.Utilities;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Util {

    public Util() {

    }

    public static void printHexString(String hint, byte[] b) {
        System.out.print(hint);
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase() + " ");
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

    public static byte[] HexString2Bytes(String src) {
        byte[] ret = new byte[8];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < 8; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    public static String DESEncrypt(String PIN, String PAN, String key) {

        try {

            ANSIFormat pass = new ANSIFormat(PIN, PAN);

            byte[] b = pass.process(PIN, PAN);

            String xor = Util.Bytes2HexString(b);

            byte[] xorbyte = Util.HexString2Bytes(xor);

            byte[] keybyte = Util.HexString2Bytes(key);

            DESKeySpec dks = new DESKeySpec(keybyte);

            SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
            SecretKey desKey = skf.generateSecret(dks);
            Cipher cipher;

            cipher = Cipher.getInstance("DES"); // DES/ECB/PKCS5Padding for SunJCE//tripledes-DESede
            cipher.init(Cipher.ENCRYPT_MODE, desKey);
            byte[] textEncryptedB = cipher.doFinal(xorbyte);

            // ken leta only 16 chars
            return Util.Bytes2HexString(textEncryptedB).substring(0, 16);

        } catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException |
                NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex) {
            
        }
        return "";
    }
}
