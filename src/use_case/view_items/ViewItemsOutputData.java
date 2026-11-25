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
        private final Set<Integer> usableHeroes;

        public Entry(String name, String description, int count,
                     Set<Integer> usableHeroes) {
            this.name = name;
            this.description = description;
            this.count = count;
            this.usableHeroes = Set.copyOf(usableHeroes);
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

        /** Positions of the heroes this item would work on. */
        public Set<Integer> getUsableHeroes() {
            return usableHeroes;
        }
    }
}
