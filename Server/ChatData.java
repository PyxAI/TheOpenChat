/*
Class to transfer information between server and client
Can be used with various error codes, messages, etc!
 */
import java.io.Serializable;
import java.util.ArrayList;

public class ChatData implements Serializable {
    enum msgType{UserMsg, GuestList, UserName, Exc};
    ArrayList<String> users;
    String str = "";
    msgType type;
    int code;
    int len;
    public ChatData(String msg, msgType type){
        this.type = type;
        this.str = msg;
    }
    public ChatData(String msg, int errCode){
        this.code = errCode;
        this.str = msg;
        this.type = msgType.Exc;
    }
    public ChatData(ArrayList<String> users){
        this.type = msgType.GuestList;
        this.users = users;
        this.len = users.size();
    }
    public ArrayList<String> getGuests(){
        return users;
    }

    public int getCode(){
        return code;
    }

    public int getLen() {
        return len;
    }

    public String getText(){
        return str;
    }
    public msgType getType(){
        return type;
    }
}
