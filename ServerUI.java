import java.util.*;

/**
  * UI of the Server
  * 
  * @author KK Sobczak
  * @version 1.0
  */
public class ServerUI implements Runnable {
    private Server server;
    public String prompt = "KIRK> ";
    /**
      * Create a new ui and bind a server to it
      *
      * @param server Server to be bound
      */
    public ServerUI(Server server){
        this.server = server;
    }
    @Override
    public void run(){
        Scanner systemIn = new Scanner(System.in);
        while(true){
            System.out.print(prompt);
            if(systemIn.hasNextLine()){
                String line = systemIn.nextLine();
                String[] words = line.split(" ");
                if(line.length() > 0){
                    String operation = words[0];
                    switch (operation){
                        case "users":
                            switch(words[1]){
                                case "add":
                                    if(words.length != 4){
                                        System.out.println("Invalid syntax");
                                    } else {
                                        server.getUsers().put(words[2], new User(words[2], words[3], new MachineID().getId()));
                                        this.print("Added user: " + words[2]);
                                    }
                                    break;
                                case "remove":
                                    if(words.length != 3){
                                        System.out.println("Invalid syntax");
                                    } else {
                                        server.getUsers().remove(words[1]);
                                        this.print("Removed user: " + words[1]);
                                    }
                                    break;
                                default:
                                    System.out.println("Not implemented");
                                    break;
                            }
                            break;
                        case "show":
                            switch(words[1]){
                                case "users":
                                    System.out.println(server.getUsers());
                                    break;
                                default:
                                    System.out.println("Not implemented");
                                    break;
                            }
                            break;
                        default:
                            System.out.println("Not implemented");
                            break;
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