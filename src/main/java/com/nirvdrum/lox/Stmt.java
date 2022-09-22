package com.nirvdrum.lox;

import java.util.List;

interface Stmt {
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
  }

  record Expression(Expr expression) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
  }

  record Print(Expr expression) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }
  }

  <R> R accept(Visitor<R> visitor);
}
