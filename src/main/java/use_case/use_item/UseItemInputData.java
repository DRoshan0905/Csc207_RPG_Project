package use_case.use_item;

public class UseItemInputData {
    private final String itemName;
    private final int heroIndex;

    public UseItemInputData(String itemName, int heroIndex) {
        this.itemName = itemName;
        this.heroIndex = heroIndex;
    }

    public String getItemName() {
        return itemName;
    }

    public int getHeroIndex() {
        return heroIndex;
    }
}
