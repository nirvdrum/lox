package com.nirvdrum.lox;

import java.util.ArrayList;
import java.util.List;

import static com.nirvdrum.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        var statements = new ArrayList<Stmt>();

        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

    private Expr expression() {
        return equality();
    }

    private Stmt statement() {
        if (match(PRINT)) {
            return printStatement();
        }

        return expressionStatement();
    }

    private Stmt printStatement() {
        var value = expression();
        consume(SEMICOLON, "Expect ';' after value.");

        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        var expression = expression();
        consume(SEMICOLON, "Expect ';' after expression.");

        return new Stmt.Expression(expression);
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            var operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            var operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            var operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            var operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            var operator = previous();
            Expr right = unary();

            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }

        if (match(TRUE)) {
            return new Expr.Literal(true);
        }

        if (match(NIL)) {
            return new Expr.Literal(null);
        }

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");

            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (var type : types) {
            if (check(type)) {
                advance();

                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }

        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);

        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type() == SEMICOLON) {
                return;
            }

            switch (peek().type()) {
                case CLASS, FOR, FUN, IF, PRINT, RETURN, VAR, WHILE: {
                    return;
                }
            }

            return;
        }
    }
}
