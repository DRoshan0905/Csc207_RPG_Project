package entity;

/** Combat stats that items and effects can change. */
public enum Stat {
    ATTACK("ATK"),
    DEFENSE("DEF");

    private final String label;

    Stat(String label) {
        this.label = label;
    }

    /** Short name for the UI, such as "ATK". */
    public String label() {
        return label;
    }
}
