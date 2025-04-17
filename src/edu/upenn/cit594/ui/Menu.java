package edu.upenn.cit594.ui;

import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.processor.AvgLivableArea;
import edu.upenn.cit594.processor.AvgMktValue;
import edu.upenn.cit594.processor.DataProcessor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Menu {
    private final Map<String, String> arguments;
    private final Logger logger = Logger.getInstance();
    private final List<Integer> availableActions = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private final DataProcessor processor;

    public Menu(Map<String, String> arguments) {
        this.arguments = arguments;
        this.processor = new DataProcessor(
                arguments.get("population"),
                arguments.get("properties"),
                arguments.get("covid")
        );
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

    public void start() {
        printMenu();
        while (true) {
            System.out.print("> ");
            System.out.flush();
            String input = scanner.nextLine().trim();
            logger.log(input);

            int selection;
            try {
                selection = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 0 and 7.");
                continue;
            }

            if (!availableActions.contains(selection)) {
                System.out.println("That action is not currently available.");
                continue;
            }

            if (selection == 0) return;
            handleSelection(selection);
        }
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
        System.out.println("7. Show the results of market value per livable square feet.");
    }

    private void showAvailableActions() {
        System.out.println("BEGIN OUTPUT");
        Collections.sort(availableActions);
        for (int action : availableActions) {
            System.out.println(action);
        }
        System.out.println("END OUTPUT");
    }

    private void handleSelection(int selection) {
        switch (selection) {
            case 1 -> showAvailableActions();
            case 2 -> {
                System.out.println("BEGIN OUTPUT");
                System.out.println(processor.getTotalPopulation());
                System.out.println("END OUTPUT");
            }
            case 3 -> handleVaccinationPerCapita();
            case 4 -> handleAverageMarketValue();
            case 5 -> handleAverageLivableArea();
            case 6 -> handleMarketValuePerCapita();
            case 7 -> handleCustomFeature();
            default -> System.out.println("Unknown action.");
        }
    }

    //3
    private void handleVaccinationPerCapita() {
        System.out.println("Enter vaccination type (partial or full):");
        System.out.print("> ");
        System.out.flush();
        String type = scanner.nextLine().trim().toLowerCase();
        logger.log(type);

        if (!type.equals("partial") && !type.equals("full")) {
            System.out.println("Invalid vaccination type.");
            return;
        }

        System.out.println("Enter date (YYYY-MM-DD):");
        System.out.print("> ");
        System.out.flush();
        String dateInput = scanner.nextLine().trim();
        logger.log(dateInput);

        LocalDate date;
        try {
            date = LocalDate.parse(dateInput);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format.");
            return;
        }

        Map<String, Double> results = processor.getVaccinationPerCapita(type, date);
        System.out.println("BEGIN OUTPUT");
        if (results.isEmpty()) {
            System.out.println("0");
        } else {
            for (Map.Entry<String, Double> entry : results.entrySet()) {
                System.out.printf("%s %.4f%n", entry.getKey(), entry.getValue());  // show result with 4 decimal places
            }
        }
        System.out.println("END OUTPUT");
    }

    //4
    private void handleAverageMarketValue() {
        System.out.println("Enter a 5-digit ZIP Code:");
        System.out.print("> ");
        System.out.flush();
        String zip = scanner.nextLine().trim();
        logger.log(zip);

        if (!zip.matches("\\d{5}")) {
            System.out.println("Invalid ZIP code. Must be exactly 5 digits.");
            return;
        }

        int result = processor.getZipStatistic(zip, new AvgMktValue());
        System.out.println("BEGIN OUTPUT");
        System.out.println(result);
        System.out.println("END OUTPUT");
    }

    //5
    private void handleAverageLivableArea() {
        System.out.println("Enter a 5-digit ZIP Code:");
        System.out.print("> ");
        System.out.flush();
        String zip = scanner.nextLine().trim();
        logger.log(zip);

        if (!zip.matches("\\d{5}")) {
            System.out.println("Invalid ZIP code. Must be exactly 5 digits.");
            return;
        }

        int result = processor.getZipStatistic(zip, new AvgLivableArea());
        System.out.println("BEGIN OUTPUT");
        System.out.println(result);
        System.out.println("END OUTPUT");
    }

    //6
    private void handleMarketValuePerCapita() {
        System.out.println("Enter a 5-digit ZIP Code:");
        System.out.print("> ");
        System.out.flush();
        String zip = scanner.nextLine().trim();
        logger.log(zip);

        if (!zip.matches("\\d{5}")) {
            System.out.println("Invalid ZIP code. Must be exactly 5 digits.");
            return;
        }

        int result = processor.getMarketValuePerCapita(zip);
        System.out.println("BEGIN OUTPUT");
        System.out.println(result);
        System.out.println("END OUTPUT");
    }

    //7
    private void handleCustomFeature() {
        System.out.println("Custom feature: Market value per square foot for a specified ZIP code.");
        System.out.println("Enter a 5-digit ZIP Code:");
        System.out.print("> ");
        System.out.flush();
        String zip = scanner.nextLine().trim();
        logger.log(zip);

        if (!zip.matches("\\d{5}")) {
            System.out.println("Invalid ZIP code. Must be exactly 5 digits.");
            return;
        }

        int result = processor.getMarketValuePerSqFt(zip);
        System.out.println("BEGIN OUTPUT");
        System.out.println(result);
        System.out.println("END OUTPUT");
    }


}
