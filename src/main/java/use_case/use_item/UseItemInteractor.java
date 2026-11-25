package use_case.use_item;

import entity.Inventory;
import entity.Item;
import entity.ItemTarget;
import use_case.ItemDataAccessInterface;

public class UseItemInteractor implements UseItemInputBoundary {
    private final ItemDataAccessInterface dataAccess;
    private final UseItemOutputBoundary presenter;

    public UseItemInteractor(ItemDataAccessInterface dataAccess,
                             UseItemOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(UseItemInputData inputData) {
        if (!dataAccess.isHeroTurn()) {
            presenter.prepareFailView("You can't use items right now.");
            return;
        }

        Inventory inventory = dataAccess.getInventory();
        Item item = inventory.find(inputData.getItemName());

        if (item == null) {
            presenter.prepareFailView(
                    "No " + inputData.getItemName() + " left.");
            return;
        }

        int heroIndex = inputData.getHeroIndex();
        ItemTarget target = dataAccess.getHeroes().get(heroIndex);

        if (!item.canUse(target)) {
            presenter.prepareFailView(item.getName()
                    + " won't have any effect on "
                    + target.getName() + ".");
            return;
        }

        int hpBefore = target.getHp();

        inventory.consume(item);
        item.use(target);

        presenter.prepareSuccessView(new UseItemOutputData(
                dataAccess.getCurrentFighterName(),
                item.getName(),
                target.getName(),
                target.getHp() - hpBefore,
                heroIndex));
    }
}
