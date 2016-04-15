package com.Utilities;;

import java.io.ObjectInputStream.GetField;
import javax.annotation.processing.Processor;

public class ANSIFormat {

    private String pin;
    private String pan;

    public ANSIFormat(String pin, String pan) {
        this.pin = pin;
        this.pan = pan;
    }

    public byte[] process(String pin, String pan) {
        
        byte arrAccno[] = getHAccno(pan);
        byte arrPin[] = getHPin(pin);
        byte arrRet[] = new byte[8];
        for (int i = 0; i < 8; i++) {
            arrRet[i] = (byte) (arrPin[i] ^ arrAccno[i]);
        }
    //    Util.printHexString("Clear PIN Block：", arrRet);
        return arrRet;
    }

    private byte[] getHPin(String pin) {
        byte arrPin[] = pin.getBytes();
        byte encode[] = new byte[8];
        
      
        encode[0] = (byte) 0x04;
        encode[1] = (byte) Util.uniteBytes(arrPin[0], arrPin[1]);
        encode[2] = (byte) Util.uniteBytes(arrPin[2], arrPin[3]);
        encode[3] = (byte) 0xFF ;
        encode[4] = (byte) 0xFF;
        encode[5] = (byte) 0xFF;
        encode[6] = (byte) 0xFF;
        encode[7] = (byte) 0xFF;
       // Util.printHexString("encoded pin：", encode);
        return encode;
    }

    private byte[] getHAccno(String pan) {

        int len = pan.length();
        byte arrTemp[] = pan.substring(len < 13 ? 0 : len - 13, len - 1).getBytes();
        byte arrAccno[] = new byte[12];
        for (int i = 0; i < 12; i++) {
            arrAccno[i] = (i <= arrTemp.length ? arrTemp[i] : (byte) 0x00);
        }
        byte encode[] = new byte[8];
        encode[0] = (byte) 0x00;
        encode[1] = (byte) 0x00;
        encode[2] = (byte) Util.uniteBytes(arrAccno[0], arrAccno[1]);
        encode[3] = (byte) Util.uniteBytes(arrAccno[2], arrAccno[3]);
        encode[4] = (byte) Util.uniteBytes(arrAccno[4], arrAccno[5]);
        encode[5] = (byte) Util.uniteBytes(arrAccno[6], arrAccno[7]);
        encode[6] = (byte) Util.uniteBytes(arrAccno[8], arrAccno[9]);
        encode[7] = (byte) Util.uniteBytes(arrAccno[10], arrAccno[11]);
      //  Util.printHexString("encoded pan：", encode);
        return encode;
    }

}
