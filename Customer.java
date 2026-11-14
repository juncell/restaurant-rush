public class Customer {
    // since all customers have this attribute --> make it protected 
    protected int satisfaction = 100;
    protected boolean readyToPay = false;
    protected boolean served = false;
    protected boolean readyToServe = false;
    protected int tableNumber = 0; 
    private Order currentOrder;
    protected int customerID; 


    public Customer(int ID){
        this.customerID = ID; 
    }; 
    
    public int getSatisfaction() {
        return this.satisfaction;
    }

    public void setSatisfaction(int satisfaction) {
        if (satisfaction > 100) {
            this.satisfaction = 100; 
        }
        else if (satisfaction < 0) {
            this.satisfaction = 0; 
        }

        else {
            this.satisfaction = satisfaction; 
        }
    }

    public void setTableNumber(int tableNumber){
        this.tableNumber = tableNumber; 
    }

    public boolean isReadyToPay() {
        return this.readyToPay;
    }

    public void setReadyToPay(boolean readyToPay) {
        this.readyToPay = readyToPay;
    }

    public boolean isServed() {
        return this.served;
    }

    public void setServed(boolean served) {
        this.served = served;
    }

    public boolean isReadyToServe() {
        return this.readyToServe;
    }

    public void setReadyToServe(boolean readyToServe) {
        this.readyToServe = readyToServe;
    }

    public Order getCurrentOrder() {
        return this.currentOrder;
    }

    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
    }

    public void satisfactionLoss() {
        if (!this.served) {
            setSatisfaction(this.satisfaction - 8);
        }
    }

    public boolean hasAbandoned() {
        return this.satisfaction == 0 && !this.served;
    }

    public int getID(){
        return this.customerID; 
    }

    /**
     * Documented deterministic rule: customer n chooses menu item (n - 1) mod menu size.
     */
    public MenuItem chooseDish(Menu menu) {
        return menu.getItem((this.customerID - 1) % menu.getItemCount());
    }
}