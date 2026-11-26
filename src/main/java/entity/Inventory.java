package entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tracks how many of each item the party has. */
public class Inventory {
    private final Map<Item, Integer> counts = new LinkedHashMap<>();

    public void add(Item item, int quantity) {
        counts.merge(item, quantity, Integer::sum);
    }

    public int count(Item item) {
        return counts.getOrDefault(item, 0);
    }

    /** Items with at least one left, in the order they were added. */
    public List<Item> available() {
        List<Item> result = new ArrayList<>();

        counts.forEach((item, count) -> {
            if (count > 0) {
                result.add(item);
            }
        });

        return result;
    }

    /** Returns the available item with this name, or null. */
    public Item find(String name) {
        for (Item item : available()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }

        return null;
    }

    public void consume(Item item) {
        if (count(item) <= 0) {
            throw new IllegalStateException("None left");
        }

        counts.merge(item, -1, Integer::sum);
    }
}
