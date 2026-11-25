package entity;

public class HealingItem implements Item {
    private final String name;
    private final int amount;

    public HealingItem(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Restores " + amount + " HP";
    }

    @Override
    public boolean canUse(ItemTarget target) {
        return target.alive() && target.getHp() < target.getMaxHp();
    }

    @Override
    public void use(ItemTarget target) {
        target.restoreHp(amount);
    }
}
