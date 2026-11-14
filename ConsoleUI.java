import java.util.List;
import java.util.OptionalDouble;
import java.util.Scanner;

public class ConsoleUI{
	private static final String LINE = "============================================================";
	
	private final Scanner scanner;
	private final boolean echoInput;
	
	public ConsoleUI(Scanner scanner) {
		this(scanner, false);
	}
	
	// prints input line so test script shows what was inputed
	public ConsoleUI(Scanner scanner, boolean echoInput) {
		this.scanner = scanner;
		this.echoInput = echoInput;
	}
	
	//INPUT HANDLING
	public WaiterAction promptWaiterAction() {
		System.out.println("Waiter task: 1 = Seat Customer, 2 = Take order, 3 = Serve order, 0 = Wait");
		int choice = readInt("Waiter > ", 0, 3);
		return WaiterAction.fromMenuNumber(choice);
	}
	
	public int promptCustomerId(String prompt) {
		return readInt(" " + prompt + " (customer ID): ", 0, 99);
	}
	
	public int promptTableNumber() {
		return readInt(" Which table? (table number): ", 0, 99);
	}
	
	public int promptChefTarget(){
		return readInt("Chef > cook for which customer? (customer ID, 0 = wait): ", 0, 99);
	}
	
	//Stage 3: Host seats one queued customer per turn in addition to Waiter
	public int promptHostTarget() {
		return readInt("Host > seat which customer? (customer ID, 0 = wait): ", 0, 99);
	}
	
	//Stage 3: asked at the start of a turn until Happy Hour has been used
	public boolean promptYesNo(String question) {
		while (true) {
			System.out.print(question + " (y/n): ");
			String line = nextInputLine();
			if (line == null) {
				System.out.println("(no more input - answering n)");
				return false;
			}
			if (line.equalsIgnoreCase("y") || line.equalsIgnoreCase("yes")) {
				return true;
			}
			if (line.equalsIgnoreCase("n") || line.equalsIgnoreCase("no")) {
				return false;
			}
		System.out.println("  ! Please type y or n.");
		
		}
	}
	
	//OUTPUT
	//Opening screen: GameManager can call at start of run()
	public void displayWelcome(Menu menu, int maxTurns) {
		System.out.println(LINE);
		System.out.println("  RESTAURANT RUSH - you are the manager");
		System.out.println(LINE);
		System.out.println("You have " + maxTurns + " turns. Customers choose their own food from this menu:");
		for (int i = 0; i < menu.getItemCount(); i++) {
			MenuItem item = menu.getItem(i);
			System.out.println(" - " + item.getName() + " " + formatMoney(item.getPrice()) + " (" + item.getPrepUnits() + " prep unit" + (item.getPrepUnits() > 1 ? "s" : "") + ")");
		}
		System.out.println("Type numbers to choose. 0 means wait.");
	}
	
	public void displayTurnStart(int turn, int maxTurns, Restaurant restaurant, GameStatistics stats) {
		System.out.println();
		System.out.println(LINE);
		System.out.println(" TURN " + turn + " / " + maxTurns);
		System.out.println("Revenue: " + formatMoney(stats.getRevenueCents()) + " Avg satisfaction (paid): " + formatAverage(stats.getAverageSatisfaction()));
		
		List <Customer> queue = restaurant.getWaitingQueue();
		if (queue.isEmpty()) {
			System.out.println("Waiting queue: (empty)");
		} else {
			System.out.println("Waiting queue:");
			for (Customer customer : queue) {
				System.out.println(" " + label(customer) + " satisfaction " + customer.getSatisfaction());
			}
		}
		
		List <Customer> seated = restaurant.getSeatedCustomers();
		if (seated.isEmpty()) {
			System.out.println("Occupied tables: (none)");
		} else {
			System.out.println("Occupied tables:");
			for (Customer customer : seated) {
				Table table = restaurant.getTableOf(customer);
				System.out.println("  T" + table.getTableNumber() + ": " + label(customer) 
				+ "  satisfaction " + customer.getSatisfaction() 
				+ "  | " + describeStatus(customer));
			}
		}
		
		System.out.println("Staff tasks: Host = seat one queued customer / wait;" 
				+ " Waiter = seat / take order / serve / wait;"
				+ " Chef = cook 1 unit of a placed order / wait;"
				+ " Cashier = automatic.");
	}
	
	//Stage 3: call right after displayTurnStart so the manager sees remaining duration
	public void displayHappyHourStatus(HappyHour happyHour) {
		if(happyHour.isActive()) {
			System.out.println("Happy Hour: ACTIVE - " + happyHour.getRemainingTurns() 
			+ " turn(s) left including this one (20% off new orders, waiting customers +15)");
		} else if (happyHour.isUsedOnce()){
			System.out.println("Happy Hour: used");
		} else {
			System.out.println("Happy Hour: available (once per game)");
		}
	}
	
	//Stage 4: shows every table's capacity and who (if anyone) sits there
	public void displayTableLayout(Restaurant restaurant) {
		System.out.println("Tables:");
		for (Table table : restaurant.getTables()) {
			String size = (table instanceof GroupTable) ? " (seats " + ((GroupTable) table).getCapacity() + ")" : "";
			String who = "free";
			if (table instanceof GroupTable && ((GroupTable) table).getParty() != null) {
				who = "occupied by " + label(((GroupTable) table).getParty());
			} else if (table instanceof GroupTable && !((GroupTable) table).getIndividuals().isEmpty()) {
				// Stage 4: individuals sharing a table with strangers
				StringBuilder names = new StringBuilder();
				for (Customer customer : ((GroupTable) table).getIndividuals()) {
					if (names.length() > 0) {
						names.append(", ");
					}
					names.append(label(customer));
				}
				int free = table.get_seatsRemaining();
				who = "shared by " + names + " - " + (free == 0 ? "full" : free + " seat(s) free for individuals");
			} else if (table.isOccupied()) {
				who = "occupied";
			}
			System.out.println("  T" + table.getTableNumber() + size + ": " + who);
		}
	}
	
	public void showMessage(String message) {
		System.out.println("  > " + message);
	}
	
	public void showError(String message) {
		System.out.println("  ! Invalid: " + message + " Choose again.");
	}
	
	public void displayTurnEnd(int turn, int maxTurns, GameStatistics stats) {
		System.out.println("-- End of turn " + turn 
				+ ": served " + stats.getServedCount() 
				+ " | unhappy departures " + stats.getUnhappyDepartures()
				+ " | paid " + stats.getPaidCount()
				+ " | revenue " + formatMoney(stats.getRevenueCents())
				+ " | avg satisfaction " + formatAverage(stats.getAverageSatisfaction())
				+ " | turns used " + turn + "/" + maxTurns);
	}
	
	public void displayFinalResult(boolean victory, GameStatistics stats) {
		System.out.println();
		System.out.println(LINE);
		System.out.println(victory ? "  VICTORY!" : "  DEFEAT.");
		System.out.println(LINE);
		OptionalDouble avg = stats.getAverageSatisfaction();
		boolean paidMet = stats.getPaidCount() >= stats.getMinPaidCustomers();
		boolean revenueMet = stats.getRevenueCents() >= stats.getMinRevenueCents();
		boolean avgMet = avg.isPresent() && avg.getAsDouble() >= stats.getMinAvgSatisfaction();
		System.out.println(" " + mark(paidMet) + " Paid customers: " + stats.getPaidCount() + " (need " + stats.getMinPaidCustomers() + ")");
		System.out.println(" " + mark(revenueMet) + " Revenue: " + formatMoney(stats.getRevenueCents()) + " (need " + formatMoney(stats.getMinRevenueCents()) + ")");
		System.out.println(" " + mark(avgMet) + " Average Satisfaction: " + formatAverage(avg) + " (need " + stats.getMinAvgSatisfaction() + ")");
		System.out.println(" Served: " + stats.getServedCount() + " Unhappy departures: " + stats.getUnhappyDepartures());
	}
	
	//PRIVATE HELPERS
	/* Reads a whole number between min and max, asking again until it gets one.
	 * If input has run out (end of a test script), min is returned (always 0 = wait).
	*/
	private int readInt(String prompt, int min, int max) {
		while(true) {
			System.out.print(prompt);
			String line = nextInputLine();
			if (line == null) {
				System.out.println("(no more input = choosing " + min + ")");
				return min;
			}
			try {
				int value = Integer.parseInt(line);
				if (value >= min && value <= max) {
					return value;
				}
				System.out.println("  ! " + value + " is out of range. Enter a number from " + min + " to " + max + ".");
			} catch(NumberFormatException e) {
				System.out.println("  ! \"" + line + "\" is not a number. Enter a number from " + min + " to " + max + ".");
			}
		}
	}
	
	/* Returns the next real input line, trimmed.
	 * Skips blank lines and lines starting with '#', so test scripts can contain comments.
	 * Returns null when there is no more input.
	 */
	private String nextInputLine() {
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			if (echoInput) {
				System.out.println(line);
			}
			return line;
		}
		return null;
	}
	
	// Converts cents to dollars, e.g. 2430 -> "$24.30".
	private String formatMoney(int cents) {
		return String.format("$%d.%02d", cents / 100, cents % 100);
	}
	
	private String formatAverage(OptionalDouble avg) {
		return avg.isPresent() ? String.format("%.1f", avg.getAsDouble()) : "N/A";
	}
	
	private String mark(boolean met) {
		return met ? "[MET]   " : "[NOT MET]";
	}
	
	/* e.g. "C2 (VIP)". The type comes from the class name: 
	 * Customer -> Regular, VIPCustomer -> VIP
	 */
	private String label(Customer customer) {
		// Stage 4: a party shows its size and each member's type, e.g. "G2 (party of 3: VIP, Regular, Critic)"
		if (customer instanceof CustomerGroup) {
			CustomerGroup group = (CustomerGroup) customer;
			StringBuilder types = new StringBuilder();
			for (Customer member : group.getMembers()) {
				if (types.length() > 0) {
					types.append(", ");
				}
				String t = member.getClass().getSimpleName().replace("Customer", "");
				types.append(t.isEmpty() ? "Regular" : t);
			}
			return "G" + group.getID() + " (party of " + group.getPartySize() + ": " + types + ")";
		}
		String type = customer.getClass().getSimpleName().replace("Customer", "");
		return "C" + customer.getID() + " (" + (type.isEmpty() ? "Regular" : type) + ")";
	}
	
	// Describes where the customer is in the service flow using only the domain's getters.
	private String describeStatus(Customer customer) {
		Order order = customer.getCurrentOrder();
		if (customer.isReadyToPay()) {
			return "ready to pay " + formatMoney(order.getLockedPriceCents());
		}
		if (customer.isServed()) {
			return "eating " + order.getMenuItem().getName();
		}
		if (order == null) {
			return "seated, waiting to order";
		}
		int done = order.getMenuItem().getPrepUnits() - order.getRemainingPrepUnits();
		return "ordered " + order.getMenuItem().getName() + " [" + done + "/" 
				+ order.getMenuItem().getPrepUnits() + " units, "
				+ order.getStatus() + "] bill" + formatMoney(order.getLockedPriceCents());
	}
}
