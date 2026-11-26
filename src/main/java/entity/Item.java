package entity;

public interface Item {
    String getName();

    String getDescription();

    /** Heroes by default; debuff items override this to target monsters. */
    default TargetSide getTargetSide() {
        return TargetSide.ALLY;
    }

    boolean canUse(ItemTarget target);

    /** Applies the item and returns a short summary such as "+30 HP". */
    String use(ItemTarget target);
}
