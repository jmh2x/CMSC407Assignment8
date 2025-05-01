import java.io.*;
import java.util.*;

public class Recognizer {
    static List<Common.Lex> tokens = new ArrayList<>();
    static int current = 0;
    static BufferedWriter writer;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java Recognizer <inputFile> <outputFile>");
            System.exit(0);
        }

        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" ", 2);
            tokens.add(new Common.Lex(Common.TokenType.valueOf(parts[0]), parts[1]));
        }
        reader.close();

        writer = new BufferedWriter(new FileWriter(args[1]));
        try {
            function();
            if (current < tokens.size()) {
                error("Only consumed " + current + " of the " + tokens.size() + " given tokens");
            }
            writer.write("PARSED!!!\n");
        } catch (Exception e) {
            // already handled in error()
        } finally {
            writer.close();
        }
    }

    static void function() {
        header();
        body();
    }

    static void header() {
        expect("header", Common.TokenType.VARTYPE);
        expect("header", Common.TokenType.IDENTIFIER);
        expect("header", Common.TokenType.LEFT_PARENTHESIS);
        if (match(Common.TokenType.VARTYPE)) {
            arg_decl();
        }
        expect("header", Common.TokenType.RIGHT_PARENTHESIS);
    }

    static void arg_decl() {
        expect("arg_decl", Common.TokenType.VARTYPE);
        expect("arg_decl", Common.TokenType.IDENTIFIER);
        while (match(Common.TokenType.COMMA)) {
            expect("arg_decl", Common.TokenType.VARTYPE);
            expect("arg_decl", Common.TokenType.IDENTIFIER);
        }
    }

    static void body() {
        expect("body", Common.TokenType.LEFT_BRACKET);
        if (!check(Common.TokenType.RIGHT_BRACKET)) {
            statement_list();
        }
        expect("body", Common.TokenType.RIGHT_BRACKET);
    }

    static void statement_list() {
        do {
            statement();
        } while (!check(Common.TokenType.RIGHT_BRACKET));
    }

    static void statement() {
        if (match(Common.TokenType.WHILE_KEYWORD)) {
            expect("while-loop", Common.TokenType.LEFT_PARENTHESIS);
            expression();
            expect("while-loop", Common.TokenType.RIGHT_PARENTHESIS);
            body();
        } else if (match(Common.TokenType.RETURN_KEYWORD)) {
            expression();
            expect("return", Common.TokenType.EOL);
        } else if (match(Common.TokenType.IDENTIFIER)) {
            expect("assignment", Common.TokenType.EQUAL);
            expression();
            expect("assignment", Common.TokenType.EOL);
        } else {
            error("In grammar rule statement, expected a valid statement");
        }
    }

    static void expression() {
        if (match(Common.TokenType.LEFT_PARENTHESIS)) {
            expression();
            expect("expression", Common.TokenType.RIGHT_PARENTHESIS);
        } else {
            term();
            while (match(Common.TokenType.BINOP)) {
                term();
            }
        }
    }

    static void term() {
        if (!(match(Common.TokenType.IDENTIFIER) || match(Common.TokenType.NUMBER))) {
            error("In grammar rule term, expected IDENTIFIER or NUMBER");
        }
    }

    static boolean match(Common.TokenType type) {
        if (check(type)) {
            current++;
            return true;
        }
        return false;
    }

    static boolean check(Common.TokenType type) {
        return current < tokens.size() && tokens.get(current).token == type;
    }

    static void expect(String rule, Common.TokenType expected) {
        if (!check(expected)) {
            String actual = current < tokens.size() ? tokens.get(current).token.toString() : "EOF";
            error("Error: In grammar rule " + rule + ", expected token #" + current + " to be " + expected + " but was " + actual);
        }
        current++;
    }

    static void error(String msg) {
        try {
            writer.write(msg + "\n");
        } catch (IOException e) {
            System.err.println("Failed to write error");
        }
        System.exit(0);
    }
}
