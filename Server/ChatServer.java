import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.*;

/*
The main app. The loop can be interrupted which closes all connections to the users in every thread.
Also verifying that the username is unique.
 */
public class ChatServer extends Thread{
    private static ServerSocket ssocket;
    private static final int port = 6666;
    private static ArrayList<ChatThread> users;
    private ChatThread user;
    private static ArrayList<String> usernames;
    private Lock lock = new ReentrantLock();
    private Condition cond = lock.newCondition();
    private ChatServerGui ui;
    ChatData outData;
    private boolean active = true;
    public void run(){
        users = new ArrayList<>();
        usernames = new ArrayList<>();
        try {
            ssocket = new ServerSocket(port);
        }
        catch (IOException err){
            System.out.println(String.format("Error creating socket:\n%s\n", err));
            System.exit(1);
        }
        Socket client;
        while (active) {
            System.out.println("restarting server\n");
            if (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println(String.format("Server listening on port %d\n", port));
                    client = ssocket.accept();
                    user = new ChatThread(client, this, cond, lock);
                    user.start();
                    lock.lock();
                    cond.await();
                    String uname = user.getUsername();
                    //if user is connected or exists - closes thread
                    if (!userExists(uname, user)) {
                        usernames.add(uname);
                        users.add(user);
                        updateGuestList();
                    } else {
                        outData = new ChatData("User exists! please choose another username!", ChatData.msgType.Exc);
                        user.msg(outData);
                    }
                    cond.signal();
                    lock.unlock();

                } catch (IOException | InterruptedException err) {
                    for (ChatThread usr: users){
                        ui.updateStatus("Killing user connection");
                        usr.disconnect();
                    }
                    ui.updateStatus(String.format("Error creating connection:\n%s\n", err));
                }
            }
        }
        ui.updateStatus("Server killed");
    }

    public void broadcast(String msg){
        ui.updateStatus(String.format("Broadcasting: %s\n",msg));
        for (ChatThread user: users){
            user.msg(msg);
        }
    }

    public boolean userExists(String user, ChatThread current){
        for (ChatThread search : users){
            if (search.getUsername().compareTo(user)==0 & search!=current) {
                return true;
            }
        }
        return false;
    }

    public void ui(ChatServerGui ui){
        this.ui = ui;
    }

    public void shutdown(){
        active = false;
        interrupt();
        try {
            ssocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void left(ChatThread leaving) {
        for (Iterator<ChatThread> it = users.iterator(); it.hasNext(); ){
            user = it.next();
            if (user == leaving) {
                usernames.remove(user.getUsername());
                it.remove();
            }
        }
        broadcast(String.format("user %s has left", leaving.getUsername()));
        updateGuestList();
    }

    /*Update the user list for every user*/
    public void updateGuestList(){
        outData = new ChatData(usernames);
        for (ChatThread usr: users){
            usr.msg(outData);
        }

    }
}
