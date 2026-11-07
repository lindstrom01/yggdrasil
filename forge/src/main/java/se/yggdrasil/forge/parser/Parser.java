package se.yggdrasil.forge.parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import se.yggdrasil.forge.ast.Expression;
import se.yggdrasil.forge.ast.Program;
import se.yggdrasil.forge.ast.Statement;
import se.yggdrasil.forge.lexer.Lexer;
import se.yggdrasil.forge.lexer.Token;
import se.yggdrasil.forge.lexer.TokenType;

public class Parser {

    private final Lexer lexer;
    private Token current;

    public Parser(Lexer lexer) {
        this.lexer = Objects.requireNonNull(lexer);
        this.current = lexer.nextToken();
    }

    public Program parse() {
        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            statements.add(statement());
        }
        return new Program(statements);
    }

    private Statement statement() {
        if (matchKeyword("let")) {
            return letStatement();
        }
        if (matchKeyword("print")) {
            return printStatement();
        }
        if (matchKeyword("while")) {
            return whileStatement();
        }
        if (matchKeyword("if")) {
            return ifStatement();
        }
        if (check(TokenType.IDENTIFIER)) {
            return assignmentStatement();
        }
        throw error("Expected 'let', 'print', 'while', or a variable assignment at the start of a statement");
    }

    private Statement letStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Expected a variable name after 'let'");
        consumeOperator("=", "Expected '=' after the variable name");
        Expression initializer = expression();
        consumePunctuation(";", "Expected ';' after the variable declaration");
        return new Statement.LetStatement(name.value(), initializer);
    }

    private Statement printStatement() {
        Expression expression = expression();
        consumePunctuation(";", "Expected ';' after the print expression");
        return new Statement.PrintStatement(expression);
    }

    private Statement whileStatement() {
        Expression condition = expression();
        consumePunctuation("{", "Expected '{' after the while condition");
        List<Statement> body = new ArrayList<>();
        while (!check(TokenType.EOF) && !checkPunctuation("}")) {
            body.add(statement());
        }
        consumePunctuation("}", "Expected '}' after the while block");
        return new Statement.WhileStatement(condition, body);
    }

    private Statement ifStatement() {
        Expression condition = expression();
        consumePunctuation("{", "Expected '{' after the if condition");
        List<Statement> thenBranch = new ArrayList<>();
        while (!check(TokenType.EOF) && !checkPunctuation("}")) {
            thenBranch.add(statement());
        }
        consumePunctuation("}", "Expected '}' after the if block");
        List<Statement> elseBranch = new ArrayList<>();
        if (matchKeyword("else")) {
            consumePunctuation("{", "Expected '{' after 'else'");
            while (!check(TokenType.EOF) && !checkPunctuation("}")) {
                elseBranch.add(statement());
            }
            consumePunctuation("}", "Expected '}' after the else block");
        }
        return new Statement.IfStatement(condition, thenBranch, elseBranch);
    }

    private Statement assignmentStatement() {
        Token name = advance();
        consumeOperator("=", "Expected '=' after the variable name");
        Expression value = expression();
        consumePunctuation(";", "Expected ';' after the assignment");
        return new Statement.Assignment(name.value(), value);
    }

    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression expression = comparison();
        String operator;
        while ((operator = matchOperator("==", "!=")) != null) {
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }
        return expression;
    }

    private Expression comparison() {
        Expression expression = term();
        String operator;
        while ((operator = matchOperator("<", "<=", ">", ">=")) != null) {
            Expression right = term();
            expression = new Expression.Binary(expression, operator, right);
        }
        return expression;
    }

    private Expression term() {
        Expression expression = factor();
        String operator;
        while ((operator = matchOperator("+", "-")) != null) {
            Expression right = factor();
            expression = new Expression.Binary(expression, operator, right);
        }
        return expression;
    }

    private Expression factor() {
        Expression expression = unary();
        String operator;
        while ((operator = matchOperator("*", "/", "%")) != null) {
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }
        return expression;
    }

    private Expression unary() {
        String operator = matchOperator("-", "+");
        if (operator != null) {
            return new Expression.Unary(operator, unary());
        }
        return primary();
    }

    private Expression primary() {
        if (check(TokenType.NUMBER)) {
            return new Expression.Literal(new BigInteger(advance().value()));
        }
        if (check(TokenType.STRING)) {
            return new Expression.Literal(advance().value());
        }
        if (check(TokenType.IDENTIFIER)) {
            return new Expression.Variable(advance().value());
        }
        if (matchPunctuation("(")) {
            Expression expression = expression();
            consumePunctuation(")", "Expected ')' after the expression");
            return new Expression.Grouping(expression);
        }
        throw error("Expected an expression");
    }

    private boolean matchKeyword(String keyword) {
        if (!check(TokenType.KEYWORD) || !current.value().equals(keyword)) {
            return false;
        }
        advance();
        return true;
    }

    private String matchOperator(String... operators) {
        if (!check(TokenType.OPERATOR)) {
            return null;
        }
        for (String operator : operators) {
            if (current.value().equals(operator)) {
                advance();
                return operator;
            }
        }
        return null;
    }

    private boolean checkPunctuation(String punctuation) {
        return check(TokenType.PUNCTUATION) && current.value().equals(punctuation);
    }

    private boolean matchPunctuation(String punctuation) {
        if (!checkPunctuation(punctuation)) {
            return false;
        }
        advance();
        return true;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(message);
    }

    private void consumeOperator(String operator, String message) {
        if (matchOperator(operator) == null) {
            throw error(message);
        }
    }

    private void consumePunctuation(String punctuation, String message) {
        if (!matchPunctuation(punctuation)) {
            throw error(message);
        }
    }

    private boolean check(TokenType type) {
        return current.type() == type;
    }

    private Token advance() {
        Token previous = current;
        if (!check(TokenType.EOF)) {
            current = lexer.nextToken();
        }
        return previous;
    }

    private ParseException error(String message) {
        String found = current.type() == TokenType.EOF
                ? "end of input"
                : "'" + current.value() + "'";
        return new ParseException(message + ", found " + found, current.line(), current.column());
    }
}
