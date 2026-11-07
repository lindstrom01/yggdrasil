package se.yggdrasil.forge.ast;

import java.math.BigInteger;
import java.util.Objects;

public sealed interface Expression permits Expression.Binary, Expression.Grouping,
        Expression.Literal, Expression.Unary, Expression.Variable {

    record Binary(Expression left, String operator, Expression right) implements Expression {
        public Binary {
            Objects.requireNonNull(left);
            Objects.requireNonNull(operator);
            Objects.requireNonNull(right);
        }
    }

    record Grouping(Expression expression) implements Expression {
        public Grouping {
            Objects.requireNonNull(expression);
        }
    }

    record Literal(Object value) implements Expression {
        public Literal {
            if (!(value instanceof BigInteger) && !(value instanceof String)
                    && !(value instanceof Boolean)) {
                throw new IllegalArgumentException("Literal must be an integer, a string or a boolean");
            }
        }
    }

    record Unary(String operator, Expression operand) implements Expression {
        public Unary {
            Objects.requireNonNull(operator);
            Objects.requireNonNull(operand);
        }
    }

    record Variable(String name) implements Expression {
        public Variable {
            Objects.requireNonNull(name);
        }
    }
}
