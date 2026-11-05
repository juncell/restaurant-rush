public class Menu{
  private MenuItem salad;
  private MenuItem burger;
  private MenuItem pasta;

  public Menu(){
    salad = new MenuItem("Salad", 900,1);
    burger = new MenuItem("Burger", 1500,1);
    pasta = new MenuItem("Pasta", 1800, 2);
  }

public MenuItem getSalad(){
  return salad;
}

public MenuItem getBurger(){
  return burger;
}

public MenuItem getPasta(){
  return pasta;
}
}
