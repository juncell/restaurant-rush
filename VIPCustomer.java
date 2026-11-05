public class VIPCustomer extends Customer {
    public VIPCustomer(int ID) {
        super(ID);       // constructor for the parent Customer class 
        this.discount = 0.10;  // override discount initialisation in Customer 
    }

    @Override
    public void satisfactionLoss() {
        if (!this.served) {
            setSatisfaction(this.satisfaction - 5);  // change satisfaction decrement
        }
    }
}