import data_access.JsonGameSaveRepository;
import entity.Battle;
import entity.CombatLogEntry;
import entity.GameState;
import interface_adapter.SaveLoadController;
import use_case.save_load.LoadGameInteractor;
import use_case.save_load.SaveGameInteractor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Swing presentation and application wiring. File/network work runs in SwingWorkers. */
public final class GameWindow extends JFrame {
    private final SaveLoadController controller;
    private final JPanel content = new JPanel(new BorderLayout());
    private final JLabel status = new JLabel("Choose New Game or Continue.");
    private final JButton newGame = new JButton("New Game");
    private final JButton continueGame = new JButton("Continue");
    private final JButton savedLog = new JButton("View Saved Log");
    private final JButton save = new JButton("Save");
    private final JButton saveAndExit = new JButton("Save & Exit");
    private final JButton battleLog = new JButton("Battle Log");
    private Main view;
    private boolean busy;
    private boolean ownsSave;

    public GameWindow() {
        super("RPG Battle");
        Path saveFile = Path.of(System.getProperty("user.home"), ".rpg-project", "save.json");
        JsonGameSaveRepository repository = new JsonGameSaveRepository(saveFile);
        controller = new SaveLoadController(new SaveGameInteractor(repository, Clock.systemUTC()),
                new LoadGameInteractor(repository));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                requestExit();
            }
        });
        newGame.addActionListener(event -> loadBattle(false));
        continueGame.addActionListener(event -> loadBattle(true));
        savedLog.addActionListener(event -> loadSavedLog());
        save.addActionListener(event -> requestSave(false));
        saveAndExit.addActionListener(event -> requestSave(true));
        battleLog.addActionListener(event -> showBattleLog());
        status.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        content.add(status, BorderLayout.SOUTH);
        setContentPane(content);
        showMenu();
        setMinimumSize(new Dimension(1120, 740));
        pack();
        setLocationRelativeTo(null);
    }

    private void showMenu() {
        JPanel menu = new JPanel(new GridBagLayout());
        menu.setPreferredSize(new Dimension(1080, 640));
        JPanel choices = new JPanel(new GridLayout(0, 1, 12, 16));
        JLabel title = new JLabel("RPG Battle", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        choices.add(title);
        choices.add(newGame);
        choices.add(continueGame);
        choices.add(savedLog);
        menu.add(choices);
        content.add(menu, BorderLayout.CENTER);
        updateControls();
    }

    private void updateControls() {
        newGame.setEnabled(!busy);
        continueGame.setEnabled(!busy && controller.hasSave());
        savedLog.setEnabled(!busy && controller.hasSave());
        save.setEnabled(!busy && view != null);
        saveAndExit.setEnabled(!busy && view != null);
        battleLog.setEnabled(!busy && view != null);
    }

    private void setBusy(boolean value, String message) {
        busy = value;
        status.setText(message);
        updateControls();
    }

    private void loadBattle(boolean continuing) {
        setBusy(true, continuing ? "Loading saved battle and assets..." : "Loading characters and animations...");
        background(() -> {
            FFBE_API api = new FFBE_API();
            return continuing ? api.load(Battle.restore(controller.load())) : api.load();
        }, assets -> {
            view = new Main(assets);
            ownsSave = continuing;
            if (continuing) {
                view.markSaved();
            }
            content.remove(((BorderLayout) content.getLayout()).getLayoutComponent(BorderLayout.CENTER));
            content.add(view, BorderLayout.CENTER);
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            toolbar.add(save);
            toolbar.add(saveAndExit);
            toolbar.add(battleLog);
            content.add(toolbar, BorderLayout.NORTH);
            content.revalidate();
            content.repaint();
            setBusy(false, continuing ? "Saved battle restored." : "New battle started. Save to keep your progress.");
        }, error -> fail("Could not load the battle", error));
    }

    private void requestSave(boolean exit) {
        if (busy || view == null) {
            return;
        }
        setBusy(true, "Waiting for the current action to finish...");
        view.atCheckpoint(() -> saveCheckpoint(exit));
    }

    private void saveCheckpoint(boolean exit) {
        if (!ownsSave && controller.hasSave()) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Replace the previous saved game with this battle?",
                    "Replace saved game", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                resume("Save cancelled. The previous save has been kept.");
                return;
            }
        }
        final GameState snapshot;
        try {
            snapshot = controller.capture(view.battle());
        } catch (RuntimeException error) {
            fail("Could not capture the battle", error);
            return;
        }
        status.setText("Saving game...");
        background(() -> {
            controller.save(snapshot);
            return snapshot;
        }, saved -> {
            ownsSave = true;
            view.markSaved();
            if (exit) {
                dispose();
            } else {
                resume("Game saved.");
            }
        }, error -> fail("Could not save the game; your battle is still open", error));
    }

    private void showBattleLog() {
        if (busy || view == null) {
            return;
        }
        setBusy(true, "Waiting for the current action to finish...");
        view.atCheckpoint(() -> {
            showLog(view.battle().log(), "Battle Log");
            resume("Battle resumed.");
        });
    }

    private void loadSavedLog() {
        setBusy(true, "Loading saved combat history...");
        background(controller::load, state -> {
            showLog(state.log(), "Saved Battle Log — " + state.savedAt());
            setBusy(false, "Choose New Game or Continue.");
        }, error -> fail("Could not load the saved log", error));
    }

    private void showLog(List<CombatLogEntry> entries, String title) {
        String text = entries.stream().map(CombatLogEntry::toString).collect(Collectors.joining("\n"));
        JTextArea history = new JTextArea(text.isEmpty() ? "No combat events yet." : text, 20, 65);
        history.setEditable(false);
        history.setLineWrap(true);
        history.setWrapStyleWord(true);
        history.setCaretPosition(history.getDocument().getLength());
        JOptionPane.showMessageDialog(this, new JScrollPane(history), title, JOptionPane.PLAIN_MESSAGE);
    }

    private void requestExit() {
        if (busy) {
            return;
        }
        if (view == null || !view.hasUnsavedChanges()) {
            dispose();
            return;
        }
        setBusy(true, "Waiting for the current action to finish...");
        view.atCheckpoint(() -> {
            Object[] choices = {"Save & Exit", "Exit Without Saving", "Cancel"};
            int answer = JOptionPane.showOptionDialog(this, "Save your battle before closing?",
                    "Unsaved progress", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, choices, choices[0]);
            if (answer == 0) {
                saveCheckpoint(true);
            } else if (answer == 1) {
                dispose();
            } else {
                resume("Exit cancelled. Battle resumed.");
            }
        });
    }

    private void resume(String message) {
        if (view != null) {
            view.resumeCombat();
        }
        setBusy(false, message);
    }

    private void fail(String title, Throwable error) {
        String detail = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        JOptionPane.showMessageDialog(this, detail, title, JOptionPane.ERROR_MESSAGE);
        resume(title + ".");
    }

    private <T> void background(Callable<T> work, Consumer<T> success, Consumer<Throwable> failure) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.call();
            }

            @Override
            protected void done() {
                try {
                    success.accept(get());
                } catch (Exception error) {
                    failure.accept(error.getCause() == null ? error : error.getCause());
                }
            }
        }.execute();
    }
}
