# restaurant-rush
Restaurant Rush is a playable restaurant management game in Java, played across 3 stages. Done as part of SC2002: Object Oriented Programming and Designing Assignment. 

The player's role is the restaurant manager. 

## How to play 
1. There are test scripts under the folder: `tests`. 
2. Compile the StageNMain.java (N = stage number) file. 
3. If using Eclipse:  
   * For Stage 1 to 3 Victory, Defeat and Invalid Input test scripts
      - Run Configurations -> Main -> Main class: StageNMain
      - Run Configurations -> Arguments -> Program arguments: tests/test_script_name.txt
      - Apply -> Run  
   * For Stage 4 Victory, Defeat and Invalid Input test scripts
      - Run Configurations -> Main -> Main class: Stage4Main.java
      - Run Configurations -> Arguments -> Program arguments: 51
      - Run Configurations -> Common -> Input File: Select Workspace -> retaurant-rush -> tests -> test_script_name.txt
      - Apply -> Run
4. If using Visual Studio Code: java StageNMain.java < tests/test_script_name.txt 

Further commands are included in the respective StageNMain.java files for reference. 

## Stage 1 
### Specifications 
1. There are 18 turns. 
2. 3 staff: waiter, chef, cashier, each doing 1 unit of task per turn. 
3. 2 total tables to seat customers.
4. 1 customer comes in at the start of turns 1, 4, 7, 10, 13, 16. 
5. Goal to succeed: at least 4 paid customers, $45 in revenue, average satisfaction >= 60.

### Classes implemented 
* `Customer`: handles satisfaction decrement, serving, seated and paid status. 
* `Order`: composite class of customer; handles the price, served and payment status. 
* `MenuItem`: handles the price, name and no. of prep units required. 
* `Console UI`: handles how the gaming interface on the terminal looks like. 
* `Staff`: parent class handling name and turn activity. 
* `Restaurant`: handles how the restaurant functions (getting queue, menu, staff operations, etc). 
* `Game Manager`: composite class that handles how the player plays the game (no. of turns, handle staff, play turn, etc). 



## Stage 2
### Specifications 
1. Implement new customer behaviours (VIP and Critic Customers). 
2. The winning condition and general rule play remains the same. 

### Classes implemented 
`VIPCustomer` and `CriticCustomer` are extended from the parent `Customer` class. 
![UML diagram of customer classes](image.png)



## Stage 3 
### Specifications 
1. Host: either host or waiter can seat the hosts, but host cannot do any other tasks than right. Can only seat 1 customer per turn
2. Combo Meal: made of 2 existing menu items.
3. Happy hour: additional 20% discount and an increment of satisfaction points (rather than decrement).

### Classes implemented 
* `ComboMeal`: subclasses `MenuItem` overrides `getPrice()` and `getPrepUnits()` using Composite.
* `HappyHour`: provides an additional 20% discount and +15 satisfaction recovery. 
* `Host`: extends the `Staff` class.
* `Menu`: Holds available base items and combos in an array. 
![alt text](image-2.png)


## Stage 4 (additional features implemented)
### Specifications
1. Host: Gives a seated customer an immediate +5 satisfaction boost and suggest who to seat next based on the lowest satisfaction rate.
2. Combo Meal: Avoid making a combo that consist of two of the same items, and format item breakdown with total dollar savings. 
3. Group seating (`Stage4Main` / `Stage4GameManager`): random number of tables (2-4) with random capacities (2-6). Arrivals (same turns as Stages 1-3) are either an individual customer (`C`) or a party (`G`, a `CustomerGroup`).
   * Individuals follow Stage 1-3 rules (own order, own bill) and may share a `GroupTable` with strangers until it is full.
   * Parties need an empty table big enough for all members, never share, and place one `GroupOrder`.
   * Run `java Stage4Main [seed]` for mixed arrivals, or `java Stage4Main [seed] --groups-only` for parties only.


## UML Diagram
This UML diagram corresponds to Stage 3. 

## Sequence Diagram
* This diagram simulates Stage 2 being played, specifically the `stage2_victory.txt`. 
* A helper function, `stage2SequenceDiagramScenario()` was used generate a sequence diagram from Visual Paradigm. 

![Sequence diagram simulating Stage 2]()

## Design Discussion 

### 1. Key Abstractions
- **`Host`**: Manages allocating seats and ensure the availability of table
- **`ComboMeal`**: Extends `MenuItem` give customer a choice to buy combination of items at a discounted price and calculate the total price of the combo 
- **`Customer` (`VIPCustomer`, `CriticCustomer`)**: contains information about the clients and tracks their satisfaction levels. Different customers can implement their own discount systems due to polymorphism. 
- **`Order`**: Tracks the whole order process (`PLACED` $\rightarrow$ `READY` $\rightarrow$ `SERVED` $\rightarrow$ `PAID`).
- **`GameManager`**: supervises the game process by controlling the 18 turns and coordinating the various employee activities. 
- **`HappyHour`**: check whether `HappyHour` is up and applies all the possible discounts for eligible orders. 

### 2. SOLID Principles Highlights
- **SRP & DIP**: Domain classes (`Host`, `ComboMeal`) are strictly decoupled from presentation logic, delegating all UI rendering to `ConsoleUI` instead of hardcoding `System.out.println`.
- **OCP & LSP**: New customer tiers and menu items integrate smoothly into loops via polymorphism without modifying core manager classes.

### 3. Trade-offs & Alternative Designs Considered
The decision that was taken was to separate the game’s logic from its interface. While initially this was not straightforward, as it meant seeing more thought had to go into how the information was passed between the classes instead of just printing things. 

However, this has resulted in the code being much better organised. Code structures and classes could be tested and modified more easily without a need to affect other game components.

### 4. Player Choices & Game Outcomes
- Strategic deployment of the `Host`, purchasing `ComboMeal` items during `HappyHour`, and managing customer tiers directly affect table turnaround times, average satisfaction ($\ge 60$), revenue ($\ge 45.00$), and final victory evaluation (`GameStatistics.isVictory()`).

