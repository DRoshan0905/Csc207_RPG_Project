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
        boolean bossWave = wave % 2 == 0;
        List<Fighter> enemies = new ArrayList<>();
        List<Fighter> roster = bossWave ? List.of(encounterEnemies.get(0)) : encounterEnemies;
        for (Fighter enemy : roster) {
            int hp = enemy.maxHp + 15 * cleared;
            int attack = enemy.attack + 3 * cleared;
            String name = enemy.name;
            if (bossWave) {
                hp *= 2;
                attack = (int) Math.ceil(attack * 1.25);
                name += " Boss";
            }
            enemies.add(new Fighter(name, false, hp, attack,
                    enemy.defense + 2 * cleared, enemy.speed, bossWave));
        }
        for (Fighter hero : completed.players) {
            hero.advanceWave();
        }
        return new Battle(completed.players, enemies, wave);
    }
}
