package server;

import java.io.*;
import java.net.*;

public class Server {

    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 23456;

    public static String getContent(String[] request) {
        if (request.length >= 3) {
            StringBuilder content = new StringBuilder();
            for (int i = 2; i < request.length; i++) {
                content.append(request[i]).append(" ");
            }
            return content.toString();
        }
        return "";
    }
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))) {
            System.out.println("Server started!");
            String request = "";
            Data data = new Data();
            while (!"POST /shutdown".equals(request)) {
                    try (
                            Socket socket = server.accept(); // accept a new client
                            DataInputStream input = new DataInputStream(socket.getInputStream());
                            DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                    ) {
                            request = input.readUTF(); // read a message from the client
                            System.out.println("Received: " + request);
                            String[] components = request.split(" ");
                            String command, fileName = null;
                            byte[] content = new byte[0];
                            if (components.length >= 2) {
                                command = components[0];
                                fileName = components[1];
                            } else {
                                command = "N/A";
                            }
                            if ("PUT".equals(command)) {
                                content = client.Binary.getBytesFromStream(input);
                            }
                            String response = switch (command) {
                                case "POST" -> "200";
                                case "GET" -> data.get(fileName);
                                case "PUT" -> data.put(fileName, content);
                                case "DELETE" -> data.delete(fileName);
                                default -> "400";
                            };
                            output.writeUTF(response);
                            System.out.println("Sent: " + response);

                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}