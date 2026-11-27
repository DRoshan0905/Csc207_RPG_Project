package app;

import data_access.GameAssets;
import data_access.GameDataAccessInterface;
import data_access.GameDataAccessObject;
import entity.Battle;
import interface_adapter.items.ItemsViewModel;
import use_case.Combat;
import use_case.WaveProgression;
import view.BattleView;
import view.BattleViewModel;
import view.ItemMenuView;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameDataAccessInterface dataAccess = new GameDataAccessObject();
            JFrame window = new JFrame("RPG Battle");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setMinimumSize(new Dimension(1120, 700));
            loadBattle(window, dataAccess, null, null);
        });
    }

    private static void loadBattle(JFrame window, GameDataAccessInterface dataAccess,
                                   GameAssets previous, JButton nextWave) {
        new SwingWorker<GameAssets, Void>() {
            @Override
            protected GameAssets doInBackground() throws Exception {
                return dataAccess.load();
            }

            @Override
            protected void done() {
                try {
                    GameAssets assets = get();
                    if (previous != null) {
                        Battle next = new WaveProgression().nextWave(previous.battle, assets.battle.enemies);
                        assets.useNextBattle(next, previous);
                    }
                    showBattle(window, dataAccess, assets);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showLoadError(window, nextWave, exception);
                } catch (ExecutionException exception) {
                    showLoadError(window, nextWave, exception.getCause());
                }
            }
        }.execute();
    }

    private static void showBattle(JFrame window, GameDataAccessInterface dataAccess, GameAssets assets) {
        Battle battle = assets.battle;
        BattleViewModel viewModel = new BattleViewModel();
        Combat.OutputBoundary presenter = new Combat.Presenter(viewModel);

        Map<String, Combat.Action> actions = new LinkedHashMap<>();
        actions.put("Attack", new Combat.Attack(battle, presenter));
        actions.put("Defend", new Combat.Defend(battle, presenter));
        actions.put("Skill", new Combat.Skill(battle, presenter));
        Combat.Controller controller = new Combat.Controller(new Combat.Interactor());

        ItemsViewModel itemsViewModel = new ItemsViewModel();
        ItemMenuView itemMenu = ItemMenuFactory.create(battle, assets.inventory, assets.icons, itemsViewModel);

        JButton nextWave = new JButton("Next Wave");
        nextWave.setEnabled(false);
        nextWave.addActionListener(event -> {
            nextWave.setEnabled(false);
            nextWave.setText("Loading...");
            loadBattle(window, dataAccess, assets, nextWave);
        });

        BattleView view = new BattleView(assets, controller, viewModel, actions, itemMenu, itemsViewModel,
                () -> nextWave.setEnabled(true));
        JPanel content = new JPanel(new BorderLayout());
        content.add(view, BorderLayout.CENTER);
        content.add(nextWave, BorderLayout.SOUTH);
        window.setContentPane(content);

        if (window.isVisible()) {
            window.validate();
            window.repaint();
        } else {
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        }
    }

    private static void showLoadError(JFrame window, JButton nextWave, Throwable error) {
        if (nextWave != null) {
            nextWave.setText("Next Wave");
            nextWave.setEnabled(true);
        }
        JOptionPane.showMessageDialog(window.isVisible() ? window : null,
                "Could not load game:\n" + error.getMessage(), "Loading Error", JOptionPane.ERROR_MESSAGE);
    }
}
