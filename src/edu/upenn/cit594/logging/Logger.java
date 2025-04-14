package edu.upenn.cit594.logging;

import java.io.*;

public class Logger {
    private static Logger instance;
    private Writer writer;
    private boolean isFile = false;

    private Logger() {
        this.writer = new OutputStreamWriter(System.err);
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void setOutputDestination(String filename) {
        try {
            if (isFile && writer != null) {
                writer.close();
            }
            FileOutputStream fos = new FileOutputStream(filename, true); // append mode
            writer = new OutputStreamWriter(fos);
            isFile = true;
        } catch (IOException e) {
            System.err.println("Error initializing logger: " + e.getMessage());
            writer = new OutputStreamWriter(System.err);
            isFile = false;
        }
    }

    public void log(String message) {
        try {
            long timestamp = System.currentTimeMillis();
            writer.write(timestamp + " " + message + System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
}
