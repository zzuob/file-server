package server;

import java.util.Optional;

/**
 * Represents the outcome of a requested operation.
 */
public enum Status {
    OK(200), BAD_REQUEST(400), FORBIDDEN(403), NOT_FOUND(404);

    public final int code;

    Status(int code) { this.code = code; }

    public static Optional<Status> getStatusFromCode(String codeString) {
        int code;
        try {
            code = Integer.parseInt(codeString);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        for (Status status : Status.values()) {
            if (code == status.code) return Optional.of(status);
        }
        return Optional.empty();
    }
}
