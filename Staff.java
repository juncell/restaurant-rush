public abstract class Staff{
  protected int ID;
  protected boolean hasActedThisTurn;

  // create constructor 
  public Staff(int ID){
    this.ID = ID;
    this.hasActedThisTurn = false;
  }

  // accessor and mutator methods
  public int getID(){
    return ID;
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

