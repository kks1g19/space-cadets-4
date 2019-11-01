import java.io.*;

public class MachineID {
    private String id;
    public MachineID(){
        Process IDProcess;
        String OSType = System.getProperty("os.name").toLowerCase();
        if(OSType.contains("mac")){
            try {    
                IDProcess = Runtime.getRuntime().exec("ioreg -rd1 -c IOPlatformExpertDevice");
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(IDProcess.getInputStream()));
                String s;
                while ((s = stdInput.readLine()) != null) {
                    //System.out.println(s);
                    if(s.contains("IOPlatformUUID")){
                        this.id = s.substring(s.lastIndexOf("= "), s.length()).trim().replace("\"", "");
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(OSType.contains("nux")){
            try {    
                IDProcess = Runtime.getRuntime().exec("cat /var/lib/dbus/machine-id");
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(IDProcess.getInputStream()));
                String s;
                while ((s = stdInput.readLine()) != null) {
                    this.id = s.trim().replace("\"", "");
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(OSType.contains("win")){
            try {    
                IDProcess = Runtime.getRuntime().exec("reg query HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Cryptography /v MachineGuid");
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(IDProcess.getInputStream()));
                String s;
                while ((s = stdInput.readLine()) != null) {
                    this.id = s.trim().replace("\"", "");
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            System.out.println("Error: (MachineID) OS Type not supported: " + OSType);
            System.exit(1);
        }
    }

    public String getId(){
        return this.id;
    }
}