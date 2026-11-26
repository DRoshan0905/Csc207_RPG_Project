package use_case.use_item;

public class UseItemOutputData {
    private final String userName;
    private final String itemName;
    private final String targetName;
    private final String effect;
    private final boolean enemyTarget;
    private final int targetIndex;

    public UseItemOutputData(String userName, String itemName,
                             String targetName, String effect,
                             boolean enemyTarget, int targetIndex) {
        this.userName = userName;
        this.itemName = itemName;
        this.targetName = targetName;
        this.effect = effect;
        this.enemyTarget = enemyTarget;
        this.targetIndex = targetIndex;
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

    /** What changed, such as "+30 HP" or "ATK 40 \u2192 30". */
    public String getEffect() {
        return effect;
    }

    public boolean isEnemyTarget() {
        return enemyTarget;
    }

    public int getTargetIndex() {
        return targetIndex;
    }
}
