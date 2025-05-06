//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package SyntaxAnalyzer;

import LexicalAnalyzer.Token;

public abstract class Expr {
    public Expr() {
    }

    public abstract <R> R accept(Visitor<R> var1);

    public static class Variable extends Expr {
        public final Token name;

        Variable(Token name) {
            this.name = name;
        }

        public <R> R accept(Visitor<R> visitor) {
            if (visitor == null) {
                return null; // or handle null as needed
            }

            return visitor.visitVariableExpr(this);
        }
    }

    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public <R> R accept(Visitor<R> visitor) {
            if (visitor == null) {
                return null; // or handle null as needed
            }

            return visitor.visitBinaryExpr(this);
        }
    }

    public static class Grouping extends Expr {
        public final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

        public <R> R accept(Visitor<R> visitor) {

            if (visitor == null) {
                return null; // or handle null as needed
            }
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Literal extends Expr {
        public final Object value;

        Literal(Object value) {
            this.value = value;
        }

        public <R> R accept(Visitor<R> visitor) {
            if (visitor == null) {
                return null; // or handle null as needed
            }

            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Unary extends Expr {
        public final Token operator;
        public final Expr right;

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        public <R> R accept(Visitor<R> visitor) {
            if (visitor == null) {
                return null; // or handle null as needed
            }
            return visitor.visitUnaryExpr(this);
        }
    }



    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;

        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        public <R> R accept(Visitor<R> visitor) {
            if (visitor == null) {
                return null; // or handle null as needed
            }
            return visitor.visitAssignExpr(this);
        }
    }

    public static class Logical extends Expr {
        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            if (visitor == null) {
                return null; // or handle null as needed
            }

            return visitor.visitLogicalExpr(this);
        }
        public final Expr left;
        public final Token operator;
        public final Expr right;
    }

    // Inside Expr.java

    public static class Increment extends Expr {
        public final Token name;


        public Increment(Token name) {
            this.name = name;

        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIncrementExpr(this);
        }
    }

    public static class Decrement extends Expr {
        public final Token name;


        public Decrement(Token name) {
            this.name = name;

        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitDecrementExpr(this);
        }
    }


    public interface Visitor<R> {
        R visitBinaryExpr(Binary var1);

        R visitGroupingExpr(Grouping var1);

        R visitLiteralExpr(Literal var1);

        R visitUnaryExpr(Unary var1);

        R visitVariableExpr(Variable var1);

        R visitAssignExpr(Assign var1);
        R visitLogicalExpr(Logical expr);
        R visitIncrementExpr(Increment expr);
        R visitDecrementExpr(Decrement expr);

    }
}
