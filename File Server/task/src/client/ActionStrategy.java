package client;

import server.Status;

import java.io.DataInputStream;

/**
 * Process the server's response based on the action that was taken.
 */
public interface ActionStrategy {
    /**
     * Generate a reply to notify the user of the outcome of the request.
     * @param status the operation's {@link Status}
     * @param id for {@link Action#PUT}, the id of the file that was saved
     * @param input used to get the file's bytes from the server
     * @return message summarising the operation's outcome
     */
    String getResponse(Status status, String id, DataInputStream input);
}
