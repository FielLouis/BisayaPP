package SematicAnalyzer;

import LexicalAnalyzer.Token;
import LexicalAnalyzer.TokenType;
import Utility.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, String> types = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if (values.containsKey(name.getLexeme())) {
            return values.get(name.getLexeme());
        }

        if(enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Unsa ni sya '" + name.getLexeme() + "'?");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.getLexeme())) {
            values.put(name.getLexeme(), value);
            return;
        }

        if(enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Unsa ni sya '" + name.getLexeme() + "'.");
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    void define(String name, Object value, String type){
        values.put(name, value);
        types.put(name, type);
    }

    String getType(String name) {
        if (types.containsKey(name)) {
            return types.get(name);
        }
        throw new RuntimeError(new Token(TokenType.IDENTIFIER, name, null, 0), "Unsa d ay ni sya nga type '" + name + "'.");
    }

    boolean containsKey(String name) {
        return values.containsKey(name);
    }
}
