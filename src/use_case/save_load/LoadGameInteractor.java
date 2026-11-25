package use_case.save_load;

import entity.GameState;
import java.io.IOException;

public final class LoadGameInteractor {
    private final GameSaveRepository repository;

    public LoadGameInteractor(GameSaveRepository repository) {
        this.repository = repository;
    }

    public boolean hasSave() {
        return repository.exists();
    }

    public GameState execute() throws IOException {
        return repository.load();
    }
}
