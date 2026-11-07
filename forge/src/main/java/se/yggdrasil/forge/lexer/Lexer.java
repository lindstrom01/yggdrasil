package se.yggdrasil.forge.lexer;

import java.util.Set;

public class Lexer {

    private static final Set<String> KEYWORDS = Set.of("let", "print", "while", "if", "else", "for", "return");

    private final String input;
    private int position;
    private int line = 1;
    private int column = 1;
    private int tokenLine;
    private int tokenColumn;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
    }

    private char advance() {
        char c = input.charAt(position++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private Token token(TokenType type, String value) {
        return new Token(type, value, tokenLine, tokenColumn);
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return input.charAt(position);
    }

    private boolean isAtEnd() {
        return position >= input.length();
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) {
            advance();
        }
    }

    private boolean isLetter(char c) {
        return Character.isLetter(c);
    }

    private boolean isOperator(char c) {
        return "+-*/=%<>!".indexOf(c) != -1;
    }

    private boolean isPunctuation(char c) {
        return ".,;(){}[]".indexOf(c) != -1;
    }

    private boolean isKeyword(String s) {
        return KEYWORDS.contains(s);
    }

    private Token readIdentifier(char firstChar) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstChar);
        while (!isAtEnd() && (isLetter(peek()) || Character.isDigit(peek()) || peek() == '_')) {
            sb.append(advance());
        }
        return token(TokenType.IDENTIFIER, sb.toString());
    }

    private Token readNumber(char firstChar) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstChar);
        while (!isAtEnd() && Character.isDigit(peek())) {
            sb.append(advance());
        }
        return token(TokenType.NUMBER, sb.toString());
    }

    private Token readString() {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && peek() != '"') {
            sb.append(advance());
        }
        if (isAtEnd()) {
            throw new LexException("Unterminated string literal", tokenLine, tokenColumn);
        }
        advance(); // Consume closing quote
        return token(TokenType.STRING, sb.toString());
    }

    private Token readKeywordOrIdentifier(char firstChar) {
        Token token = readIdentifier(firstChar);
        if (isKeyword(token.value())) {
            return token(TokenType.KEYWORD, token.value());
        }
        return token;
    }

    private Token readOperator(char firstChar) {
        boolean canBeFollowedByEquals = "=<>!".indexOf(firstChar) != -1;
        if (canBeFollowedByEquals && peek() == '=') {
            advance();
            return token(TokenType.OPERATOR, firstChar + "=");
        }
        return token(TokenType.OPERATOR, String.valueOf(firstChar));
    }

    private Token readPunctuation(char firstChar) {
        // Handle multi-character punctuation if needed
        // For now, we only handle single-character punctuation
        // Currently, we do not check for multi-character punctuation
        // If multi-character punctuation is added, check for them here before returning
        // the single-character punctuation.
        // Example for handling multi-character punctuation:
        // if (firstChar == '(' && peek() == ')') {
        // advance();
        // return new Token(TokenType.PUNCTUATION, "()");
        // }
        return token(TokenType.PUNCTUATION, String.valueOf(firstChar));
    }

    private Token readToken(char current) {
        if (isLetter(current) || current == '_') {
            return readKeywordOrIdentifier(current);
        }

        if (Character.isDigit(current)) {
            return readNumber(current);
        }

        if (current == '"') {
            return readString();
        }

        if (isOperator(current)) {
            return readOperator(current);
        }

        if (isPunctuation(current)) {
            return readPunctuation(current);
        }

        throw new LexException("Ogiltigt tecken: " + current, tokenLine, tokenColumn);
    }

    public Token nextToken() {
        skipWhitespace();
        tokenLine = line;
        tokenColumn = column;
        if (isAtEnd()) {
            return token(TokenType.EOF, "");
        }
        char current = advance();
        return readToken(current);
    }

}