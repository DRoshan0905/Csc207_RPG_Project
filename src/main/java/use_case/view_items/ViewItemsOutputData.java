package use_case.view_items;

import java.util.List;
import java.util.Set;

public class ViewItemsOutputData {
    private final List<Entry> entries;

    public ViewItemsOutputData(List<Entry> entries) {
        this.entries = List.copyOf(entries);
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public static class Entry {
        private final String name;
        private final String description;
        private final int count;
        private final boolean enemyTarget;
        private final Set<Integer> usableTargets;

        public Entry(String name, String description, int count,
                     boolean enemyTarget, Set<Integer> usableTargets) {
            this.name = name;
            this.description = description;
            this.count = count;
            this.enemyTarget = enemyTarget;
            this.usableTargets = Set.copyOf(usableTargets);
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getCount() {
            return count;
        }

        /** True if this item is used on enemies instead of heroes. */
        public boolean isEnemyTarget() {
            return enemyTarget;
        }

        /** Positions on the target side this item would work on. */
        public Set<Integer> getUsableTargets() {
            return usableTargets;
        }
    }
}
