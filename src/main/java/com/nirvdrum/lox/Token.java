package com.nirvdrum.lox;

public record Token(TokenType type, String lexeme, Object literal, int line) {}
