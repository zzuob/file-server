package client;

import server.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Menu {

    private static final int START_INDEX = 1;
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");
    public static final String MAIN_PROMPT;
    public static final String PATH = Data.ROOT+"/client/data/";
    private String inputFileName;

    static {
        StringBuilder sb = new StringBuilder("Enter action (");
        for (int i = START_INDEX; i < Action.values().length; i++) {
            sb.append(i).append(" - ").append(Action.values()[i].getText()).append(" a file,");
        }
        sb.deleteCharAt(sb.length()-1).append("): ");
        MAIN_PROMPT = sb.toString();
    }

    /**
     * Get a line of input from the user.
     * @return the input string
     */
    public static String getLineFromInput() {
        Scanner scan = new Scanner(System.in);
        if (scan.hasNextLine()) {
            return scan.nextLine().trim();
        } else {
            return "";
        }
    }

    /**
     * Loop until user enters a number representing a valid action or "exit"
     * @param msg prompt to be shown to the user
     * @param actionLength how many choices
     * @param zeroAllowed is zero valid? otherwise start from 1
     * @return integer value of action chosen, 0 if choice is "exit"
     */
    public int chooseAction(String msg, int actionLength, boolean zeroAllowed) {
        while (true) {
            System.out.println(msg);
            String input = getLineFromInput();
            if (DIGIT_PATTERN.matcher(input).matches()) {
                int value = Integer.parseInt(input);
                if (START_INDEX <= value && value <= actionLength) {
                    return value;
                }
            } else if ("exit".equals(input) && zeroAllowed) { // 0 is reserved for "exit" commands
                return 0;
            }
        }
    }

    /**
     * Create a request to be sent to the server from the user's input.
     * @param action operation for the server to execute
     * @return formatted request string
     */
    public String createRequest(Action action) {
        String command = action.name();
        StringBuilder request = new StringBuilder(command);
        request.append(" ");
        switch (action) {
            case POST: // create a shutdown command
                request.append("/shutdown");
                break;
            case GET,DELETE:
                String prompt1 = String.format("Do you want to %s the file by name or by id (1 - name, 2 - id): ",
                        action.getText());
                // create the second prompt and add the BY_NAME/BY_ID flag
                String prompt2 = "";
                switch (chooseAction(prompt1, 2, false)) {
                    case 1:
                        prompt2 = "Enter name of the file: ";
                        request.append("BY_NAME ");
                        break;
                    case 2:
                        prompt2 = "Enter id: ";
                        request.append("BY_ID ");
                        break;
                }
                System.out.println(prompt2);
                request.append(getLineFromInput()); // get the last parameter
                break;
            case PUT:
                System.out.println("Enter name of the file: ");
                inputFileName = getLineFromInput();
                System.out.println("Enter name of the file to be saved on the server: ");
                request.append(getLineFromInput()); // can be empty
                break;
        }
        return request.toString();
    }

    /**
     * Retrieve the bytes from the input file
     * @return byte array of file data
     */
    public byte[] getFileBytes() {
        try {
            return Files.readAllBytes(Path.of(PATH+inputFileName));
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
