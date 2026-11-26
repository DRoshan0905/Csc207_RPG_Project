package use_case.view_items;

import entity.Inventory;
import entity.Item;
import entity.ItemTarget;
import use_case.ItemDataAccessInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewItemsInteractor implements ViewItemsInputBoundary {
    private final ItemDataAccessInterface dataAccess;
    private final ViewItemsOutputBoundary presenter;

    public ViewItemsInteractor(ItemDataAccessInterface dataAccess,
                               ViewItemsOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        Inventory inventory = dataAccess.getInventory();
        List<? extends ItemTarget> heroes = dataAccess.getHeroes();
        List<ViewItemsOutputData.Entry> entries = new ArrayList<>();

        for (Item item : inventory.available()) {
            Set<Integer> usable = new HashSet<>();

            for (int i = 0; i < heroes.size(); i++) {
                if (item.canUse(heroes.get(i))) {
                    usable.add(i);
                }
            }

            entries.add(new ViewItemsOutputData.Entry(
                    item.getName(), item.getDescription(),
                    inventory.count(item), usable));
        }

        presenter.present(new ViewItemsOutputData(entries));
    }
}
