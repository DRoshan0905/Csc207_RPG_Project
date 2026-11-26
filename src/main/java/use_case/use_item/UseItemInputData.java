package use_case.use_item;

public class UseItemInputData {
    private final String itemName;
    private final int targetIndex;

    public UseItemInputData(String itemName, int targetIndex) {
        this.itemName = itemName;
        this.targetIndex = targetIndex;
    }

    public String getItemName() {
        return itemName;
    }

    public int getTargetIndex() {
        return targetIndex;
    }
}
