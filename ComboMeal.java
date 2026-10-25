public class ComboMeal extends MenuItem {
    private final MenuItem item1;
    private final MenuItem item2;

    public ComboMeal(MenuItem item1, MenuItem item2) {
        super(item1.getName() + " + " + item2.getName() + " Combo", 0, item1.getPrepUnits() + item2.getPrepUnits());
        this.item1 = item1;
        this.item2 = item2;
    }

    public int getPrice() {
        int combinedCents = item1.getPrice() + item2.getPrice();
        return (int) Math.round(combinedCents * 0.90);
    }

    public int getPrepUnits() {
        return item1.getPrepUnits() + item2.getPrepUnits();
    }

    public MenuItem getItem1() {
        return item1;
    }

    public MenuItem getItem2() {
        return item2;
    }
}


