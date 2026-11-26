package entity;

/**
 * Lowers one of a monster's stats for the rest of the battle.
 * Uses stack, but a stat never drops below half of its starting value.
 */
public class WeakenItem implements Item {
    /** Largest total reduction allowed, as a percentage of the base stat. */
    private static final int MAX_REDUCTION_PERCENT = 50;

    private final String name;
    private final Stat stat;
    private final int percent;

    public WeakenItem(String name, Stat stat, int percent) {
        this.name = name;
        this.stat = stat;
        this.percent = percent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Enemy " + stat.label() + " -" + percent + "%";
    }

    @Override
    public TargetSide getTargetSide() {
        return TargetSide.ENEMY;
    }

    @Override
    public boolean canUse(ItemTarget target) {
        return target.alive() && target.getStat(stat) > floor(target);
    }

    @Override
    public String use(ItemTarget target) {
        int before = target.getStat(stat);
        int amount = Math.max(1, target.getBaseStat(stat) * percent / 100);
        int after = Math.max(floor(target), before - amount);

        target.lowerStat(stat, before - after);

        return stat.label() + " " + before + " → " + after;
    }

    private int floor(ItemTarget target) {
        return target.getBaseStat(stat)
                * (100 - MAX_REDUCTION_PERCENT) / 100;
    }
}
