import java.net.*;
import javax.net.ssl.*;
import java.io.*;
import java.util.*;
import java.math.*;
import java.security.cert.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Client client;
    private ClientUI clientUI;
    private boolean registered = false;
    private String username = null;
    private String password = null;
    private Scanner systemIn;
    private Scanner request;
    private PrintStream response;

    public ClientHandler(Socket socket, Client client){
        this.socket = socket;
        this.client = client;
    }

    public void setUserDetails(String username, String password){
        this.username = username;
        this.password = password;
    }

    public void register(){
        ArrayList<String> from = new ArrayList<String>(Arrays.asList(new String[] {username}));
        ArrayList<String> to = new ArrayList<String>();
        HashMap<String, String> content = new HashMap<String, String>();

        Message msg = new Message("REGISTER", from, to, content);
        response.println(msg.parseMessage());
    }

    public void unregister(){
        ArrayList<String> from = new ArrayList<String>(Arrays.asList(new String[] {username}));
        ArrayList<String> to = new ArrayList<String>();
        HashMap<String, String> content = new HashMap<String, String>();

        Message msg = new Message("UNREGISTER", from, to, content);
        this.registered = false;
        this.username = null;
        response.println(msg.parseMessage());
    }

    public void sendMessage(String[] target, String line){
        ArrayList<String> from = new ArrayList<String>(Arrays.asList(new String[] {username}));
        ArrayList<String> to = new ArrayList<String>(Arrays.asList(target));
        HashMap<String, String> content = new HashMap<String, String>();

        content.put("text", line);
        Message msg = new Message("TEXT", from, to, content);
        response.println(msg.parseMessage());
    }

    @Override
    public void run(){
        try {
            System.setProperty("javax.net.ssl.trustStore", this.client.trustStore);
            System.setProperty("javax.net.ssl.trustStorePassword", this.client.trustStorePassword);

            SSLSession session = ((SSLSocket) socket).getSession();
            Certificate[] cchain = session.getPeerCertificates();

            this.response = new PrintStream(socket.getOutputStream());
            this.request = new Scanner(socket.getInputStream());

            this.systemIn = new Scanner(System.in);

            boolean shouldClose = false;

            this.clientUI = new ClientUI(systemIn, this);
            new Thread(this.clientUI).start();

            while(!shouldClose){
                if(request.hasNextLine()){
                    String line = request.nextLine();
                    if(line.trim().charAt(0) != '{'){
                        continue;
                    }

                    Message msg = new Message(line);

                    switch (msg.getType()) {
                        case "AUTHORIZE":
                            if(password != null && username != null){
                                ArrayList<String> from = new ArrayList<String>(Arrays.asList(new String[] {username}));
                                ArrayList<String> to = new ArrayList<String>();
                                HashMap<String, String> content = new HashMap<String, String>();

                                MessageDigest md = MessageDigest.getInstance("SHA-256");
                                String challengeString = msg.getContent().get("challengeString");
                                int challengeNumber = Integer.parseInt(msg.getContent().get("challengeNumber"));
                                int tries = Integer.parseInt(msg.getContent().get("tries"));

                                String hashPart1 = new String(md.digest((Integer.toString(challengeNumber) + from.get(0)).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
                                String hashPart2 = new String(md.digest((challengeString + password).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
                                String hash = new String(md.digest((StringMerger.merge(hashPart1, hashPart2) + Integer.toString(tries)).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

                                content.put("challengeString", challengeString);
                                content.put("challengeNumber", Integer.toString(challengeNumber));
                                content.put("tries", Integer.toString(tries));
                                content.put("hash", hash);
                                content.put("contact", socket.getLocalAddress() + ":" + socket.getLocalPort());

                                Message responseContent = new Message("REGISTER", from ,to, content);
                                response.println(responseContent.parseMessage());
                            }
                            break;
                        case "AUTH OK":
                            this.clientUI.prompt = "[" + username + "]";
                            clientUI.print("Logged in as user: " + username);
                            this.registered = true;
                            this.password = null;
                            break;
                        case "TEXT":
                            clientUI.print("[" + (msg.getSenders().get(0).equals(username) ? "me" : msg.getSenders().get(0)) + "] " + msg.getContent().get("text"));
                            break;
                        default:
                            clientUI.print("Unsupported message: " + msg.getType());
                            break;
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}