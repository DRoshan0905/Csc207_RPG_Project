package use_case;

import entity.Inventory;
import entity.ItemTarget;

import java.util.List;

/** What the item use cases need from the running battle. */
public interface ItemDataAccessInterface {
    Inventory getInventory();

    List<? extends ItemTarget> getHeroes();

    String getCurrentFighterName();

    /** True while a hero is allowed to act. */
    boolean isHeroTurn();
}
