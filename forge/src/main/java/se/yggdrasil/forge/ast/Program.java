package se.yggdrasil.forge.ast;

import java.util.List;

public record Program(List<Statement> statements) {
    public Program {
        statements = List.copyOf(statements);
    }
}
