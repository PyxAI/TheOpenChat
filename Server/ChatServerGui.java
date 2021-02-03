import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.locks.*;

/*
Simple GUI with on - off button for the server
Plus a text area to show server logs.
 */
public class ChatServerGui extends JFrame {
    JFrame frame;
    JPanel switchPanel;
    JRadioButton on;
    JRadioButton off;
    ChatServer server;
    Lock lock;
    Condition c;
    JTextArea info;
    private Lock Slock = new ReentrantLock();
    private Condition Scond = Slock.newCondition();

    int width = 300;
    int height = 200;
    public ChatServerGui(Lock l, Condition c){
        this.lock = l;
        this.c = c;
        frame = new JFrame("The Open Chat Server");
        frame.setLayout(new BorderLayout());
        switchPanel = new JPanel();
        switchPanel.setLayout(new FlowLayout());
        //On-Off buttons
        on = new JRadioButton("On", true);
        off = new JRadioButton("Off");
        on.addActionListener(new onoffListener());
        off.addActionListener(new onoffListener());
        ButtonGroup onoff = new ButtonGroup();
        onoff.add(on);
        onoff.add(off);
        switchPanel.add(on);
        switchPanel.add(off);
        frame.add(switchPanel,BorderLayout.NORTH);

        info = new JTextArea(3,10);
        info.setText("Server running");
        frame.add(info, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width,height);
        frame.setVisible(true);
    }

    private class onoffListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource()==off){
                info.setText("Stopping server");
                server.shutdown();
            }
            else {
                lock.lock();
                info.setText("Starting server");
                c.signal();
                lock.unlock();

            }
        }
    }

    public void updateStatus(String txt){
        info.setText(txt);
    }
    public void setServer(ChatServer server){
        this.server = server;
    }

}
