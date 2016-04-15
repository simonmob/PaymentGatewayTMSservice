/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.MainFiles;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author Collins
 */
public class InstanceChecker implements Runnable {

    @Override
    public void run() {
        OneInstanceRunning();
    }

    public boolean OneInstanceRunning() {
        try {
            ServerSocket Serversock;
            Serversock = new ServerSocket(5658);
            Serversock.accept();
            return true;
        } catch (IOException ex) {
            System.out.println("Another Instance Econnect TMS is Running...");
            System.exit(0); //Kill this instance
            return false;
        }
    }
}
