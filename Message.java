import org.json.*;
import java.util.HashMap;
import java.util.ArrayList;

/**
  * Message logic
  * 
  * @author KK Sobczak
  * @version 1.0
  */
public class Message {
    private String type;
    private ArrayList<String> senders = new ArrayList<String>();
    private ArrayList<String> receivers = new ArrayList<String>();
    private HashMap<String, String> content = new HashMap<String, String>();

    public Message(String msg){
        JSONObject parsedMessage = new JSONObject(msg);
        this.type = parsedMessage.getString("type");
        JSONArray senders = parsedMessage.getJSONArray("from");
        JSONArray receivers = parsedMessage.getJSONArray("to");
        JSONObject content = parsedMessage.getJSONObject("content");

        for(int i = 0; i < senders.length(); i++){
            this.senders.add(senders.getString(i));
        }
        for(int i = 0; i < receivers.length(); i++){
            this.receivers.add(receivers.getString(i));
        }
        for(String key : content.keySet()){
            this.content.put(key, content.getString(key));
        }
    }

    public Message(String type, ArrayList<String> sender, ArrayList<String> receiver, HashMap<String, String> content){
        this.type = type;
        this.senders = sender;
        this.receivers = receiver;
        this.content = content;
    }

    public String parseMessage(){
        JSONObject msg = new JSONObject();
        JSONArray senders = new JSONArray(this.senders);
        JSONArray receivers = new JSONArray(this.receivers);
        JSONObject content = new JSONObject();
        for(String key : this.content.keySet()){
            content.put(key, this.content.get(key));
        }
        msg.put("type", this.type);
        msg.put("from", senders);
        msg.put("to", receivers);
        msg.put("content", content);
        return msg.toString();
    }

    public ArrayList<String> getSenders(){
        return this.senders;
    }

    public ArrayList<String> getReceivers(){
        return this.receivers;
    }

    public String getType(){
        return this.type;
    }

    public HashMap<String, String> getContent(){
        return this.content;
    }
}