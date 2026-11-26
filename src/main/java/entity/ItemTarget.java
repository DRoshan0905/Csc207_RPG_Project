package entity;

/** The only things an item needs to know about a fighter. */
public interface ItemTarget {
    String getName();

    boolean alive();

    int getHp();

    int getMaxHp();

    /** Adds HP without exceeding the maximum. */
    void restoreHp(int amount);

    /** The stat's value right now, including any reductions. */
    int getStat(Stat stat);

    /** The stat's value before any reductions. */
    int getBaseStat(Stat stat);

    /** Lowers the stat by the given amount for the rest of the battle. */
    void lowerStat(Stat stat, int amount);
}
