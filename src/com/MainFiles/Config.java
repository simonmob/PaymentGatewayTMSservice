/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.MainFiles;

/**
 *
 * @author Collins
 */
public class Config {

    public static String ERROR_DIR = "C:/Logs/";
    public static String COUNT_FILE = "C:/count.txt";

    public static String INITIAL_CONTEXT_FACTORY = "weblogic.jndi.WLInitialContextFactory";
    public static String PROVIDER_URL = "t3://192.168.60.233:7010,192.168.60.233:7010";
    public static String MACHINE1 = "t3://192.168.60.233:7010";
    public static String MACHINE2 = "t3://192.168.60.232:7010";

    public static long flexTimeOut = 40;
    
    public static String ESBWSUrl ="http://192.168.60.233:7010/ESBWSConnector";
}
