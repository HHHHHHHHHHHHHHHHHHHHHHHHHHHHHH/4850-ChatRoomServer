/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server;

import java.io.IOException;

/**
 *
 * @author dh
 */
public class ChatRoomServer {
    public static void main(String[] args) throws IOException {
        Server server=new Server(14727);
        System.out.println("Waiting for a client to connect...");
        
    }
}
