package interface_adapter.items;

import use_case.use_item.UseItemInputBoundary;
import use_case.use_item.UseItemInputData;

public class UseItemController {
    private final UseItemInputBoundary interactor;

    public UseItemController(UseItemInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String itemName, int heroIndex) {
        interactor.execute(new UseItemInputData(itemName, heroIndex));
    }
}
