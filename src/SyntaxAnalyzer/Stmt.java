//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package SyntaxAnalyzer;

import LexicalAnalyzer.Token;
import LexicalAnalyzer.TokenType;

import java.util.List;

public abstract class Stmt {
    public Stmt() {
    }

    public abstract <R> R accept(Visitor<R> var1);

    public static class VarDeclaration extends Stmt {
        public final List<Var> variables;

        public VarDeclaration(List<Var> variables) {
            this.variables = variables;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDeclaration(this);
        }
    }



    public static class Expression extends Stmt {
        public final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class Block extends Stmt {
        public final List<Stmt> statements;

        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class Print extends Stmt {
        public final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    public static class Input extends Stmt {
        private final List<Token> variableNames;

        public Input(List<Token> variableNames) {
            this.variableNames = variableNames;
        }

        public List<Token> getVariableNames() {
            return variableNames;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInputStmt(this);
        }
    }

    public static class Sugod extends Stmt {
        public final List<Stmt> statements;
        Sugod(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSugodStmt(this);
        }
    }

    public static class While extends Stmt {
        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }

        public final Expr condition;
        public final Stmt body;
    }

    public static class Var extends Stmt {
        public final Token name;
        public final Expr initializer;
        final Token type;

        public Var(Token name, Expr initializer, Token type) {
            this.name = name;
            this.initializer = initializer;
            this.type = type;
        }

        public String getType() {
            if(type.getTokenType().equals(TokenType.NUMERO)){
                return "NUMERO";
            }else if(type.getTokenType().equals(TokenType.TIPIK)){
                return "TIPIK";
            }else if(type.getTokenType().equals(TokenType.LETRA)){
                return "LETRA";
            }else if(type.getTokenType().equals(TokenType.TINUOD)){
                return "TINUOD";
            }
            return null;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    public static class If extends Stmt {
        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
    }

    public interface Visitor<R> {
        R visitBlockStmt(Block var1);

        R visitExpressionStmt(Expression var1);

        R visitPrintStmt(Print var1);

        R visitVarStmt(Var var1);
        R visitIfStmt(If stmt);
        R visitVarDeclaration(VarDeclaration stmt);
        R visitSugodStmt(Sugod stmt);
        R visitInputStmt(Input stmt);
        R visitWhileStmt(While stmt);
    }
}
