package client;

import server.Handler;
import server.Status;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplyHandler implements Handler {

    private static final Pattern CODE_PATTERN = Pattern.compile("\\d{3}");
    private static final String UNIMPLEMENTED = "Unimplemented";
    private static final Map<Action, ActionStrategy> STRATEGY_MAP = new HashMap<>();

    static {
        STRATEGY_MAP.put(Action.POST, (status, id, input) -> switch (status) {
            case OK -> "Server shutdown successful.";
            case BAD_REQUEST, FORBIDDEN, NOT_FOUND -> UNIMPLEMENTED;
        });
        STRATEGY_MAP.put(Action.GET, (status, id, input) -> switch (status) {
            case OK -> {
                byte[] fileBytes;
                try {
                    int length = input.readInt(); // read the length of the byte array
                    fileBytes = new byte[length];
                    input.readFully(fileBytes, 0, length); // read the incoming bytes into the variable
                } catch (IOException e) {
                    yield "Could not receive the file's data from the server.";
                }
                System.out.println("The file was downloaded! Specify a name for it: ");
                String fileName = Menu.getLineFromInput();
                File newFile = new File(Menu.PATH+fileName);
                try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
                    outputStream.write(fileBytes);
                    yield "File saved on the hard drive!";
                } catch (IOException e) {
                    yield "Could not save the file to the hard drive.";
                }
            }
            case NOT_FOUND -> "The response says that this file is not found!";
            case BAD_REQUEST, FORBIDDEN -> UNIMPLEMENTED;
        });
        STRATEGY_MAP.put(Action.PUT, (status, id, input) -> switch (status) {
            case OK -> "Response says that file is saved! ID = "+id;
            case FORBIDDEN -> "The response says that creating the file was forbidden!";
            case NOT_FOUND -> "The response says the file directory was not found.";
            case BAD_REQUEST -> UNIMPLEMENTED;
        });
        STRATEGY_MAP.put(Action.DELETE, (status, id, input) -> switch (status) {
            case OK -> "The response says that the file was successfully deleted!";
            case NOT_FOUND -> "The response says that the file was not found!";
            case BAD_REQUEST, FORBIDDEN -> UNIMPLEMENTED;
        });
    }

    private final Action action;

    public ReplyHandler(Action action) {
        this.action = action;
    }

    @Override
    public String runOperation(String reply, DataInputStream input, DataOutputStream output) {
        Status status;
        String id = null;
        Matcher m = CODE_PATTERN.matcher(reply);
        if (m.find()) {
            Optional<Status> optionalStatus = Status.getStatusFromCode(m.group());
            if (optionalStatus.isPresent()) {
                status = optionalStatus.get();
                String[] splitReply = reply.split(" ");
                if (splitReply.length == 2) {
                    id = splitReply[1];
                }
            } else {
                return "Invalid code from server \""+m.group()+"\"";
            }
        } else {
            return "Invalid response from server \""+reply+"\"";
        }
        return STRATEGY_MAP.get(action).getResponse(status, id, input);
    }
}
