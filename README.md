# RPG Battle

A Java Swing combat prototype with local save/continue and a persistent combat log.
The current game is one encounter, with five heroes and three randomly selected monsters.

## Run and test

Requires **JDK 17 or newer**. The Maven wrapper downloads Maven and dependencies on its
first run; a separate Maven installation is not needed.

```sh
./mvnw compile exec:java
./mvnw test
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.
In IntelliJ IDEA, import/open `pom.xml` as a Maven project, select a JDK of at least 17,
reload Maven dependencies, and run `Main.main`.
The existing `src/` layout is retained; tests live in `test/`.

First-time art loading requires internet access. The existing `FFBE_API` downloads
art from a GitHub-hosted FFBE asset collection and caches it in `ffbe-cache/`, relative
to the working directory. Cached assets can be used offline. Saving and loading game
state do not require an external API, account, or server. The course's external API
requirement is separate: confirm with the TA whether the existing HTTP asset
integration qualifies.

## Save and continue

- **New Game** creates a new encounter without changing the previous saved game.
- **Continue** restores the saved encounter, including the same monster art, HP,
  stats, guarding, exact turn order, current turn, and combat history.
- **View Saved Log** shows the saved history without downloading or loading artwork.
- **Save** writes the current battle to the single save slot. A new game asks before
  replacing an earlier save. Saving the continued battle updates its slot directly.
- **Save & Exit** closes the window only after saving succeeds.
- **Battle Log** opens a scrollable history of completed combat events and pauses
  combat until the dialog is closed.
- Closing a battle with unsaved changes offers Save & Exit, Exit Without Saving, or
  Cancel. There is no automatic save; log entries persist when the battle is saved.

If a save, log, or exit request arrives during an attack, the action finishes first.
The checkpoint is taken **after damage and turn advancement**, before the next action.
Combat stays paused while the save is written. Continue resumes at that checkpoint;
animation frames and selected targets are reset. Future random enemy choices are made
normally. Finished battles can also be saved and reopened with their outcome intact.

The save file is UTF-8 JSON at:

```text
~/.rpg-project/save.json
```

Here `~` is Java's `user.home` directory. This location is independent of the project's
working directory. Saves are local to this computer/user and are not committed to Git.
The format has a version field to allow future migrations. Artwork stays in the asset
cache, not in the save. Loading validates the JSON structure, HP/stats, fighter IDs,
turn references, battle status, and log ordering. Missing or invalid saves show a
readable error and leave New Game available. Unsupported art IDs or party sizes are
rejected before loading art. Invalid files are not deleted or automatically replaced.

Writes go to a temporary file in the save directory, are flushed to disk, and then
replace the save with an atomic move where the filesystem supports it. A regular
replacement move is used where atomic moves are unsupported. Save errors keep the
current battle open. This is one slot for one running game; simultaneous application
instances and cloud synchronization are not supported.

## Design

The save/continue feature follows inward dependencies:

```text
GameWindow / Main (Swing)
        -> interface_adapter.SaveLoadController
        -> use_case.save_load (save/load interactors and repository port)
        -> entity (Battle, GameState, CombatLogEntry)

data_access.JsonGameSaveRepository -> repository port + GameState
```

- `Battle` owns combat rules and records successful attack/defend events and outcomes.
  Its restore path bypasses the new-game shuffle and preserves fighter identity.
- `GameState` is an immutable snapshot of fighter values, ordered rosters, turn order,
  cursor, ended status, turn number, and history. It contains no Swing or Gson types.
- `SaveGameInteractor` captures a detached snapshot using an injectable clock, then
  saves through `GameSaveRepository`. Capturing happens on Swing's event thread;
  disk writes use the immutable snapshot on a worker thread.
- `LoadGameInteractor` retrieves a validated snapshot through the same repository port.
- `JsonGameSaveRepository` uses Gson and implements the filesystem details. It can be
  replaced by another storage adapter without changing the combat model or use cases.
- `GameWindow` wires dependencies, presents menus/status/errors, and performs file and
  asset loading in `SwingWorker`s. `Main` retains combat rendering and animations and
  exposes a checkpoint callback that pauses between completed actions.
- `FFBE_API.load(Battle)` loads art for restored fighters rather than constructing a
  new random encounter.

The original prototype still places combat interaction and animation sequencing in
`Main`; this feature does not claim to refactor the entire application into Clean
Architecture. Future work could extract a combat use case/controller, add multiple
save slots, or extend `GameState` with campaign progress and migrations.

Tests cover snapshot isolation and restoration, guarding, dead fighters, exact turn
order, victory/defeat, JSON round trips and validation, replacement and write failures,
use-case storage failures, and an actual Swing attack followed by a queued checkpoint.
