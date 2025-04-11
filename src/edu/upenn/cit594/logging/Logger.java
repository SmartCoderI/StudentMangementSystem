package edu.upenn.cit594.logging;
import java.io.*;

public class Logger {
    private static Logger instance;
    private PrintWriter writer;
    private boolean isSystemErr = true;


    private Logger() {
        writer = new PrintWriter(System.err, true);
    }


    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }


    public void setOutputDestination(String filename) {
        if (!isSystemErr && writer != null) {
            writer.close();
        }
        try {
            writer = new PrintWriter(new FileOutputStream(filename, true), true); // append mode
            isSystemErr = false;
        } catch (IOException e) {
            writer = new PrintWriter(System.err, true);
            isSystemErr = true;
        }
    }


    public void log(String message) {
        writer.println(System.currentTimeMillis() + " " + message);
    }
}
