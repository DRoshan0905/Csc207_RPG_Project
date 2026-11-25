import entity.Battle;
import entity.GameState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GameApiStateTest {
    @TempDir Path cache;

    @Test
    void newGamesUseMastersRosterStatsAndStableAssetIdentities() throws Exception {
        cachedAssets();
        GameAssets game = new Game_API(cache).load();
        assertEquals(List.of("warrior", "paladin", "mage", "bowman", "Wizard"),
                game.battle.players.stream().map(f -> f.assetId).toList());
        assertEquals(List.of("slime", "bat", "mushroom"),
                game.battle.enemies.stream().map(f -> f.assetId).toList());
        assertEquals(180, game.battle.players.get(1).maxHp);
        assertEquals(90, game.battle.players.get(3).speed);
        assertEquals(8, game.art.size());
        GameState state = game.battle.snapshot(BattleFixtures.SAVED_AT);
        assertEquals(8, state.turnOrder().stream().distinct().count());
        assertEquals(colour(state.backgroundId()), game.background.getRGB(0, 0));
    }

    @Test
    void continuingLoadsTheSavedArtAndBackgroundWithoutRecreatingTheBattle() throws Exception {
        cachedAssets();
        Battle original = BattleFixtures.battle();
        Battle battle = new Battle(original.players, original.enemies, 19);
        battle.players.get(0).hp = 70;
        battle.useItem(battle.bag.available().get(0), battle.players.get(0));
        battle.next();
        GameState state = battle.snapshot(BattleFixtures.SAVED_AT);
        Battle restored = Battle.restore(state);
        GameAssets game = new Game_API(cache).load(restored);
        assertSame(restored, game.battle);
        assertEquals(state, game.battle.snapshot(BattleFixtures.SAVED_AT));
        assertEquals(colour(19), game.background.getRGB(0, 0));
        assertEquals(3, game.art.size());
        assertTrue(game.art.keySet().containsAll(restored.players));
        assertTrue(game.art.keySet().containsAll(restored.enemies));
    }

    @Test
    void unsupportedSavedAssetsAreRejectedBeforeDownloadingAnything() {
        Battle battle = new Battle(List.of(new Battle.Fighter("h", "dragon", "Hero", true, 100, 1, 1, 1)),
                BattleFixtures.battle().enemies);
        IOException error = assertThrows(IOException.class, () -> new Game_API(cache).load(battle));
        assertTrue(error.getMessage().contains("Unsupported fighter asset"));
    }

    @Test
    void oversizedSavedPartiesAreRejectedBeforeDownloadingAnything() {
        Battle.Fighter hero = BattleFixtures.battle().players.get(0);
        Battle battle = new Battle(java.util.Collections.nCopies(6, hero), BattleFixtures.battle().enemies);
        assertThrows(IOException.class, () -> new Game_API(cache).load(battle));
    }

    private void cachedAssets() throws IOException {
        Files.createDirectories(cache.resolve("backgrounds"));
        for (int id = 1; id <= 24; id++) {
            BufferedImage background = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            background.setRGB(0, 0, colour(id));
            ImageIO.write(background, "png", cache.resolve("backgrounds/" + id + ".png").toFile());
        }
        BufferedImage sprite = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        sprite.setRGB(1, 1, 0xffffffff);
        for (String folder : List.of("char/warrior", "char/paladin", "char/mage", "char/bowman",
                "char/Wizard", "monster/slime", "monster/bat", "monster/mushroom")) {
            Files.createDirectories(cache.resolve(folder));
            for (String action : List.of("idle", "attack")) {
                ImageIO.write(sprite, "gif", cache.resolve(folder + "/" + action + ".gif").toFile());
            }
        }
    }

    private int colour(int id) {
        return 0xff000000 | id;
    }
}
