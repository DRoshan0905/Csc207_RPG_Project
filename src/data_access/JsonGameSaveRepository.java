package data_access;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import entity.GameState;
import use_case.save_load.GameSaveRepository;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/** One local save slot. JSON and filesystem details stay outside the use cases. */
public final class JsonGameSaveRepository implements GameSaveRepository {
    private final Path file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .setStrictness(Strictness.STRICT).disableJdkUnsafe().create();

    public JsonGameSaveRepository(Path file) {
        this.file = file.toAbsolutePath();
    }

    @Override
    public boolean exists() {
        return Files.isRegularFile(file);
    }

    @Override
    public void save(GameState state) throws IOException {
        if (state == null) {
            throw new IllegalArgumentException("No battle to save");
        }
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), ".save-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE);
                 Writer writer = Channels.newWriter(channel, StandardCharsets.UTF_8)) {
                gson.toJson(state, writer);
                writer.flush();
                channel.force(true);
            }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    @Override
    public GameState load() throws IOException {
        if (!exists()) {
            throw new IOException("No saved game was found.");
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            checkRequiredFields(json);
            // Record constructors enforce battle invariants after JSON shape validation.
            return gson.fromJson(json, GameState.class);
        } catch (RuntimeException exception) {
            throw new IOException("The saved game is invalid or uses an unsupported format. "
                    + "You can start a new game; the existing file has been kept.", exception);
        }
    }

    /** Missing booleans/numbers must not silently become false/zero during decoding. */
    private void checkRequiredFields(JsonObject json) {
        require(json != null, "Missing save object");
        numbers(json, "formatVersion", "cursor", "turnNumber");
        strings(json, "savedAt");
        booleans(json, "ended");
        for (String side : new String[]{"players", "enemies"}) {
            for (JsonElement element : array(json, side)) {
                JsonObject fighter = element.getAsJsonObject();
                strings(fighter, "id", "assetId", "name");
                numbers(fighter, "maxHp", "hp", "attack", "defense", "speed");
                booleans(fighter, "player", "guarding");
            }
        }
        for (JsonElement id : array(json, "turnOrder")) {
            require(id.isJsonPrimitive() && id.getAsJsonPrimitive().isString(),
                    "Invalid turn order ID");
        }
        for (JsonElement element : array(json, "log")) {
            JsonObject entry = element.getAsJsonObject();
            numbers(entry, "turn");
            strings(entry, "message");
        }
    }

    private JsonArray array(JsonObject object, String key) {
        require(object.has(key) && object.get(key).isJsonArray(), "Missing array: " + key);
        return object.getAsJsonArray(key);
    }

    private void strings(JsonObject object, String... keys) {
        for (String key : keys) {
            require(object.has(key) && object.get(key).isJsonPrimitive()
                    && object.getAsJsonPrimitive(key).isString(), "Missing string: " + key);
        }
    }

    private void numbers(JsonObject object, String... keys) {
        for (String key : keys) {
            require(object.has(key) && object.get(key).isJsonPrimitive()
                    && object.getAsJsonPrimitive(key).isNumber(), "Missing number: " + key);
            // Gson's tree reader otherwise truncates fractions or overflowing integers.
            object.getAsJsonPrimitive(key).getAsBigDecimal().intValueExact();
        }
    }

    private void booleans(JsonObject object, String... keys) {
        for (String key : keys) {
            require(object.has(key) && object.get(key).isJsonPrimitive()
                    && object.getAsJsonPrimitive(key).isBoolean(), "Missing boolean: " + key);
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
