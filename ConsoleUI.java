import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Scanner;

/**
 * Boundary class: the only class that reads the keyboard / test script and prints to the console.
 * It collects the manager's decisions and displays the game state; it holds no game rules.
 */
public class ConsoleUI {
	private static final int WIDTH = 66;
	private static final String HEAVY_LINE = repeat('=', WIDTH);
	private static final String LIGHT_LINE = "  " + repeat('-', WIDTH - 2);
	private static final int LABEL_WIDTH = 18;   // column for "C2 (VIP)" / "G1 (party of 3)"
	private static final int BAR_SLOTS = 10;     // satisfaction bar: one slot per 10 points
	private static final int MENU_WIDTH = 46;    // width of the menu table

	// indentation levels, so every line sits under the one it belongs to
	private static final String PROMPT = "  ";          // "  Waiter > "
	private static final String DETAIL = "           ";  // follow-up questions, under the answer
	private static final String NOTE = "    ";           // "    > message" and "    ! error"

	private final Scanner scanner;
	private final boolean echoInput;
	private boolean decisionsHeaderPending = false;   // printed once per turn, before the first prompt

	public ConsoleUI(Scanner scanner) {
		this(scanner, false);
	}

	// echoInput prints each input line, so a test-script transcript shows what was "typed"
	public ConsoleUI(Scanner scanner, boolean echoInput) {
		this.scanner = scanner;
		this.echoInput = echoInput;
	}

	// ------------------------------------------------------------------ INPUT

	public WaiterAction promptWaiterAction() {
		startDecisions();
		System.out.println();
		System.out.println(PROMPT + "WAITER   1 Seat customer   2 Take order   3 Serve order   0 Wait");
		int choice = readInt(PROMPT + "Waiter > ", 0, 3);
		return WaiterAction.fromMenuNumber(choice);
	}

	public int promptCustomerId(String prompt) {
		return readInt(DETAIL + prompt + " (customer ID): ", 0, 99);
	}

	public int promptTableNumber() {
		return readInt(DETAIL + "Which table? (table number): ", 0, 99);
	}

	public int promptChefTarget() {
		startDecisions();
		System.out.println();
		System.out.println(PROMPT + "CHEF     cook 1 unit of a placed order: type the customer ID, or 0 to wait");
		return readInt(PROMPT + "Chef   > ", 0, 99);
	}

	// Stage 3: the Host seats one queued customer per turn, in addition to the Waiter
	public int promptHostTarget() {
		startDecisions();
		System.out.println();
		System.out.println(PROMPT + "HOST     seat one queued customer: type the customer ID, or 0 to wait");
		return readInt(PROMPT + "Host   > ", 0, 99);
	}

	// Stage 3: asked at the start of a turn until Happy Hour has been used
	public boolean promptYesNo(String question) {
		startDecisions();
		System.out.println();
		while (true) {
			System.out.print(PROMPT + question + " (y/n): ");
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
			System.out.println(NOTE + "! Please type y or n.");
		}
	}

	// ----------------------------------------------------------------- OUTPUT

	// Opening screen, shown once before turn 1
	public void displayWelcome(Menu menu, int maxTurns) {
		System.out.println(HEAVY_LINE);
		System.out.println(centre("R E S T A U R A N T   R U S H", WIDTH));
		System.out.println(centre("you are the manager", WIDTH));
		System.out.println(HEAVY_LINE);
		System.out.println();
		System.out.println("  " + maxTurns + " turns. Customers choose their own food from the menu below.");
		System.out.println();
		String indent = "    ";
		System.out.println(indent + centre("M E N U", MENU_WIDTH));
		System.out.println(indent + repeat('-', MENU_WIDTH));
		System.out.println(indent + String.format("%-26s %8s   %s", "Dish", "Price", "Prep"));
		System.out.println(indent + repeat('-', MENU_WIDTH));
		for (int i = 0; i < menu.getItemCount(); i++) {
			MenuItem item = menu.getItem(i);
			System.out.println(indent + String.format("%-26s %8s   %s",
					item.getName(), formatMoney(item.getPrice()), plural(item.getPrepUnits(), "unit")));
		}
		System.out.println(indent + repeat('-', MENU_WIDTH));
		System.out.println();
		System.out.println("  Type a number and press Enter. 0 always means wait.");
	}

	// Start of every turn: money, queue, tables, staff
	public void displayTurnStart(int turn, int maxTurns, Restaurant restaurant, GameStatistics stats) {
		System.out.println();
		System.out.println(HEAVY_LINE);
		System.out.println("  TURN " + turn + " / " + maxTurns
				+ "      Revenue " + formatMoney(stats.getRevenueCents())
				+ "   |   Avg satisfaction (paid) " + formatAverage(stats.getAverageSatisfaction()));
		System.out.println(HEAVY_LINE);

		section("WAITING QUEUE");
		List<Customer> queue = restaurant.getWaitingQueue();
		if (queue.isEmpty()) {
			System.out.println("    (empty)");
		}
		int position = 1;
		for (Customer customer : queue) {
			String waiting = "waiting for a table";
			if (customer instanceof CustomerGroup) {
				waiting += "  (" + memberTypes((CustomerGroup) customer) + ")";
			}
			System.out.println(row("Q" + position, customer, waiting));
			position++;
		}

		section("TABLES");
		boolean anyoneSeated = false;
		boolean groupTables = false;
		for (Table table : restaurant.getTables()) {
			groupTables = groupTables || (table instanceof GroupTable);
			List<Customer> atTable = customersAt(table, restaurant);
			if (atTable.isEmpty()) {
				if (!(table instanceof GroupTable)) {   // Stage 4 shows free tables on the seating map
					System.out.println("    T" + table.getTableNumber() + "  - free -");
				}
				continue;
			}
			anyoneSeated = true;
			for (Customer customer : atTable) {
				System.out.println(row("T" + table.getTableNumber(), customer, describeStatus(customer)));
			}
		}
		if (groupTables && !anyoneSeated) {
			System.out.println("    (nobody seated yet - see the seating map below)");
		}

		section("STAFF");
		System.out.println("    Waiter    seat, take an order, serve, or wait");
		System.out.println("    Chef      cook 1 unit of a placed order, or wait");
		System.out.println("    Cashier   collects 1 payment per turn (automatic)");

		decisionsHeaderPending = true;
	}

	// Stage 3: called right after displayTurnStart, so the manager also sees the Host and Happy Hour
	public void displayHappyHourStatus(HappyHour happyHour) {
		System.out.println("    Host      seat 1 queued customer, or wait");
		section("HAPPY HOUR");
		if (happyHour.isActive()) {
			System.out.println("    ACTIVE   " + plural(happyHour.getRemainingTurns(), "turn")
					+ " left including this one");
			System.out.println("             20% off new orders, waiting customers +15 at turn start");
		} else if (happyHour.isUsedOnce()) {
			System.out.println("    used");
		} else {
			System.out.println("    available (once per game, lasts 2 turns)");
		}
	}

	// Stage 4: every table drawn as its seats ("o" taken, "." free), with who sits there
	public void displayTableLayout(Restaurant restaurant) {
		section("SEATING MAP");
		int widest = 0;
		for (Table table : restaurant.getTables()) {
			widest = Math.max(widest, seatPicture(table).length());
		}
		for (Table table : restaurant.getTables()) {
			String size = (table instanceof GroupTable) ? plural(((GroupTable) table).getCapacity(), "seat") : "";
			System.out.println("    T" + table.getTableNumber() + "  " + pad(seatPicture(table), widest + 2)
					+ pad(size, 10) + describeTable(table));
		}
	}

	public void showMessage(String message) {
		System.out.println(NOTE + "> " + message);
	}

	public void showError(String message) {
		System.out.println(NOTE + "! Invalid: " + message + " Choose again.");
	}

	public void displayTurnEnd(int turn, int maxTurns, GameStatistics stats) {
		System.out.println();
		System.out.println(LIGHT_LINE);
		System.out.println("  END OF TURN " + turn + "/" + maxTurns);
		System.out.println("  served " + stats.getServedCount()
				+ "  |  left unhappy " + stats.getUnhappyDepartures()
				+ "  |  paid " + stats.getPaidCount()
				+ "  |  revenue " + formatMoney(stats.getRevenueCents())
				+ "  |  avg satisfaction " + formatAverage(stats.getAverageSatisfaction()));
		System.out.println(LIGHT_LINE);
	}

	public void displayFinalResult(boolean victory, GameStatistics stats) {
		OptionalDouble avg = stats.getAverageSatisfaction();
		boolean paidMet = stats.getPaidCount() >= stats.getMinPaidCustomers();
		boolean revenueMet = stats.getRevenueCents() >= stats.getMinRevenueCents();
		boolean avgMet = avg.isPresent() && avg.getAsDouble() >= stats.getMinAvgSatisfaction();

		System.out.println();
		System.out.println(HEAVY_LINE);
		System.out.println(centre(victory ? "VICTORY!" : "DEFEAT.", WIDTH));
		System.out.println(HEAVY_LINE);
		System.out.println(resultRow(paidMet, "Paid customers", String.valueOf(stats.getPaidCount()),
				String.valueOf(stats.getMinPaidCustomers())));
		System.out.println(resultRow(revenueMet, "Revenue", formatMoney(stats.getRevenueCents()),
				formatMoney(stats.getMinRevenueCents())));
		System.out.println(resultRow(avgMet, "Average satisfaction", formatAverage(avg),
				String.valueOf(stats.getMinAvgSatisfaction())));
		System.out.println();
		System.out.println("  Served " + stats.getServedCount() + "  |  Unhappy departures " + stats.getUnhappyDepartures());
		System.out.println(HEAVY_LINE);
	}

	// ------------------------------------------------------- PRIVATE HELPERS

	/* Reads a whole number between min and max, asking again until it gets one.
	 * If input has run out (end of a test script), min is returned (always 0 = wait).
	 */
	private int readInt(String prompt, int min, int max) {
		while (true) {
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
				System.out.println(NOTE + "! " + value + " is out of range. Enter a number from " + min + " to " + max + ".");
			} catch (NumberFormatException e) {
				System.out.println(NOTE + "! \"" + line + "\" is not a number. Enter a number from " + min + " to " + max + ".");
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

	// prints the "YOUR DECISIONS" divider before the first prompt of each turn
	private void startDecisions() {
		if (decisionsHeaderPending) {
			section("YOUR DECISIONS");
			decisionsHeaderPending = false;
		}
	}

	// "  -- TITLE -------------------------------------------------"
	private void section(String title) {
		System.out.println();
		String start = "  -- " + title + " ";
		System.out.println(start + repeat('-', Math.max(3, WIDTH - start.length())));
	}

	// one customer line: "    T1  C1 (Regular)        92 [#########-]  ordered Salad ..."
	private String row(String where, Customer customer, String status) {
		return "    " + pad(where, 4) + pad(shortLabel(customer), LABEL_WIDTH) + bar(customer.getSatisfaction())
				+ "  " + status;
	}

	// "C2 (VIP)", or "G1 (party of 3)" for a Stage 4 party
	private String shortLabel(Customer customer) {
		if (customer instanceof CustomerGroup) {
			return "G" + customer.getID() + " (party of " + ((CustomerGroup) customer).getPartySize() + ")";
		}
		return "C" + customer.getID() + " (" + typeName(customer) + ")";
	}

	// "G1 (party of 3: Critic, Critic, Regular)" or "C2 (VIP)"
	private String label(Customer customer) {
		if (customer instanceof CustomerGroup) {
			CustomerGroup group = (CustomerGroup) customer;
			return "G" + group.getID() + " (party of " + group.getPartySize() + ": " + memberTypes(group) + ")";
		}
		return shortLabel(customer);
	}

	private String memberTypes(CustomerGroup group) {
		List<String> types = new ArrayList<>();
		for (Customer member : group.getMembers()) {
			types.add(typeName(member));
		}
		return String.join(", ", types);
	}

	// Customer -> Regular, VIPCustomer -> VIP, CriticCustomer -> Critic
	private String typeName(Customer customer) {
		String type = customer.getClass().getSimpleName().replace("Customer", "");
		return type.isEmpty() ? "Regular" : type;
	}

	// Where the customer is in the service flow, using only the domain's getters
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
		String dish = order.getMenuItem().getName();
		String bill = "  (bill " + formatMoney(order.getLockedPriceCents()) + ")";
		if (order.getStatus() == Order.Status.READY) {
			return dish + " is READY to serve" + bill;
		}
		int total = order.getMenuItem().getPrepUnits();
		int done = total - order.getRemainingPrepUnits();
		return "ordered " + dish + ", cooked " + done + "/" + total + bill;
	}

	// "[o o o . .]": one mark per seat, "o" taken and "." free
	private String seatPicture(Table table) {
		if (!(table instanceof GroupTable)) {
			return table.isOccupied() ? "[o]" : "[.]";
		}
		int capacity = ((GroupTable) table).getCapacity();
		int taken = capacity - table.get_seatsRemaining();
		List<String> seats = new ArrayList<>();
		for (int i = 0; i < capacity; i++) {
			seats.add(i < taken ? "o" : ".");
		}
		return "[" + String.join(" ", seats) + "]";
	}

	// Stage 4 seating map: who sits at this table
	private String describeTable(Table table) {
		if (!(table instanceof GroupTable)) {
			return table.isOccupied() ? "occupied" : "free";
		}
		GroupTable groupTable = (GroupTable) table;
		if (groupTable.getParty() != null) {
			return label(groupTable.getParty());
		}
		if (groupTable.getIndividuals().isEmpty()) {
			return "free";
		}
		List<String> names = new ArrayList<>();
		for (Customer customer : groupTable.getIndividuals()) {
			names.add(shortLabel(customer));
		}
		int free = table.get_seatsRemaining();
		String sharing = (names.size() > 1) ? "shared, " : "";
		return String.join(", ", names) + "  -  " + sharing
				+ (free == 0 ? "full" : plural(free, "seat") + " free for individuals");
	}

	// Everyone seated at this table (one party, or one or more individuals)
	private List<Customer> customersAt(Table table, Restaurant restaurant) {
		List<Customer> result = new ArrayList<>();
		for (Customer customer : restaurant.getSeatedCustomers()) {
			if (restaurant.getTableOf(customer) == table) {
				result.add(customer);
			}
		}
		return result;
	}

	// " 84 [########--]"
	private String bar(int satisfaction) {
		int filled = Math.round(satisfaction / (100f / BAR_SLOTS));
		return String.format("%3d", satisfaction) + " [" + repeat('#', filled) + repeat('-', BAR_SLOTS - filled) + "]";
	}

	private String resultRow(boolean met, String what, String value, String target) {
		return String.format("  %-9s  %-22s %8s   (need %s)", met ? "[MET]" : "[NOT MET]", what, value, target);
	}

	// Converts cents to dollars, e.g. 2430 -> "$24.30"
	private String formatMoney(int cents) {
		return String.format("$%d.%02d", cents / 100, cents % 100);
	}

	private String formatAverage(OptionalDouble avg) {
		return avg.isPresent() ? String.format("%.1f", avg.getAsDouble()) : "N/A";
	}

	// "1 unit", "2 units"
	private static String plural(int count, String noun) {
		return count + " " + noun + (count == 1 ? "" : "s");
	}

	private static String centre(String text, int width) {
		return spaces(Math.max(0, (width - text.length()) / 2)) + text;
	}

	private static String pad(String text, int width) {
		return text.length() >= width ? text + " " : text + spaces(width - text.length());
	}

	private static String spaces(int count) {
		return repeat(' ', Math.max(0, count));
	}

	private static String repeat(char c, int count) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			builder.append(c);
		}
		return builder.toString();
	}
}
