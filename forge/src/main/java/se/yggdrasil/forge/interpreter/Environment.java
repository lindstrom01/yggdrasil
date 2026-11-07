package se.yggdrasil.forge.interpreter;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Map<String, Object> variables = new HashMap<>();

    public void define(String name, Object value) {
        variables.put(name, value);
    }

    public Object get(String name) {
        if (!variables.containsKey(name)) {
            throw new IllegalArgumentException("Undefined variable: " + name);
        }
        return variables.get(name);
    }

    public void assign(String name, Object value) {
        if (!variables.containsKey(name)) {
            throw new IllegalArgumentException("Undefined variable: " + name);
        }
        variables.put(name, value);
    }

}
