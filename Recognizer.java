import java.io.*;
import java.util.*;

public class Recognizer {
    static List<Common.Lex> tokens;
    static int index = 0;
    static BufferedWriter writer;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Recognizer <inputFile> <outputFile>");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(args[0]));
             BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]))) {

            Recognizer.writer = writer;
            tokens = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 2);
                tokens.add(new Common.Lex(Common.Token.valueOf(parts[0]), parts[1]));
            }

            try {
                function();
                if (index != tokens.size()) {
                    error("body", "Only consumed " + index + " of the " + tokens.size() + " given tokens");
                } else {
                    writer.write("PARSED!!!\n");
                }
            } catch (IOException e) {
                writer.write(e.getMessage() + "\n");
            }

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    static void error(String rule, String expected, String actual) throws IOException {
        String message = "Error: In grammar rule " + rule + ", expected " + expected + " but was " + actual;
        writer.write(message + "\n");
        writer.close();
        System.exit(0);
    }

    static void error(String rule, String expectedNonTerminal) throws IOException {
        String message = "Error: In grammar rule " + rule + ", expected a valid " + expectedNonTerminal + " non-terminal to be present but was not.";
        writer.write(message + "\n");
        writer.close();
        System.exit(0);
    }

    static Common.Lex peek() {
        return index < tokens.size() ? tokens.get(index) : null;
    }

    static Common.Lex match(Common.Token expected, String ruleName) throws IOException {
        Common.Lex tok = peek();
        if (tok == null || tok.token != expected) {
            error(ruleName, "token #" + index + " to be " + expected, tok != null ? tok.token.toString() : "null");
        }
        index++;
        return tok;
    }

    static void function() throws IOException {
        if (peek() == null || peek().token != Common.Token.VARTYPE) {
            error("function", "header");
        }
        header();
        body();
    }

    static void header() throws IOException {
        match(Common.Token.VARTYPE, "header");
        match(Common.Token.IDENTIFIER, "header");
        match(Common.Token.LEFT_PARENTHESIS, "header");
        if (peek() != null && peek().token == Common.Token.VARTYPE) arg_decl();
        match(Common.Token.RIGHT_PARENTHESIS, "header");
    }

    static void arg_decl() throws IOException {
        match(Common.Token.VARTYPE, "arg-decl");
        match(Common.Token.IDENTIFIER, "arg-decl");
        while (peek() != null && peek().token == Common.Token.COMMA) {
            match(Common.Token.COMMA, "arg-decl");
            match(Common.Token.VARTYPE, "arg-decl");
            match(Common.Token.IDENTIFIER, "arg-decl");
        }
    }

    static void body() throws IOException {
        match(Common.Token.LEFT_BRACKET, "body");
        if (peek() != null && peek().token != Common.Token.RIGHT_BRACKET) {
            statement_list();
        }
        match(Common.Token.RIGHT_BRACKET, "body");
    }

    static void statement_list() throws IOException {
        statement();
        while (peek() != null && (peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.RETURN_KEYWORD || peek().token == Common.Token.WHILE_KEYWORD)) {
            statement();
        }
    }

    static void statement() throws IOException {
        if (peek().token == Common.Token.WHILE_KEYWORD) {
            while_loop();
        } else if (peek().token == Common.Token.RETURN_KEYWORD) {
            ret();
        } else if (peek().token == Common.Token.IDENTIFIER) {
            assignment();
        } else {
            error("statement", "a valid statement non-terminal");
        }
    }

    static void while_loop() throws IOException {
        match(Common.Token.WHILE_KEYWORD, "while-loop");
        match(Common.Token.LEFT_PARENTHESIS, "while-loop");
        if (peek() == null || (peek().token != Common.Token.IDENTIFIER && peek().token != Common.Token.NUMBER)) {
            error("while-loop", "expression");
        }
        expression("while-loop");
        match(Common.Token.RIGHT_PARENTHESIS, "while-loop");
        body();
    }

    static void ret() throws IOException {
        match(Common.Token.RETURN_KEYWORD, "return");
        if (peek() == null || (peek().token != Common.Token.IDENTIFIER && peek().token != Common.Token.NUMBER)) {
            error("return", "expression");
        }
        expression("return");
        match(Common.Token.EOL, "return");
    }

    static void assignment() throws IOException {
        match(Common.Token.IDENTIFIER, "assignment");
        match(Common.Token.EQUAL, "assignment");
        if (peek() == null || (peek().token != Common.Token.IDENTIFIER && peek().token != Common.Token.NUMBER)) {
            error("assignment", "expression");
        }
        expression("assignment");
        match(Common.Token.EOL, "assignment");
    }

    static void expression(String ruleName) throws IOException {
        if (peek().token == Common.Token.LEFT_PARENTHESIS) {
            match(Common.Token.LEFT_PARENTHESIS, ruleName);
            expression(ruleName);
            match(Common.Token.RIGHT_PARENTHESIS, ruleName);
        } else {
            term(ruleName);
            while (peek() != null && peek().token == Common.Token.BINOP) {
                match(Common.Token.BINOP, ruleName);
                term(ruleName);
            }
        }
    }

    static void term(String ruleName) throws IOException {
        if (peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.NUMBER) {
            match(peek().token, ruleName);
        } else {
            error("term", "IDENTIFIER or NUMBER");
        }
    }
}
