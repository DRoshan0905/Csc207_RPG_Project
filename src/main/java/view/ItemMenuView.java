package view;

import interface_adapter.items.ItemRow;
import interface_adapter.items.ItemsViewModel;
import interface_adapter.items.UseItemController;
import interface_adapter.items.ViewItemsController;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/** The item list shown in the bottom-left box. */
public class ItemMenuView {
    private static final int MAX_ROWS = 3;

    private static final Color TEXT = new Color(239, 248, 255);
    private static final Color ACCENT = new Color(105, 217, 242);
    private static final Color MUTED = new Color(150, 180, 195);

    private final ItemsViewModel viewModel;
    private final ViewItemsController viewItems;
    private final UseItemController useItem;
    private final Map<String, BufferedImage> icons;

    private boolean open;
    private ItemRow selected;

    // Index of the first visible row when there are more than MAX_ROWS.
    private int scroll;

    public ItemMenuView(ItemsViewModel viewModel,
                        ViewItemsController viewItems,
                        UseItemController useItem,
                        Map<String, BufferedImage> icons) {
        this.viewModel = viewModel;
        this.viewItems = viewItems;
        this.useItem = useItem;
        this.icons = icons;
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
        selected = null;
        scroll = 0;
        viewItems.execute();
    }

    /** Scrolls the list by whole rows; positive moves down. */
    public void scroll(int rows) {
        int last = Math.max(0, viewModel.getRows().size() - MAX_ROWS);
        scroll = Math.max(0, Math.min(last, scroll + rows));
    }

    public void close() {
        open = false;
        selected = null;
    }

    public boolean hasSelection() {
        return selected != null;
    }

    /** True if the selected item is used on enemies. */
    public boolean targetsEnemies() {
        return selected != null && selected.targetsEnemies();
    }

    public boolean canUseOnHero(int heroIndex) {
        return selected != null
                && !selected.targetsEnemies()
                && selected.canUseOn(heroIndex);
    }

    public boolean canUseOnEnemy(int enemyIndex) {
        return selected != null
                && selected.targetsEnemies()
                && selected.canUseOn(enemyIndex);
    }

    /** Uses the selected item on this position of its target side. */
    public void useOn(int targetIndex) {
        if (selected != null) {
            useItem.execute(selected.getName(), targetIndex);
        }
    }

    public boolean contains(Point point, Rectangle box) {
        return rowAt(point, box) != null;
    }

    /** Selects the clicked item. Returns false if none was clicked. */
    public boolean select(Point point, Rectangle box) {
        ItemRow row = rowAt(point, box);

        if (row == null) {
            return false;
        }

        selected = row;
        viewModel.setMessage(
                (row.targetsEnemies() ? "Click an enemy" : "Click a hero")
                        + " to use " + row.getName() + ".");

        return true;
    }

    public void draw(Graphics2D graphics, Rectangle box, Point mouse) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.clip(box);

        text(g, viewModel.getMessage(),
                box.x + 12, box.y + 20, 13, ACCENT);

        for (int i = 0; i < visibleRows(); i++) {
            drawRow(g, viewModel.getRows().get(scroll + i),
                    rowBox(box, i), mouse);
        }

        drawScrollHint(g, box);

        g.dispose();
    }

    private void drawRow(Graphics2D g, ItemRow row,
                         Rectangle r, Point mouse) {
        if (row == selected || r.contains(mouse)) {
            g.setColor(new Color(
                    105, 217, 242, row == selected ? 75 : 35));
            g.fillRoundRect(r.x, r.y, r.width, r.height, 6, 6);
        }

        BufferedImage icon = icons.get(row.getName());

        if (icon != null) {
            // Double small pixel art, but keep larger icons inside the row.
            double scale = Math.min(2.0, 24.0 / icon.getHeight());
            int w = (int) (icon.getWidth() * scale);
            int h = (int) (icon.getHeight() * scale);

            g.drawImage(icon, r.x + 18 - w / 2,
                    r.y + (r.height - h) / 2, w, h, null);
        }

        text(g, row.getName(), r.x + 40, r.y + 20, 14, TEXT);
        text(g, row.getDescription(),
                r.x + 150, r.y + 20, 12, MUTED);
        text(g, row.getCount(),
                r.x + r.width - 32, r.y + 20, 14, ACCENT);
    }

    // Arrows in the top-right corner when rows are hidden above or below.
    private void drawScrollHint(Graphics2D g, Rectangle box) {
        boolean above = scroll > 0;
        boolean below =
                scroll + MAX_ROWS < viewModel.getRows().size();

        if (!above && !below) {
            return;
        }

        String hint = (above ? "\u25B2" : " ")
                + (below ? "\u25BC" : " ") + " scroll";

        text(g, hint, box.x + box.width - 78, box.y + 20, 11, MUTED);
    }

    private ItemRow rowAt(Point point, Rectangle box) {
        List<ItemRow> rows = viewModel.getRows();

        for (int i = 0; i < visibleRows(); i++) {
            if (rowBox(box, i).contains(point)) {
                return rows.get(scroll + i);
            }
        }

        return null;
    }

    private int visibleRows() {
        return Math.min(MAX_ROWS, viewModel.getRows().size() - scroll);
    }

    private Rectangle rowBox(Rectangle box, int i) {
        return new Rectangle(
                box.x + 6, box.y + 28 + i * 32, box.width - 12, 30);
    }

    private void text(Graphics2D g, String text,
                      int x, int y, int size, Color color) {
        g.setFont(new Font("Dialog", Font.BOLD, size));
        g.setColor(color);
        g.drawString(text, x, y);
    }
}
