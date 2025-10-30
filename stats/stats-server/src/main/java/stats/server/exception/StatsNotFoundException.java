package stats.server.exception;

public class StatsNotFoundException extends RuntimeException {
    public StatsNotFoundException(String message) {
        super(message);
    }
}
