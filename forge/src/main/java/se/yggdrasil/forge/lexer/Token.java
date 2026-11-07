package se.yggdrasil.forge.lexer;

public record Token(TokenType type, String value, int line, int column) {

    public boolean isKeyword() {
        return TokenType.KEYWORD.equals(type);
    }

    public boolean isIdentifier() {
        return TokenType.IDENTIFIER.equals(type);
    }

    public boolean isNumber() {
        return TokenType.NUMBER.equals(type);
    }

    public boolean isString() {
        return TokenType.STRING.equals(type);
    }

    public boolean isOperator() {
        return TokenType.OPERATOR.equals(type);
    }

    public boolean isPunctuation() {
        return TokenType.PUNCTUATION.equals(type);
    }

    public boolean isEOF() {
        return TokenType.EOF.equals(type);
    }

    public boolean isKeywordOrIdentifier() {
        return isKeyword() || isIdentifier();
    }

    public boolean isLiteral() {
        return isNumber() || isString();
    }

    public boolean isKeywordOrIdentifierOrLiteral() {
        return isKeywordOrIdentifier() || isLiteral();
    }

    public boolean isKeywordOrIdentifierOrLiteralOrOperator() {
        return isKeywordOrIdentifierOrLiteral() || isOperator();
    }

    public boolean isKeywordOrIdentifierOrLiteralOrOperatorOrPunctuation() {
        return isKeywordOrIdentifierOrLiteralOrOperator() || isPunctuation();
    }

    public boolean isKeywordOrIdentifierOrLiteralOrOperatorOrPunctuationOrEOF() {
        return isKeywordOrIdentifierOrLiteralOrOperatorOrPunctuation() || isEOF();
    }

}
