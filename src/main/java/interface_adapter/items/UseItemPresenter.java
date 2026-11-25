package interface_adapter.items;

import use_case.use_item.UseItemOutputBoundary;
import use_case.use_item.UseItemOutputData;

public class UseItemPresenter implements UseItemOutputBoundary {
    private final ItemsViewModel viewModel;

    public UseItemPresenter(ItemsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(UseItemOutputData outputData) {
        viewModel.setMessage(outputData.getUserName()
                + " uses " + outputData.getItemName()
                + " on " + outputData.getTargetName()
                + " (+" + outputData.getHpRestored() + " HP).");

        viewModel.fireItemUsed(outputData.getHeroIndex());
    }

    @Override
    public void prepareFailView(String error) {
        viewModel.setMessage(error);
    }
}
