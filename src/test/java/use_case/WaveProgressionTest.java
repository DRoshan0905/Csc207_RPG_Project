package use_case;

import entity.Battle;
import entity.Fighter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WaveProgressionTest {
    private final WaveProgression progression = new WaveProgression();
    private final Fighter hero = new Fighter("Warrior", true, 100, 30, 20, 80);
    private final Fighter ally = new Fighter("Mage", true, 80, 20, 10, 70);
    private final Fighter enemy = new Fighter("Slime", false, 10, 10, 5, 50);
    private final Battle first = new Battle(List.of(hero, ally), List.of(enemy));

    @Test
    void victoryStartsTheNextWaveWithStrongerFightersAndCarriesPartyState() {
        hero.takeDamage(25);
        hero.defend();
        ally.takeDamage(80);
        first.attack(enemy);
        Fighter baseEnemy = new Fighter("Bat", false, 50, 15, 8, 60);
        Battle next = progression.nextWave(first, List.of(baseEnemy));

        assertEquals(2, next.getWave());
        assertSame(hero, next.current());
        assertEquals(105, hero.getMaxHp());
        assertEquals(80, hero.hp);
        assertEquals(31, hero.getAttack());
        assertEquals(21, hero.getDefense());
        assertFalse(hero.guarding);
        assertFalse(ally.alive());
        assertEquals(10, next.enemies.get(0).getDefense());
        assertEquals(50, baseEnemy.hp);
        assertFalse(next.ended);
    }

    @Test
    void evenWavesUseOneBossAndOddWavesUseTheOrdinaryEncounter() {
        Fighter bat = new Fighter("Bat", false, 10, 10, 5, 50);
        Fighter slime = new Fighter("Slime", false, 15, 7, 2, 45);
        List<Fighter> encounter = List.of(bat, slime);
        first.attack(enemy);
        Battle second = progression.nextWave(first, encounter);
        assertEquals(2, second.getWave());
        assertEquals(1, second.enemies.size());
        Fighter boss = second.enemies.get(0);
        assertTrue(boss.isBoss());
        assertEquals("Bat Boss", boss.name);
        assertEquals(50, boss.getMaxHp());
        assertEquals(17, boss.getAttack());
        assertEquals(7, boss.getDefense());
        assertEquals(50, boss.speed);

        defeatEnemies(second);
        assertTrue(second.isVictory());
        Battle third = progression.nextWave(second, encounter);
        assertEquals(3, third.getWave());
        assertEquals(2, third.enemies.size());
        assertTrue(third.enemies.stream().noneMatch(Fighter::isBoss));
        assertEquals(40, third.enemies.get(0).getMaxHp());
        assertEquals(16, third.enemies.get(0).getAttack());
        defeatEnemies(third);
        Battle fourth = progression.nextWave(third, encounter);
        assertEquals(4, fourth.getWave());
        assertEquals(1, fourth.enemies.size());
        assertTrue(fourth.enemies.get(0).isBoss());
        assertEquals(110, fourth.enemies.get(0).getMaxHp());
        assertEquals(24, fourth.enemies.get(0).getAttack());
    }

    private void defeatEnemies(Battle battle) {
        for (Fighter target : battle.enemies) {
            while (target.alive()) {
                battle.attack(target);
            }
        }
    }

    @Test
    void ongoingOrLostBattleDoesNotAdvanceOrChangeTheParty() {
        assertThrows(IllegalArgumentException.class, () -> progression.nextWave(first, List.of(enemy)));
        hero.takeDamage(100);
        ally.takeDamage(80);
        first.next();
        assertTrue(first.ended);
        assertFalse(first.isVictory());
        assertThrows(IllegalArgumentException.class, () -> progression.nextWave(first, List.of(enemy)));
        assertEquals(100, hero.getMaxHp());
    }
}
