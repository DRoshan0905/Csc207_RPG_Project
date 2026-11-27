package use_case;

import entity.Battle;
import entity.Fighter;

import java.util.ArrayList;
import java.util.List;

public class WaveProgression {
    public Battle nextWave(Battle completed, List<Fighter> encounterEnemies) {
        if (!completed.isVictory() || encounterEnemies.isEmpty()) {
            throw new IllegalArgumentException("The next wave requires a victory and an enemy encounter.");
        }

        int wave = completed.getWave() + 1;
        int cleared = wave - 1;
        List<Fighter> enemies = new ArrayList<>();
        for (Fighter enemy : encounterEnemies) {
            enemies.add(new Fighter(enemy.name, false, enemy.maxHp + 15 * cleared,
                    enemy.attack + 3 * cleared, enemy.defense + 2 * cleared, enemy.speed));
        }
        for (Fighter hero : completed.players) {
            hero.advanceWave();
        }
        return new Battle(completed.players, enemies, wave);
    }
}
