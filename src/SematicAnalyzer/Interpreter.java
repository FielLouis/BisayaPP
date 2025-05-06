package SematicAnalyzer;

import LexicalAnalyzer.Bisayapreter;
import LexicalAnalyzer.Token;
import LexicalAnalyzer.TokenType;
import SyntaxAnalyzer.Expr;
import SyntaxAnalyzer.Stmt;
import Utility.RuntimeError;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Interpreter implements Expr.Visitor, Stmt.Visitor<Void> {
    private Environment environment = new Environment();

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.getTokenType() == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        return switch (expr.operator.getTokenType()) {
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                yield -(double) right;
            }
            case NOT -> !isTruthy(right);
            default ->
                // Unreachable.
                    null;
        };
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "null";
        if (object instanceof Boolean) {
            return (Boolean) object ? "OO" : "DILI";
        }
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }


    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        if(expr == null) return null;
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
//        executeBlock(stmt.statements, new Environment(environment));
        executeBlock(stmt.statements, environment);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        if (stmt.expression instanceof Expr.Variable) {
            Expr.Variable variableExpr = (Expr.Variable) stmt.expression;
            String varName = variableExpr.name.getLexeme();  // Get the variable name (e.g., 'ctr')

            // Check if the variable exists in the environment
            if (!environment.containsKey(varName)) {
                throw new RuntimeException("Undefined variable: " + varName);
            }
        }

        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while(isTruthy(evaluate(stmt.condition))){
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.print(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;

        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        } else {
            // Assign default based on declared type
            value = switch (stmt.getType()) {
                case "NUMERO" -> 0.0;
                case "TIPIK" -> 0.0f;
                case "TINUOD" -> false;
                case "LETRA" -> '\0';
                default -> throw new RuntimeError(stmt.name, "Unsupported variable type: " + stmt.getType());
            };
        }

        environment.define(stmt.name.getLexeme(), value, stmt.getType());
        return null;
    }
    @Override
    public Void visitVarDeclaration(Stmt.VarDeclaration stmt) {
        for (Stmt.Var var : stmt.variables) {
            Object value = null;

            if (var.initializer != null) {
                value = evaluate(var.initializer);

                if (var.getType().equals("NUMERO") && !(value instanceof Double)) {
                    throw new RuntimeError(var.name, "Variable " + var.name.getLexeme() + " must be of type NUMERO.");
                } else if (var.getType().equals("TIPIK") && !(value instanceof Float)) {
                    throw new RuntimeError(var.name, "Variable " + var.name.getLexeme() + " must be of type TIPIK.");
                } else if (var.getType().equals("LETRA") && !(value instanceof Character)) {
                    throw new RuntimeError(var.name, "Variable " + var.name.getLexeme() + " must be of type LETRA.");
                } else if (var.getType().equals("TINUOD") && (!(value instanceof Boolean))) {
                    throw new RuntimeError(var.name, "Variable " + var.name.getLexeme() + " must be of type TINUOD.");
                }
            }
            environment.define(var.name.getLexeme(), value, var.getType());
        }
        return null;
    }

    public Void visitInputStmt(Stmt.Input inputStmt) {
        List<Token> variables = inputStmt.getVariableNames();

        // Prompt once for all variables
        System.out.print("Enter value");
        if(variables.size() > 1) {
            System.out.print("s (comma-separated)");
        }
        System.out.print(" for ( ");
        for (Token var : variables) {
            System.out.print(var.getLexeme() + " ");
        }
        System.out.print("): ");

        Scanner scanner = new Scanner(System.in);
        String inputLine = scanner.nextLine();
        String[] inputs = inputLine.split(",");

        if (inputs.length != variables.size()) {
            throw new RuntimeError(variables.get(0), "Expected " + variables.size() + " inputs, but got " + inputs.length);
        }

        for (int i = 0; i < variables.size(); i++) {
            Token varName = variables.get(i);
            String inputValue = inputs[i].trim();

            Object existing = environment.get(varName);
            String varType = environment.getType(varName.getLexeme());

            if (varType == null && existing == null) {
                throw new RuntimeError(varName, "Variable type is undefined.");
            }

            try {
                if (varType != null && (varType.equals("NUMERO") || varType.equals("TIPIK"))) {
                    environment.assign(varName, Double.parseDouble(inputValue));
                } else if (varType != null && varType.equals("TINUOD")) {
                    if (inputValue.equalsIgnoreCase("OO")) {
                        environment.assign(varName, true);
                    } else if (inputValue.equalsIgnoreCase("DILI")) {
                        environment.assign(varName, false);
                    } else {
                        throw new RuntimeError(varName, "TINUOD should be OO or DILI.");
                    }
                } else if (varType != null && varType.equals("LETRA")) {
                    if (inputValue.length() == 1) {
                        environment.assign(varName, inputValue.charAt(0));
                    } else {
                        throw new RuntimeError(varName, "Expected a single character for LETRA.");
                    }
                } else if (existing instanceof Double) {
                    environment.assign(varName, Double.parseDouble(inputValue));
                } else if (existing instanceof Boolean) {
                    if (inputValue.equalsIgnoreCase("OO")) {
                        environment.assign(varName, true);
                    } else if (inputValue.equalsIgnoreCase("DILI")) {
                        environment.assign(varName, false);
                    } else {
                        throw new RuntimeError(varName, "Expected OO or DILI for boolean input.");
                    }
                } else if (existing instanceof Character) {
                    environment.assign(varName, inputValue.charAt(0));
                } else {
                    environment.assign(varName, inputValue);
                }
            } catch (NumberFormatException e) {
                throw new RuntimeError(varName, "Invalid number input: " + inputValue);
            }
        }

        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        String type = environment.getType(expr.name.getLexeme()); //naay problema diri

        if (value == null) {
            environment.assign(expr.name, null);
            return null;
        }

        if (type.equals("NUMERO") && !(value instanceof Double)) {
            throw new RuntimeError(expr.name, "Expected a number for NUMERO variable.");
        }else if(type.equals("TIPIK") && !(value instanceof Float)) {
            System.out.println(value.getClass());
            throw new RuntimeError(expr.name, "Expected a number for TIPIK variable.");
        }else if(type.equals("LETRA") && !(value instanceof Character)) {
            throw new RuntimeError(expr.name, "Expected a character for LETRA variable.");
        }else if(type.equals("TINUOD") && !(value instanceof Boolean)) {
            throw new RuntimeError(expr.name, "Expected a boolean for TINUOD variable.");
        }

        environment.assign(expr.name, value);
        return value;
    }


    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.getTokenType()) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case DIVIDE:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case MULTIPLY:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                if (left instanceof Character && right instanceof Character) {
                    return (Character)left + (Character) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers, two strings, or two characters.");
            case GREATER_THAN:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS_THAN:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case NOT_EQUALS:
                return !isEqual(left, right);
            case EQUALS:
                return isEqual(left, right);
            case CONCAT:
                return stringify(left) + stringify(right);
            case NEXT_LINE:
                return stringify(left) + "\n" + stringify(right);
        }
        // Unreachable.
        return null;
    }

    @Override
    public Object visitIncrementExpr(Expr.Increment expr) {
        Object value = environment.get(expr.name);

        if (value instanceof Double) {
            double oldValue = (Double) value;
            double newValue = oldValue + 1;
            environment.assign(expr.name, newValue);

            return newValue;
        }
        throw new RuntimeError(expr.name, "Only numbers can be incremented.");
    }

    @Override
    public Object visitDecrementExpr(Expr.Decrement expr) {
        Object value = environment.get(expr.name);

        if (value instanceof Double) {
            double oldValue = (Double) value;
            double newValue = oldValue - 1;
            environment.assign(expr.name, newValue);

            return newValue;
        }

        throw new RuntimeError(expr.name, "Only numbers can be decremented.");
    }

    @Override
    public Void visitSugodStmt(Stmt.Sugod stmt) {
        executeBlock(stmt.statements, environment);
        return null;
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Bisayapreter.runtimeError(error);
        }
    }

}
