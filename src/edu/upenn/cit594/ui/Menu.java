package edu.upenn.cit594.ui;
import edu.upenn.cit594.logging.Logger;
import java.util.*;

public class Menu {
    private final Map<String, String> arguments;
    private final Logger logger = Logger.getInstance();
    private final List<Integer> availableActions = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);

    public Menu(Map<String, String> arguments) {
        this.arguments = arguments;
        initializeAvailableActions();
    }

    private void initializeAvailableActions() {
        availableActions.add(0); // Exit
        availableActions.add(1); // Show available actions
        if (arguments.containsKey("population")) availableActions.add(2);
        if (arguments.containsKey("covid") && arguments.containsKey("population")) availableActions.add(3);
        if (arguments.containsKey("properties")) availableActions.add(4);
        if (arguments.containsKey("properties")) availableActions.add(5);
        if (arguments.containsKey("properties") && arguments.containsKey("population")) availableActions.add(6);
        if (arguments.keySet().containsAll(Set.of("properties", "population", "covid"))) availableActions.add(7);
    }


    private void printMenu() {
        System.out.println("Menu:");
        System.out.println("0. Exit the program.");
        System.out.println("1. Show the available actions.");
        System.out.println("2. Show the total population for all ZIP Codes.");
        System.out.println("3. Show the total vaccinations per capita for each ZIP Code for the specified date.");
        System.out.println("4. Show the average market value for properties in a specified ZIP Code.");
        System.out.println("5. Show the average total livable area for properties in a specified ZIP Code.");
        System.out.println("6. Show the total market value of properties, per capita, for a specified ZIP Code.");
        System.out.println("7. Show the results of your custom feature.");
    }


}
