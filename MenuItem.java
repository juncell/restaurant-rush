public class MenuItem {
    private final String name;
    private final int basePriceCents;
    private final int prepUnits;

    public MenuItem(String name, int basePriceCents, int prepUnits) {
        this.name = name;
        this.basePriceCents = basePriceCents;
        this.prepUnits = prepUnits;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return basePriceCents;
    }

    public int getPrepUnits() {
        return prepUnits;
    }
}


