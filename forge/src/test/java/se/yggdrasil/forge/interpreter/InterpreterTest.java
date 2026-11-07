package se.yggdrasil.forge.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import se.yggdrasil.forge.parser.ParseException;

class InterpreterTest {

    @Test
    void evaluatesDeclarationsAssignmentsAndPrintStatements() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Interpreter interpreter = new Interpreter(new PrintStream(output, true, StandardCharsets.UTF_8));

        interpreter.interpret("let x = 5; x = x + 5; print x;");

        assertEquals("10" + System.lineSeparator(), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void evaluatesArithmeticWithPrecedenceAndParentheses() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Interpreter interpreter = new Interpreter(new PrintStream(output, true, StandardCharsets.UTF_8));

        interpreter.interpret("print 2 + 3 * 4; print -(2 + 3);");

        assertEquals("14" + System.lineSeparator() + "-5" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void evaluatesWhileStatements() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Interpreter interpreter = new Interpreter(new PrintStream(output, true, StandardCharsets.UTF_8));
        interpreter.interpret("let x = 0; while x < 3 { print x; x = x + 1; }");

        assertEquals("0" + System.lineSeparator() + "1" + System.lineSeparator() + "2" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void evaluatesComparisonsAndEquality() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Interpreter interpreter = new Interpreter(new PrintStream(output, true, StandardCharsets.UTF_8));

        interpreter.interpret("print 1 < 2; print 2 <= 1; print 3 >= 3; print 1 + 1 == 2; print 1 != 1;");

        String nl = System.lineSeparator();
        assertEquals("true" + nl + "false" + nl + "true" + nl + "true" + nl + "false" + nl,
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void whileLoopNeverRunsWhenConditionIsFalse() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Interpreter interpreter = new Interpreter(new PrintStream(output, true, StandardCharsets.UTF_8));

        interpreter.interpret("while 1 > 2 { print 1; }");

        assertEquals("", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void whileConditionMustBeBoolean() {
        Interpreter interpreter = new Interpreter(new PrintStream(new ByteArrayOutputStream()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> interpreter.interpret("while 1 { print 1; }"));

        assertEquals("Condition must be a comparison, but was: 1", exception.getMessage());
    }

    @Test
    void reportsMissingClosingBraceOfWhile() {
        Interpreter interpreter = new Interpreter(new PrintStream(new ByteArrayOutputStream()));

        ParseException exception = assertThrows(
                ParseException.class,
                () -> interpreter.interpret("let x = 0; while x < 3 { x = x + 1;"));

        assertTrue(exception.getMessage().contains("Expected '}' after the while block, found end of input"));
    }

    @Test
    void printsStringLiterals() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Interpreter interpreter = new Interpreter(new PrintStream(output, true, StandardCharsets.UTF_8));

        interpreter.interpret("print \"hello\";");

        assertEquals("hello" + System.lineSeparator(), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void parsesEachSourcePassedToInterpret() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Interpreter interpreter = new Interpreter(new PrintStream(output, true, StandardCharsets.UTF_8));

        interpreter.interpret("print 1;");
        interpreter.interpret("print 2;");

        assertEquals("1" + System.lineSeparator() + "2" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void reportsAssignmentToUndefinedVariable() {
        Interpreter interpreter = new Interpreter(new PrintStream(new ByteArrayOutputStream()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> interpreter.interpret("x = 10;"));

        assertEquals("Undefined variable: x", exception.getMessage());
    }

    @Test
    void reportsUseOfUndefinedVariable() {
        Interpreter interpreter = new Interpreter(new PrintStream(new ByteArrayOutputStream()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> interpreter.interpret("print missing;"));

        assertEquals("Undefined variable: missing", exception.getMessage());
    }

    @Test
    void reportsInvalidArithmeticOperands() {
        Interpreter interpreter = new Interpreter(new PrintStream(new ByteArrayOutputStream()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> interpreter.interpret("print \"one\" + 1;"));

        assertEquals("Binary operator '+' requires integer operands", exception.getMessage());
    }

    @Test
    void reportsDivisionByZero() {
        Interpreter interpreter = new Interpreter(new PrintStream(new ByteArrayOutputStream()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> interpreter.interpret("print 1 / 0;"));

        assertEquals("Division by zero", exception.getMessage());
    }

    @Test
    void reportsSyntaxErrorsFromTheParser() {
        Interpreter interpreter = new Interpreter(new PrintStream(new ByteArrayOutputStream()));

        assertThrows(ParseException.class, () -> interpreter.interpret("print 1"));
    }
}
