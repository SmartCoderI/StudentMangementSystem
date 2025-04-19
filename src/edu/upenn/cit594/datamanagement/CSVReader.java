package edu.upenn.cit594.datamanagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    private final CharacterReader reader;
    private int prevChar = -1;

    public CSVReader(CharacterReader reader) {
        this.reader = reader;
    }

    private int getNextChar() throws IOException {
        if (prevChar != -1) {
            int temp = prevChar;
            prevChar = -1;
            return temp;
        }
        return reader.read();
    }

    private void push(int c) {
        prevChar = c;
    }

    public String[] readRow() throws IOException, CSVFormatException {
        List<String> currentRow = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();

        int state = 0; // 0=startNewField, 1=unquotedField, 2=quotedField
        boolean rowCompleted = false;
        boolean charRead = false;

        while (!rowCompleted) {
            int charNum = getNextChar();

            if (charNum == -1) {
                if (!charRead && currentRow.isEmpty() && state == 0) {
                    return null; // EOF
                }
                if (state == 2) {
                    throw new CSVFormatException("EOF reached inside quoted field");
                }
                if (state == 1 || state == 0) {
                    currentRow.add(currentField.toString());
                }
                break;
            }

            charRead = true;
            char ch = (char) charNum;

            if (ch == '\r' || ch == '\n') {
                if (state == 2) {
                    currentField.append(ch);
                    if (ch == '\r') {
                        int next = getNextChar();
                        if (next != -1 && (char) next == '\n') {
                            currentField.append((char) next);
                        } else if (next != -1) {
                            push(next);
                        }
                    }
                    continue;
                } else {
                    if (ch == '\r') {
                        int next = getNextChar();
                        if (next != -1 && (char) next != '\n') {
                            push(next);
                        }
                    }
                    if (state == 1 || state == 0) {
                        currentRow.add(currentField.toString());
                    }
                    rowCompleted = true;
                    continue;
                }
            }

            switch (state) {
                case 0: // startNewField
                    if (ch == '"') {
                        state = 2; // quotedField
                        currentField.setLength(0);
                    } else if (ch == ',') {
                        currentRow.add("");
                    } else {
                        state = 1; // unquotedField
                        currentField.setLength(0);
                        currentField.append(ch);
                    }
                    break;

                case 1: // unquotedField
                    if (ch == ',') {
                        currentRow.add(currentField.toString());
                        currentField.setLength(0);
                        state = 0;
                    } else if (ch == '"') {
                        throw new CSVFormatException("Unexpected quote in unquoted field");
                    } else {
                        currentField.append(ch);
                    }
                    break;

                case 2: // quotedField
                    if (ch == '"') {
                        int next = getNextChar();
                        if (next == -1) {
                            currentRow.add(currentField.toString());
                            return currentRow.toArray(new String[0]);
                        }
                        char nextChar = (char) next;
                        if (nextChar == '"') {
                            currentField.append('"');
                        } else if (nextChar == ',') {
                            currentRow.add(currentField.toString());
                            currentField.setLength(0);
                            state = 0;
                        } else if (nextChar == '\r' || nextChar == '\n') {
                            currentRow.add(currentField.toString());
                            currentField.setLength(0);
                            if (nextChar == '\r') {
                                int lf = getNextChar();
                                if (lf != -1 && (char) lf != '\n') {
                                    push(lf);
                                }
                            }
                            rowCompleted = true;
                        } else {
                            throw new CSVFormatException("Invalid character after closing quote");
                        }
                    } else {
                        currentField.append(ch);
                    }
                    break;
            }
        }

        return currentRow.toArray(new String[0]);
    }
}
