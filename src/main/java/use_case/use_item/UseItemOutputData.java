package use_case.use_item;

public class UseItemOutputData {
    private final String userName;
    private final String itemName;
    private final String targetName;
    private final int hpRestored;
    private final int heroIndex;

    public UseItemOutputData(String userName, String itemName, String targetName, int hpRestored, int heroIndex) {
        this.userName = userName;
        this.itemName = itemName;
        this.targetName = targetName;
        this.hpRestored = hpRestored;
        this.heroIndex = heroIndex;
    }

    public String getUserName() {
        return userName;
    }

    public String getItemName() {
        return itemName;
    }

    public String getTargetName() {
        return targetName;
    }

    public int getHpRestored() {
        return hpRestored;
    }

    public int getHeroIndex() {
        return heroIndex;
    }
}
