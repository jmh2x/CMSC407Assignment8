
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

    static Common.Lex match(Common.Token expected) throws IOException {
        Common.Lex tok = peek();
        if (tok == null || tok.token != expected) {
            error("Error: In grammar rule, expected token #" + index + " to be " + expected + " but was " + (tok != null ? tok.token : "null"));
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
        match(Common.Token.VARTYPE);
        match(Common.Token.IDENTIFIER);
        match(Common.Token.LEFT_PARENTHESIS);
        if (peek() != null && peek().token == Common.Token.VARTYPE) {
            arg_decl();
        }
        match(Common.Token.RIGHT_PARENTHESIS);
    }

    static void arg_decl() throws IOException {
        match(Common.Token.VARTYPE);
        match(Common.Token.IDENTIFIER);
        while (peek() != null && peek().token == Common.Token.COMMA) {
            match(Common.Token.COMMA);
            match(Common.Token.VARTYPE);
            match(Common.Token.IDENTIFIER);
        }
    }

    static void body() throws IOException {
        match(Common.Token.LEFT_BRACKET);
        if (peek() != null && peek().token != Common.Token.RIGHT_BRACKET) {
            statement_list();
        }
        if (peek() == null || peek().token != Common.Token.RIGHT_BRACKET) {
            error("Error: In grammar rule body, expected token #" + index + " to be RIGHT_BRACKET but was " + (peek() != null ? peek().token : "null"));
        }
        match(Common.Token.RIGHT_BRACKET);
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
        if (peek() == null) {
            error("Error: In grammar rule statement, expected a valid statement non-terminal");
        }
        if (peek().token == Common.Token.WHILE_KEYWORD) while_loop();
        else if (peek().token == Common.Token.RETURN_KEYWORD) ret();
        else if (peek().token == Common.Token.IDENTIFIER) assignment();
        else error("Error: In grammar rule statement, expected a valid statement non-terminal");
    }

    static void while_loop() throws IOException {
        match(Common.Token.WHILE_KEYWORD);
        match(Common.Token.LEFT_PARENTHESIS);
        if (peek() == null || !(peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.NUMBER || peek().token == Common.Token.LEFT_PARENTHESIS)) {
            error("Error: In grammar rule while-loop, expected a valid expression non-terminal to be present but was not");
        }
        expression();
        match(Common.Token.RIGHT_PARENTHESIS);
        body();
    }

    static void ret() throws IOException {
        match(Common.Token.RETURN_KEYWORD);
        if (peek() == null || !(peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.NUMBER || peek().token == Common.Token.LEFT_PARENTHESIS)) {
            error("Error: In grammar rule return, expected a valid expression non-terminal to be present but was not");
        }
        expression();
        if (peek() == null || peek().token != Common.Token.EOL) {
            error("Error: In grammar rule return, expected token #" + index + " to be EOL but was " + (peek() != null ? peek().token : "null"));
        }
        match(Common.Token.EOL);
    }

    static void assignment() throws IOException {
        match(Common.Token.IDENTIFIER);
        match(Common.Token.EQUAL);
        if (peek() == null || !(peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.NUMBER || peek().token == Common.Token.LEFT_PARENTHESIS)) {
            error("Error: In grammar rule assignment, expected a valid expression non-terminal to be present but was not");
        }
        expression();
        if (peek() == null || peek().token != Common.Token.EOL) {
            error("Error: In grammar rule assignment, expected token #" + index + " to be EOL but was " + (peek() != null ? peek().token : "null"));
        }
        match(Common.Token.EOL);
    }

    static void expression() throws IOException {
        if (peek() != null && peek().token == Common.Token.LEFT_PARENTHESIS) {
            match(Common.Token.LEFT_PARENTHESIS);
            expression();
            match(Common.Token.RIGHT_PARENTHESIS);
        } else {
            term();
            while (peek() != null && peek().token == Common.Token.BINOP) {
                match(Common.Token.BINOP);
                term();
            }
        }
    }

    static void term() throws IOException {
        if (peek() == null || !(peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.NUMBER)) {
            error("Error: In grammar rule term, expected IDENTIFIER or NUMBER");
        }
        match(peek().token);
    }
}

