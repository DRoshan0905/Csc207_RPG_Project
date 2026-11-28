package data_access;

import entity.Battle;
import entity.Fighter;
import entity.HealingItem;
import entity.Inventory;
import org.junit.jupiter.api.Test;
import use_case.WaveProgression;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameAssetsTest {
    @Test
    void nextWaveUsesThePartyInventoryAndTheNewEncountersEnemyArt() {
        Fighter hero = new Fighter("Warrior", true, 100, 30, 20, 80);
        Fighter slime = new Fighter("Slime", false, 10, 10, 5, 50);
        Fighter bat = new Fighter("Bat", false, 50, 15, 8, 60);
        GameAssets previous = new GameAssets();
        previous.battle = new Battle(List.of(hero), List.of(slime));
        previous.inventory = new Inventory();
        HealingItem potion = new HealingItem("Potion", 30);
        previous.inventory.add(potion, 2);
        previous.inventory.consume(potion);
        GameAssets.Art heroArt = new GameAssets.Art();
        previous.art.put(hero, heroArt);
        previous.battle.attack(slime);

        GameAssets encounter = new GameAssets();
        encounter.battle = new Battle(List.of(), List.of(bat));
        encounter.inventory = new Inventory();
        GameAssets.Art batArt = new GameAssets.Art();
        encounter.art.put(bat, batArt);
        Battle next = new WaveProgression().nextWave(previous.battle, encounter.battle.enemies);
        encounter.useNextBattle(next, previous);

        assertSame(next, encounter.battle);
        assertEquals(1, encounter.inventory.count(potion));
        assertSame(heroArt, encounter.art.get(hero));
        assertSame(batArt, encounter.art.get(next.enemies.get(0)));
        assertEquals(2, encounter.art.size());
    }
}
