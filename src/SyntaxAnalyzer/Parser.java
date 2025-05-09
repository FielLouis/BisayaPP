package SyntaxAnalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import LexicalAnalyzer.Bisayapreter;
import LexicalAnalyzer.Token;
import LexicalAnalyzer.TokenType;

public class Parser {
    public static class ParseError extends RuntimeException {
    }
    private  final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        if(!peek().getLexeme().equals("SUGOD")){
            throw error(peek(),"Expect 'SUGOD' at the start of the program.");
        }

        statements.add(sugodStatement());

        if(!isAtEnd()){
            throw error(peek(),"Expected 'KATAPUSAN' at the end of the program.");
        }
        return statements;
    }

    private Stmt sugodStatement() {
        consume(TokenType.START, "Expected 'SUGOD' at the start of the program.");
        List<Stmt> statements = new ArrayList<>();

        while(!peek().getLexeme().equals("KATAPUSAN") && !isAtEnd()){
            statements.add(declaration());
        }

        consume(TokenType.END, "Expected 'KATAPUSAN' inig human sa program dapat.");
        return new Stmt.Sugod(statements);
    }

    private Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.MUGNA)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }


    private Stmt statement() {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.INPUT)){
            consume(TokenType.COLON, "Expected ':' after input statement.");
            return inputStatement();
        }
        if (match(TokenType.PRINT)){
            consume(TokenType.COLON, "Expected ':' after print statement.");
            return printStatement();
        }

        if (match(TokenType.LBRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(TokenType.LPAREN, "Expected '(' after 'ALANG SA'.");

        Stmt initializer;
        if (match(TokenType.COMMA)) {
            initializer = null;
        }
        else if (match(TokenType.MUGNA)) {
            initializer = singleVarDeclaration();
        } else {
            initializer = expressionStatement();
        }

        consume(TokenType.COMMA, "Expected ',' after initializer.");

        Expr condition = null;
        if (!check(TokenType.COMMA)) {
            condition = expression();
        }
        consume(TokenType.COMMA, "Expected ',' after loop condition.");

        Expr increment = null;
        if (!check(TokenType.RPAREN)) {
            increment = expression();
        }

        consume(TokenType.RPAREN, "Expected ')' after for clauses.");
        consume(TokenType.BLOCK, "Expected 'PUNDOK' after ).");

        Stmt body = statement();
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }

        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        List<Stmt> statements = new ArrayList<>();
        if (initializer != null) statements.add(initializer);
        statements.add(body);
        body = new Stmt.Block(statements);

        return body;
    }

    private Stmt whileStatement() {
        consume(TokenType.LPAREN,  "Expected '(' after 'SAMTANG'.");
        Expr condition = expression();
        consume(TokenType.RPAREN, "Expected ')' inig human sa kondisyon.");
        consume(TokenType.BLOCK, "Expected 'PUNDOK' inig human sa ')'.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {
        consume(TokenType.LPAREN, "Expected '(' inig human sa 'KUNG'.");
        Expr condition = expression();
        consume(TokenType.RPAREN, "Expected ')' inig human sa KUNG kondisyon.");
        consume(TokenType.BLOCK, "Expected 'PUNDOK' inig human anhi ')'");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE_IF)) {
            elseBranch = ifStatement();
        } else if (match(TokenType.ELSE)) {
            consume(TokenType.BLOCK, "Expected 'PUNDOK' inig human sa 'KUNG WALA'");
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt inputStatement() {
        List<Token> variableNames = new ArrayList<>();

        do {
            Token variableName = consume(TokenType.IDENTIFIER, "variable name na dapat.");
            variableNames.add(variableName);
        } while (match(TokenType.COMMA));

        return new Stmt.Input(variableNames);
    }

    private Stmt printStatement() {
        int beforeExpr = current;

        Expr value = expression();

        if (!isAtEnd() && !check(TokenType.NEXT_LINE) && !check(TokenType.RBRACE)) {
            Token token = peek();

            if (token.getTokenType() == TokenType.IDENTIFIER ||
                    token.getTokenType() == TokenType.STRING ||
                    token.getTokenType() == TokenType.NUMBER ||
                    token.getTokenType() == TokenType.CHARACTER ||
                    token.getTokenType() == TokenType.BOOL_TRUE ||
                    token.getTokenType() == TokenType.BOOL_FALSE ||
                    token.getTokenType() == TokenType.LPAREN) {

                throw error(token, "Expected '&' sa tunga sa mga variable/expression sa IPAKITA statement.");
            }
        }

        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {
        consume(TokenType.NUMERO, TokenType.LETRA, TokenType.TINUOD, TokenType.TIPIK);
        Token type = previous();
        List<Stmt.Var> vars = new ArrayList<>();

        do {
            Token name = consume(TokenType.IDENTIFIER, "variable name na dapat.");

            if (!check(TokenType.ASSIGNMENT)) {
                vars.add(new Stmt.Var(name, null, type));
            } else {
                consume(TokenType.ASSIGNMENT, "Expected '=' inig human sa variable name lageh.");
                Expr initializer = expression();
                vars.add(new Stmt.Var(name, initializer, type));
            }
        } while (match(TokenType.COMMA));

        return new Stmt.VarDeclaration(vars);
    }

    private Stmt singleVarDeclaration() {
        consume(TokenType.NUMERO, TokenType.LETRA, TokenType.TINUOD, TokenType.TIPIK);
        Token type = previous();
        List<Stmt.Var> vars = new ArrayList<>();
        Token name = consume(TokenType.IDENTIFIER, "variable name na dapat.");

        // Check if the variable has an initializer
        Expr initializer = null;
        if (match(TokenType.ASSIGNMENT)) {
            initializer = expression();
            vars.add(new Stmt.Var(name, initializer, type));
        }
        return new Stmt.VarDeclaration(vars);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.RBRACE, "Expected '}' after block.");
        return statements;
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(TokenType.ASSIGNMENT)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.NOT_EQUALS, TokenType.EQUALS)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER_THAN, TokenType.GREATER_EQUAL, TokenType.LESS_THAN, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS, TokenType.CONCAT, TokenType.NEXT_LINE, TokenType.MODULO)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.DIVIDE, TokenType.MULTIPLY, TokenType.MODULO)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.BOOL_FALSE)) return new Expr.Literal(false);
        if (match(TokenType.BOOL_TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NULL)) return new Expr.Literal(null);
        if (match(TokenType.NUMBER)) {
            return new Expr.Literal(previous().getLiteral());
        }
        if (match(TokenType.STRING)) {
            return new Expr.Literal(previous().getLiteral());
        }
        if (match(TokenType.CHARACTER)) {
            return new Expr.Literal(previous().getLiteral());
        }
        if (match(TokenType.FLOAT)) {
            return new Expr.Literal(previous().getLiteral());
        }
        if (match(TokenType.LPAREN)) {
            Expr expr = expression();
            consume(TokenType.RPAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if (match(TokenType.IDENTIFIER)) {
            Token variable = previous();

            if (match(TokenType.INCREMENT)) {
                Expr value = new Expr.Increment(variable);
                consume(TokenType.PLUS, "Expected '+' after '++'.");

                return new Expr.Assign(variable, value);
            } else if (match(TokenType.DECREMENT)) {
                Expr value = new Expr.Decrement(variable);
                consume(TokenType.MINUS, "Expected '-' after '--'.");
                return new Expr.Assign(variable, value);
            }
            return new Expr.Variable(variable); // AHAHAHHA
        }
        if (match(TokenType.ESCAPE_CODE)) {
            String value = previous().getLiteral().toString();

            if (isIdentifier(value)) {
                return new Expr.Variable(new Token(TokenType.IDENTIFIER, value, value, previous().getLine()));
            }
            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
                return new Expr.Literal(value);
            }
            return new Expr.Literal(previous().getLiteral());
        }

        if (match(TokenType.NEXT_LINE)) return new Expr.Literal('\n');
        throw this.error(this.peek(), "Expected expression.");
    }

    private boolean isIdentifier(String value) {
        return value.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw this.error(this.peek(), message);
    }

    private Token consume(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) return advance();
        }
        throw this.error(peek(), "Expected one of " + Arrays.toString(types));
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getTokenType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getTokenType() == TokenType.EOF;
    }
    private Token peek() {
        return tokens.get(current);
    }
    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Bisayapreter.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().getTokenType() == TokenType.NEXT_LINE) return;

            switch (peek().getTokenType()) {
                case MUGNA:
                case START: //sugod
                case END: //katapusan
                case PRINT: // ipakita
                case INPUT: // dawat
                case BLOCK: // pundok
                case IF: // kung
                case ELSE_IF: // kung wala
                case ELSE:  //kung dili
                case FOR: // alang sa
                    return;
            }
            advance();
        }
    }
}
