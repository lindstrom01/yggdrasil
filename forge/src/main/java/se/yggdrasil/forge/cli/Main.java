package se.yggdrasil.forge.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import se.yggdrasil.forge.interpreter.Interpreter;

public final class Main {

    static final int EXIT_OK = 0;
    static final int EXIT_PROGRAM_ERROR = 1;
    static final int EXIT_USAGE_ERROR = 2;

    private static final String USAGE = "Usage: forge [file]\n"
            + "  forge <file>   run a program from a file\n"
            + "  forge          start an interactive session (type 'exit' to quit)";

    private Main() {
    }

    public static void main(String[] args) {
        int exitCode = run(args, System.in, System.out, System.err);
        if (exitCode != EXIT_OK) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
            out.println(USAGE);
            return EXIT_OK;
        }
        if (args.length > 1) {
            err.println(USAGE);
            return EXIT_USAGE_ERROR;
        }

        Interpreter interpreter = new Interpreter(out);
        if (args.length == 1) {
            return runFile(Path.of(args[0]), interpreter, err);
        }
        return runRepl(in, interpreter, out, err);
    }

    private static int runFile(Path path, Interpreter interpreter, PrintStream err) {
        String source;
        try {
            source = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            err.println("error: cannot read file '" + path + "'");
            return EXIT_USAGE_ERROR;
        }
        try {
            interpreter.interpret(source);
            return EXIT_OK;
        } catch (IllegalArgumentException e) {
            err.println("error: " + e.getMessage());
            return EXIT_PROGRAM_ERROR;
        }
    }

    private static int runRepl(InputStream in, Interpreter interpreter, PrintStream out,
            PrintStream err) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        try {
            while (true) {
                out.print("> ");
                out.flush();
                String line = reader.readLine();
                if (line == null || line.strip().equals("exit")) {
                    out.println();
                    return EXIT_OK;
                }
                if (line.isBlank()) {
                    continue;
                }
                try {
                    interpreter.interpret(line);
                } catch (IllegalArgumentException e) {
                    err.println("error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            err.println("error: cannot read input");
            return EXIT_USAGE_ERROR;
        }
    }
}
