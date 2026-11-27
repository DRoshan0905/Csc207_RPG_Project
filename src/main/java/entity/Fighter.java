package entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Fighter implements ItemTarget {

    public final String name;
    public final boolean player;

    public final int maxHp;
    public final int attack;
    public final int defense;
    public final int speed;
    private final boolean boss;

    public int hp;
    public boolean guarding;
    private int wavesCleared;
    private int finishingBlows;
    private int attackBonus;
    private int defenseBonus;
    private int hpBonus;

    private enum Buff {
        ATTACK, DEFENSE, HP
    }

    public Fighter(
            String name,
            boolean player,
            int maxHp,
            int attack,
            int defense,
            int speed) {
        this(name, player, maxHp, attack, defense, speed, false);
    }

    public Fighter(String name, boolean player, int maxHp, int attack, int defense, int speed, boolean boss) {

        this.name = name;
        this.player = player;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.boss = boss;

        this.hp = maxHp;
        this.guarding = false;
    }

    public boolean isBoss() {
        return boss;
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
        return maxHp + 5 * wavesCleared + hpBonus;
    }

    public int getAttack() {
        return attack + wavesCleared + attackBonus;
    }

    public int getDefense() {
        return defense + wavesCleared + defenseBonus;
    }

    public int getFinishingBlows() {
        return finishingBlows;
    }

    String recordFinishingBlow(Random random) {
        finishingBlows++;
        if (finishingBlows % 2 != 0) {
            return "";
        }

        List<Buff> available = new ArrayList<>();
        if (attackBonus < 2) {
            available.add(Buff.ATTACK);
        }
        if (defenseBonus < 2) {
            available.add(Buff.DEFENSE);
        }
        if (hpBonus < 6) {
            available.add(Buff.HP);
        }
        if (available.isEmpty()) {
            return "";
        }

        Buff buff = available.get(random.nextInt(available.size()));
        if (buff == Buff.ATTACK) {
            attackBonus++;
            return name + " gained +1 attack!";
        }
        if (buff == Buff.DEFENSE) {
            defenseBonus++;
            return name + " gained +1 defence!";
        }
        hpBonus += 3;
        restoreHp(3);
        return name + " gained +3 max HP!";
    }

    public void advanceWave() {
        wavesCleared++;
        if (alive()) {
            restoreHp(5);
        }
        stopDefending();
    }

    @Override
    public void restoreHp(int amount) {
        hp = Math.min(getMaxHp(), hp + amount);
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
