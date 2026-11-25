package interface_adapter.items;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class ItemsViewModel {
    /** Fired after an item is used; the new value is the hero's index. */
    public static final String ITEM_USED = "itemUsed";

    private final PropertyChangeSupport support =
            new PropertyChangeSupport(this);

    private List<ItemRow> rows = List.of();
    private String message = "";

    public List<ItemRow> getRows() {
        return rows;
    }

    public void setRows(List<ItemRow> rows) {
        this.rows = List.copyOf(rows);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void fireItemUsed(int heroIndex) {
        support.firePropertyChange(ITEM_USED, null, heroIndex);
    }

    public void addPropertyChangeListener(
            PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
