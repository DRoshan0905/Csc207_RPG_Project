package app;

import data_access.BattleItemDataAccess;
import entity.Battle;
import entity.Inventory;
import interface_adapter.items.*;
import use_case.ItemDataAccessInterface;
import use_case.use_item.UseItemInteractor;
import use_case.view_items.ViewItemsInteractor;
import view.ItemMenuView;

import java.awt.image.BufferedImage;
import java.util.Map;

/** Connects all layers of the item feature. */
public final class ItemMenuFactory {
    private ItemMenuFactory() {
    }

    public static ItemMenuView create(Battle battle,
                                      Inventory inventory,
                                      Map<String, BufferedImage> icons,
                                      ItemsViewModel viewModel) {
        ItemDataAccessInterface dataAccess =
                new BattleItemDataAccess(battle, inventory);

        return new ItemMenuView(
                viewModel,
                new ViewItemsController(new ViewItemsInteractor(
                        dataAccess, new ViewItemsPresenter(viewModel))),
                new UseItemController(new UseItemInteractor(
                        dataAccess, new UseItemPresenter(viewModel))),
                icons);
    }
}
