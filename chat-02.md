# AI Chat History - Stage 4 (ChatGPT)

**AI Tool:** ChatGPT  
**Purpose:** Review of Stage 3 classes, OOP design principles and alignment with the SC2002 Restaurant Rush project brief.

---

## 1. User - Project Brief and Review Request

I provided the SC2002 Restaurant Rush Group Assignment project brief and requested a review of my `Host.java` and `ComboMeal.java` classes.

I wanted ChatGPT to check whether my implementation was correct, aligned with the Stage 3 requirements, and demonstrated appropriate object-oriented design.

## 2. Assistant - Review Approach

ChatGPT confirmed that it would review the classes based on:

- Correctness and functionality.
- Alignment with the project brief.
- OOP principles, including encapsulation, inheritance, polymorphism and composition.
- Design quality and responsibility allocation.
- Integration with the existing Restaurant Rush classes.
- Potential issues that could affect assessment.

It also clarified that actual requirement violations would be distinguished from optional improvements, to avoid unnecessary code changes.

---

## 3. User - Submitted Java Classes

### ComboMeal.java

```java
public class ComboMeal extends MenuItem {
    private final MenuItem item1;
    private final MenuItem item2;

    public ComboMeal(MenuItem item1, MenuItem item2) {
        super(item1.getName() + " + " + item2.getName() + " Combo",
              calculateComboPrice(item1, item2),
              item1.getPrepUnits() + item2.getPrepUnits());

        if (item1.getName().equalsIgnoreCase(item2.getName())) {
            throw new IllegalArgumentException(
                "A ComboMeal cannot contain two of the same item!"
            );
        }
        this.item1 = item1;
        this.item2 = item2;
    }

    private static int calculateComboPrice(MenuItem item1, MenuItem item2) {
        int combinedCents = item1.getPrice() + item2.getPrice();
        return (int) Math.round(combinedCents * 0.90);
    }

    @Override
    public int getPrice() {
        return calculateComboPrice(item1, item2);
    }

    @Override
    public int getPrepUnits() {
        return item1.getPrepUnits() + item2.getPrepUnits();
    }

    public String getDetailedBreakdown() {
        int savings = (item1.getPrice() + item2.getPrice()) - getPrice();
        return String.format(
            "%s (Includes %s & %s) - Saves $%.2f!",
            getName(),
            item1.getName(),
            item2.getName(),
            savings / 100.0
        );
    }

    public MenuItem getItem1() {
        return item1;
    }

    public MenuItem getItem2() {
        return item2;
    }
}
```

### Host.java

```java
import java.util.List;

public class Host extends Staff {
    public static final int moraleBoost = 5;

    public Host(int ID) {
        super(ID);
    }

    // Standard Stage 3 seating: seats at most one queued customer per turn, in addition to Waiter
    public boolean seatCustomer(Customer customer, Table table) {
        if (hasActedThisTurn || customer == null || table == null || table.isOccupied()) {
            return false;
        }
        table.assignCustomer(customer);
        this.hasActedThisTurn = true;
        return true;
    }

    // Stage 4 extra feature: seats customer/party and grants +5 satisfaction
    public boolean seatCustomerWithMoraleBoost(Customer customer, Table table) {
        if (!seatCustomer(customer, table)) {
            return false;
        }
        customer.setSatisfaction(customer.getSatisfaction() + moraleBoost);
        return true;
    }

    // Stage 4 extra feature: suggests the queued customer with the lowest satisfaction
    public Customer suggestNextCustomer(List<Customer> queue) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        Customer mostUrgent = null;
        for (Customer current : queue) {
            if (current != null && (mostUrgent == null
                    || current.getSatisfaction() < mostUrgent.getSatisfaction())) {
                mostUrgent = current;
            }
        }
        return mostUrgent;
    }
}
```

### Menu.java

```java
public class Menu {
    private MenuItem[] items;
    private int itemCount;

    public Menu() {
        this(true);
    }

    public Menu(boolean includeCombos) {
        items = new MenuItem[10];
        itemCount = 0;

        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);

        addItem(salad);
        addItem(burger);
        addItem(pasta);

        // Stage 3: ComboMeals (90% combined price, summed prep units)
        if (includeCombos) {
            addItem(new ComboMeal(salad, burger)); // $21.60, 2 units
            addItem(new ComboMeal(salad, pasta));  // $24.30, 3 units
            addItem(new ComboMeal(burger, pasta)); // $29.70, 3 units
        }
    }

    public void addItem(MenuItem item) {
        if (itemCount < size()) {
            items[itemCount] = item;
            itemCount++;
        }
    }

    public MenuItem getItem(int index) {
        if (index >= 0 && index < itemCount) {
            return items[index];
        }
        return null;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int size() {
        return items.length;
    }
}
```

### MenuItem.java

```java
public class MenuItem {
    private final String name;
    private final int basePriceCents;
    private final int prepUnits;

    public MenuItem(String name, int basePriceCents, int prepUnits) {
        this.name = name;
        this.basePriceCents = basePriceCents;
        this.prepUnits = prepUnits;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return basePriceCents;
    }

    public int getPrepUnits() {
        return prepUnits;
    }
}
```

---

## 4. Assistant - Class-by-Class Review

ChatGPT reviewed all four classes and concluded that the implementation was largely aligned with the Stage 3 requirements.

### 4.1 ComboMeal.java

**Assessment: Mostly correct.**

The implementation satisfies the main ComboMeal requirements:

| Project requirement | Implementation | Assessment |
|---|---|---|
| Combo consists of two existing menu items | `item1` and `item2` | OK |
| Preparation units are summed | `getPrepUnits()` | OK |
| Price is 90% of combined price | `calculateComboPrice()` | OK |
| Price rounded to nearest cent | `Math.round()` | OK |
| Components encapsulated | `private final` fields | OK |

ChatGPT identified the following OOP concepts:

**Composition**

A ComboMeal contains two MenuItem objects rather than duplicating their names, prices and preparation units.

**Inheritance and polymorphism**

`ComboMeal extends MenuItem`, allowing combo meals to be treated as ordinary menu items through a common parent reference.

The overridden `getPrice()` and `getPrepUnits()` methods allow ComboMeal to provide its own behaviour.

**Money calculations**

The implementation uses integer cents, following the project brief.

| Combo | Combined price | Combo price | Preparation units |
|---|---:|---:|---:|
| Salad + Burger | $24.00 | $21.60 | 2 |
| Salad + Pasta | $27.00 | $24.30 | 3 |
| Burger + Pasta | $33.00 | $29.70 | 3 |

**Suggested improvements:**

1. Validate null components before accessing their methods.
2. Avoid recalculating the combo price unnecessarily, although the existing implementation is functionally consistent because the component fields are immutable.

These were identified as improvements rather than major functional problems.

### 4.2 Host.java

**Assessment: Stage 3 core requirement satisfied, with minor improvements.**

The project requires the Host to seat at most one queued customer per turn, in addition to the Waiter's task.

| Requirement | Implementation | Assessment |
|---|---|---|
| Host extends Staff | `extends Staff` | OK |
| Seats at most one customer per turn | `hasActedThisTurn` | OK |
| Receives a customer and table | `seatCustomer()` | Conditional |
| Requires a free table | `table.isOccupied()` | OK |
| Operates independently of Waiter | Separate Staff object | OK |

**What was implemented correctly:**

The method checks whether the Host has already acted, whether the arguments are null, and whether the table is occupied.

The Host's action flag is only set after successful seating. This means an unsuccessful action does not consume the Host's turn.

The Host also has a separate responsibility from the GameManager, which is appropriate for object-oriented design.

**Additional Stage 4 features:**

- `seatCustomerWithMoraleBoost()` provides a satisfaction boost after successful seating.
- `suggestNextCustomer()` identifies the queued customer with the lowest satisfaction.

These are additional features rather than mandatory Stage 3 requirements.

**Issues to check:**

1. Queue removal must happen after successful seating. This can remain the GameManager's responsibility.
2. Satisfaction should not exceed 100 when the morale boost is applied. ChatGPT suggested using `Math.min(100, ...)`.
3. The GameManager must reset the Host's `hasActedThisTurn` flag at the start of every turn.

### 4.3 Menu.java

**Assessment: Correct structure.**

ChatGPT identified the following strengths:

- Uses an array to store MenuItem objects.
- Supports both original menu items and ComboMeals.
- Registers all three original menu items.
- Registers all three possible combinations.
- Uses polymorphism by storing ComboMeal objects in a `MenuItem[]` array.

One useful design decision was the overloaded constructor:

```java
public Menu() {
    this(true);
}
```

This allows the menu to include combos by default, while:

```java
new Menu(false);
```

can create a menu without combos.

This supports different stage configurations, provided the respective stages initialise the menu correctly.

**Optional improvements:**

- Declare the array `final` because it is not reassigned.
- Consider rejecting null items in `addItem()`.
- Distinguish between array capacity (`size()`) and the number of registered items (`getItemCount()`).

### 4.4 MenuItem.java

**Assessment: Correct.**

ChatGPT identified that the class provides a suitable base class for the menu system.

Its fields are private and final, supporting encapsulation and preventing accidental modification after construction.

The getter methods provide controlled access to the stored attributes.

This also makes ComboMeal calculations more reliable because the underlying menu item prices and preparation units cannot change after construction.

An optional improvement would be constructor validation for invalid names, negative prices or negative preparation units.

---

## 5. Assistant - Integration Checks

ChatGPT clarified that reviewing individual classes alone was insufficient to fully certify the Stage 3 implementation.

It identified four integration areas requiring further verification:

| Integration area | What needs to be checked |
|---|---|
| GameManager | Host can seat one customer independently of the Waiter. |
| Staff | `hasActedThisTurn` resets every turn. |
| Customer | Combo selection follows a documented deterministic rule. |
| Order / pricing | Combo prices, VIP discounts and Happy Hour discounts are applied correctly and locked when the order is taken. |

It also clarified that Happy Hour pricing does not need to be implemented inside ComboMeal itself. That responsibility can remain in the order creation or pricing logic.

---

## 6. Final Assessment

ChatGPT's overall assessment was:

| Class | Assessment |
|---|---|
| ComboMeal | OK |
| Host | OK with minor improvements |
| Menu | OK |
| MenuItem | OK |

The implementation demonstrates several relevant OOP concepts:

- **Inheritance:** ComboMeal extends MenuItem, and Host extends Staff.
- **Encapsulation:** Private fields and controlled access through getter methods.
- **Polymorphism:** ComboMeal overrides MenuItem methods.
- **Composition:** ComboMeal contains two MenuItem objects.

ChatGPT recommended retaining the existing class structures rather than rewriting them.

The next suggested step was to review `GameManager.java` and `Staff.java` to verify that the Host integrates correctly with the turn sequence and satisfies the Stage 3 requirements.
