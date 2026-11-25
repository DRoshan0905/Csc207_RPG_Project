package view;

import java.awt.*;

/** A circle on the ground under the fighter whose turn it is. */
public class TurnMarker {
    private static final Color HERO = new Color(255, 221, 87);
    private static final Color ENEMY = new Color(255, 95, 95);

    private static final int MIN_WIDTH = 86;
    private static final int MAX_WIDTH = 140;

    /** Draws the circle centred on the fighter's feet. */
    public void draw(Graphics2D graphics, Point feet,
                     int spriteWidth, boolean hero) {
        Graphics2D g = (Graphics2D) graphics.create();

        Color color = hero ? HERO : ENEMY;
        int width = Math.max(
                MIN_WIDTH, Math.min(MAX_WIDTH, spriteWidth));
        int height = width / 4;
        int x = feet.x - width / 2;
        int y = feet.y - height / 2;

        g.setColor(new Color(
                color.getRed(), color.getGreen(),
                color.getBlue(), 80));
        g.fillOval(x, y, width, height);

        g.setColor(color);
        g.setStroke(new BasicStroke(3));
        g.drawOval(x, y, width, height);

        g.dispose();
    }
}
