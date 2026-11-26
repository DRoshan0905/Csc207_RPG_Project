package data_access;

import entity.Battle;
import entity.Inventory;
import entity.ItemTarget;
import use_case.ItemDataAccessInterface;

import java.util.List;

/** Lets the item use cases work with the existing Battle class. */
public class BattleItemDataAccess implements ItemDataAccessInterface {
    private final Battle battle;
    private final Inventory inventory;

    public BattleItemDataAccess(Battle battle, Inventory inventory) {
        this.battle = battle;
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public List<? extends ItemTarget> getHeroes() {
        return battle.players;
    }

    @Override
    public List<? extends ItemTarget> getEnemies() {
        return battle.enemies;
    }

    @Override
    public String getCurrentFighterName() {
        return battle.current().name;
    }

    @Override
    public boolean isHeroTurn() {
        return !battle.ended
                && battle.current() != null
                && battle.current().player;
    }
}
