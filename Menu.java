public class Menu {
    private MenuItem[] items;
    private int itemCount;

    public Menu() {
        items = new MenuItem[10];
        itemCount = 0;

        addItem(new MenuItem("Salad", 900, 1));
        addItem(new MenuItem("Burger", 1500, 1));
        addItem(new MenuItem("Pasta", 1800, 2));
    }

    public void addItem(MenuItem item) {
        if (itemCount < size()) {
            items[itemCount] = item;
            itemCount++;
        }
    }

    public MenuItem getItem(int index) {
        if (index >= 0 && index < itemCount) {
            return items[index];
        }
        return null;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int size() {
        return items.length;
    }
}
