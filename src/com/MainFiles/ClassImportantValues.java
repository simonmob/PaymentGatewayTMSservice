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
import com.Logger.Logging;
import static com.MainFiles.Functions.StackTraceWriter;
import com.Utilities.EsbFormatter;
import java.util.Date;
import java.awt.*;
import java.io.FileInputStream;
import java.util.Properties;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassImportantValues {

    Properties prop = new Properties();
    Logging logging = new Logging();

    //Load database connectionsew
    public void CheckDirectoryExist(String foldername) {
        File theDir = new File(foldername);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            boolean result = false;
            try {
                theDir.mkdir();
                result = true;
                System.out.println(" \nLOG DIRECTORY CREATED " + foldername);
            } catch (SecurityException se) {
                //handle it
            }
        }
    }

    public void logs(String filename, String content) {
        logging.log(content, filename);
    }

}
