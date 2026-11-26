package entity;

import java.util.EnumMap;
import java.util.Map;

public class Fighter implements ItemTarget {

    public final String name;
    public final boolean player;

    public final int maxHp;
    public final int attack;
    public final int defense;
    public final int speed;

    public int hp;
    public boolean guarding;

    // How much each stat has been lowered by items.
    private final Map<Stat, Integer> reductions =
            new EnumMap<>(Stat.class);

    public Fighter(
            String name,
            boolean player,
            int maxHp,
            int attack,
            int defense,
            int speed) {

        this.name = name;
        this.player = player;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;

        this.hp = maxHp;
        this.guarding = false;
    }

    @Override
    public boolean alive() {
        return hp > 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public int getMaxHp() {
        return maxHp;
    }

    @Override
    public void restoreHp(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    @Override
    public int getStat(Stat stat) {
        return getBaseStat(stat) - reductions.getOrDefault(stat, 0);
    }

    @Override
    public int getBaseStat(Stat stat) {
        return stat == Stat.ATTACK ? attack : defense;
    }

    @Override
    public void lowerStat(Stat stat, int amount) {
        reductions.merge(stat, amount, Integer::sum);
    }

    /** Attack after any reductions. Use this in damage formulas. */
    public int getAttack() {
        return getStat(Stat.ATTACK);
    }

    /** Defense after any reductions. Use this in damage formulas. */
    public int getDefense() {
        return getStat(Stat.DEFENSE);
    }

    /** True if an item has lowered this stat. */
    public boolean isWeakened(Stat stat) {
        return getStat(stat) < getBaseStat(stat);
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    public void defend() {
        guarding = true;
    }

    public void stopDefending() {
        guarding = false;
    }
}