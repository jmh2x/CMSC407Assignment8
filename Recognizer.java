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
                    writer.write("Error: Only consumed " + index + " of the " + tokens.size() + " given tokens\n");
                } else {
                    writer.write("PARSED!!!\n");
                }
            } catch (Exception e) {
                writer.write("Error: " + e.getMessage() + "\n");
            }

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    static void error(String msg) throws IOException {
        writer.write(msg + "\n");
        throw new IOException(msg);
    }

    static Common.Lex peek() {
        return index < tokens.size() ? tokens.get(index) : null;
    }

    static Common.Lex match(Common.Token expected, String ruleName) throws IOException {
        Common.Lex tok = peek();
        if (tok == null || tok.token != expected) {
            error("Error: In grammar rule " + ruleName + ", expected token #" + index + " to be " + expected + " but was " + (tok != null ? tok.token : "null"));
        }
        index++;
        return tok;
    }

    static void function() throws IOException {
        if (peek() == null || peek().token != Common.Token.VARTYPE) {
            error("Error: In grammar rule function, expected a valid header non-terminal to be present but was not");
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
        match(Common.Token.VARTYPE, "header");
        match(Common.Token.IDENTIFIER, "header");
        while (peek() != null && peek().token == Common.Token.COMMA) {
            match(Common.Token.COMMA, "header");
            match(Common.Token.VARTYPE, "header");
            match(Common.Token.IDENTIFIER, "header");
        }
    }

    static void body() throws IOException {
        match(Common.Token.LEFT_BRACKET, "body");
        if (peek() != null && peek().token != Common.Token.RIGHT_BRACKET) statement_list();
        match(Common.Token.RIGHT_BRACKET, "body");
    }

    static void statement_list() throws IOException {
        statement();
        while (peek() != null && (
                peek().token == Common.Token.IDENTIFIER ||
                peek().token == Common.Token.RETURN_KEYWORD ||
                peek().token == Common.Token.WHILE_KEYWORD)) {
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
            error("Error: In grammar rule statement, expected a valid statement non-terminal");
        }
    }

    static void while_loop() throws IOException {
        match(Common.Token.WHILE_KEYWORD, "while-loop");
        match(Common.Token.LEFT_PARENTHESIS, "while-loop");
        if (peek() == null || (
            peek().token != Common.Token.IDENTIFIER &&
            peek().token != Common.Token.NUMBER &&
            peek().token != Common.Token.LEFT_PARENTHESIS)) {
            error("Error: In grammar rule while-loop, expected a valid expression non-terminal to be present but was not");
        }
        expression("while-loop");
        match(Common.Token.RIGHT_PARENTHESIS, "while-loop");
        body();
    }

    static void ret() throws IOException {
        match(Common.Token.RETURN_KEYWORD, "return");
        if (peek() == null || (
            peek().token != Common.Token.IDENTIFIER &&
            peek().token != Common.Token.NUMBER &&
            peek().token != Common.Token.LEFT_PARENTHESIS)) {
            error("Error: In grammar rule return, expected a valid expression non-terminal to be present but was not");
        }
        expression("return");
        match(Common.Token.EOL, "return");
    }

    static void assignment() throws IOException {
        match(Common.Token.IDENTIFIER, "assignment");
        match(Common.Token.EQUAL, "assignment");
        expression("assignment");
        match(Common.Token.EOL, "assignment");
    }

    static void expression(String ruleName) throws IOException {
        if (peek().token == Common.Token.LEFT_PARENTHESIS) {
            match(Common.Token.LEFT_PARENTHESIS, ruleName);
            expression(ruleName);
            match(Common.Token.RIGHT_PARENTHESIS, ruleName);
        } else {
            term();
            while (peek() != null && peek().token == Common.Token.BINOP) {
                match(Common.Token.BINOP, ruleName);
                term();
            }
        }
    }

    static void term() throws IOException {
        if (peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.NUMBER) {
            match(peek().token, "term");
        } else {
            error("Error: In grammar rule term, expected IDENTIFIER or NUMBER");
        }
    }
}


