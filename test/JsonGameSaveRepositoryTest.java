import com.google.gson.Gson;
import com.google.gson.JsonObject;
import data_access.JsonGameSaveRepository;
import entity.Battle;
import entity.GameState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JsonGameSaveRepositoryTest {
    @TempDir Path temporary;

    @Test
    void savesAndLoadsACompleteBattleAsReadableJson() throws Exception {
        Path file = temporary.resolve("nested/save.json");
        JsonGameSaveRepository repository = new JsonGameSaveRepository(file);
        assertFalse(repository.exists());
        Battle battle = BattleFixtures.battle();
        battle.attack(battle.enemies.get(0));
        battle.next();
        GameState expected = battle.snapshot(BattleFixtures.SAVED_AT);
        repository.save(expected);
        assertTrue(repository.exists());
        assertEquals(expected, repository.load());
        assertTrue(Files.readString(file).contains("\n  \"formatVersion\": 1"));
        try (var files = Files.list(file.getParent())) {
            assertEquals(1, files.count(), "Temporary files must be cleaned up");
        }
    }

    @Test
    void replacesPreviousSaveOnlyWhenSavingAgain() throws Exception {
        JsonGameSaveRepository repository = new JsonGameSaveRepository(temporary.resolve("save.json"));
        repository.save(BattleFixtures.state());
        Battle newBattle = BattleFixtures.battle();
        assertEquals(BattleFixtures.state(), repository.load());
        newBattle.defend();
        newBattle.next();
        GameState updated = newBattle.snapshot(BattleFixtures.SAVED_AT);
        repository.save(updated);
        assertEquals(updated, repository.load());
    }

    @Test
    void missingFileProducesAReadableError() {
        JsonGameSaveRepository repository = new JsonGameSaveRepository(temporary.resolve("missing.json"));
        assertTrue(assertThrows(IOException.class, repository::load).getMessage().contains("No saved game"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "null", "[]", "{}", "{\"formatVersion\":1} trailing", "// comment\n{}"})
    void malformedJsonIsRejectedAndKept(String contents) throws Exception {
        Path file = temporary.resolve("save.json");
        Files.writeString(file, contents);
        assertThrows(IOException.class, new JsonGameSaveRepository(file)::load);
        assertEquals(contents, Files.readString(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"version", "missingHp", "missingGuard", "missingEnded", "hpTooHigh",
            "negativeHp", "duplicateId", "wrongSide", "unknownTurnId", "duplicateTurnId",
            "cursor", "ended", "emptyParty", "timestamp", "deadCurrent", "logTurn", "assetPath",
            "fractionalHp", "overflowVersion"})
    void inconsistentOrIncompleteGameStatesAreRejected(String corruption) throws Exception {
        JsonObject json = new Gson().toJsonTree(BattleFixtures.state()).getAsJsonObject();
        JsonObject hero = json.getAsJsonArray("players").get(0).getAsJsonObject();
        switch (corruption) {
            case "version" -> json.addProperty("formatVersion", 99);
            case "missingHp" -> hero.remove("hp");
            case "missingGuard" -> hero.remove("guarding");
            case "missingEnded" -> json.remove("ended");
            case "hpTooHigh" -> hero.addProperty("hp", 999);
            case "negativeHp" -> hero.addProperty("hp", -1);
            case "duplicateId" -> hero.addProperty("id", "monster");
            case "wrongSide" -> hero.addProperty("player", false);
            case "unknownTurnId" -> json.getAsJsonArray("turnOrder").set(0, new Gson().toJsonTree("unknown"));
            case "duplicateTurnId" -> json.getAsJsonArray("turnOrder").set(0, new Gson().toJsonTree("fina"));
            case "cursor" -> json.addProperty("cursor", -1);
            case "ended" -> json.addProperty("ended", true);
            case "emptyParty" -> json.getAsJsonArray("players").remove(0);
            case "timestamp" -> json.addProperty("savedAt", "yesterday");
            case "deadCurrent" -> hero.addProperty("hp", 0);
            case "logTurn" -> json.getAsJsonArray("log").get(0).getAsJsonObject().addProperty("turn", 20);
            case "assetPath" -> hero.addProperty("assetId", "../../secret");
            case "fractionalHp" -> hero.addProperty("hp", 100.5);
            case "overflowVersion" -> json.addProperty("formatVersion", 4294967297L);
            default -> fail("Unknown corruption");
        }
        Path file = temporary.resolve("save.json");
        String contents = json.toString();
        Files.writeString(file, contents);
        assertThrows(IOException.class, new JsonGameSaveRepository(file)::load);
        assertEquals(contents, Files.readString(file));
    }

    @Test
    void unwritableSaveLocationReportsFailureAndPreservesPreviousSave() throws Exception {
        Path directory = Files.createDirectory(temporary.resolve("read-only"));
        assumeTrue(Files.getFileStore(directory).supportsFileAttributeView("posix"),
                "Read-only permission simulation requires a POSIX filesystem");
        Path file = directory.resolve("save.json");
        JsonGameSaveRepository repository = new JsonGameSaveRepository(file);
        repository.save(BattleFixtures.state());
        String previous = Files.readString(file);
        var permissions = Files.getPosixFilePermissions(directory);
        try {
            Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("r-x------"));
            assumeFalse(Files.isWritable(directory), "Cannot simulate read-only directory as this user");
            assertThrows(IOException.class, () -> repository.save(BattleFixtures.state()));
            assertEquals(previous, Files.readString(file));
        } finally {
            Files.setPosixFilePermissions(directory, permissions);
        }
    }

    @Test
    void aFileCannotBeUsedAsTheSaveDirectory() throws Exception {
        Path blocker = temporary.resolve("not-a-directory");
        Files.writeString(blocker, "keep this file");
        JsonGameSaveRepository repository = new JsonGameSaveRepository(blocker.resolve("save.json"));
        assertThrows(IOException.class, () -> repository.save(BattleFixtures.state()));
        assertEquals("keep this file", Files.readString(blocker));
    }
}
