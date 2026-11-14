// ADDITIONAL FEATURE: group order - one order placed by a CustomerGroup 

/*
- Extends the Order class. 
- The whole order of the table is 1 ticket = sum of every members' dish (combined MenuItem)
- Chef, waiter and cashier works the same 
- Each member is still billed with their own discount and they know what they ordered 
(order taken together but treated seperately)
*/

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupOrder extends Order {
    private final CustomerGroup group;
    private final List<Order> lineOrders;

    public GroupOrder(CustomerGroup group, List<MenuItem> memberItems, HappyHour happyHour) {
        super(group, combine(group, memberItems), happyHour);
        this.group = group;
        this.lineOrders = new ArrayList<>();
        List<Customer> members = group.getMembers();
        for (int i = 0; i < members.size(); i++) {
            // order = member id, items, happyHour discount
            this.lineOrders.add(new Order(members.get(i), memberItems.get(i), happyHour));
        }
    }

    // builds the single kitchen ticket
    private static MenuItem combine(CustomerGroup group, List<MenuItem> memberItems) {
        if (group == null || memberItems == null || memberItems.size() != group.getPartySize()) {
            throw new IllegalArgumentException("A group order needs exactly one dish per member.");
        }
        StringBuilder name = new StringBuilder();
        int price = 0;
        int prepUnits = 0;
        for (MenuItem item : memberItems) {
            if (item == null) {
                throw new IllegalArgumentException("Every member must choose a dish.");
            }
            if (name.length() > 0) {
                name.append(", ");
            }
            name.append(item.getName());
            price += item.getPrice();
            prepUnits += item.getPrepUnits();
        }
        return new MenuItem(name.toString(), price, prepUnits);
    }

    // the group bill: sum of every member's individually discounted price
    @Override
    public int getLockedPriceCents() {
        int total = 0;
        for (Order line : this.lineOrders) {
            total += line.getLockedPriceCents();
        }
        return total;
    }

    // status changes moves to every member's line order
    @Override
    public void markServed() {
        super.markServed();
        for (Order line : this.lineOrders) {
            line.markServed();
        }
    }

    @Override
    public void markPaid() {
        super.markPaid();
        for (Order line : this.lineOrders) {
            line.markPaid();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        for (Order line : this.lineOrders) {
            line.cancel();
        }
    }

    public CustomerGroup getGroup() {
        return this.group;
    }

    public List<Order> getLineOrders() {
        return Collections.unmodifiableList(this.lineOrders);
    }
}
