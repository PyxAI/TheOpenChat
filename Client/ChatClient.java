import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/*
Using Object streams, the client can communicate efficiently with the server.
 */
public class ChatClient implements Runnable {
    Socket client;
    String host;
    ChatClientGui ui;
    String username;
    ArrayList<String> guests;
    ChatData indata;
    ChatData outdata;
    ObjectOutputStream oout;
    ObjectInputStream oin;
    int iport =0;
    boolean connected = false;
    public ChatClient(){
        guests = new ArrayList<>();
    }
    public void run(){
        //Connecting to server
        try {
            System.out.printf("Trying to connect...");
            client = new Socket(host, iport);
            oout = new ObjectOutputStream(client.getOutputStream());
            oin = new ObjectInputStream(client.getInputStream());
            connected = true;
            guests.add(username);
            outdata = new ChatData(username, ChatData.msgType.UserName);
            oout.writeObject(outdata); //send username to server thread
            while ((indata = (ChatData)oin.readObject()) != null ){
                if (indata.getType().toString().equals(ChatData.msgType.UserMsg.toString())) {
                    System.out.println("Got user message");
                    ui.msg(indata.getText());
                }
                if (indata.getType().toString().equals(ChatData.msgType.GuestList.toString())) {
                    System.out.printf("got a list of %d users", indata.getGuests().size());
                    ui.updateGuests(indata.getGuests());
                }
                if (indata.getType().toString().equals(ChatData.msgType.Exc.toString())) {
                    JOptionPane.showMessageDialog(null, indata.getText());
                    disconnect();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Disconnected from server");
        }
        catch (ClassNotFoundException e){
            JOptionPane.showMessageDialog(null, "Tried to read the wrong object");

        }
    }
    public void setup(String host, String port, String username) {
        try {
            iport = Integer.parseInt(port);
            this.username = username;
        } catch (NumberFormatException err) {
            JOptionPane.showMessageDialog(null, err.getMessage());
        }
        this.host = host;
    }
    public void ui(ChatClientGui ui){
        this.ui = ui;
    }

    protected boolean validUsername(String user){
        if (user.length()<1)
            return false;
        else
            return true;
    }


    public void disconnect(){
        outdata = new ChatData("User disconnected", 1);
        try {
            oout.writeObject(outdata);
            oout.close();
            oin.close();
            client.close();
            connected = false;
        } catch (IOException e) {
        }
    }
    public void sendMsg(String msg)
    {
        outdata = new ChatData(msg, ChatData.msgType.UserMsg);
        try {
            oout.writeObject(outdata);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}