import java.net.*;
import javax.net.ssl.*;
import java.io.*;
import java.util.*;
import java.math.*;
import java.security.cert.*;


/**
  * Client of the chat
  * 
  * @author KK Sobczak
  * @version 1.0
  */
public class Client {
    public String trustStore;
    public String trustStorePassword;
    public Client(String targetIP, int targetPort, String trustStore, String trustStorePassword){
        try {
            this.trustStore = trustStore;
            this.trustStorePassword = trustStorePassword;
            System.setProperty("javax.net.ssl.trustStore", trustStore);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket s = ssf.createSocket(targetIP, targetPort);

            Runnable clientHandler = new ClientHandler(s, this);
            new Thread(clientHandler).start();

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}