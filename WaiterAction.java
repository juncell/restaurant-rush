
public enum WaiterAction {

	WAIT(0, "Wait"),
	SEAT_CUSTOMER(1, "Seat a customer"),
	TAKE_ORDER(2, "Take an order"),
	SERVE_ORDER(3, "Serve a READY order");
	
	private final int menuNumber;
	private final String label;
	
	WaiterAction(int menuNumber, String label){
		this.menuNumber = menuNumber;
		this.label = label;
	}
	
	//Getters
	public int getMenuNumber() {
		return menuNumber;
	}
	
	public String getLabel() {
		return label;
	}
	
	// Converts number typed by the manager into an action. Unknown numbers mean WAIT
	public static WaiterAction fromMenuNumber(int number) {
		for (WaiterAction action : values()) {
			if (action.menuNumber == number) {
				return action;
			}
		}
		return WAIT;
	}

}
