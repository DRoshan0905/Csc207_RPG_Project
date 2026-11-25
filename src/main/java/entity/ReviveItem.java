package entity;

public class ReviveItem implements Item {
    private final String name;

    public ReviveItem(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Revives at half HP";
    }

    @Override
    public boolean canUse(ItemTarget target) {
        return !target.alive();
    }

    @Override
    public void use(ItemTarget target) {
        target.restoreHp(target.getMaxHp() / 2);
    }
}
