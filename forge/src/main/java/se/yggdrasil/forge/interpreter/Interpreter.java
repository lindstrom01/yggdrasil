package se.yggdrasil.forge.interpreter;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Objects;
import se.yggdrasil.forge.ast.Expression;
import se.yggdrasil.forge.ast.Program;
import se.yggdrasil.forge.ast.Statement;
import se.yggdrasil.forge.lexer.Lexer;
import se.yggdrasil.forge.parser.Parser;

public class Interpreter {

    private final Environment environment = new Environment();
    private final PrintStream output;

    public Interpreter() {
        this(System.out);
    }

    public Interpreter(PrintStream output) {
        this.output = Objects.requireNonNull(output);
    }

    public void interpret(String code) {
        Program program = new Parser(new Lexer(Objects.requireNonNull(code))).parse();
        interpret(program);
    }

    public void interpret(Program program) {
        Objects.requireNonNull(program);
        for (Statement statement : program.statements()) {
            execute(statement);
        }
    }

    private void execute(Statement statement) {
        if (statement instanceof Statement.LetStatement declaration) {
            Object value = evaluate(declaration.initializer());
            environment.define(declaration.name(), value);
        } else if (statement instanceof Statement.Assignment assignment) {
            Object value = evaluate(assignment.value());
            environment.assign(assignment.name(), value);
        } else if (statement instanceof Statement.PrintStatement print) {
            output.println(evaluate(print.expression()));
        } else if (statement instanceof Statement.WhileStatement loop) {
            while (isTrue(evaluate(loop.condition()))) {
                for (Statement inner : loop.body()) {
                    execute(inner);
                }
            }
        } else if (statement instanceof Statement.IfStatement conditional) {
            if (isTrue(evaluate(conditional.condition()))) {
                for (Statement inner : conditional.thenBranch()) {
                    execute(inner);
                }
            } else {
                for (Statement inner : conditional.elseBranch()) {
                    execute(inner);
                }
            }
        } else {
            throw new IllegalStateException(
                    "Unsupported statement type: " + statement.getClass().getName());
        }
    }

    private boolean isTrue(Object condition) {
        if (condition instanceof Boolean value) {
            return value;
        }
        throw new IllegalArgumentException("Condition must be a comparison, but was: " + condition);
    }

    private Object evaluate(Expression expression) {
        if (expression instanceof Expression.Literal literal) {
            return literal.value();
        }
        if (expression instanceof Expression.Variable variable) {
            return environment.get(variable.name());
        }
        if (expression instanceof Expression.Grouping grouping) {
            return evaluate(grouping.expression());
        }
        if (expression instanceof Expression.Unary unary) {
            return evaluateUnary(unary);
        }
        if (expression instanceof Expression.Binary binary) {
            return evaluateBinary(binary);
        }
        throw new IllegalStateException(
                "Unsupported expression type: " + expression.getClass().getName());
    }

    private Object evaluateUnary(Expression.Unary unary) {
        Object value = evaluate(unary.operand());
        if (!(value instanceof BigInteger integer)) {
            throw new IllegalArgumentException(
                    "Unary operator '" + unary.operator() + "' requires an integer operand");
        }
        return switch (unary.operator()) {
            case "+" -> integer;
            case "-" -> integer.negate();
            default -> throw new IllegalArgumentException(
                    "Unsupported unary operator: " + unary.operator());
        };
    }

    private Object evaluateBinary(Expression.Binary binary) {
        Object left = evaluate(binary.left());
        Object right = evaluate(binary.right());
        if (binary.operator().equals("==")) {
            return left.equals(right);
        }
        if (binary.operator().equals("!=")) {
            return !left.equals(right);
        }
        if (!(left instanceof BigInteger leftInteger)
                || !(right instanceof BigInteger rightInteger)) {
            throw new IllegalArgumentException(
                    "Binary operator '" + binary.operator() + "' requires integer operands");
        }
        boolean divides = binary.operator().equals("/") || binary.operator().equals("%");
        if (divides && rightInteger.signum() == 0) {
            throw new IllegalArgumentException("Division by zero");
        }
        return switch (binary.operator()) {
            case "+" -> leftInteger.add(rightInteger);
            case "-" -> leftInteger.subtract(rightInteger);
            case "*" -> leftInteger.multiply(rightInteger);
            case "/" -> leftInteger.divide(rightInteger);
            case "%" -> leftInteger.remainder(rightInteger);
            case "<" -> leftInteger.compareTo(rightInteger) < 0;
            case "<=" -> leftInteger.compareTo(rightInteger) <= 0;
            case ">" -> leftInteger.compareTo(rightInteger) > 0;
            case ">=" -> leftInteger.compareTo(rightInteger) >= 0;
            default -> throw new IllegalArgumentException(
                    "Unsupported binary operator: " + binary.operator());
        };
    }
}
