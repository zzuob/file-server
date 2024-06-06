package server;

import java.io.*;
import java.net.*;

public class Server {

    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 23456;
    public static final String SHUTDOWN = "POST /shutdown";

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))) {
            System.out.println("Server started!");
            String request = "";
            CommandHandler handler = new CommandHandler(new Data());
            while (!SHUTDOWN.equals(request)) {
                try (Socket socket = server.accept(); // accept a new client
                     DataInputStream input = new DataInputStream(socket.getInputStream());
                     DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
                        request = input.readUTF(); // read a message from the client
                        System.out.println("Received: " + request);
                        String sent = handler.runOperation(request, input, output);
                        System.out.println("Sent: " + sent);
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}