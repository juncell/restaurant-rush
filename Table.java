public class Table {
    private int tableNumber = 0; 
    private int capacity = 0; 
    private int numRemaining = 0; 

    public Table(int tableNumber, int capacity){
        this.tableNumber = tableNumber; 
        this.capacity = capacity; 
    }

    public void assignCustomer(Customer customer){
        if (this.numRemaining == 0) {
            System.out.println("Table full!")
        }

        else{
            this.numRemaining -= 1; 
            customer.setTableNumber(this.tableNumber);

        }  
    }

    public int get_numRemaining(){
        return this.numRemaining; 
    }

    public void removeCustomer() {
        if (this.numRemaining == this.capacity) {
            System.out.println("No one to remove from an empty table!"); 
        }

        else {
            this.numRemaining += 1; 
        }
    }

    public boolean isOccupied(){
        if (this.numRemaining == 0) {
            return true; 
        }

        else {
            return false; 
        }
    }
}