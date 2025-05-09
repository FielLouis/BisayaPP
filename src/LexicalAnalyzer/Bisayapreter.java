package LexicalAnalyzer;

import SematicAnalyzer.Interpreter;
import SyntaxAnalyzer.Parser;
import SyntaxAnalyzer.Stmt;
import Utility.RuntimeError;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Bisayapreter {
    private static final Interpreter interpreter = new Interpreter();
    public static boolean hadError = false;
    public static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes((Paths.get(path)));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code
        if(hadError) System.exit(65);
        if(hadRuntimeError) System.exit(70);
    }

      // For console input
//    private static void runPrompt() throws IOException {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//        StringBuilder sourceBuilder = new StringBuilder();
//
//        System.out.println("Type ':run' to execute your code.");
//
//        while (true) {
//            System.out.print("> ");
//            String line = reader.readLine();
//
//            if (line == null || line.equals(":exit")) break;
//
//            if (line.equals(":run")) {
//                run(sourceBuilder.toString());
//                sourceBuilder.setLength(0); // Clear after running
//                hadError = false;
//            } else {
//                sourceBuilder.append(line).append("\n");
//            }
//        }
//    }

    // For file input
    private static void runPrompt() throws IOException {
        // Read the entire file content as a single string
        BufferedReader reader = new BufferedReader(new FileReader("src/Test/NoErrorTests/Dawat.txt"));
        StringBuilder sourceBuilder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            sourceBuilder.append(line).append("\n");
        }
        reader.close();

        run(sourceBuilder.toString());
    }

    private static void run(String source) {
        Iskaner scanner = new Iskaner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);

        List<Stmt> statements = null;
        try {
            statements = parser.parse();
        } catch (Parser.ParseError e) {
            return;
        }

        // Stop if there was a syntax error
        if(hadError) return;

        try {
            interpreter.interpret(statements);
        } catch (RuntimeError error) {
            return;
        }

        if(!hadError && !hadRuntimeError) {
            System.out.println();
            System.out.println("------------------");
            System.out.println("-- Program Done --");
            System.out.println("------------------");
        }

        // Checking tokens
//        for (Token token : tokens) {
//            System.out.println(token);
//        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println("[line " + error.getToken().line + "] " + error.getMessage());
        hadRuntimeError = true;
    }

    public static void reportRuntimeError(RuntimeError error) {
        if (error.getToken() != null) {
            System.err.println("[line " + error.getToken().line + "] Error at '" +
                    error.getToken().lexeme + "': " + error.getMessage());
        } else {
            System.err.println("Runtime Error: " + error.getMessage());
        }
    }
}
