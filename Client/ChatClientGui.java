import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


/*BorderLayout where the north is a panel with Grid layout.
    Some cells in the grid are panels for a flow layout presentation
 */
public class ChatClientGui extends JFrame implements Runnable{
    private static String scroller = "";
    private static JTextArea chat;
    private static JTextField userInput;
    private static ChatClient client;
    private static JTextField serverAddress;
    private static JTextField serverAddressPort;
    private static JTextField username;
    private static JButton stam;
    private static JTextArea usersList;
    private static final int height = 800;
    private static final int width = 1100;
    private static final int txtlength = height/50;
    private static int txtCounter = 0;
    private static String[] scrollerArr;
    public ChatClientGui(ChatClient client) {
        this.client = client;
        scrollerArr = new String[txtlength];
    }

    public void run(){
        client.ui(this);
        JFrame frame = new JFrame("The open chat");
        frame.setLayout(new BorderLayout());

        //Main text
        chat = new JTextArea(txtlength,40);
        chat.setEditable(false);
        addTxt("Welcome to the chat! please connect to start talking");
        frame.add(chat, BorderLayout.CENTER);

        //Settings
        JPanel settings = new JPanel();
        settings.setLayout(new GridLayout(4,4));

        //Settings top area - address
        JPanel settingsTop = new JPanel();
        settingsTop.setLayout(new FlowLayout());
        serverAddress = new JTextField("localhost",10);
        JTextField serverTitle = new JTextField("Hostname:",10);
        serverTitle.setEditable(false);
        settingsTop.add(serverTitle);
        settingsTop.add(serverAddress);
        settings.add(settingsTop);
        //Settings top area - port
        JPanel settingsTopPort = new JPanel();
        settingsTopPort.setLayout(new FlowLayout());
        serverAddressPort = new JTextField("6666",6);
        JTextField serverTitlePort = new JTextField("Port:",10);
        serverTitlePort.setEditable(false);
        settingsTopPort.add(serverTitlePort);
        settingsTopPort.add(serverAddressPort);
        settings.add(settingsTopPort);
        //Settings username
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new FlowLayout());
        username =new JTextField(10);
        JTextField usertitle = new JTextField("Username:",10);
        usertitle.setEditable(false);
        userPanel.add(usertitle);
        userPanel.add(username);
        settings.add(userPanel);

        //connect disconnect buttons
        JPanel ConnectPanel = new JPanel();
        ConnectPanel.setLayout(new FlowLayout());
        JButton connect = new JButton("Connect");
        connect.addActionListener(new connectListener());
        JButton disconnect = new JButton("disconnect");
        disconnect.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                client.disconnect();
            }
        });
        ConnectPanel.add(connect);
        ConnectPanel.add(disconnect);
        settings.add(ConnectPanel);

        //user input area
        JPanel userInputPanel = new JPanel();
        userInputPanel.setLayout(new FlowLayout());
        JButton send = new JButton("Send");
        userInput = new JTextField(50);
        userInput.addActionListener(new UserInputHandler());
        send.addActionListener(new UserInputHandler());
        userInputPanel.add(userInput);
        userInputPanel.add(send);

        //Userlist
        //user input area
        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new FlowLayout());
        usersList = new JTextArea(20,10);
        usersList.setEditable(false);
        usersPanel.add(usersList);
        frame.add(usersPanel, BorderLayout.WEST);

        //Finalizing
        settings.add(userInputPanel);
        settings.setPreferredSize(new Dimension(width, 250));
        frame.add(settings, BorderLayout.NORTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width,height);
        frame.setVisible(true);
    }

    private void addTxt(String in){
        //Should be replaced with a linked list for efficiency.
        for (int i=txtCounter-1; i>0; i--)
            scrollerArr[i] = scrollerArr[i-1];
        scrollerArr[0] = in;
        if (txtlength > txtCounter)
            txtCounter++;

        scroller = "";
        for (int i=0; i<txtCounter; i++) {
            if (scrollerArr[i]!= null)
                scroller = scroller + scrollerArr[i] + "\n";
        }
        chat.setText(scroller);
        repaint();
    }

    public void alert(String msg){
        JOptionPane.showMessageDialog(null, msg);
    }

    public void updateGuests(ArrayList<String> guests){
        usersList.setText("");
        for (String guest: guests){
            usersList.append(guest + "\n");
        }
    }
    public String gethost(){
        return serverAddress.getText();
    }
    public String getPort(){
        return serverAddressPort.getText();
    }

    public static class connectListener implements  ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String usernameTxt = username.getText();
            if (!client.validUsername(usernameTxt)){
                JOptionPane.showMessageDialog(null, "Username is invalid!");
            }
            else {
                String hostname = serverAddress.getText();
                String port = serverAddressPort.getText();
                client.setup(hostname, port, usernameTxt);
                new Thread(client).start();
            }
        }
    }
    public class UserInputHandler implements ActionListener{
        public void actionPerformed(ActionEvent e){
            String txt = userInput.getText();
            client.sendMsg(txt);
            userInput.selectAll();
        }
    }

    public void msg(String in){
        addTxt(in);
    }


}
