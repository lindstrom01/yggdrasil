package se.yggdrasil.forge.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.yggdrasil.forge.ast.Expression;
import se.yggdrasil.forge.ast.Program;
import se.yggdrasil.forge.ast.Statement;
import se.yggdrasil.forge.lexer.Lexer;

class ParserTest {

        @Test
        void parsesVariableDeclarationAndPrintStatement() {
                Program program = parse("let answer = 42; print answer;");

                assertEquals(List.of(
                                new Statement.LetStatement("answer",
                                                new Expression.Literal(new BigInteger("42"))),
                                new Statement.PrintStatement(new Expression.Variable("answer"))),
                                program.statements());
        }

        @Test
        void multiplicationHasHigherPrecedenceThanAddition() {
                Program program = parse("print 2 + 3 * 4;");

                Statement.PrintStatement print = assertInstanceOf(Statement.PrintStatement.class,
                                program.statements().getFirst());
                assertEquals(
                                new Expression.Binary(
                                                new Expression.Literal(new BigInteger("2")),
                                                "+",
                                                new Expression.Binary(
                                                                new Expression.Literal(new BigInteger("3")),
                                                                "*",
                                                                new Expression.Literal(new BigInteger("4")))),
                                print.expression());
        }

        @Test
        void preservesParenthesesAndParsesUnaryOperators() {
                Program program = parse("print -(2 + 3);");

                Statement.PrintStatement print = assertInstanceOf(Statement.PrintStatement.class,
                                program.statements().getFirst());
                assertEquals(
                                new Expression.Unary(
                                                "-",
                                                new Expression.Grouping(
                                                                new Expression.Binary(
                                                                                new Expression.Literal(
                                                                                                new BigInteger("2")),
                                                                                "+",
                                                                                new Expression.Literal(
                                                                                                new BigInteger("3"))))),
                                print.expression());
        }

        @Test
        void parsesStringLiterals() {
                Program program = parse("print \"hello\";");

                assertEquals(
                                new Statement.PrintStatement(new Expression.Literal("hello")),
                                program.statements().getFirst());
        }

        @Test
        void parsesAnEmptyProgram() {
                assertEquals(List.of(), parse("").statements());
        }

        @Test
        void reportsMissingSemicolon() {
                ParseException exception = assertThrows(
                                ParseException.class,
                                () -> parse("print 1"));

                assertEquals("[line 1, column 8] Expected ';' after the print expression, found end of input",
                                exception.getMessage());
        }

        @Test
        void reportsUnexpectedTokenAtStatementStart() {
                ParseException exception = assertThrows(
                                ParseException.class,
                                () -> parse("42;"));

                assertEquals(
                                "[line 1, column 1] Expected 'let', 'print', 'while', or a variable assignment at the start of a statement, found '42'",
                                exception.getMessage());
        }

        @Test
        void parsesWhileWithComparisonCondition() {
                Program program = parse("while x <= 3 { x = x + 1; }");

                assertEquals(
                                new Statement.WhileStatement(
                                                new Expression.Binary(
                                                                new Expression.Variable("x"),
                                                                "<=",
                                                                new Expression.Literal(new BigInteger("3"))),
                                                List.of(new Statement.Assignment("x",
                                                                new Expression.Binary(
                                                                                new Expression.Variable("x"),
                                                                                "+",
                                                                                new Expression.Literal(new BigInteger(
                                                                                                "1")))))),
                                program.statements().getFirst());
        }

        @Test
        void parsesIfStatement() {
                Program program = parse("if x <= 3 { x = x + 1; } else { x = x - 1; }");

                assertEquals(
                                new Statement.IfStatement(
                                                new Expression.Binary(
                                                                new Expression.Variable("x"),
                                                                "<=",
                                                                new Expression.Literal(new BigInteger("3"))),
                                                List.of(new Statement.Assignment("x",
                                                                new Expression.Binary(
                                                                                new Expression.Variable("x"),
                                                                                "+",
                                                                                new Expression.Literal(new BigInteger(
                                                                                                "1"))))),
                                                List.of(new Statement.Assignment("x",
                                                                new Expression.Binary(
                                                                                new Expression.Variable("x"),
                                                                                "-",
                                                                                new Expression.Literal(new BigInteger(
                                                                                                "1")))))),
                                program.statements().getFirst());
        }

        @Test
        void comparisonBindsLooserThanArithmeticAndTighterThanEquality() {
                Program program = parse("print 1 + 2 < 4 == 5 > 2;");

                Statement.PrintStatement print = assertInstanceOf(Statement.PrintStatement.class,
                                program.statements().getFirst());
                Expression.Binary equality = assertInstanceOf(Expression.Binary.class, print.expression());
                assertEquals("==", equality.operator());
                assertEquals("<", assertInstanceOf(Expression.Binary.class, equality.left()).operator());
                assertEquals(">", assertInstanceOf(Expression.Binary.class, equality.right()).operator());
        }

        @Test
        void reportsMissingBraceAfterWhileCondition() {
                ParseException exception = assertThrows(
                                ParseException.class,
                                () -> parse("while x < 3 print x;"));

                assertEquals("[line 1, column 13] Expected '{' after the while condition, found 'print'",
                                exception.getMessage());
        }

        private Program parse(String source) {
                return new Parser(new Lexer(source)).parse();
        }
}
