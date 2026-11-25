import data_access.JsonGameSaveRepository;
import entity.Battle;
import entity.GameState;
import entity.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ItemSaveStateTest {
    @TempDir Path temporary;

    @Test
    void healingPersistsHpQuantityAndHistoryAndCanBeUsedAgainAfterContinue() throws Exception {
        Battle battle = BattleFixtures.battle();
        battle.players.get(0).hp = 70;
        Item potion = named(battle, "Potion");
        String event = battle.useItem(potion, battle.players.get(0));
        battle.next();
        GameState checkpoint = battle.snapshot(BattleFixtures.SAVED_AT);
        JsonGameSaveRepository repository = new JsonGameSaveRepository(temporary.resolve("save.json"));
        repository.save(checkpoint);
        Battle restored = Battle.restore(repository.load());

        assertEquals(checkpoint, restored.snapshot(BattleFixtures.SAVED_AT));
        assertEquals(100, restored.players.get(0).hp);
        assertEquals(2, restored.bag.count(named(restored, "Potion")));
        assertEquals(event, restored.log().get(1).message());
        restored.players.get(1).hp = 40;
        Item restoredPotion = named(restored, "Potion");
        restored.useItem(restoredPotion, restored.players.get(1));
        restored.next();
        assertEquals(70, restored.players.get(1).hp);
        assertEquals(1, restored.bag.count(restoredPotion));
        assertEquals(2, battle.bag.count(potion), "Restored bags must be independent");
        assertEquals(3, restored.log().size());
    }

    @Test
    void depletedInventoryStaysEmptyInsteadOfReceivingStartingItems() {
        Battle battle = BattleFixtures.battle();
        for (Item item : battle.bag.available()) {
            while (battle.bag.count(item) > 0) {
                battle.bag.consume(item);
            }
        }
        GameState checkpoint = battle.snapshot(BattleFixtures.SAVED_AT);
        Battle restored = Battle.restore(checkpoint);
        assertTrue(restored.bag.available().isEmpty());
        assertEquals(3, checkpoint.inventory().size());
        assertTrue(checkpoint.inventory().stream().allMatch(stack -> stack.quantity() == 0));
        assertEquals(checkpoint.inventory(), restored.bag.snapshot());
    }

    @Test
    void revivalAndItsConsumedItemSurviveContinue() {
        Battle battle = BattleFixtures.battle();
        battle.players.get(1).hp = 0;
        battle.useItem(named(battle, "Revive"), battle.players.get(1));
        battle.next();
        Battle restored = Battle.restore(battle.snapshot(BattleFixtures.SAVED_AT));
        assertEquals(62, restored.players.get(1).hp);
        assertEquals("mage", restored.current().id);
        assertTrue(restored.bag.available().stream().noneMatch(item -> item.name().equals("Revive")));
        assertEquals("Warrior revives Mage!", restored.log().get(1).message());
    }

    @Test
    void invalidItemUseDoesNotConsumeOrLogAnything() {
        Battle battle = BattleFixtures.battle();
        GameState before = battle.snapshot(BattleFixtures.SAVED_AT);
        Item potion = named(battle, "Potion");
        assertThrows(IllegalStateException.class, () -> battle.useItem(potion, battle.players.get(0)));
        assertFalse(battle.canUse(null, battle.players.get(0)));
        Battle.Fighter outsider = new Battle.Fighter("outside", "warrior", "Outsider", true, 100, 1, 1, 1);
        outsider.hp = 20;
        assertFalse(battle.canUse(potion, outsider));
        assertEquals(before, battle.snapshot(BattleFixtures.SAVED_AT));
    }

    @Test
    void snapshotsDoNotChangeWhenAnItemIsConsumedLater() {
        Battle battle = BattleFixtures.battle();
        GameState before = battle.snapshot(BattleFixtures.SAVED_AT);
        battle.bag.consume(named(battle, "Potion"));
        assertEquals(3, before.inventory().get(0).quantity());
        assertThrows(UnsupportedOperationException.class, () -> before.inventory().clear());
    }

    private Item named(Battle battle, String name) {
        return battle.bag.available().stream().filter(item -> item.name().equals(name)).findFirst().orElseThrow();
    }
}
