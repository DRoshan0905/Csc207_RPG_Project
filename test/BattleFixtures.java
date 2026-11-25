import entity.Battle;
import entity.GameState;
import java.util.List;

final class BattleFixtures {
    static final String SAVED_AT = "2026-11-22T15:00:00Z";

    static Battle battle() {
        return new Battle(List.of(
                new Battle.Fighter("warrior", "warrior", "Warrior", true, 160, 34, 24, 100),
                new Battle.Fighter("mage", "mage", "Mage", true, 125, 27, 18, 90)),
                List.of(new Battle.Fighter("monster", "slime", "Slime", false,
                        105, 25, 18, 55)));
    }

    static GameState state() {
        return battle().snapshot(SAVED_AT);
    }
}
