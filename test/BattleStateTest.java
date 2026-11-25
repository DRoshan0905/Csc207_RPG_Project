import entity.Battle;
import entity.GameState;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BattleStateTest {
    @Test
    void snapshotRestoresHpAssetsSidePositionsTurnAndHistory() {
        Battle battle = BattleFixtures.battle();
        battle.attack(battle.enemies.get(0));
        battle.next();
        GameState state = battle.snapshot(BattleFixtures.SAVED_AT);
        Battle restored = Battle.restore(state);

        assertEquals(state, restored.snapshot(BattleFixtures.SAVED_AT));
        assertEquals("mage", restored.current().id);
        assertEquals("slime", restored.enemies.get(0).assetId);
        assertNotSame(battle.players.get(0), restored.players.get(0));
        assertEquals(List.of("warrior", "mage", "monster"), state.turnOrder());
    }

    @Test
    void guardingSurvivesRestorationAndReducesTheNextHit() {
        Battle battle = BattleFixtures.battle();
        battle.defend();
        battle.next();
        battle.defend();
        battle.next();
        Battle restored = Battle.restore(battle.snapshot(BattleFixtures.SAVED_AT));
        assertTrue(restored.players.get(0).guarding);
        assertEquals("monster", restored.current().id);

        String expected = battle.attack(battle.players.get(0));
        String actual = restored.attack(restored.players.get(0));
        assertEquals(expected, actual);
        assertEquals(148, restored.players.get(0).hp);
        assertFalse(restored.players.get(0).guarding);
    }

    @Test
    void snapshotIsDetachedFromFutureCombat() {
        Battle battle = BattleFixtures.battle();
        GameState before = battle.snapshot(BattleFixtures.SAVED_AT);
        battle.attack(battle.enemies.get(0));
        battle.next();
        assertEquals(105, before.enemies().get(0).hp());
        assertEquals(1, before.log().size());
        assertThrows(UnsupportedOperationException.class, () -> before.log().clear());
    }

    @Test
    void defeatedFightersStayDeadAndAreSkippedAfterRestoring() {
        Battle battle = BattleFixtures.battle();
        battle.players.get(1).hp = 0;
        battle.defend();
        battle.next();
        Battle restored = Battle.restore(battle.snapshot(BattleFixtures.SAVED_AT));
        assertEquals(0, restored.players.get(1).hp);
        assertEquals("monster", restored.current().id);
        assertTrue(restored.preview(8).stream().noneMatch(f -> f.id.equals("mage")));
    }

    @Test
    void victoryAndOutcomeLogRestoreWithoutAddingDuplicateEvents() {
        Battle battle = BattleFixtures.battle();
        battle.enemies.get(0).hp = 1;
        battle.attack(battle.enemies.get(0));
        battle.next();
        GameState state = battle.snapshot(BattleFixtures.SAVED_AT);
        Battle restored = Battle.restore(state);
        assertTrue(restored.ended);
        assertEquals("Victory!", restored.prompt());
        assertEquals("Victory!", restored.log().get(2).message());
        restored.next();
        assertEquals(state, restored.snapshot(BattleFixtures.SAVED_AT));
        assertThrows(IllegalStateException.class, restored::defend);
    }

    @Test
    void defeatRestoresAsDefeat() {
        Battle battle = BattleFixtures.battle();
        battle.players.forEach(f -> f.hp = 0);
        battle.next();
        Battle restored = Battle.restore(battle.snapshot(BattleFixtures.SAVED_AT));
        assertTrue(restored.ended);
        assertEquals("Your party was defeated.", restored.prompt());
        assertTrue(restored.preview(5).isEmpty());
    }

    @Test
    void savedTurnOrderIsRestoredVerbatimRatherThanSortedAgain() {
        GameState original = BattleFixtures.state();
        GameState ordered = new GameState(GameState.FORMAT_VERSION, original.savedAt(), original.players(), original.enemies(),
                List.of("monster", "mage", "warrior"), 1, false, 1, original.log(), original.backgroundId(), original.inventory());
        assertEquals(ordered, Battle.restore(ordered).snapshot(BattleFixtures.SAVED_AT));
    }
}
