import java.io.IOException;
import java.util.concurrent.locks.*;

/*
Manager needed because we need someone to restart the server once its off and on again
I'm using locks to manage the sequence of the loading.
 */
public class ServerManager{
    Lock lock;
    Condition c;
    public ServerManager(){
        lock = new ReentrantLock();
        c = lock.newCondition();
        lock.lock();
        ChatServer server = new ChatServer();
        server.start();
        ChatServerGui gui = new ChatServerGui(lock, c);
        gui.setServer(server);
        server.ui(gui);
        gui.updateStatus("Server started");
        lock.unlock();
        while (true) {
            lock.lock();
            try {
                c.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock();
            server = new ChatServer();
            server.start();
            gui.setServer(server);
            server.ui(gui);
            gui.updateStatus("Server started");
        }
    }

}
