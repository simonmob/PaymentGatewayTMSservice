/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.MainFiles;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author Collins
 */
public class DataConversions {

    static private final char[] HEX_DIGITS = new char[]{
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String asciiToHex(String asciiValue) {
        char[] chars = asciiValue.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {

            hex.append(Integer.toHexString((int) chars[i]));

            if (hex.length() < 2) {
                hex.insert(0, '0'); // pad with leading zero if needed
            }

        }

        return hex.toString();
    }

    public String IntegertoASCII(int value) {
        int length = 4;
        StringBuilder builder = new StringBuilder(length);
        for (int i = length - 1; i >= 0; i--) {
            builder.append((char) ((value >> (8 * i)) & 0xFF));
        }
        return builder.toString();
    }

    public static String hexToASCII(String hexValue) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2) {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();

    }

    public static String asciiToString(String asciiValue) {
        char enc;
        String encmsg = "";
        String msg = asciiValue;
        int len = msg.length();
        for (int i = 0; i < len; i++) {
            char cur = msg.charAt(i);
            int val = (int) cur;
            val = val - 30;
            enc = (char) val;
            encmsg = encmsg + enc;
        }
        return encmsg;
    }

    public String AsciiToHEX(String ascii) {
        try {
            String result = "";
            //byte[] encoded = ascii.getBytes("ISO-8859-15");
            byte[] encoded = ascii.getBytes("ISO_8859_1");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < encoded.length; i++) {
                byte b = encoded[i];
                 // instead of the two lines below you could write: String.format("%02X", b)
                // but that would probably be slower
                sb.append(intToHexDigit((b >> 4) & 0xF));
                sb.append(intToHexDigit(b & 0xF));
                 //sb.append(String.format("%02X", b));

            }
            result = sb.toString();
            return result;
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    static private char intToHexDigit(int b) {

        assert b >= 0 && b < 16;
        return HEX_DIGITS[b];
    }

//   private String hextoAscii(String hex) throws UnsupportedEncodingException {
//		if (0 != (hex.length() & 1))
//			throw new BadInputStringException("The hex string must contain even number of digits!");
//		int encoded_len = hex.length() / 2;
//		byte[] encoded = new byte[encoded_len];
// 
//		for (int i=0; i<encoded_len; i++) {
//			encoded[i] = (byte)((hexDigitToInt(hex.charAt(i*2)) << 4) | hexDigitToInt(hex.charAt(i*2+1))); 
//		}
//		return new String(encoded, ENCODING);
//	}
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return hexToBinary(sb.toString());
    }

    public static String asciiToBinary(String asciiString) {

        try {
            byte[] bytes = asciiString.getBytes("ISO_8859_1");
            StringBuilder binary = new StringBuilder();
            for (byte b : bytes) {
                int val = b;
                for (int i = 0; i < 8; i++) {
                    binary.append((val & 128) == 0 ? 0 : 1);
                    val <<= 1;
                }
                // binary.append(' ');
            }
            return binary.toString();
        } catch (UnsupportedEncodingException ex) {

        }
        return null;
    }

    public static String binaryToASCII(String binaryValue) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < binaryValue.length(); i += 8) {
            result.append((char) Integer.parseInt(binaryValue.substring(i, i + 8), 2));
        }
        return result.toString();
    }

    public static String Hex2Ascii(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static String convertHexToString(String hex) {

        String ascii = "";
        String str;

        // Convert hex string to "even" length
        int rmd, length;
        length = hex.length();
        rmd = length % 2;
        if (rmd == 1) {
            hex = "0" + hex;
        }

        // split into two characters
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //split the hex into pairs
            String pair = hex.substring(i, (i + 2));
            //convert hex to decimal
            int dec = Integer.parseInt(pair, 16);
            str = CheckCode(dec);
            ascii = ascii + " " + str;
        }
        return ascii;
    }

    public static String CheckCode(int dec) {
        String str;

        //convert the decimal to character
        str = Character.toString((char) dec);

//             if(dec<32 || dec>126 && dec<161)
//                    str="n/a";  
        return str;
    }

    public static String hexToBinary(String hexValue) {
        int b = Integer.parseInt(hexValue, 16);
        System.out.println(b);
        return Integer.toBinaryString(b);
    }

    public static String PadZeros(int width, String value) {
        //return String.format("%0"+width+"d", value); //It doent seem to work
        String z = "";
        for (int i = 0; i < width - value.length(); i++) {
            z += "0";
        }
        return z + value;
    }

    public static String PadSpaces(int width, String value) {
        //return String.format("%0"+width+"d", value); //It doent seem to work
        String z = "";
        for (int i = 0; i < width - value.length(); i++) {
            z += " ";
        }
        return z + value;
    }

    public static String LeadingZeros(int width, String value) {
        //return String.format("%0"+width+"d", value); //It doent seem to work
        String z = "";
        for (int i = 0; i < width - value.length(); i++) {
            z += "0";
        }
        return value + z;
    }

}

class DataEncoding {

    public static final String ASC = "ASC";
    public static final String DEC = "DEC";
    public static final String HEX = "HEX";
}
