package client;

/**
 * Represents all possible operations the server can execute.
 */
public enum Action {
    POST("exit"), GET("get"), PUT("save"), DELETE("delete");

    /**
     * Used to generate menu prompts.
     */
    private final String verb;
    /**
     * Get the action's associated verb.
     */
    public String getText() {
        return this.verb;
    }

    /**
     * Construct a new action.
     */
    Action(String verb) {
        this.verb = verb;
    }
}
