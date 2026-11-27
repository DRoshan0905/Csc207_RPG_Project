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
        assertEquals(65, next.enemies.get(0).hp);
        assertEquals(18, next.enemies.get(0).getAttack());
        assertEquals(10, next.enemies.get(0).getDefense());
        assertEquals(50, baseEnemy.hp);
        assertFalse(next.ended);
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
