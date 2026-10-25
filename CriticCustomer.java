public class CriticCustomer extends Customer {
    
    // preference/ unique behavior: 
    // 1) randomly select a favourite dish of the critic from preference array 
    // 2) if that dish is ordered --> satisfaction loss = 10 rather than 12 
    
    private final String[] preference = {"Burger", "Salad", "Pasta"};
    private final String preferredDish;

    public CriticCustomer() {
        this("Pasta");
    }

    public CriticCustomer(String preferredDish) {
        super();
        this.discount = 0.0;
        this.preferredDish = preferredDish;
    }

    public String[] getPreference() {
        return this.preference;
    }

    public String getPreferredDish() {
        return this.preferredDish;
    }

    @Override
    public void satisfactionLoss() {
        if (!this.served) {
            Order order = getCurrentOrder();
            if (order != null && order.getMenuItem().getName().contains(this.preferredDish)) {
                setSatisfaction(this.satisfaction - 10);
            } else {
                setSatisfaction(this.satisfaction - 12);
            }
        }
    }
}