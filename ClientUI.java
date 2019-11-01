import java.io.*;
import java.util.*;

/**
  * UI of the Client
  * 
  * @author KK Sobczak
  * @version 1.0
  */
public class ClientUI implements Runnable {
    private Scanner systemIn;
    private ClientHandler handler;
    public String prompt = "[unregistered]";
    /**
      * Create a new ui and bind a client handler and input to it
      *
      * @param systemIn Input to be bound
      * @param handler ClientHandler to be bound
      */
    public ClientUI(Scanner systemIn, ClientHandler handler){
        this.systemIn = systemIn;
        this.handler = handler;
    }

    @Override
    public void run(){
        while (true){
            System.out.print(prompt);
            if(systemIn.hasNextLine()){
                String line = systemIn.nextLine();
                if(line.length() > 0){
                    if(line.charAt(0) == '/'){
                        String[] words = line.split(" ");
                        String operation = words[0];
                        switch(operation) {
                            case "/register":
                                if(words.length != 3){
                                    System.out.println("Invalid syntax, use /register [username] [password]");
                                } else {
                                    handler.setUserDetails(words[1], words[2]);
                                    handler.register();
                                }
                                break;
                            case "/unregister":
                                handler.unregister();
                                break;
                            case "/to":
                                String message = String.join(" ", Arrays.asList(words).subList(2, words.length));
                                String[] users = words[1].split(",");
                                handler.sendMessage(users, message);
                                break;
                            default:
                                System.out.println("No such command");
                                break;
                        }
                    } else {
                        handler.sendMessage(new String[] {"everyone"}, line);
                    }
                }
            }
        }
    }

    /**
      * Print the message and prompt
      *
      * @param toPrint String to be printed
      */
    public void print(String toPrint){
        System.out.flush();
        System.out.println(toPrint);
        System.out.print(prompt);
    }
}