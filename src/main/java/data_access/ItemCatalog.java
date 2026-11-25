package data_access;

import entity.HealingItem;
import entity.Inventory;
import entity.Item;
import entity.ReviveItem;

import java.util.LinkedHashMap;
import java.util.Map;

/** The items a party starts with and where their icons are stored. */
public class ItemCatalog {
    private static final String POTIONS =
            "medieval-fantasy/items/food-&-potion/potion.png";

    private final Inventory inventory = new Inventory();
    private final Map<String, String> iconPaths = new LinkedHashMap<>();

    public ItemCatalog() {
        // To add an item, register it here.
        register(new HealingItem("Potion", 30), 3, POTIONS + "#0");
        register(new HealingItem("Super Potion", 70), 1, POTIONS + "#3");
        register(new ReviveItem("Revive"), 1,
                "medieval-fantasy/hud/full-heart.png");
    }

    private void register(Item item, int quantity, String iconPath) {
        inventory.add(item, quantity);
        iconPaths.put(item.getName(), iconPath);
    }

    public Inventory getInventory() {
        return inventory;
    }

    /** Item name to image path; "#n" picks a square from a sheet. */
    public Map<String, String> getIconPaths() {
        return iconPaths;
    }
}
