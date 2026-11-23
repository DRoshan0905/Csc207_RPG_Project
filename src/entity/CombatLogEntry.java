package entity;

/** A completed combat event, numbered by the turn on which it happened. */
public record CombatLogEntry(int turn, String message) {
    public CombatLogEntry {
        if (turn < 1 || message == null || message.isBlank()) {
            throw new IllegalArgumentException("Invalid combat log entry");
        }
    }

    @Override
    public String toString() {
        return "Turn " + turn + ": " + message;
    }
}
