package se.yggdrasil.forge.lexer;

public class LexException extends IllegalArgumentException {

    private final int line;
    private final int column;

    public LexException(String message, int line, int column) {
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
