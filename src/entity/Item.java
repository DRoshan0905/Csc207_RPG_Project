package entity;

import java.util.*;

public interface Item {
    String name();

    /**
     * true = used on your heroes, false = used on enemies
     */
    boolean targetsAllies();

    boolean canUse(Battle.Fighter target);

    /**
     * Applies the effect and returns the battle message.
     */
    String use(Battle.Fighter user, Battle.Fighter target);


    class Healing implements Item {
        private final String name;
        private final int amount;

        public Healing(String name, int amount) {
            if (name == null || name.isBlank() || amount <= 0) {
                throw new IllegalArgumentException("Invalid healing item");
            }
            this.name = name;
            this.amount = amount;
        }

        public String name() {
            return name;
        }

        public int amount() {
            return amount;
        }

        public boolean targetsAllies() {
            return true;
        }

        public boolean canUse(Battle.Fighter t) {
            return t.alive() && t.hp < t.maxHp;
        }

        public String use(Battle.Fighter user, Battle.Fighter t) {
            int healed = Math.min(amount, t.maxHp - t.hp);
            t.hp += healed;
            return user.name + " uses " + name + " on " + t.name
                    + " (+" + healed + " HP).";
        }
    }

    class Revive implements Item {
        public String name() {
            return "Revive";
        }

        public boolean targetsAllies() {
            return true;
        }

        public boolean canUse(Battle.Fighter t) {
            return !t.alive();
        }

        public String use(Battle.Fighter user, Battle.Fighter t) {
            t.hp = t.maxHp / 2;
            return user.name + " revives " + t.name + "!";
        }
    }

    class Bag {
        private final Map<Item, Integer> counts = new LinkedHashMap<>();

        public void add(Item item, int qty) {
            if (item == null || qty < 0) {
                throw new IllegalArgumentException("Invalid inventory entry");
            }
            counts.merge(item, qty, Integer::sum);
        }

        /** Includes empty stacks so save/continue preserves item ordering and quantities. */
        public List<GameState.ItemState> snapshot() {
            List<GameState.ItemState> entries = new ArrayList<>();
            counts.forEach((item, quantity) -> entries.add(GameState.ItemState.from(item, quantity)));
            return List.copyOf(entries);
        }

        public int count(Item item) {
            return counts.getOrDefault(item, 0);
        }

        public List<Item> available() {
            List<Item> list = new ArrayList<>();
            counts.forEach((item, n) -> {
                if (n > 0) list.add(item);
            });
            return list;
        }

        public void consume(Item item) {
            if (count(item) <= 0) throw new IllegalStateException("None left");
            counts.merge(item, -1, Integer::sum);
        }
    }
}
