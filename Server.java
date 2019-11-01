import java.net.*;
import javax.net.ssl.*;
import java.io.*;
import java.util.*;
import java.math.*;
import java.security.cert.*;


/**
  * Server entry point
  * 
  * @author KK Sobczak
  * @version 1.0
  */
public class Server {
    private HashMap<String, User> users = new HashMap<String, User>();
    private ArrayList<Runnable> handlers = new ArrayList<Runnable>();
    public String keyStore;
    public String keyStorePassword;
    public ServerUI ui;

    /**
      * Create new Server
      * 
      * @param port Int port to be listened on 
      */
    public Server(int port, String keyStore, String keyStorePassword){
        Scanner systemIn = new Scanner(System.in);
        try {
            this.keyStore = keyStore;
            this.keyStorePassword = keyStorePassword;
            System.setProperty("javax.net.ssl.keyStore", keyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);

            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            ServerSocket ss = ssf.createServerSocket(port);
            this.ui = new ServerUI(this);
            new Thread(this.ui).start();
            while (true) {
                Socket s = ss.accept();
                
                Runnable serverHandler = new ServerHandler(s, this);
                handlers.add(serverHandler);
                new Thread(serverHandler).start();
            }
        } catch (Exception e){
            e.printStackTrace();
        } 
    }

    /**
      * Get all users
      * 
      * @return HashMap<String, User> mapping username -> user
      */
    public HashMap<String, User> getUsers(){
        return this.users;
    }

    /**
      * Get all handlers
      *
      * @return ArrayList<Runnable> of all handlers
      */
    public ArrayList<Runnable> getHandlers(){
        return this.handlers;
    }
}