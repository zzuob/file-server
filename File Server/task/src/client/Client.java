package client;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;

    public static String getLineFromInput() {
        Scanner scan = new Scanner(System.in);
        if (scan.hasNextLine()) {
            return scan.nextLine();
        } else {
            return "";
        }
    }
    private static int chooseAction() {
        int action = -1;
        while (!(0 <= action && action <= 3)) {
            System.out.print("Enter action (1 - get a file, 2 - create a file, 3 - delete a file): ");
            String input = getLineFromInput();
            if (input.matches("\\d+")) {
                action = Integer.parseInt(input);
            } else if ("exit".equals(input)) {
                action = 0;
            }
        }
        return action;
    }

    private static String createRequest(int action) {
        if (action == 0) {
            return "POST /shutdown";
        }
        String command = switch (action) {
            case 1 -> "GET";
            case 2 -> "PUT";
            case 3 -> "DELETE";
            default -> "?"; // impossible if chooseAction() is used to generate the action
        };
        StringBuilder request = new StringBuilder(command);
        request.append(" ");
        System.out.print("Enter filename: ");
        request.append(getLineFromInput());
        if ("PUT".equals(command)) {
            request.append(" ");
            System.out.print("Enter file content: ");
            request.append(getLineFromInput());
        }
        return request.toString();
    }

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream())
        ) {
            int action = chooseAction();
            String request = createRequest(action);
            output.writeUTF(request); // send a message to the server
            System.out.println("The request was sent.");
            String code = input.readUTF(); // read the reply from the server
            switch (action) {
                case 0 -> {
                    if ("200".equals(code)) System.out.println("Server shutdown successful");
                }
                case 1 -> System.out.println("unimplemented");
                case 2 -> {
                    switch (code) {
                        case "200" -> System.out.println("The response says that the file was created!");
                        case "403" -> System.out.println("The response says that creating the file was forbidden!");
                        case "404" -> System.out.println("The response says the file directory was not found");
                    }
                }
                case 3 -> System.out.println("unimplemented");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
