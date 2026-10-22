public class MenuItem{
  // instance variables
  private String name;
  private int basePrice;
  private int prepUnits; 

  // create constructors 
  public MenuItem(String name, int basePrice, int prepUnits){
    this.name = name;
    this.basePrice = basePrice;
    this.prepUnits = prepUnits;
  }

  // accessor methods
  public String getName(){
    return name;
  }

  public int getPrice(){
    return basePrice;
  }

  public int getPrepUnits(){
    return prepUnits;
  }
}
