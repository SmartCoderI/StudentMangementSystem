package edu.upenn.cit594;

import edu.upenn.cit594.ui.Menu;
import edu.upenn.cit594.logging.Logger;

import java.io.File;
import java.util.*;
import java.util.regex.*;

public class Main {
    //extract arguments like --poluation=pop.csv
    //--covid=covid.csv --population=pop.csv --log=log.txt
    private static final Pattern ARG_PATTERN = Pattern.compile("^--(?<name>.+?)=(?<value>.+)$");
    //valid argument names
    private static final Set<String> VALID_ARGS = Set.of("covid", "properties", "population", "log");

    public static void main(String[] args) {
        Map<String, String> arguments = parseArguments(args);
        if (arguments == null) return;

        if (!validateFiles(arguments)) return;

        Logger logger = Logger.getInstance();
        if (arguments.containsKey("log")) {
            logger.setOutputDestination(arguments.get("log"));
        }
        logger.log(String.join(" ", args));

        new Menu(arguments).start();
    }

    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        Set<String> providedNames = new HashSet<>();

        for (String arg : args) {
            Matcher matcher = ARG_PATTERN.matcher(arg);
            if (!matcher.matches()) {
                System.err.println("Invalid argument format: " + arg);
                return null;
            }

            String name = matcher.group("name").toLowerCase();
            String value = matcher.group("value");

            if (!VALID_ARGS.contains(name)) {
                System.err.println("Invalid argument name: " + name);
                return null;
            }

            if (!providedNames.add(name)) {
                System.err.println("Duplicate argument: " + name);
                return null;
            }

            arguments.put(name, value);
        }

        return arguments;
    }

    private static boolean validateFiles(Map<String, String> arguments) {
        Logger logger = Logger.getInstance();

        for (String key : List.of("covid", "properties", "population")) {
            if (arguments.containsKey(key)) {
                File file = new File(arguments.get(key));
                if (!file.exists() || !file.canRead()) {
                    System.err.println("Cannot read file: " + arguments.get(key));
                    return false;
                }
                logger.log(arguments.get(key));

                if (key.equals("covid")) {
                    String name = file.getName().toLowerCase();
                    if (!name.endsWith(".csv") && !name.endsWith(".json")) {
                        System.err.println("COVID file must be .csv or .json");
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
