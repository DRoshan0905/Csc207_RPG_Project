package entity;

/** The only things an item needs to know about a fighter. */
public interface ItemTarget {
    String getName();

    boolean alive();

    int getHp();

    int getMaxHp();

    /** Adds HP without exceeding the maximum. */
    void restoreHp(int amount);
}
