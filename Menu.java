public class Menu {
    private MenuItem[] items;
    private int itemCount;

    public Menu() {
        items = new MenuItem[10];
        itemCount = 0;

        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);

        addItem(salad);
        addItem(burger);
        addItem(pasta);
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
