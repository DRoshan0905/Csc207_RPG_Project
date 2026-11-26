package use_case.use_item;

import entity.Inventory;
import entity.Item;
import entity.ItemTarget;
import entity.TargetSide;
import use_case.ItemDataAccessInterface;

import java.util.List;

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

        List<? extends ItemTarget> side =
                dataAccess.getSide(item.getTargetSide());
        int targetIndex = inputData.getTargetIndex();

        if (targetIndex < 0 || targetIndex >= side.size()) {
            presenter.prepareFailView("Choose a valid target.");
            return;
        }

        ItemTarget target = side.get(targetIndex);

        if (!item.canUse(target)) {
            presenter.prepareFailView(item.getName()
                    + " won't have any effect on "
                    + target.getName() + ".");
            return;
        }

        inventory.consume(item);
        String effect = item.use(target);

        presenter.prepareSuccessView(new UseItemOutputData(
                dataAccess.getCurrentFighterName(),
                item.getName(),
                target.getName(),
                effect,
                item.getTargetSide() == TargetSide.ENEMY,
                targetIndex));
    }
}
