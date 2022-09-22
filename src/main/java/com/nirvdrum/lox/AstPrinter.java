package com.nirvdrum.lox;

import java.util.List;

public class AstPrinter implements Expr.Visitor<String>,
                                   Stmt.Visitor<Void> {
    private String output;

    public static void main(String[] args) {
        var expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(new Expr.Literal(45.67))
        );

        System.out.println(new AstPrinter().print(expression));
    }

    String print(Expr expression) {
        return expression.accept(this);
    }

    String print(List<Stmt> statements) {
        output = "";

        for (var statement : statements) {
            statement.accept(this);
        }

        return output;
    }

    private String parenthesize(String name, Expr... exprs) {
        var builder = new StringBuilder();

        builder.append("(").append(name);
        for (var expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator().lexeme(), expr.left(), expr.right());
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression());
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value() == null ? "nil" : expr.value().toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator().lexeme(), expr.right());
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        output += stmt.expression().accept(this);

        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        output += "(print " + stmt.expression().accept(this) + ")";

        return null;
    }
}
