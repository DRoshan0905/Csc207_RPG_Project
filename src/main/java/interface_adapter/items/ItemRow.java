package interface_adapter.items;

import java.util.Set;

/** One line of the item list, ready to display. */
public class ItemRow {
    private final String name;
    private final String description;
    private final String count;
    private final Set<Integer> usableHeroes;

    public ItemRow(String name, String description, String count,
                   Set<Integer> usableHeroes) {
        this.name = name;
        this.description = description;
        this.count = count;
        this.usableHeroes = usableHeroes;
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

    public boolean canUseOn(int heroIndex) {
        return usableHeroes.contains(heroIndex);
    }
}
