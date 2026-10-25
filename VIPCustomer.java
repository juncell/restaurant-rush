public class VIPCustomer extends Customer {
    public VIPCustomer() {
        super();       // constructor for the parent Customer class 
        this.discount = 0.10;
    }

    @Override
    public void satisfactionLoss() {
        if (!this.served) {
            setSatisfaction(this.satisfaction - 5);
        }
    }
}