import java.net.*;
import javax.net.ssl.*;
import java.io.*;
import java.util.*;
import java.math.*;
import java.security.cert.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
  * Logic of the Server
  * 
  * @author KK Sobczak
  * @version 1.0
  */
public class ServerHandler implements Runnable {
    private Socket socket;
    private Server server;
    public ServerHandler(Socket socket, Server server){
        this.socket = socket;
        this.server = server;
    }

    public String getSocket(){
        return this.socket.getRemoteSocketAddress().toString();
    }

    public void sendMessage(String msg){
        try {
            new PrintStream(this.socket.getOutputStream()).println(msg);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try {
            System.setProperty("javax.net.ssl.keyStore", this.server.keyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", this.server.keyStorePassword);

            SSLSession session = ((SSLSocket) socket).getSession();
            Certificate[] cchain2 = session.getLocalCertificates();

            PrintStream response = new PrintStream(socket.getOutputStream());
            Scanner request = new Scanner(socket.getInputStream());

            this.server.ui.print("Client " + this.getSocket() + " connected.");

            boolean requestHandlingFinished = false;

            while(!requestHandlingFinished){
                if(request.hasNextLine()){
                    String requestContent = request.nextLine();

                    if(requestContent.trim().charAt(0) != '{'){
                        continue;
                    }

                    Message msg = new Message(requestContent);

                    switch (msg.getType()){
                        case "REGISTER":
                            Message responseContent;
                            ArrayList<String> from = msg.getSenders();
                            if(from.size() != 1){
                                this.server.ui.print("Wrong REGISTER message received from: " + socket.getRemoteSocketAddress().toString());
                                responseContent = new Message("ERROR", new ArrayList<String>(), new ArrayList<String>(), new HashMap<String, String>());
                            } else if(msg.getContent().get("contact") == null) {
                                HashMap<String, String> content = new HashMap<String, String>();
                                content.put("challengeString", RandomString.nextString(32));
                                content.put("challengeNumber", Integer.toString(new Random().nextInt(1024)));
                                content.put("tries", Integer.toString(1));
                                responseContent = new Message("AUTHORIZE", new ArrayList<String>(), new ArrayList<String>(Arrays.asList(from.get(0))), content);
                            } else {
                                String challengeString = msg.getContent().get("challengeString");
                                int challengeNumber = Integer.parseInt(msg.getContent().get("challengeNumber"));
                                int tries = Integer.parseInt(msg.getContent().get("tries"));
                                String responseHash = msg.getContent().get("hash");
                                if(server.getUsers().get(from.get(0)) != null){
                                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                                    String hashPart1 = new String(md.digest((Integer.toString(challengeNumber) + from.get(0)).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
                                    String hashPart2 = new String(md.digest((challengeString + new String(this.server.getUsers().get(from.get(0)).getPassword(StringMerger.merge(new MachineID().getId(), from.get(0))))).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
                                    String hash = new String(md.digest((StringMerger.merge(hashPart1, hashPart2) + Integer.toString(tries)).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
                                    if(responseHash.equals(hash)){
                                        responseContent = new Message("AUTH OK", new ArrayList<String>(), new ArrayList<String>(Arrays.asList(from.get(0))), new HashMap<String, String>());
                                        this.server.getUsers().get(from.get(0)).setContact(msg.getContent().get("contact"));
                                        this.server.ui.print("User " + from.get(0) + " logged in");
                                        //requestHandlingFinished = true;
                                    } else {
                                        HashMap<String, String> content = new HashMap<String, String>();
                                        content.put("challengeString", RandomString.nextString(32));
                                        content.put("challengeNumber", Integer.toString(new Random().nextInt(1024)));
                                        content.put("tries", Integer.toString(tries + 1));
                                        responseContent = new Message("AUTHORIZE", new ArrayList<String>(), new ArrayList<String>(), content);
                                    } 
                                } else {
                                        HashMap<String, String> content = new HashMap<String, String>();
                                        content.put("challengeString", RandomString.nextString(32));
                                        content.put("challengeNumber", Integer.toString(new Random().nextInt(1024)));
                                        content.put("tries", Integer.toString(tries + 1));
                                        responseContent = new Message("AUTHORIZE", new ArrayList<String>(), new ArrayList<String>(), content);
                                    } 
                            }
                            response.println(responseContent.parseMessage());
                            break;
                        case "UNREGISTER":
                            server.getUsers().get(msg.getSenders().get(0)).setContact(null);
                            requestHandlingFinished = true;
                            break;
                        case "TEXT":
                            if(msg.getReceivers().get(0).equals("everyone")){
                                for(Runnable h : server.getHandlers()){
                                    ServerHandler handler = (ServerHandler) h;
                                    for(String username : this.server.getUsers().keySet()){
                                        User user = this.server.getUsers().get(username);
                                        if(user.getContact().equals(handler.getSocket())){
                                            handler.sendMessage(requestContent);
                                        }
                                    }
                                }
                            } else {
                                for(String username : msg.getReceivers()){
                                    for(Runnable h : server.getHandlers()){
                                        ServerHandler handler = (ServerHandler) h;
                                        User user = server.getUsers().get(username);
                                        if(user != null){
                                            if(handler.getSocket().equals(user.getContact())){
                                                handler.sendMessage(requestContent);
                                            } else {
                                                HashMap<String, String> content = new HashMap<String, String>();
                                                content.put("text", "No contact to user: " + username);
                                                response.println(new Message("TEXT", new ArrayList<String>(Arrays.asList(new String[] {"server"})), msg.getSenders(), content));
                                            }
                                        } else {
                                            HashMap<String, String> content = new HashMap<String, String>();
                                            content.put("text", "No such user: " + username);
                                            response.println(new Message("TEXT", new ArrayList<String>(Arrays.asList(new String[] {"server"})), msg.getSenders(), content));
                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            this.server.ui.print("Invalid message received, type: " + msg.getType());
                            //requestHandlingFinished = true;
                            break;
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}