package interface_adapter.items;

import use_case.view_items.ViewItemsInputBoundary;

public class ViewItemsController {
    private final ViewItemsInputBoundary interactor;

    public ViewItemsController(ViewItemsInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute() {
        interactor.execute();
    }
}
