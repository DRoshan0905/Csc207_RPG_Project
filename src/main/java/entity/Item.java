package entity;

public interface Item {
    String getName();

    String getDescription();

    boolean canUse(ItemTarget target);

    void use(ItemTarget target);
}
