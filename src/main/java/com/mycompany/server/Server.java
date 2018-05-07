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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dh
 */
public final class Server {

    /**
     * Difinition or initializations
     */
    final int MAXCLIENTS = 3;
    public int num = 0;
    public ArrayList<ConnectionToClient> clientList;
    private LinkedBlockingQueue<Object> messages;
    private ServerSocket serverSocket;
    private ArrayList<UserList> userList;

    public Server(int port) throws IOException {
        userList = new ArrayList<>();
        clientList = new ArrayList<>();
        messages = new LinkedBlockingQueue<>();
        serverSocket = new ServerSocket(port);

        /**
         * read user data from a file
         */
        readFile(userList);

        /**
         * accept socket from client(s)
         *
         */
        Thread accept = new Thread() {
            @Override
            public void run() {

                while (true) {
                    try {
                        Socket s = serverSocket.accept();
                        int id = (int) System.currentTimeMillis() % 100;
                        clientList.add(new ConnectionToClient(s, id));

                    } catch (IOException e) {
                    }
                }
            }
        };

        accept.start();

        /**
         * make different actions according to different shells
         *
         */
        Thread messageHandling = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {

                        Object message = messages.take();
                        String[] buff = stringGuide((String) message);
                        if (buff == null) {
                            System.out.println("Wrong input: " + message);//check message style

                        } else {
                            boolean flag = false;

                            /**
                             * get client
                             *
                             *
                             */
                            Integer userid = Integer.parseInt(buff[buff.length - 1]);
                            ConnectionToClient user = getConnect(userid);
                            while (user == null) {
                                System.out.println("user = null");
                                user = getConnect(userid);
                            }
                            /**
                             * Check if there is too many people in the room.
                             *
                             */
                            if (num >= MAXCLIENTS && user.askLogin() == false) {
                                sendToOne(user, "You cannot login because the room can only contain 3 people.");

                            } /**
                             * login
                             */
                            else if ("login".equals(buff[0])) {
                                boolean exist=false;
                                for (int i = 0; i < userList.size(); i++) {

                                    if (userList.get(i).askUser(buff[1], buff[2]) == true) {

                                        for(int j=0;j<clientList.size();j++){
                                            if(clientList.get(j).getName() == null ? buff[1] == null : clientList.get(j).getName().equals(buff[1]))
                                                exist=true;
                                        }
                                        if (exist == false) {
                                            flag = true;
                                            user.setName(buff[1]);
                                            user.setLogin(true);

                                        } else {
                                            flag = true;
                                            sendToOne(user, "You have already logged in, don't try to login again, please.");
                                        }

                                    }

                                }
                                if (flag == true && exist==false) {
                                    System.out.println(buff[1] + " log in.");
                                    sendToOne(user, "Login success!");
                                    sendToAll(buff[1] + " come in.");
                                    num++;
                                } else if(exist!=true){
                                    sendToOne(user, "Login failed, please check you name and password.");

                                }

                            } /**
                             * sendall
                             */
                            else if ("sendall".equals(buff[0])) {
                                for (int i = 0; i < userList.size(); i++) {
                                    if (userList.get(i).askUser(buff[2], buff[3]) && user.askLogin() == true) {
                                        flag = true;
                                    }

                                }
                                if (flag == true) {
                                    System.out.println(user.getName() + " send all: " + buff[1]);
                                    sendToAll(user.getName() + " to everyone :" + buff[1]);
                                    //sendToOne(userid, "You send message to all in succeed.");
                                } else {
                                    sendToOne(user, "You failed to send message to all.");
                                }

                            } /**
                             * send
                             */
                            else if ("send".equals(buff[0])) {
                                ConnectionToClient him = null;
                                for (int i = 0; i < userList.size(); i++) {
                                    if (userList.get(i).askUser(buff[3], buff[4]) && user.askLogin() == true) {
                                        flag = true;
                                    }

                                }
                                for (int i = 0; i < clientList.size(); i++) {
                                    
                                    if (clientList.get(i).getName() == null ? buff[1] == null : clientList.get(i).getName().equals(buff[1])) {
                                        if (clientList.get(i).askLogin() == true) {
                                            him = clientList.get(i);
                                        }
                                    }
                                }
                                if (flag == true) {
                                    if (him != null) {
                                        System.out.println(user.getName() + " send " + him.getName() + ": " + buff[2]);
                                        sendToOne(him, user.getName() + " : " + buff[2]);
                                    } else {
                                        sendToOne(user, "There is no this person or it is not in the room.");
                                    }
                                } else {
                                    sendToOne(user, "You should login first!");
                                }

                            } /**
                             * who
                             */
                            else if ("who".equals(buff[0])) {
                                for (int i = 0; i < userList.size(); i++) {
                                    if (userList.get(i).askUser(buff[1], buff[2]) && user.askLogin() == true) {
                                        flag = true;
                                    }

                                }
                                if (flag == true) {
                                    String result = "";
                                    for (int i = 0; i < clientList.size(); i++) {
                                        if (clientList.get(i).askLogin() == true) {
                                            result += clientList.get(i).getName() + "; ";
                                        }
                                    }
                                    System.out.println(user.getName() + " ask who is in this room.");
                                    sendToOne(user, "People in this room: " + result);
                                } else {
                                    sendToOne(user, "You should login first!");
                                }

                            } /**
                             * logout
                             *
                             */
                            else if ("logout".equals(buff[0])) {
                                for (int i = 0; i < userList.size(); i++) {
                                    if (userList.get(i).askUser(buff[1], buff[2]) && user.askLogin() == true) {
                                        flag = true;
                                        user.setLogin(false);

                                    }

                                }
                                if (flag == true) {
                                    sendToOne(user, "You log out!");
                                    num--;
                                    sendToAll(user.getName() + " leave from the room.");
                                    System.out.println(user.getName() + " log out.");
                                    user.cleanName();
                                    user.setLogin(false);

                                } else {
                                    sendToOne(user, "You should login first!");
                                }

                            } /**
                             * newuser
                             */
                            else if ("newuser".equals(buff[0])) {
                                userList.add(new UserList(buff[1], buff[2]));
                                writeFile(buff[1], buff[2]);
                                sendToOne(user, "Your account is build well!");
                                System.out.println("An account with username '" + buff[1] + "' is created.");
                            } else {
                                System.out.println("Nothing!");
                            }
                        }

                    } catch (InterruptedException e) {
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };

        messageHandling.start();
    }

    public ConnectionToClient getConnect(Integer id) {
        for (int i = 0; i < clientList.size(); i++) {
            if (Objects.equals(clientList.get(i).id, id)) {
                return clientList.get(i);
            }
        }

        return null;
    }

    /**
     * The class to connect to client.
     */
    private class ConnectionToClient {

        ObjectInputStream in;
        ObjectOutputStream out;
        Socket socket;
        String name;
        Integer id;
        Boolean login;

        public void cleanName(){
            this.name=null;
            this.login=false;
        }
        public void setLogin(boolean login) {
            this.login = login;
        }

        public boolean askLogin() {
            return this.login;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        ConnectionToClient(Socket socket, int id) throws IOException {

            this.socket = socket;
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            login = false;
            name = null;
            this.id = id;
            Thread read = new Thread() {

                @Override
                public void run() {
                    while (true) {
                        try {
                            Object obj = in.readObject();

                            String[] buff = ((String) obj).split(" ");
                            if (buff != null) {
                                //add id to the shell

                                obj = (String) obj + " " + id;
                                messages.put(obj);
                            } else {
                                System.out.println("Useless input: " + (String) obj);
                            }

                        } catch (IOException e) {
                        } catch (ClassNotFoundException | InterruptedException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }

            };

            read.start();

        }

        /**
         * write sth
         *
         * @param obj
         */
        public void write(Object obj) {
            try {
                out.writeObject(obj);
            } catch (IOException e) {
            }
        }

    }

    /**
     * The class to opreate on userdata
     *
     */
    private class UserList {

        private String name = "";
        private String password = "";

        public UserList(String name, String password) {
            this.name = name;
            this.password = password;
        }

        /**
         * set value into the user data
         *
         * @param name
         * @param password
         */
        public void setUser(String name, String password) {

            this.name = name;
            this.password = password;

        }

        /**
         * to know if the name with password is a right pair
         *
         * @param name
         * @param password
         * @return
         */
        public boolean askUser(String name, String password) {

            return this.name.equals(name) && this.password.equals(password);
        }

    }

    /**
     * send a message to someone
     *
     * @param message
     */
    public void sendToOne(ConnectionToClient user, Object message) {

        user.write(message);
    }

    /**
     * send the same message to every one
     *
     * @param message
     */
    public void sendToAll(Object message) {
        clientList.forEach((client) -> {
            client.write(message);
        });
    }

    /**
     * write the newuser info. to the user file
     *
     * @param name
     * @param password
     * @throws IOException
     */
    public void writeFile(String name, String password) throws IOException {

        try {
            try (
                    FileWriter writer = new FileWriter("./user.txt", true)) {
                writer.write(name + " " + password + "\n");
            }
        } catch (IOException e) {
        }

    }

    /**
     * read user lists from the file
     *
     * @param userLists
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void readFile(ArrayList<UserList> userLists) throws FileNotFoundException, IOException {
        String pathname = "./user.txt";
        File filename = new File(pathname);
        InputStreamReader reader = new InputStreamReader(
                new FileInputStream(filename));
        BufferedReader br = new BufferedReader(reader);
        String line = null;
        UserList ul;
        while (true) {
            line = br.readLine();
            if (!"".equals(line) && line != null) {
                String[] buff = line.split(" ");
                ul = new UserList(buff[0], buff[1]);
                userLists.add(ul);

            } else {
                break;
            }
        }

    }

    /**
     * to know wether a shell is legal for the program
     *
     * @param str
     * @return
     */
    private String[] stringGuide(String str) {
        String[] buff = str.split(" ");
        switch (buff[0]) {
            case "login":
                if (buff.length != 4) {
                    return null;
                }
                break;
            case "sendall":
                if (buff.length != 5) {
                    return null;
                }
                break;
            case "send":
                if (buff.length != 6) {
                    return null;
                }
                break;
            case "who":
                if (buff.length != 4) {
                    return null;
                }
                break;
            case "logout":
                if (buff.length != 4) {
                    return null;
                }
                break;
            case "newuser":
                if (buff.length != 4) {
                    return null;
                }
                break;
            default:
                return null;

        }

        return buff;
    }

}
