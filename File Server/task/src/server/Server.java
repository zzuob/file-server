package server;

import client.Binary;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.*;

public class Server {

    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 23456;
    private static final Data data = new Data();

    static class RequestHandler implements Callable<Boolean> {

        private final Socket socket;

        public RequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public Boolean call() {
            String threadName = Thread.currentThread().getName();
            String startMsg = String.format("Thread %s started", threadName);
            System.out.println(startMsg);
            try (
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream())
            ) {
                String request = input.readUTF();
                System.out.printf("%s Received: %s\n", threadName, request);
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
                    int length = input.readInt();
                    content = new byte[length];
                    input.readFully(content, 0, content.length);
                }
                String response = switch (command) {
                    case "POST" -> "200";
                    case "GET" -> data.get(fileName);
                    case "PUT" -> data.put(fileName, content);
                    case "DELETE" -> data.delete(fileName);
                    default -> "400";
                };
                output.writeUTF(response);
                if ("GET".equals(command) && "200".equals(response)) {
                    content = data.getFileBytes(fileName);
                    Binary.sendBytesToStream(output, content);
                }
                System.out.println("Sent: " + response);
                socket.close();
                return !"POST /shutdown".equals(request);
            } catch (IOException e) {
                System.out.printf("Thread %s encountered an error:\n", threadName);
                e.printStackTrace();
            }
            return true;
        }
    }
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))) {
            System.out.println("Server started!");
            boolean serverIsAlive = true;
            ExecutorService executor = Executors.newFixedThreadPool(4);
            while (serverIsAlive) {
                Future<Boolean> isUp = executor.submit(new RequestHandler(server.accept()));
                serverIsAlive = isUp.get();
                }
            executor.shutdown();
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}