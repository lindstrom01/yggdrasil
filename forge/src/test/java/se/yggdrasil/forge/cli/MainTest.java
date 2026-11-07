package se.yggdrasil.forge.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    private static final String NL = System.lineSeparator();

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String stdin, String... args) {
        return Main.run(
                args,
                new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    private String stdout() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private String stderr() {
        return err.toString(StandardCharsets.UTF_8);
    }

    @Test
    void runsAProgramFromAFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("program.fg");
        Files.writeString(file, "let x = 2 + 3;\nprint x;\n", StandardCharsets.UTF_8);

        int exitCode = run("", file.toString());

        assertEquals(Main.EXIT_OK, exitCode);
        assertEquals("5" + NL, stdout());
        assertEquals("", stderr());
    }

    @Test
    void reportsSyntaxErrorsWithPositionAndFailingExitCode(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("broken.fg");
        Files.writeString(file, "print 1;\nprint 2", StandardCharsets.UTF_8);

        int exitCode = run("", file.toString());

        assertEquals(Main.EXIT_PROGRAM_ERROR, exitCode);
        assertEquals("", stdout());
        assertEquals("error: [line 2, column 8] Expected ';' after the print expression,"
                + " found end of input" + NL, stderr());
    }

    @Test
    void reportsRuntimeErrors(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("runtime.fg");
        Files.writeString(file, "print missing;", StandardCharsets.UTF_8);

        int exitCode = run("", file.toString());

        assertEquals(Main.EXIT_PROGRAM_ERROR, exitCode);
        assertEquals("error: Undefined variable: missing" + NL, stderr());
    }

    @Test
    void reportsMissingFiles(@TempDir Path dir) {
        int exitCode = run("", dir.resolve("missing.fg").toString());

        assertEquals(Main.EXIT_USAGE_ERROR, exitCode);
        assertTrue(stderr().startsWith("error: cannot read file"));
    }

    @Test
    void rejectsTooManyArguments() {
        int exitCode = run("", "a.fg", "b.fg");

        assertEquals(Main.EXIT_USAGE_ERROR, exitCode);
        assertTrue(stderr().startsWith("Usage: forge"));
    }

    @Test
    void printsHelp() {
        int exitCode = run("", "--help");

        assertEquals(Main.EXIT_OK, exitCode);
        assertTrue(stdout().startsWith("Usage: forge"));
    }

    @Test
    void replKeepsVariablesBetweenLinesAndSurvivesErrors() {
        int exitCode = run("let x = 4;\nprint y;\nprint x * 2;\nexit\nprint 99;\n");

        assertEquals(Main.EXIT_OK, exitCode);
        assertEquals("> > > 8" + NL + "> " + NL, stdout());
        assertEquals("error: Undefined variable: y" + NL, stderr());
    }

    @Test
    void replEndsAtEndOfInput() {
        int exitCode = run("print 1;\n");

        assertEquals(Main.EXIT_OK, exitCode);
        assertEquals("> 1" + NL + "> " + NL, stdout());
    }
}
