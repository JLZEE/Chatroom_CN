// Server code
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;

public class Server {   //================================================== MAIN CLASS ==========================================
    ServerSocket ss = null;
    List<getClient> clientUsrs = new ArrayList<getClient>();
    // save client name and thread
    Map<String, getClient> clientMap = Collections.synchronizedMap(new HashMap<String, getClient>());
    // save client name and login time
    Map<String, Long> clientLogInTimeMap = Collections.synchronizedMap(new HashMap<String, Long>());
    // record the active time
    Map<String, Long> clientActiveTimeMap = Collections.synchronizedMap(new HashMap<String, Long>());
    // record the block time
    Map<String, Long> clientBlockTimeMap = Collections.synchronizedMap(new HashMap<String, Long>());
    boolean srvrOnline = true;
    static int portNum = 2222;  // default port number

    public static void main(String[] args) {    //-------------------------- main function ---------------------------------------
        Server svr = new Server();
        if (args.length > 0) {
            portNum = Integer.parseInt(args[0]);
        }
        svr.startServer();
    }

    public void startServer() { //------------------------------------------- start server function ------------------------------
        try {
            ss = new ServerSocket(portNum);
            Socket clientSocket = null;

            //set up log in
            logIn logInSetUp = new logIn();

            // new time monitor class
            timeMonitor tm = new timeMonitor();

            // start time monitor thread
            new Thread(tm).start();

            // new server monitor class
            svrMonitor sm = new svrMonitor();

            // start server monitor thread
            new Thread(sm).start();

            while (true) {  //----------------------------------------------- start accepting clients ---------------------------
                clientSocket = ss.accept();
                System.out.println("Client connected");
                getClient gc = new getClient(clientSocket);
                //logInClient gc = new logInClient(clientSocket);     // logInClient is client trying to log in           

                new Thread(gc).start();
                //clientUsrs.add(gc);
            }
        } catch (BindException e) {
            System.out.println("Port is used");
        } catch (IOException e) {
            System.out.println("Error");
            e.printStackTrace();
        } finally {
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // this class is for server to use. Chack all those lists
    class svrMonitor implements Runnable {  //=============================== INNER CLASS svrMonitor =============================
        public void showClients() {
            try {
                Iterator iter = clientMap.keySet().iterator();
                StringBuffer showCltStr = new StringBuffer("Online users: ");
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    showCltStr.append(key);
                    showCltStr.append(" ");
                }
                System.out.println(showCltStr.toString());
            } catch (Exception e) {
                System.out.println("showClients fail");
            }
        }
        
        public void showUserTime(String mode) {
            Map<String, Long> getMap = null;
            String headMsg = null;
            if (mode.equals("logInTime")) {
                getMap = clientLogInTimeMap;
                headMsg = "Login time: \n";
            } else if (mode.equals("blockTime")) {
                getMap = clientBlockTimeMap;
                headMsg = "Block time: \n";
            } else if (mode.equals("activeTime")) {
                getMap = clientActiveTimeMap;
                headMsg = "Active time: \n";
            } 
            /*
            switch (mode) {
                case ("logInTime"):
                    getMap = clientLogInTimeMap;
                    headMsg = "Login time: \n";
                    break;
                case ("blockTime"):
                    getMap = clientBlockTimeMap;
                    headMsg = "Block time: \n";
                    break;
                case ("activeTime"):
                    getMap = clientActiveTimeMap;
                    headMsg = "Active time: \n";
                    break;
                default:
                    break;
            }
            */
            try {
                Iterator iter = getMap.keySet().iterator();
                StringBuffer showLgTmStr = new StringBuffer(headMsg);
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    long timeVal = getMap.get(key);
                    SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    showLgTmStr.append(key);
                    showLgTmStr.append(": ");
                    showLgTmStr.append(fm.format(timeVal));
                    showLgTmStr.append("\n");
                }
                System.out.println(showLgTmStr.toString());
            } catch (Exception e) {
                System.out.println("showLogInTime fail");
            }
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String writeStr = "no input";
                while (srvrOnline) {
                    writeStr = br.readLine().toString();
                    if (writeStr.equals("1")) {
                        this.showClients();
                    } else if (writeStr.equals("2")) {
                        this.showUserTime("logInTime");
                    } else if (writeStr.equals("3")) {
                        this.showUserTime("blockTime");
                    } else if (writeStr.equals("4")) {
                        this.showUserTime("activeTime");
                    } else {
                        System.out.println("Command:\n'1': show online clients" + 
                            "\n'2': show clients log in time\n'3': show block users\n'4': show active user time");
                    }
                    /*
                    switch (writeStr) {
                        case ("1"):
                            this.showClients();
                            break;
                        case ("2"):
                            this.showUserTime("logInTime");
                            break;
                        case ("3"):
                            this.showUserTime("blockTime");
                            break;
                        case ("4"):
                            this.showUserTime("activeTime");
                            break;
                        default:
                            System.out.println("Command:\n'1': show online clients" + 
                                "\n'2': show clients log in time\n'3': show block users\n'4': show active user time");
                    }
                    */
                }
            } catch (Exception e) {
                System.out.println("server monitor error");
            }
        }
    }

    // this class is used to monitor time of every client
    class timeMonitor implements Runnable {  //=============================== INNER CLASS timeMonitor ============================
        private long timeNow = 0;
        long blockTimeMin = 3;
        long activeTimeMin = 3;
        long lastLogTimeMin = 3;

        public void logOutUnactiveClient() {    //--------------------------- check unactive client, and log them out -------------
            try {
                Iterator iter = clientActiveTimeMap.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    long timeVal = clientActiveTimeMap.get(key);
                    if (timeVal < (timeNow - activeTimeMin * 60000)) {
                        
                        getClient rmClient = clientMap.get(key);
                        rmClient.sendMessage("Oops! too long inactive time, server has logged you out!");
                        rmClient.dis.close();
                        rmClient.dos.close();
                        rmClient.usrSocket.close();
                        clientMap.remove(key);

                        clientActiveTimeMap.remove(key);

                    } else {
                        continue;
                    }
                }
            } catch (Exception e) {
                System.out.println("Manage inactive client fail!");
            }
        }

        public void mangBlockUsr() {    //----------------------------------- manage blocked users --------------------------------
            try {
                Iterator iter = clientBlockTimeMap.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    long timeVal = clientBlockTimeMap.get(key);
                    if (timeVal <= (timeNow - blockTimeMin * 60000)) {
                        clientBlockTimeMap.remove(key);
                    } else {
                        continue;
                    }
                }
            } catch (Exception e) {
                System.out.println("Manage block user list fail!");
            }      
        }

        public void mangLoginList() {   //----------------------------------- manage last log in list -----------------------------
            try {
                Iterator iter = clientLogInTimeMap.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    long timeVal = clientLogInTimeMap.get(key);
                    if (clientMap.containsKey(key)) {
                        continue;
                    } else if (timeVal > (timeNow - lastLogTimeMin * 60000)) {
                        continue;
                    } else {
                        clientLogInTimeMap.remove(key);
                        clientActiveTimeMap.remove(key);
                    }
                }
            } catch (Exception e) {
                System.out.println("Manage log in user list fail!");
            }
        }

        public void run() { //----------------------------------------------- run function of timeMonitor -------------------------
            try{
                while(srvrOnline) {
                    Thread.currentThread().sleep(60000);
                    timeNow = System.currentTimeMillis();
                    this.logOutUnactiveClient();
                    this.mangBlockUsr();
                    this.mangLoginList();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Time monitor gets error");
            }
            
        } // end timeMonitor run function
    }


    // this class is used to process every client thread
    class getClient implements Runnable {   //=============================== INNER CLASS getClient ==============================
        private Socket usrSocket;
        private DataInputStream dis = null;
        private DataOutputStream dos = null;
        boolean clientOnline = false;
        String clientName = null;

        getClient(Socket clientSocket) {    //------------------------------ class constructor ------------------------------------
            this.usrSocket = clientSocket;
            try {
                dis = new DataInputStream(this.usrSocket.getInputStream());
                dos = new DataOutputStream(this.usrSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {   //--------------------- send message to client ---------------------------------
            try {
                dos.writeUTF(message);
            } catch (SocketException e) {
                //e.printStackTrace();
                System.out.println("user log out");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String recvMessage() {   //---------------------------- receive message from client ----------------------------------
            String recvStr = null;
            try {
                recvStr = dis.readUTF();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return recvStr;
        }

        public void rcdMessage(String message) {  //--------------------------------- record the sended message -----------------------------
            long timeNow = System.currentTimeMillis();
            SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            StringBuffer date = new StringBuffer(fm.format(timeNow));
            date.append("-->");
            date.append(message);
            date.append("\n");
            try {
                FileWriter fileWriter = new FileWriter("chat_record.txt", true);
                fileWriter.write(date.toString());
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {             
                e.printStackTrace();
                System.out.println("Record chat message fail");
            }            
        }

        // run function of getClient
        public void run() {
            String readStr = "no meassage";
            String clientUsrName = null;
            String clientPasword = null;
            try {
                // first check the username and password
                int checkWrongInputNumber = 2;
                //---------------------------------------------------- log in process ------------------------------------------------
                while (true) {
                    this.sendMessage("Enter username:");
                    clientUsrName = this.recvMessage();
                    this.sendMessage("Enter password:");
                    clientPasword = this.recvMessage();

                    logIn clientLogIn = new logIn(clientUsrName, clientPasword);
                    if (clientUsrName.equals("exit")) { //------------------------- if user wants to exit now ---------
                        // if user want to eixt
                        clientOnline = false;
                        break;
                    } else if (clientBlockTimeMap.containsKey(clientUsrName)) { //-- if user name is blocked now ------
                        // if user is block list
                        this.sendMessage("Sorry, you have input wrong login informaiton more than 3 times\nTry again after 3 mins");
                        clientOnline = false;
                        continue;
                    } else if (clientLogIn.checkRightPassword() == false) { //------ if password is not correct -------
                        // if enter wrong username or password
                        clientOnline = false;
                        if (checkWrongInputNumber > 0) {
                            this.sendMessage("Wrong username or password, try again, you have " + checkWrongInputNumber + " chances");
                            checkWrongInputNumber --;
                            continue;
                        } else {
                            this.sendMessage("Oops, wrong input over 3 times!");
                            // put this user in block list
                            clientBlockTimeMap.put(clientUsrName, new Long(System.currentTimeMillis()));
                            clientOnline = false;
                            break;
                        } 
                    } else if (clientMap.containsKey(clientUsrName)) {  //if someone already logged in with same username
                        // if username already logged in
                        this.sendMessage("Oops, this user is already logged in!");
                        System.out.println("another '" + clientUsrName + "' log in failed");
                        clientOnline = false;
                        continue;
                    } else {    //----------------------------------- correct input -------------------------------------
                        // if user can log in
                        String welcomeMessage = "welcome, " + clientLogIn.checkUserName();                       
                        this.sendMessage(welcomeMessage);
                        clientName = clientLogIn.checkUserName();
                        clientOnline = true;
                        break;
                    }
                }

                //------------------------------------------- put client information in lists ----------------------------------------------
                if (clientOnline) {
                    // add new client to threadlist
                    //clientUsrs.add(this);
                    clientMap.put(clientName, this);
                    // record the log in time
                    clientLogInTimeMap.put(clientName, new Long(System.currentTimeMillis()));
                    // record the clientActiveTimeMap
                    clientActiveTimeMap.put(clientName, new Long(System.currentTimeMillis()));
                }

                // recognize commends and send messages
                Iterator iter = null;       // use to do iteration in Map
                long timeNow = 0, timeLogIn = 0;
                //--------------------------------------------- use command line ------------------------------------------------------------
                while (srvrOnline && clientOnline) {
                    if (readStr.equals("exit")) {
                        break;
                    }                  
                    readStr = dis.readUTF();
                    // record it down
                    StringBuffer rcdMsg = new StringBuffer(this.clientName);
                    rcdMsg.append(":");
                    rcdMsg.append(readStr);
                    this.rcdMessage(rcdMsg.toString());

                    // process command
                    clientActiveTimeMap.put(clientName, new Long(System.currentTimeMillis()));
                    // now check what readStr includes
                    String[] readStrSplit = readStr.split(" ");
                    int choseMethod = -1;
                    if (readStrSplit[0].equals("who")) {
                        choseMethod = 1;
                    } else if (readStrSplit[0].equals("last")) {
                        choseMethod = 2;
                    } else if (readStrSplit[0].equals("broadcast")) {
                        choseMethod = 3;
                    } else if (readStrSplit[0].equals("send")) {
                        choseMethod = 4;
                    } else if (readStrSplit[0].equals("logout")) {
                        choseMethod = 5;
                    } else if (readStrSplit[0].equals("exit")) {
                        choseMethod = 6;
                    } 


                    switch (choseMethod) {                      
                        case (1):   //-------------------------- command who -------------------------------
                        // find who is online
                            iter = clientMap.keySet().iterator();
                            StringBuffer onlineUsrList = new StringBuffer("Online user: ");
                            while (iter.hasNext()) {
                                String key = (String)iter.next();
                                if (key.equals(clientUsrName)) {
                                    continue;
                                } else {
                                    onlineUsrList.append(key);
                                    onlineUsrList.append(" ");
                                }
                            }
                            this.sendMessage(onlineUsrList.toString());
                            break;
                        case (2):  //------------------------- command last ------------------------------
                        // find user who loged in in last xx minutes
                            if (readStrSplit.length != 2) {
                                this.sendMessage("Format for last is:last <integer(minutes 1 ~ 60)>");
                            } else {
                                timeNow = System.currentTimeMillis();
                                //timeLogIn = clientLogInTimeMap.get(clientName).longValue();
                                StringBuffer lastLogInUsr = new StringBuffer("Last login users: ");
                                //this.sendMessage(String.valueOf(timeNow));
                                //this.sendMessage(String.valueOf(timeLogIn));
                                try {
                                    long lastMinute = Long.parseLong(readStrSplit[1]);
                                    long timeFlag = timeNow - lastMinute * 60 * 1000;
                                    iter = clientLogInTimeMap.keySet().iterator();
                                    while(iter.hasNext()) {
                                        String key = (String)iter.next();
                                        timeLogIn = clientLogInTimeMap.get(key);
                                        if (timeLogIn >= timeFlag) {
                                            lastLogInUsr.append(key);
                                            lastLogInUsr.append(" ");
                                        }
                                    }
                                    this.sendMessage(lastLogInUsr.toString());
                                } catch (Exception e) {
                                    this.sendMessage("Oops, can't read last login users, try again!");
                                }
                            }
                            break;
                        case (3): //---------------------- command broadcast ------------------------
                        // broadcast message to all users
                            StringBuffer sendMsgToUsr = new StringBuffer();
                            for (int i = 1; i < readStrSplit.length; i++) {
                                sendMsgToUsr.append(readStrSplit[i]);
                                sendMsgToUsr.append(" ");
                            }
                            iter = clientMap.keySet().iterator();
                            while (iter.hasNext()) {
                                String key = (String)iter.next();
                                getClient val = clientMap.get(key);
                                val.sendMessage("<<" + clientName + ">>   " + sendMsgToUsr.toString());
                            }
                            break;
                        case (4):  //-------------------------- command send -------------------------------
                        // send message to some specific users
                            try {
                                String[] readStrSplitUsr1 = readStr.split("\\(");
                                String[] readStrSplitUsr2 = readStrSplitUsr1[1].split("\\)");
                                String[] readStrSplitUsr3 = readStrSplitUsr2[0].split(" ");
                                String countPstion = "send (" + readStrSplitUsr2[0] + ") ";
                                StringBuffer sendMsgToUsr2 = new StringBuffer(readStr);
                                String sendMessage2 = sendMsgToUsr2.substring(countPstion.length());
                                for (int i = 0; i < readStrSplitUsr3.length; i++) {
                                    // send message to all users in the list
                                    if (clientMap.containsKey(readStrSplitUsr3[i])) {
                                        getClient val = clientMap.get(readStrSplitUsr3[i]);
                                        val.sendMessage("<<" + clientName + ">>   " + sendMessage2);
                                    }
                                    else {
                                        continue;
                                    }
                                }
                            } catch (Exception e) {
                                this.sendMessage("Format for send is:send (<user1> <user2> ... <userN>) <message>");
                            }                           
                            break;
                        case (5):    //------------------------- command logout and eixt ------------------
                            this.sendMessage("Please use commend 'exit' to log out");
                            break;
                        case (6):
                            this.sendMessage("User exit now");
                            break;
                        default:
                            this.sendMessage("Conmmands contains:\nwho\nlast <number>\nbroadcast <message>\nsend (<users>) <message>\nexit/logout>>>");
                    }
                    /*
                    Iterator iter = clientMap.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = (String)iter.next();
                        getClient val = clientMap.get(key);
                        val.sendMessage("<<" + clientName + ">>   " + readStr);

                    for (int i = 0; i < clientUsrs.size(); i++) {
                        getClient c = clientUsrs.get(i);
                        c.sendMessage("<<" + clientName + ">>   " + readStr);
                    }
                    */
                }
                //------------------------------------------ before leave, remove related item in lists -------------------------------------
                if (clientOnline) {
                    // delete new client to threadlist
                    clientOnline = false;
                    // tell server this user loged out
                    System.out.println("User '" + clientName + "' log out");
                    //clientUsrs.remove(this);
                    // remove this client from user list
                    clientMap.remove(clientName);
                    // remove this client from user login time list
                    //clientLogInTimeMap.remove(clientName);
                    // remove this client form active list
                    clientActiveTimeMap.remove(clientName);
                }
                //------------------------------------------- client thread over, close socket -----------------------------------------------
                dis.close();
                dos.close();
                usrSocket.close();
                //------------------------------------------------ exception handling ---------------------------------------------------------
            } catch (Exception e) {
                System.out.println("Error, con't sent message");
            } finally {
                try {
                    dis.close();
                    usrSocket.close();
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //------------------------------------------- end exception handling in run function of getClient------------------------------------
        } // end run function of getClient
    } // end class of getClient
} // end server
