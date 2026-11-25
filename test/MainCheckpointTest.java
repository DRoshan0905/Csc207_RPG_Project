import entity.Battle;
import entity.GameState;
import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class MainCheckpointTest {
    @Test
    void savingDuringAttackWaitsForTurnEndAndDoesNotRepeatDamageOnContinue() throws Exception {
        AtomicReference<Main> panel = new AtomicReference<>();
        AtomicReference<GameState> saved = new AtomicReference<>();
        CountDownLatch checkpointReached = new CountDownLatch(1);
        try {
            SwingUtilities.invokeAndWait(() -> {
                Main view = new Main(assets(BattleFixtures.battle()));
                panel.set(view);
                view.setSize(1080, 640);
                click(view, 130, 410); // Select the enemy.
                click(view, 900, 60); // Start Warrior's attack.
                view.atCheckpoint(() -> {
                    saved.set(view.battle().snapshot(BattleFixtures.SAVED_AT));
                    checkpointReached.countDown();
                });
                assertNull(saved.get(), "A moving attacker must not be saved immediately");
            });
            assertTrue(checkpointReached.await(5, TimeUnit.SECONDS), "Attack should finish and reach a checkpoint");
            GameState state = saved.get();
            assertNotNull(state);
            assertEquals(67, state.enemies().get(0).hp());
            assertEquals("mage", state.turnOrder().get(state.cursor()));
            assertEquals(2, state.log().size(), "Attack must be logged exactly once");
            SwingUtilities.invokeAndWait(() -> {
                assertEquals(state, panel.get().battle().snapshot(BattleFixtures.SAVED_AT));
                Main continued = new Main(assets(Battle.restore(state)));
                try {
                    continued.atCheckpoint(() -> assertEquals(state,
                            continued.battle().snapshot(BattleFixtures.SAVED_AT)));
                    // A selected target is presentation state, so Continue starts at the turn prompt.
                    assertEquals("Mage's turn. Choose Attack, Defend or Items.", continued.battle().prompt());
                } finally {
                    continued.removeNotify();
                }
            });
        } finally {
            if (panel.get() != null) {
                SwingUtilities.invokeAndWait(() -> panel.get().removeNotify());
            }
        }
    }

    @Test
    void checkpointAtAnIdleTurnPausesInputUntilResumed() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Main view = new Main(assets(BattleFixtures.battle()));
            try {
                view.setSize(1080, 640);
                GameState before = view.battle().snapshot(BattleFixtures.SAVED_AT);
                view.atCheckpoint(() -> { });
                click(view, 900, 100); // Defend while paused: ignored.
                assertEquals(before, view.battle().snapshot(BattleFixtures.SAVED_AT));
                view.resumeCombat();
                click(view, 900, 100);
                assertEquals("mage", view.battle().current().id);
                assertTrue(view.battle().players.get(0).guarding);
            } finally {
                view.removeNotify();
            }
        });
    }

    @Test
    void usingAnItemThroughTheUiMarksProgressUnsavedAndLogsTheEvent() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Battle battle = BattleFixtures.battle();
            battle.players.get(0).hp = 80;
            Main view = new Main(assets(battle));
            try {
                view.setSize(1080, 640);
                view.markSaved();
                click(view, 900, 134); // Items.
                click(view, 900, 60); // Potion.
                click(view, 450, 550); // Warrior's card.
                assertEquals(110, battle.players.get(0).hp);
                assertTrue(view.hasUnsavedChanges());
                assertEquals("mage", battle.current().id);
                assertEquals("Warrior uses Potion on Warrior (+30 HP).", battle.log().get(1).message());
                view.atCheckpoint(() -> assertEquals(2,
                        battle.snapshot(BattleFixtures.SAVED_AT).inventory().get(0).quantity()));
            } finally {
                view.removeNotify();
            }
        });
    }

    @Test
    void reviveThroughTheUiRestoresATargetAndConsumesTheSavedReviveStack() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Battle battle = BattleFixtures.battle();
            battle.players.get(1).hp = 0;
            Main view = new Main(assets(battle));
            try {
                view.setSize(1080, 640);
                view.markSaved();
                click(view, 900, 134); // Items.
                click(view, 900, 134); // Revive (third slot).
                click(view, 800, 550); // Mage's card.
                assertEquals(62, battle.players.get(1).hp);
                assertTrue(view.hasUnsavedChanges());
                assertEquals("mage", battle.current().id);
                assertEquals(0, battle.snapshot(BattleFixtures.SAVED_AT).inventory().get(2).quantity());
            } finally {
                view.removeNotify();
            }
        });
    }

    private static void click(Main view, int x, int y) {
        view.dispatchEvent(new MouseEvent(view, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0, x, y, 1, false, MouseEvent.BUTTON1));
    }

    private static GameAssets assets(Battle battle) {
        GameAssets assets = new GameAssets();
        assets.battle = battle;
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0xffffffff);
        assets.background = image;
        for (Battle.Fighter fighter : java.util.stream.Stream.concat(
                battle.players.stream(), battle.enemies.stream()).toList()) {
            GameAssets.Art art = new GameAssets.Art();
            art.idle = new GameAssets.Clip(new BufferedImage[]{image}, new int[]{20}, .5, 1);
            art.move = art.idle;
            art.attack = art.idle;
            art.dead = fighter.player ? art.idle : null;
            assets.art.put(fighter, art);
        }
        return assets;
    }
}
