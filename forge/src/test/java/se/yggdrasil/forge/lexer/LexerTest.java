package se.yggdrasil.forge.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LexerTest {

    private static void lexAll(String source) {
        Lexer lexer = new Lexer(source);
        while (lexer.nextToken().type() != TokenType.EOF) {
            // consume until EOF or a lexical error
        }
    }

    private static void assertToken(TokenType type, String value, Token actual) {
        assertEquals(type, actual.type());
        assertEquals(value, actual.value());
    }

    private static void assertPosition(int line, int column, Token actual) {
        assertEquals(line, actual.line(), "line of " + actual);
        assertEquals(column, actual.column(), "column of " + actual);
    }

    @Test
    void recognizesKeywordsAndWholeIdentifiers() {
        Lexer lexer = new Lexer("let letter print printer");

        assertToken(TokenType.KEYWORD, "let", lexer.nextToken());
        assertToken(TokenType.IDENTIFIER, "letter", lexer.nextToken());
        assertToken(TokenType.KEYWORD, "print", lexer.nextToken());
        assertToken(TokenType.IDENTIFIER, "printer", lexer.nextToken());
    }

    @Test
    void recognizesDifferentTokenTypes() {
        Lexer lexer = new Lexer("let age = 42\nprint age\nprint \"hello\"");

        assertToken(TokenType.KEYWORD, "let", lexer.nextToken());
        assertToken(TokenType.IDENTIFIER, "age", lexer.nextToken());
        assertToken(TokenType.OPERATOR, "=", lexer.nextToken());
        assertToken(TokenType.NUMBER, "42", lexer.nextToken());
        assertToken(TokenType.KEYWORD, "print", lexer.nextToken());
        assertToken(TokenType.IDENTIFIER, "age", lexer.nextToken());
        assertToken(TokenType.KEYWORD, "print", lexer.nextToken());
        assertToken(TokenType.STRING, "hello", lexer.nextToken());
    }

    @Test
    void readsIdentifiersStartingWithUnderscore() {
        Lexer lexer = new Lexer("_counter item_2");

        assertToken(TokenType.IDENTIFIER, "_counter", lexer.nextToken());
        assertToken(TokenType.IDENTIFIER, "item_2", lexer.nextToken());
    }

    @Test
    void readsNumbersStringsOperatorsAndPunctuation() {
        Lexer lexer = new Lexer("123 \"hello\" + (x);");

        assertToken(TokenType.NUMBER, "123", lexer.nextToken());
        assertToken(TokenType.STRING, "hello", lexer.nextToken());
        assertToken(TokenType.OPERATOR, "+", lexer.nextToken());
        assertToken(TokenType.PUNCTUATION, "(", lexer.nextToken());
        assertToken(TokenType.IDENTIFIER, "x", lexer.nextToken());
        assertToken(TokenType.PUNCTUATION, ")", lexer.nextToken());
        assertToken(TokenType.PUNCTUATION, ";", lexer.nextToken());
    }

    @Test
    void readsComparisonAndMultiCharacterOperators() {
        Lexer lexer = new Lexer("< <= > >= == != =");

        assertToken(TokenType.OPERATOR, "<", lexer.nextToken());
        assertToken(TokenType.OPERATOR, "<=", lexer.nextToken());
        assertToken(TokenType.OPERATOR, ">", lexer.nextToken());
        assertToken(TokenType.OPERATOR, ">=", lexer.nextToken());
        assertToken(TokenType.OPERATOR, "==", lexer.nextToken());
        assertToken(TokenType.OPERATOR, "!=", lexer.nextToken());
        assertToken(TokenType.OPERATOR, "=", lexer.nextToken());
    }

    @Test
    void skipsWhitespaceAndReturnsEofRepeatedly() {
        Lexer lexer = new Lexer(" \t\n");

        assertToken(TokenType.EOF, "", lexer.nextToken());
        assertToken(TokenType.EOF, "", lexer.nextToken());
    }

    @Test
    void tracksLineAndColumnOfEachToken() {
        Lexer lexer = new Lexer("let x = 1;\n  print \"a\nb\";\nx");

        assertPosition(1, 1, lexer.nextToken());  // let
        assertPosition(1, 5, lexer.nextToken());  // x
        assertPosition(1, 7, lexer.nextToken());  // =
        assertPosition(1, 9, lexer.nextToken());  // 1
        assertPosition(1, 10, lexer.nextToken()); // ;
        assertPosition(2, 3, lexer.nextToken());  // print
        assertPosition(2, 9, lexer.nextToken());  // multi-line string starts here
        assertPosition(3, 3, lexer.nextToken());  // ;
        assertPosition(4, 1, lexer.nextToken());  // x
        assertPosition(4, 2, lexer.nextToken());  // EOF
    }

    @Test
    void rejectsUnknownCharactersWithPosition() {
        LexException exception = assertThrows(
                LexException.class,
                () -> lexAll("let x = 1;\n  @"));

        assertEquals("[line 2, column 3] Ogiltigt tecken: @", exception.getMessage());
        assertEquals(2, exception.line());
        assertEquals(3, exception.column());
    }

    @Test
    void rejectsUnterminatedStringsAtTheirStart() {
        LexException exception = assertThrows(
                LexException.class,
                () -> lexAll("let s = \"utan slut"));

        assertEquals("[line 1, column 9] Unterminated string literal", exception.getMessage());
    }
}
