package interface_adapter.items;

import java.util.Set;

/** One line of the item list, ready to display. */
public class ItemRow {
    private final String name;
    private final String description;
    private final String count;
    private final boolean enemyTarget;
    private final Set<Integer> usableTargets;

    public ItemRow(String name, String description, String count,
                   boolean enemyTarget, Set<Integer> usableTargets) {
        this.name = name;
        this.description = description;
        this.count = count;
        this.enemyTarget = enemyTarget;
        this.usableTargets = usableTargets;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCount() {
        return count;
    }

    /** True if this item is used on enemies instead of heroes. */
    public boolean targetsEnemies() {
        return enemyTarget;
    }

    /** Whether the item would work on this position of its target side. */
    public boolean canUseOn(int targetIndex) {
        return usableTargets.contains(targetIndex);
    }
}
