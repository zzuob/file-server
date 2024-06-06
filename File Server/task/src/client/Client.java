package client;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;

    public static void main(String[] args) {
        Menu menu = new Menu();
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output  = new DataOutputStream(socket.getOutputStream())
        ) {
            int choice = menu.chooseAction(Menu.MAIN_PROMPT, Action.values().length, true);
            Action action = Action.values()[choice];
            ReplyHandler handler = new ReplyHandler(action);
            String request = menu.createRequest(action);
            output.writeUTF(request); // send a request to the server
            if (action == Action.PUT) {
                byte[] content = menu.getFileBytes();
                output.writeInt(content.length);
                output.write(content);
            }
            System.out.println("The request was sent.");
            // read and process the reply from the server, then print the outcome
            System.out.println(handler.runOperation(input.readUTF(), input, output));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
