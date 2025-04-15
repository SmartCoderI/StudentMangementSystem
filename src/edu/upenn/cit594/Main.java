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

        Map<String, String> arguments = new HashMap<>();
        Set<String> providedNames = new HashSet<>();

        //iterate over arguments
        for (String arg : args) {
            //argument needs to be in the format of --name=value
            Matcher matcher = ARG_PATTERN.matcher(arg);
            //throw error if not
            if (!matcher.matches()) {
                System.err.println("Invalid argument format: " + arg);
                return;
            }
            //extract name and value
            String name = matcher.group("name").toLowerCase();
            String value = matcher.group("value");

            //if name isn't covid, properties, population,log, throw error
            if (!VALID_ARGS.contains(name)) {
                System.err.println("Invalid argument name: " + name);
                return;
            }
            //if duplicated, throw error
            if (providedNames.contains(name)) {
                System.err.println("Duplicate argument: " + name);
                return;
            }
            //valid name, store name and values
            arguments.put(name, value);
            providedNames.add(name);
        }

        //get singleton logger
        Logger logger = Logger.getInstance();
        if (arguments.containsKey("log")) {
            logger.setOutputDestination(arguments.get("log"));
        }
        logger.log(String.join(" ", args));
        //check input files
        for (String key : List.of("covid", "properties", "population")) {
            if (arguments.containsKey(key)) {
                File file = new File(arguments.get(key));
                if (!file.exists() || !file.canRead()) {
                    System.err.println("Cannot read file: " + arguments.get(key));
                    return;
                }
                logger.log(arguments.get(key));
                if (key.equals("covid")) {
                    String name = file.getName().toLowerCase();
                    if (!name.endsWith(".csv") && !name.endsWith(".json")) {
                        System.err.println("COVID file must be .csv or .json");
                        return;
                    }
                }
            }
        }
        //finally prompt menu
        new Menu(arguments).start();
    }
}
