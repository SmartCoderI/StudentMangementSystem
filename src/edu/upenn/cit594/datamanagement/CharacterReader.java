package edu.upenn.cit594.datamanagement;

import java.io.*;

public class CharacterReader implements Closeable {
    private final Reader reader;

    public CharacterReader(String filename) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(filename));
    }

    public int read() throws IOException {
        return reader.read();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}


