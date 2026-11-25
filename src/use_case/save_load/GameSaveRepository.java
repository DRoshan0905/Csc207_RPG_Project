package use_case.save_load;

import entity.GameState;
import java.io.IOException;

/** Persistence port; use cases do not know about files, JSON, or Swing. */
public interface GameSaveRepository {
    boolean exists();
    void save(GameState state) throws IOException;
    GameState load() throws IOException;
}
