package use_case.save_load;

import entity.Battle;
import entity.GameState;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;

public final class SaveGameInteractor {
    private final GameSaveRepository repository;
    private final Clock clock;

    public SaveGameInteractor(GameSaveRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Call on the game thread while combat is paused at a completed action. */
    public GameState capture(Battle battle) {
        return battle.snapshot(Instant.now(clock).toString());
    }

    /** The detached snapshot can safely be written on a background thread. */
    public void execute(GameState state) throws IOException {
        repository.save(state);
    }
}
