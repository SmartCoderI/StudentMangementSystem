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

    /**
     * Sets or changes the log file output destination.
     * If it fails, logs will go to System.err.
     *
     * @param filename log file path (append mode)
     */
    public void setOutputDestination(String filename) {
        try {
            // Only close file-based writers (never System.err)
            if (isFile && writer != null) {
                writer.close();
            }

            FileOutputStream fos = new FileOutputStream(filename, true); // append mode
            writer = new PrintWriter(fos);
            isFile = true;

        } catch (IOException e) {
            System.err.println("Error initializing logger: " + e.getMessage());
            writer = new PrintWriter(System.err);
            isFile = false;
        }
    }

    /**
     * Logs a message with a timestamp.
     *
     * @param message The message to log.
     */
    public void log(String message) {
        long timestamp = System.currentTimeMillis();

        if (writer == null) {
            writer = new PrintWriter(System.err);
            isFile = false;
        }

        try {
            writer.write(timestamp + " " + message + System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
}
