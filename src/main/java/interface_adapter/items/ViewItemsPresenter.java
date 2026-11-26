package interface_adapter.items;

import use_case.view_items.ViewItemsOutputBoundary;
import use_case.view_items.ViewItemsOutputData;

import java.util.ArrayList;
import java.util.List;

public class ViewItemsPresenter implements ViewItemsOutputBoundary {
    private final ItemsViewModel viewModel;

    public ViewItemsPresenter(ItemsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(ViewItemsOutputData outputData) {
        List<ItemRow> rows = new ArrayList<>();

        for (ViewItemsOutputData.Entry entry : outputData.getEntries()) {
            rows.add(new ItemRow(
                    entry.getName(),
                    entry.getDescription(),
                    "x" + entry.getCount(),
                    entry.isEnemyTarget(),
                    entry.getUsableTargets()));
        }

        viewModel.setRows(rows);
        viewModel.setMessage(rows.isEmpty()
                ? "The bag is empty."
                : "Choose an item.");
    }
}
