package se.yggdrasil.forge.ast;

import java.util.List;
import java.util.Objects;

public sealed interface Statement
        permits Statement.LetStatement, Statement.PrintStatement, Statement.Assignment, Statement.WhileStatement,
        Statement.IfStatement {

    record LetStatement(String name, Expression initializer) implements Statement {
        public LetStatement {
            Objects.requireNonNull(name);
            Objects.requireNonNull(initializer);
        }
    }

    record PrintStatement(Expression expression) implements Statement {
        public PrintStatement {
            Objects.requireNonNull(expression);
        }
    }

    record Assignment(String name, Expression value) implements Statement {
        public Assignment {
            Objects.requireNonNull(name);
            Objects.requireNonNull(value);
        }
    }

    record WhileStatement(Expression condition, List<Statement> body) implements Statement {
        public WhileStatement {
            Objects.requireNonNull(condition);
            body = List.copyOf(body);
        }
    }

    record IfStatement(Expression condition, List<Statement> thenBranch, List<Statement> elseBranch)
            implements Statement {
        public IfStatement {
            Objects.requireNonNull(condition);
            thenBranch = List.copyOf(thenBranch);
            elseBranch = List.copyOf(elseBranch);
        }
    }
}
