package view;

import java.awt.*;

/** A damage number that rises above a fighter and fades. */
public class DamagePopup {
    private static final int DURATION_MS = 900;
    private static final int SIZE = 30;

    private final String label;
    private final Color color;
    private final long startedAt = System.currentTimeMillis();

    public DamagePopup(int damage, boolean heroWasHit) {
        label = "-" + damage;
        color = heroWasHit
                ? new Color(255, 95, 95)
                : new Color(255, 221, 87);
    }

    /** Draws the number above the given sprite area. */
    public void draw(Graphics2D graphics, Rectangle box) {
        float t = (System.currentTimeMillis() - startedAt)
                / (float) DURATION_MS;

        if (t >= 1) {
            return;
        }

        Graphics2D g = (Graphics2D) graphics.create();

        // Fully visible for the first half, then fades out.
        g.setComposite(AlphaComposite.SrcOver.derive(
                Math.min(1f, (1 - t) * 2)));

        g.setFont(new Font("Dialog", Font.BOLD, SIZE));

        int width = g.getFontMetrics().stringWidth(label);
        int x = box.x + (box.width - width) / 2;
        int y = box.y + 20 - (int) (t * 35);

        // Dark outline keeps the number readable.
        g.setColor(Color.BLACK);

        for (int dx = -2; dx <= 2; dx += 2) {
            for (int dy = -2; dy <= 2; dy += 2) {
                g.drawString(label, x + dx, y + dy);
            }
        }

        g.setColor(color);
        g.drawString(label, x, y);
        g.dispose();
    }
}
