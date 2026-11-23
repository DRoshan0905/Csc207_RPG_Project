package interface_adapter;

import entity.Battle;
import entity.GameState;
import use_case.save_load.LoadGameInteractor;
import use_case.save_load.SaveGameInteractor;
import java.io.IOException;

/** UI-facing entry points. Swing and storage implementations are wired at startup. */
public final class SaveLoadController {
    private final SaveGameInteractor save;
    private final LoadGameInteractor load;

    public SaveLoadController(SaveGameInteractor save, LoadGameInteractor load) {
        this.save = save;
        this.load = load;
    }

    public boolean hasSave() {
        return load.hasSave();
    }

    public GameState capture(Battle battle) {
        return save.capture(battle);
    }

    public void save(GameState state) throws IOException {
        save.execute(state);
    }

    public GameState load() throws IOException {
        return load.execute();
    }
}
