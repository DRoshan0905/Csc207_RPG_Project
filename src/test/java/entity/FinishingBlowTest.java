package entity;

import org.junit.jupiter.api.Test;
import use_case.WaveProgression;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FinishingBlowTest {
    private final Fighter hero = new Fighter("Warrior", true, 100, 30, 20, 90);
    private final Random firstBuff = new Random() {
        @Override
        public int nextInt(int bound) {
            return 0;
        }
    };

    @Test
    void onlyFinalHitsCountAndSecondKillChangesCombatDamage() {
        Fighter first = enemy(40);
        Fighter second = enemy(1);
        Fighter third = enemy(100);
        Battle battle = new Battle(List.of(hero), List.of(first, second, third), 1, firstBuff);

        battle.attack(first);
        assertEquals(0, hero.getFinishingBlows());
        assertFalse(battle.attack(first).contains("gained"));
        battle.attack(first);
        assertEquals(1, hero.getFinishingBlows());
        assertTrue(battle.attack(second).contains("+1 attack"));
        assertEquals(2, hero.getFinishingBlows());
        battle.attack(third);
        assertEquals(71, third.hp);
    }

    @Test
    void skipsCappedBuffsAndStopsAwardingWhenAllCapsAreReached() {
        hero.takeDamage(10);
        for (int i = 0; i < 14; i++) {
            String reward = hero.recordFinishingBlow(firstBuff);
            if (i % 2 == 0 || i == 13) {
                assertTrue(reward.isEmpty());
            } else {
                assertFalse(reward.isEmpty());
            }
        }
        assertEquals(14, hero.getFinishingBlows());
        assertEquals(32, hero.getAttack());
        assertEquals(22, hero.getDefense());
        assertEquals(106, hero.getMaxHp());
        assertEquals(96, hero.hp);
    }

    @Test
    void rewardsBelongToTheHeroAndCarryAcrossWaves() {
        Fighter ally = new Fighter("Mage", true, 80, 20, 10, 70);
        Fighter first = enemy(1);
        Fighter second = enemy(1);
        Battle battle = new Battle(List.of(hero, ally), List.of(first, second), 1, firstBuff);
        battle.attack(first);
        assertEquals(1, hero.getFinishingBlows());
        assertEquals(0, ally.getFinishingBlows());
        battle.attack(second);
        Battle next = new WaveProgression().nextWave(battle, List.of(enemy(1)));

        assertSame(hero, next.players.get(0));
        assertEquals(2, hero.getFinishingBlows());
        assertEquals(32, hero.getAttack());
        Fighter newRunHero = new Fighter("Warrior", true, 100, 30, 20, 90);
        assertEquals(0, newRunHero.getFinishingBlows());
        assertEquals(30, newRunHero.getAttack());
    }

    private Fighter enemy(int hp) {
        return new Fighter("Slime", false, hp, 10, 5, 50);
    }
}
