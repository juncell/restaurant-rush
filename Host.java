public class Host extends Staff{
  // create constructor 
  public Host(String name){
    super(name);
  }

  public boolean seatCustomer(Customer customer, Table table){
    if (hasActedThisTurn){
      System.out.println(name + " has already acted this turn!");
      return false;
    }

    if (table.isOccupied()){
      System.out.println("Table is already occupied!"); 
      return false; 
    }
    
    table.seat(customer);
    this.hasActedThisTurn = true;
    return true;
  }
}
