package server;

import client.Action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler implements Handler {

    /**
     * Used to split input request into separate parameters.
     */
    private static final Pattern splitter = Pattern.compile("\\s");
    /**
     * Default value returned on invalid request.
     */
    private static final List<String> invalid = new ArrayList<>() {
        { add("N/A"); }
    };
    /**
     * Used for read/write operations.
     */
    private final Data data;

    /**
     * Initializes the data object.
     */
    public CommandHandler(Data data) {
        this.data = data;
    }

    /**
     * Split a request string into a list of parameters.
     * @param input string from client
     * @return parameter list
     */
    private List<String> splitCommands(String input) {
        List<String> result = new ArrayList<>();
        Matcher m = splitter.matcher(input);
        if (m.find()) {
            int startIndex = 0, endIndex;
            // process the command (argument 0)
            Action action;
            try {
                action = Action.valueOf(input.substring(startIndex, m.start()));
            } catch (IllegalArgumentException e) {
                return invalid;
            }
            result.add(action.name());
            startIndex = m.end(); // index of the start of the first parameter (argument 1)
            int paramLength = switch (action) {
                case GET, PUT -> 2;
                case DELETE -> 1;
                case POST -> 0;
            };
            int parameters = 0; // how many parameters have been collected
            while (m.find() && parameters < paramLength && startIndex < input.length()) {
                endIndex = m.start(); // end of current parameter
                result.add(input.substring(startIndex, endIndex));
                startIndex = m.end(); // start of next parameter
                parameters++;
            }
            if (input.length() != startIndex) {
                result.add(input.substring(startIndex));
            }
        }
        return validateCommands(result);
    }

    /**
     * Validate a list of parameters.
     * @param result list to validate
     * @return the input list if valid, else the {@link #invalid} list
     */
    private List<String> validateCommands(List<String> result) {
        if (result.isEmpty()) {
            return invalid;
        } else {
            String first = result.get(0);
            if (("GET".equals(first) || "DELETE".equals(first))) {
                if (result.size() != 3) return invalid;
                String second = result.get(1);
                if (!("BY_ID".equals(second) || "BY_NAME".equals(second))) return invalid;
            } else if ("PUT".equals(first) && result.size() > 2) {
                return invalid;
            }
        }
        return result;
    }

    /**
     * Execute a given request from the client.
     * @param request input string
     */
    @Override
    public String runOperation(String request, DataInputStream input, DataOutputStream output) {
        Reply reply = new Reply();
        if (Server.SHUTDOWN.equals(request)) {
            reply.setAction(Action.POST);
            reply.setStatus(data.onTerminate());
        } else {
            List<String> commands = splitCommands(request);
            if (Arrays.stream(Action.values())
                                    .anyMatch(action -> action.name().equals(commands.get(0)))) {
                reply.setAction(Action.valueOf(commands.get(0)));
                List<String> parameters = commands.size() > 1 ? commands.subList(1, commands.size()) : new ArrayList<>();
                reply.execute(data, parameters, input);
            } else {
                reply.setStatus(Status.BAD_REQUEST);
            }
        }
        return reply.updateClient(output);
    }
}
