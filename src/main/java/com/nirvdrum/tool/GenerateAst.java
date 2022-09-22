package com.nirvdrum.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        var outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Expression : Expr expression",
                "Print      : Expr expression"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        var path = outputDir + '/' + baseName + ".java";
        var writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.nirvdrum.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("interface " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // The AST classes.
        for (var type : types) {
            var className = type.split(":")[0].trim();
            var fields = type.split(":")[1].trim();

            writer.println();
            defineType(writer, baseName, className, fields);
        }

        // The base accept() method.
        writer.println();
        writer.println("  <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("  interface Visitor<R> {");

        for (var type : types) {
            var typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("  record " + className + "(" + fieldList + ") implements " + baseName + " {");

        // Visitor pattern.
        writer.println("    @Override");
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        writer.println("  }");
    }
}
