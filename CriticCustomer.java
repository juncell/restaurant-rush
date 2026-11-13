import java.util.Random; 

public class CriticCustomer extends Customer {
    
    // preference/ unique behavior: 
    // 1) randomly select a favourite dish of the critic from preference array 
    // 2) if that dish is ordered --> satisfaction loss = 15 rather than 12 
    
    private final String[] preference = {"Burger", "Salad", "Pasta"};
    private final String preferredDish;

    public CriticCustomer(int ID) {
        this(ID, new Random());
    }

    // Stage 4: pass the game's seeded Random so the critic's favourite dish is reproducible
    public CriticCustomer(int ID, Random random) {
        super(ID); 
        this.discount = 0.0; 

        // choosing random dish as favourite
        int randInt = random.nextInt(this.preference.length); 
        this.preferredDish = this.preference[randInt];  
    }

    public String[] getPreference() {
        return this.preference;
    }

    public String getPreferredDish() {
        return this.preferredDish;
    }

    @Override
    public void satisfactionLoss() {
        // unique behaviour: if customer has ordered preferred dish -> satisfaction -15 
        // else, satisfaction -12 

        if (!this.served) {
            Order order = getCurrentOrder();

            // if the preferred dish is ordered 
            if (order != null && order.getMenuItem().getName().contains(this.preferredDish)) {
                setSatisfaction(this.satisfaction - 15);
            } else {
                setSatisfaction(this.satisfaction - 12); // the default 
            }
        }
    }
}