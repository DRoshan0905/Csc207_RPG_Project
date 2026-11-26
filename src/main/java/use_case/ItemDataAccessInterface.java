package use_case;

import entity.Inventory;
import entity.ItemTarget;
import entity.TargetSide;

import java.util.List;

/** What the item use cases need from the running battle. */
public interface ItemDataAccessInterface {
    Inventory getInventory();

    List<? extends ItemTarget> getHeroes();

    List<? extends ItemTarget> getEnemies();

    /** The heroes or the enemies, depending on the side. */
    default List<? extends ItemTarget> getSide(TargetSide side) {
        return side == TargetSide.ENEMY ? getEnemies() : getHeroes();
    }

    String getCurrentFighterName();

    /** True while a hero is allowed to act. */
    boolean isHeroTurn();
}
