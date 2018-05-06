/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dh
 */
public class Server {

    final int MAXCLIENTS = 3;

    public int num=0;
    public ArrayList<String> clientIDList;
    public ArrayList<ConnectionToClient> clientList;
    private LinkedBlockingQueue<Object> messages;
    private ServerSocket serverSocket;

    //private UserList[] userList = new UserList[4];
    private ArrayList<UserList> userList;

    public Server(int port) throws IOException {
        userList = new ArrayList<>();
        clientIDList = new ArrayList<>();
        clientList = new ArrayList<>();
        messages = new LinkedBlockingQueue<>();
        serverSocket = new ServerSocket(port);

        userList.add(new UserList("Tom", "Tom11"));
        userList.add(new UserList("David", "David22"));
        userList.add(new UserList("Beth", "Beth33"));
        userList.add(new UserList("John", "John44"));

        Thread accept = new Thread() {
            @Override
            public void run() {

                while (true) {
                    try {
                        Socket s = serverSocket.accept();
                        clientList.add(new ConnectionToClient(s));
                        
                    } catch (IOException e) {
                    }
                }
            }
        };

        //accept.setDaemon(true);
        accept.start();

        Thread messageHandling = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {

                        Object message = messages.take();
                        // Do some handling here...

                        //System.out.println("Server Received: " + message);

                        String[] buff = stringGuide((String) message);
                        if (buff == null) {
                            System.out.println("Wrong input!");
                            
                        } else {
                            boolean flag = false;
                            
                            String username = buff[buff.length - 2];
                            
                            int userid = clientIDList.indexOf(username);
                            while (userid == -1) {
                                userid = clientIDList.indexOf(username);
                                
                            }

                            if (num >= MAXCLIENTS&&userList.get(userid).askLogin()==false) {
                                sendToOne(userid, "You cannot login because the room can only contain 3 people.");
                                clientIDList.remove(userid);
                                System.out.println(userid+" is removed.");
                            } /////////
                            else if ("login".equals(buff[0])) {

                                for (int i = 0; i < userList.size(); i++) {

                                    if (userList.get(i).askUser(buff[1], buff[2]) == true) {

                                        if(userList.get(i).askLogin() == false){
                                        flag = true;
                                        userList.get(i).setLogin(true);
                                        }
                                        else{
                                            sendToOne(userid, "You have already logged in, don't try to login again, please.");
                                        }

                                    }

                                }

                                if (flag == true) {

                                    sendToOne(userid, "Login success!");
                                    sendToAll(username + " come in.");
                                    num++;
                                } else {
                                    sendToOne(userid, "Login failed, please check you name and password.");
                                    clientIDList.remove(userid);
                                System.out.println(userid+" is removed.");
                                }

                            } //login user pwd
                            else if ("sendall".equals(buff[0])) {
                                for (int i = 0; i < userList.size(); i++) {
                                    if (userList.get(i).askUser(buff[2], buff[3]) && userList.get(i).askLogin() == true) {
                                        flag = true;
                                    }

                                }
                                if (flag == true) {
                                    sendToAll(username + " to everyone :" + buff[1]);
                                    sendToOne(userid, "You send message to all in succeed.");
                                } else {
                                    sendToOne(userid, "You failed to send message to all.");
                                }

                            } //sendall msg name pwd
                            else if ("send".equals(buff[0])) {
                                for (int i = 0; i < userList.size(); i++) {
                                    if (userList.get(i).askUser(buff[3], buff[4]) && userList.get(i).askLogin() == true) {
                                        flag = true;
                                    }

                                }
                                if (flag == true) {
                                    if (clientIDList.indexOf(buff[1]) != -1) {
                                        sendToOne(userid, "You send message in succeed.");
                                        sendToOne(clientIDList.indexOf(buff[1]), username + " : " + buff[2]);
                                    } else {
                                        sendToOne(userid, "There is no this person or it is not in the room.");
                                    }
                                } else {
                                    sendToOne(userid, "You should login first!");
                                }

                            } //send someone msg name pwd
                            else if ("who".equals(buff[0])) {
                                for (int i = 0; i < userList.size(); i++) {
                                    if (userList.get(i).askUser(buff[1], buff[2]) && userList.get(i).askLogin() == true) {
                                        flag = true;
                                    }

                                }
                                if (flag == true) {
                                    String result = "";
                                    for (String object : clientIDList) {
                                        if (!"".equals(object)) {
                                            result += object + ", ";
                                        }
                                    }
                                    sendToOne(userid, "You asked who is in the room.");
                                    sendToOne(userid, result + " is in the room.");
                                } else {
                                    sendToOne(userid, "You should login first!");
                                }

                            } //who name pwd
                            else if ("logout".equals(buff[0])) {
                                for (int i = 0; i < userList.size(); i++) {
                                    if (userList.get(i).askUser(buff[1], buff[2]) && userList.get(i).askLogin() == true) {
                                        flag = true;
                                        userList.get(i).setLogin(false);
                                        
                                    }

                                }
                                if (flag == true) {
                                    sendToOne(userid, "You log out!");
                                    //clientIDList.set(userid, "");
                                    clientIDList.remove(userid);
                                    clientList.remove(userid);
                                System.out.println(userid+" is removed.");
                                    //clientList.remove(userid);
                                    num--;
                                    sendToAll(username + " leave from the room.");

                                } else {
                                    sendToOne(userid, "You should login first!");
                                }

                            } //logout name pwd
                            else if ("newuser".equals(buff[0])) {
                                userList.add(new UserList(buff[1], buff[2]));
                                System.out.println("new user name is "+userList.get(userList.size()-1).name);
                                System.out.println("new user password is "+userList.get(userList.size()-1).password);
                                sendToOne(userid, "Your account is build well!");
                                clientIDList.remove(userid);
                                System.out.println(userid+" is removed.");
                            }
                            //newuser name Password
                            else
                            {
                                System.out.println("Nothing!");
                            }
                        }

                    } catch (InterruptedException e) {
                    }
                }
            }
        };

        //messageHandling.setDaemon(true);
        messageHandling.start();
    }

    private class ConnectionToClient {

        ObjectInputStream in;
        ObjectOutputStream out;
        Socket socket;
        public String name;

        ConnectionToClient(Socket socket) throws IOException {
            this.socket = socket;
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());

            Thread read = new Thread() {

                @Override
                public void run() {
                    while (true) {
                        try {
                            Object obj = in.readObject();
                            messages.put(obj);
                            String[] buff = ((String) obj).split(" ");
                            if (buff != null) {

                                if ("login".equals(buff[0])||"newuser".equals(buff[0])) {
                                    clientIDList.add(buff[buff.length - 2]);
                                    System.out.println(buff[buff.length - 2]+" get his ID-> "+(clientIDList.size()-1));
                                }
                                

                            }
                            else{
                                System.out.println("Useless input: "+(String)obj);
                            }

                        } catch (IOException e) {
                        } catch (ClassNotFoundException | InterruptedException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }

            };

            //read.setDaemon(true); // terminate when main ends
            read.start();

        }

        public void write(Object obj) {
            try {
                out.writeObject(obj);
            } catch (IOException e) {
            }
        }

    }

    private class UserList {

        private String name = "";
        private String password = "";
        private Boolean login = false;

        public UserList(String name, String password) {
            this.name = name;
            this.password = password;
        }

        public void setUser(String name, String password) {

            this.name = name;
            this.password = password;

        }

        public void setLogin(Boolean login) {

            this.login = login;

        }

        public boolean askUser(String name, String password) {

            return this.name.equals(name) && this.password.equals(password);
        }

        public boolean askLogin() {

            return this.login;
        }

    }

    public void sendToOne(int index, Object message) {

        clientList.get(index).write(message);
    }

    public void sendToAll(Object message) {
        clientList.forEach((client) -> {
            client.write(message);
        });
    }

    private String[] stringGuide(String str) {
        String[] buff = str.split(" ");
        switch (buff[0]) {
            case "login":
                if (buff.length != 3) {
                    return null;
                }
                break;
            case "sendall":
                if (buff.length != 4) {
                    return null;
                }
                break;
            case "send":
                if (buff.length != 5) {
                    return null;
                }
                break;
            case "who":
                if (buff.length != 3) {
                    return null;
                }
                break;
            case "logout":
                if (buff.length != 3) {
                    return null;
                }
                break;
            case "newuser":
                if(buff.length != 3) {
                    return null;
                }
                break;
            default:
                return null;

        }
       
        return buff;
    }
}
