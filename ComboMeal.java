public class ComboMeal extends MenuItem{
  private MenuItem item1;
  private MenuItem item2;

// create constructor 
  public ComboMeal(MenuItem item1, MenuItem item2){
    super (item1.getName() + " + " + item2.getName() + " Combo ", 0, item1.getPrepUnits() + item2.getPrepUnits());
    this.item1 = item1;
    this.item2 = item2;
  }

  // override 
  public int getPrice(){
    int combinedPrice = item1.getPrice() + item2.getPrice();
    return (int) Math.round(combinedPrice * 0.9); 
  } 

  public int getPrepUnits(){
    return item1.getPrepUnits() + item2.getPrepUnits();
  }

  public MenuItem getItem1(){
    return item1;
  }

  public MenuItem getItem2(){
    return item2;
  }
}
