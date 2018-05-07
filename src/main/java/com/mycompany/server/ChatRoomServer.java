/**
 * Name: Ding Hao
 * Date: 5/1/2018
 * Description:
 *     It is the server part of the ChatRoom Program. 
 * Help: 
 *     newuser username userpassword: create a new account
 * 
 *     login username userpassword: login an account with username and password
 * 
 *     send someone message: send message to someone
 * 
 *     send all: send everyone a message
 * 
 *     who: show you who are in this chatroom
 * 
 *     logout: logout from the account and close the client
 * 
 */
package com.mycompany.server;

import java.io.IOException;

/**
 *
 * @author dh
 */
public class ChatRoomServer {

    public static void main(String[] args) throws IOException {
        Server server = new Server(14727);
        System.out.println("Waiting for a client to connect...");

    }
}
