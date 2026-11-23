package entity;

import java.util.*;

public class Battle {

    public static class Fighter {
        public final String id;
        public final String assetId;
        public final String name;
        public final boolean player;
        public final int maxHp, attack, defense, speed;

        public int hp;
        public boolean guarding;

        public Fighter(String name, boolean player, int hp,
                       int attack, int defense, int speed) {
            this(UUID.randomUUID().toString(), null, name, player,
                    hp, attack, defense, speed);
        }

        public Fighter(String id, String assetId, String name, boolean player,
                       int hp, int attack, int defense, int speed) {
            this.id = id;
            this.assetId = assetId;
            this.name = name;
            this.player = player;
            this.hp = this.maxHp = hp;
            this.attack = attack;
            this.defense = defense;
            this.speed = speed;
        }

        public boolean alive() {
            return hp > 0;
        }
    }

    public final List<Fighter> players;
    public final List<Fighter> enemies;

    private final List<Fighter> order = new ArrayList<>();
    private int cursor;
    private int turnNumber = 1;
    private final List<CombatLogEntry> log = new ArrayList<>();

    public boolean ended;

    public Battle(List<Fighter> players, List<Fighter> enemies) {
        this(players, enemies, true);
        log.add(new CombatLogEntry(turnNumber, "An enemy group approaches!"));
    }

    private Battle(List<Fighter> players, List<Fighter> enemies, boolean newGame) {
        this.players = players;
        this.enemies = enemies;

        order.addAll(players);
        order.addAll(enemies);

        if (newGame) {
            Collections.shuffle(order);
            order.sort(Comparator.comparingInt(
                    (Fighter f) -> f.speed).reversed());
        }
    }

    public List<CombatLogEntry> log() {
        return List.copyOf(log);
    }

    /** Captures values, never references to mutable fighters or Swing state. */
    public GameState snapshot(String savedAt) {
        return new GameState(GameState.FORMAT_VERSION, savedAt,
                players.stream().map(GameState.FighterState::from).toList(),
                enemies.stream().map(GameState.FighterState::from).toList(),
                order.stream().map(f -> f.id).toList(), cursor, ended,
                turnNumber, log);
    }

    public static Battle restore(GameState state) {
        List<Fighter> players = state.players().stream()
                .map(GameState.FighterState::restore).toList();
        List<Fighter> enemies = state.enemies().stream()
                .map(GameState.FighterState::restore).toList();
        Battle battle = new Battle(players, enemies, false);
        Map<String, Fighter> fighters = new HashMap<>();
        battle.order.forEach(f -> fighters.put(f.id, f));
        battle.order.clear();
        state.turnOrder().forEach(id -> battle.order.add(fighters.get(id)));
        battle.cursor = state.cursor();
        battle.ended = state.ended();
        battle.turnNumber = state.turnNumber();
        battle.log.addAll(state.log());
        return battle;
    }

    public Fighter current() {
        for (int i = 0; i < order.size(); i++) {
            Fighter f = order.get(cursor);

            if (f.alive()) {
                return f;
            }

            cursor = (cursor + 1) % order.size();
        }

        return null;
    }

    public List<Fighter> preview(int count) {
        List<Fighter> result = new ArrayList<>();

        if (ended || current() == null) {
            return result;
        }

        for (int i = 0; result.size() < count; i++) {
            Fighter f = order.get((cursor + i) % order.size());

            if (f.alive()) {
                result.add(f);
            }
        }

        return result;
    }

    public String attack(Fighter target) {
        Fighter actor = current();

        if (ended || actor == null || target == null
                || !target.alive()
                || actor.player == target.player
                || !order.contains(target)) {
            throw new IllegalStateException("Invalid attack target");
        }

        double guard = target.guarding ? 0.5 : 1;

        int damage = Math.max(1, (int) Math.round(
                (8 + 16.0 * actor.attack
                        / Math.max(1, target.defense)) * guard));

        damage = Math.min(damage, target.hp);

        target.hp -= damage;
        target.guarding = false;

        String result = actor.name + " hits " + target.name
                + " for " + damage + "."
                + (target.alive()
                ? ""
                : " " + target.name + " is defeated!");
        log.add(new CombatLogEntry(turnNumber, result));
        return result;
    }

    public String defend() {
        Fighter actor = current();
        if (ended || actor == null) {
            throw new IllegalStateException("The battle has ended");
        }
        actor.guarding = true;

        String result = actor.name + " braces for the next hit.";
        log.add(new CombatLogEntry(turnNumber, result));
        return result;
    }

    public void next() {
        if (ended) {
            return;
        }
        ended = players.stream().noneMatch(Fighter::alive)
                || enemies.stream().noneMatch(Fighter::alive);

        if (ended) {
            log.add(new CombatLogEntry(turnNumber, prompt()));
        } else {
            turnNumber++;
            cursor = (cursor + 1) % order.size();
            current().guarding = false;
        }
    }

    public String prompt() {
        if (ended) {
            return players.stream().anyMatch(Fighter::alive)
                    ? "Victory!"
                    : "Your party was defeated.";
        }

        Fighter f = current();

        return f.name + "'s turn."
                + (f.player ? " Choose Attack or Defend." : "");
    }
}
