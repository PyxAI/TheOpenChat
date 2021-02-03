public class TheOpenChatClient {
    private static ChatClientGui  ui;
    private static ChatClient  client;
    public static void main(String[] args){
        client = new ChatClient();
        ui = new ChatClientGui(client);
        new Thread(ui).start();
    }
}




