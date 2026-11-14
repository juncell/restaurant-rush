public class ComboMeal extends MenuItem {
    private final MenuItem item1;
    private final MenuItem item2;

    public ComboMeal(MenuItem item1, MenuItem item2) {
        super(item1.getName() + " + " + item2.getName() + " Combo",
              calculateComboPrice(item1, item2),
              item1.getPrepUnits() + item2.getPrepUnits());

        if (item1.getName().equalsIgnoreCase(item2.getName())) {
            throw new IllegalArgumentException(
                "A ComboMeal cannot contain two of the same item!"
            );
        }
        this.item1 = item1;
        this.item2 = item2;
    }

    private static int calculateComboPrice(MenuItem item1, MenuItem item2) {
        int combinedCents = item1.getPrice() + item2.getPrice();
        return (int) Math.round(combinedCents * 0.90);
    }

    @Override
    public int getPrice() {
        return calculateComboPrice(item1, item2);
    }

    @Override
    public int getPrepUnits() {
        return item1.getPrepUnits() + item2.getPrepUnits();
    }

    public String getDetailedBreakdown() {
        int savings = (item1.getPrice() + item2.getPrice()) - getPrice();
        return String.format(
            "%s (Includes %s & %s) - Saves $%.2f!",
            getName(),
            item1.getName(),
            item2.getName(),
            savings / 100.0
        );
    }

    public MenuItem getItem1() {
        return item1;
    }

    public MenuItem getItem2() {
        return item2;
    }
}
