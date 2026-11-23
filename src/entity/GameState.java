package entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** Immutable, versioned checkpoint at the boundary between completed actions. */
public record GameState(int formatVersion, String savedAt,
                        List<FighterState> players, List<FighterState> enemies,
                        List<String> turnOrder, int cursor, boolean ended,
                        int turnNumber, List<CombatLogEntry> log) {
    public static final int FORMAT_VERSION = 1;

    public GameState {
        if (formatVersion != FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported save format version: " + formatVersion);
        }
        Instant.parse(savedAt);
        players = List.copyOf(players);
        enemies = List.copyOf(enemies);
        turnOrder = List.copyOf(turnOrder);
        log = List.copyOf(log);
        if (players.isEmpty() || enemies.isEmpty() || turnNumber < 1) {
            throw new IllegalArgumentException("Missing fighters or invalid turn number");
        }
        Set<String> ids = new HashSet<>();
        Stream.concat(players.stream(), enemies.stream()).forEach(f -> {
            if (!ids.add(f.id())) {
                throw new IllegalArgumentException("Duplicate fighter ID: " + f.id());
            }
        });
        if (players.stream().anyMatch(f -> !f.player())
                || enemies.stream().anyMatch(FighterState::player)) {
            throw new IllegalArgumentException("Fighter is on the wrong side");
        }
        if (turnOrder.size() != ids.size() || !ids.equals(new HashSet<>(turnOrder))
                || cursor < 0 || cursor >= turnOrder.size()) {
            throw new IllegalArgumentException("Invalid turn order or cursor");
        }
        boolean finished = players.stream().noneMatch(f -> f.hp() > 0)
                || enemies.stream().noneMatch(f -> f.hp() > 0);
        if (ended != finished) {
            throw new IllegalArgumentException("Battle outcome does not match remaining HP");
        }
        String currentId = turnOrder.get(cursor);
        if (!ended && Stream.concat(players.stream(), enemies.stream())
                .filter(f -> f.id().equals(currentId))
                .anyMatch(f -> f.hp() == 0)) {
            throw new IllegalArgumentException("Current fighter is defeated");
        }
        int previousTurn = 0;
        for (CombatLogEntry entry : log) {
            if (entry.turn() < previousTurn || entry.turn() > turnNumber) {
                throw new IllegalArgumentException("Combat log is out of turn order");
            }
            previousTurn = entry.turn();
        }
    }

    public record FighterState(String id, String assetId, String name, boolean player,
                               int maxHp, int hp, int attack, int defense,
                               int speed, boolean guarding) {
        public FighterState {
            if (id == null || id.isBlank() || assetId == null
                    || !assetId.matches("[0-9]{8,9}")
                    || name == null || name.isBlank() || maxHp <= 0
                    || hp < 0 || hp > maxHp || attack < 0 || defense < 0 || speed < 0
                    || (hp == 0 && guarding)) {
                throw new IllegalArgumentException("Invalid fighter data");
            }
        }

        public static FighterState from(Battle.Fighter fighter) {
            return new FighterState(fighter.id, fighter.assetId, fighter.name,
                    fighter.player, fighter.maxHp, fighter.hp, fighter.attack,
                    fighter.defense, fighter.speed, fighter.guarding);
        }

        public Battle.Fighter restore() {
            Battle.Fighter fighter = new Battle.Fighter(id, assetId, name, player,
                    maxHp, attack, defense, speed);
            fighter.hp = hp;
            fighter.guarding = guarding;
            return fighter;
        }
    }
}
