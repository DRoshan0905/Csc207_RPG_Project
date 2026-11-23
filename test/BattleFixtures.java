import entity.Battle;
import entity.GameState;
import java.util.List;

final class BattleFixtures {
    static final String SAVED_AT = "2026-11-22T15:00:00Z";

    static Battle battle() {
        return new Battle(List.of(
                new Battle.Fighter("rain", "100000102", "Rain", true, 160, 34, 24, 100),
                new Battle.Fighter("fina", "100000302", "Fina", true, 125, 27, 18, 90)),
                List.of(new Battle.Fighter("monster", "10000003", "Monster 1", false,
                        105, 25, 18, 55)));
    }

    static GameState state() {
        return battle().snapshot(SAVED_AT);
    }
}
