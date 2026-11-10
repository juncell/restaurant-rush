public class Table {
    private int tableNumber = 0; 
    private int capacity = 1;  // for the 3 stages, capacity = 1 (default), else can scale 
    private int seatsRemaining = 0; 

    // constructor 
    public Table(int tableNumber, int capacity){
        this.tableNumber = tableNumber; 
        this.capacity = capacity; 
        this.seatsRemaining = capacity; 
    }

    public void assignCustomer(Customer customer){
        // if no more seats -> dont assign customer 
        if (this.seatsRemaining == 0) {
            System.out.println("Table full! Choose another table.");

        }

        else{
            this.seatsRemaining -= 1; 
            customer.setTableNumber(this.tableNumber);
        }  
    }

    public int get_seatsRemaining(){
        return this.seatsRemaining; 
    }

    public void removeCustomer() {
        // if all table seats are empty -> cannot remove customer 
        if (this.seatsRemaining == this.capacity) {
            System.out.println("No one to remove from empty table!"); 
        }

        else {
            this.seatsRemaining += 1; 
        }
    }

    public boolean isOccupied(){
        if (this.seatsRemaining == 0) {
            return true; 
        }

        else {
            return false; 
        }
    }

    public int getTableNumber(){
        return this.tableNumber; 
    }

}