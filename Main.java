public class Main {
    private static final MachineID id = new MachineID();
    public static void main(String[] args) {
        if(args[0].equals("server")){
            Server srv = new Server(Integer.parseInt(args[1]), "/Users/konradsobczak/.javakeystore", "randpswd");
        } else if(args[0].equals("client")){
            Client cli = new Client(args[1], Integer.parseInt(args[2]), "/Users/konradsobczak/.javatruststore", "randpswd");
        }
    }
}