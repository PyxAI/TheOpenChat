import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
/*
User thread, each user gets one. It is timed with locks with the server:
First we open the streams and read the username
then the server checks if it exists or not
continue if everything is ok.
method msg sends ChatData class via ObjectOutputStream.
 */
public class ChatThread extends Thread{
    private Socket socket;
    ChatData inData;
    ChatData outData;
    ObjectOutputStream oout;
    ObjectInputStream oin;
    Lock lock;
    Condition c;
    private ChatServer server;
    private String username = "";
    public ChatThread(Socket socket, ChatServer server, Condition c, Lock lock) {
        System.out.println("creating new chat thread");
        this.socket = socket;
        this.server = server;
        this.c = c;
        this.lock = lock;
    }

    @Override
    public void run(){
        try {
            lock.lock();
            oout = new ObjectOutputStream(socket.getOutputStream());
            oin = new ObjectInputStream(socket.getInputStream());
            System.out.println("Thread listening");
            while ((inData = (ChatData)oin.readObject()) ==null){} // To make sure we get a username first
            username = inData.getText();
            c.signal();
            c.await();
            while (server.userExists(username, this)) {
                inData = (ChatData)oin.readObject();
                username = inData.getText();
            }
        }
        catch (IOException err){
            System.out.println("Error opening reading or writing stream");
        }
        catch (ClassNotFoundException err){
            System.out.println("communication error with client...");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock.unlock();
        server.broadcast(String.format("User %s has joined the chatroom!\n", username));
        String output;
        try {
            //Server listening to client
            while (((inData = (ChatData)oin.readObject()) != null | !Thread.currentThread().isInterrupted())) {
                if (inData.getType().toString().compareTo(ChatData.msgType.UserMsg.toString())==0) {
                    output = String.format("%s: %s", username, inData.getText());
                    server.broadcast(output);
                    System.out.println(output);
                }
                if (inData.getType().toString().compareTo(ChatData.msgType.Exc.toString())==0) {
                    if (inData.getCode() == 1) {
                        server.left(this);
                        return;
                    }
                }
            }
        }
        catch (ClassNotFoundException | IOException err){}
        server.left(this);
    }

    public String getUsername(){
        return username;
    }

    public void msg(String txt){
        outData = new ChatData(txt+"\n", ChatData.msgType.UserMsg);
        try {
            oout.writeObject(outData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void msg(ChatData msg){
        try {
            oout.reset();
            oout.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
