public abstract class Staff{
  protected String name;
  protected boolean hasActedThisTurn;

  // create constructor 
  public Staff(String name){
    this.name = name;
    this.hasActedThisTurn = false;
  }

  // accessor and mutator methods
  public String getName(){
    return name;
  }

  public boolean hasActed(){
    return hasActedThisTurn;
  }

  public void setActed(boolean acted){
    this.hasActedThisTurn = acted;
  }

  public void resetTurn(){
    this.hasActedThisTurn = false;
  }
}

