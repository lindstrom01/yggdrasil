package se.yggdrasil.forge.parser;

public class ParseException extends IllegalArgumentException {

    private final int line;
    private final int column;

    public ParseException(String message, int line, int column) {
        super("[line " + line + ", column " + column + "] " + message);
        this.line = line;
        this.column = column;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }
}
