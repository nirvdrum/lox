package com.nirvdrum.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    private enum Mode {
        AST,
        EVAL,
        BOTH
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 3) {
            System.out.println("Usage: jlox [-m mode] [script]");
            System.exit(64);
        } else if (args.length == 3 && args[0].equals("-m")) {
            var mode = Mode.valueOf(args[1].toUpperCase());
            runFile(mode, args[1]);
        } else if (args.length == 2) {
            var mode = Mode.valueOf(args[1].toUpperCase());
            runPrompt(mode);
        } else if (args.length == 1) {
            runFile(Mode.EVAL, args[0]);
        } else {
            runPrompt(Mode.BOTH);
        }
    }

    private static void runFile(Mode mode, String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(mode, new String(bytes, Charset.defaultCharset()));

        if (hadError) {
            System.exit(65);
        }

        if (hadRuntimeError) {
            System.exit(70);
        }
    }

    private static void runPrompt(Mode mode) throws IOException {
        var input = new InputStreamReader(System.in);
        var reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            var line = reader.readLine();

            if (line == null) {
                break;
            }

            run(mode, line);
            hadError = false;
        }
    }

    private static void run(Mode mode, String source) {
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();
        var parser = new Parser(tokens);
        var statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) {
            return;
        }

        switch (mode) {
            case AST: {
                System.out.println(new AstPrinter().print(statements));
                return;
            }

            case EVAL: {
                interpreter.interpret(statements);
                return;
            }

            case BOTH: {
                System.out.println(new AstPrinter().print(statements));
                interpreter.interpret(statements);

                return;
            }

            default: {
                System.err.println("Unknown mode: " + mode);
            }
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.printf("[line %d] Error %s: %s%n", line, where, message);

        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line() + "]");
        hadRuntimeError = true;
    }
}
