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
        // Stage 2 specification: Critic loses 12 satisfaction per waiting turn
        if (!this.served) {
            setSatisfaction(this.satisfaction - 12);
        }
    }

    /**
     * Critic special behaviour: always orders the most demanding item on the menu
     * (most preparation units; ties broken by highest price).
     */
    @Override
    public MenuItem chooseDish(Menu menu) {
        MenuItem mostDemanding = menu.getItem(0);
        for (int i = 1; i < menu.getItemCount(); i++) {
            MenuItem candidate = menu.getItem(i);
            if (candidate.getPrepUnits() > mostDemanding.getPrepUnits()
                    || (candidate.getPrepUnits() == mostDemanding.getPrepUnits()
                        && candidate.getPrice() > mostDemanding.getPrice())) {
                mostDemanding = candidate;
            }
        }
        return mostDemanding;
    }
}