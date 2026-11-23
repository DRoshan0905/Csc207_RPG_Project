import entity.Battle;
import entity.GameState;
import interface_adapter.SaveLoadController;
import org.junit.jupiter.api.Test;
import use_case.save_load.GameSaveRepository;
import use_case.save_load.LoadGameInteractor;
import use_case.save_load.SaveGameInteractor;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.*;

class SaveLoadInteractorTest {
    @Test
    void controllerCapturesOnGameThreadAndSavesDetachedStateThroughThePort() throws Exception {
        MemoryRepository repository = new MemoryRepository();
        SaveLoadController controller = controller(repository);
        assertFalse(controller.hasSave());
        Battle battle = BattleFixtures.battle();
        GameState checkpoint = controller.capture(battle);
        battle.attack(battle.enemies.get(0));
        battle.next();
        controller.save(checkpoint);
        assertTrue(controller.hasSave());
        assertEquals(BattleFixtures.SAVED_AT, controller.load().savedAt());
        assertEquals(105, controller.load().enemies().get(0).hp());
        assertEquals(1, controller.load().log().size());
    }

    @Test
    void repositoryFailureReachesTheCallerWithoutChangingBattleState() {
        MemoryRepository repository = new MemoryRepository();
        repository.fail = true;
        SaveLoadController controller = controller(repository);
        Battle battle = BattleFixtures.battle();
        GameState before = controller.capture(battle);
        assertThrows(IOException.class, () -> controller.save(before));
        assertThrows(IOException.class, controller::load);
        assertEquals(before, controller.capture(battle));
    }

    private SaveLoadController controller(GameSaveRepository repository) {
        return new SaveLoadController(new SaveGameInteractor(repository,
                Clock.fixed(Instant.parse(BattleFixtures.SAVED_AT), ZoneOffset.UTC)),
                new LoadGameInteractor(repository));
    }

    private static class MemoryRepository implements GameSaveRepository {
        GameState state;
        boolean fail;

        public boolean exists() { return state != null; }

        public void save(GameState value) throws IOException {
            if (fail) { throw new IOException("Disk full"); }
            state = value;
        }

        public GameState load() throws IOException {
            if (fail || state == null) { throw new IOException("Cannot read save"); }
            return state;
        }
    }
}
