package edu.upenn.cit594;

import edu.upenn.cit594.datamanagement.PropertyLoader;

import java.io.*;
import java.util.*;

public class Zip19127Debugger {
    private static final String ZIP_TO_TEST = "19127";
    private static final int MIN_ZIP_LENGTH = 5;

    public static void main(String[] args) {
        String fullFile = "properties.csv";
        String smallFile = "only19127.csv";

        List<String> linesFromFull = extractZipLines(fullFile, "lines_from_full.csv.txt");
        List<String> linesFromManual = extractZipLines(smallFile, "lines_from_manual.csv.txt");

        compareLines(linesFromFull, linesFromManual, "diff_only_in_manual.txt");
    }

    /**
     * Extract lines where zip starts with ZIP_TO_TEST and write to outputFile
     */
    private static List<String> extractZipLines(String filename, String outputFile) {
        List<String> matchedLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine(); // Skip header
            if (header == null) return matchedLines;

            String[] columns = header.split(",", -1);
            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < columns.length; i++) {
                colIndex.put(columns[i].replaceAll("[\"\uFEFF]", "").trim().toLowerCase(), i);
            }

            Integer zipIndex = colIndex.get("zip_code");
            if (zipIndex == null) {
                System.err.println("No zip_code column found in " + filename);
                return matchedLines;
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length <= zipIndex) continue;

                String zipRaw = parts[zipIndex].trim();
                if (zipRaw.length() < MIN_ZIP_LENGTH) continue;

                String zip = zipRaw.substring(0, 5);
                if (zip.equals(ZIP_TO_TEST)) {
                    matchedLines.add(line);
                }
            }

            writeLinesToFile(matchedLines, outputFile);
            System.out.println("Wrote " + matchedLines.size() + " lines from " + filename + " to " + outputFile);

        } catch (IOException e) {
            System.err.println("Failed to read " + filename + ": " + e.getMessage());
        }
        return matchedLines;
    }

    /**
     * Compare two lists and write lines that are in listB but not in listA
     */
    private static void compareLines(List<String> fullList, List<String> manualList, String diffFile) {
        Set<String> fullSet = new HashSet<>(fullList);
        List<String> onlyInManual = new ArrayList<>();
        for (String line : manualList) {
            if (!fullSet.contains(line)) {
                onlyInManual.add(line);
            }
        }

        writeLinesToFile(onlyInManual, diffFile);
        System.out.println("Found " + onlyInManual.size() + " lines only in manual file → " + diffFile);
    }

    private static void writeLinesToFile(List<String> lines, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to " + filename + ": " + e.getMessage());
        }
    }
}
