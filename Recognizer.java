// Govan Henry CMSC 304 Assignment 8
import java.io.*;
import java.util.*;

public class Recognizer {
    static List<Common.Lex> tokens; // List of tokens to parse
    static int index = 0; // Current index in the token list
    static BufferedWriter writer; // Writer for output file

    public static void main(String[] args) {
        // Ensure the program is run with two arguments: input and output file paths
        if (args.length < 2) {
            System.err.println("Usage: java Recognizer <inputFile> <outputFile>");
            return;
        }

        // Use try-with-resources to handle file I/O safely
        try (BufferedReader reader = new BufferedReader(new FileReader(args[0]));
             BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]))) {

            Recognizer.writer = writer;
            tokens = new ArrayList<>();
            String line;

            // Read the input file line by line and parse tokens
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 2);
                tokens.add(new Common.Lex(Common.Token.valueOf(parts[0]), parts[1]));
            }

            try {
                // Start parsing with the top-level grammar rule
                function();
                // Check if all tokens were consumed
                if (index != tokens.size()) {
                    writer.write("Error: Only consumed " + index + " of the " + tokens.size() + " given tokens\n");
                } else {
                    writer.write("PARSED!!!\n");
                }
            } catch (Exception e) {
                // Write any parsing errors to the output file
                writer.write("Error: " + e.getMessage() + "\n");
            }

        } catch (IOException e) {
            // Handle file I/O errors
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    // Write an error message and throw an exception to halt parsing
    static void error(String msg) throws IOException {
        writer.write(msg + "\n");
        throw new IOException(msg);
    }

    // Peek at the current token without consuming it
    static Common.Lex peek() {
        return index < tokens.size() ? tokens.get(index) : null;
    }

    // Match the current token with the expected token and consume it
    static Common.Lex match(Common.Token expected) throws IOException {
        Common.Lex tok = peek();
        if (tok == null || tok.token != expected) {
            error("Error: In grammar rule, expected token #" + index + " to be " + expected + " but was " + (tok != null ? tok.token : "null"));
        }
        index++;
        return tok;
    }

    // Parse the top-level grammar rule: function
    static void function() throws IOException {
        header();
        body();
    }

    // Parse the header of a function
    static void header() throws IOException {
        match(Common.Token.VARTYPE);
        match(Common.Token.IDENTIFIER);
        match(Common.Token.LEFT_PARENTHESIS);
        if (peek() != null && peek().token == Common.Token.VARTYPE) arg_decl();
        match(Common.Token.RIGHT_PARENTHESIS);
    }

    // Parse argument declarations in a function header
    static void arg_decl() throws IOException {
        match(Common.Token.VARTYPE);
        match(Common.Token.IDENTIFIER);
        while (peek() != null && peek().token == Common.Token.COMMA) {
            match(Common.Token.COMMA);
            match(Common.Token.VARTYPE);
            match(Common.Token.IDENTIFIER);
        }
    }

    // Parse the body of a function
    static void body() throws IOException {
        match(Common.Token.LEFT_BRACKET);
        if (peek() != null && peek().token != Common.Token.RIGHT_BRACKET) statement_list();
        match(Common.Token.RIGHT_BRACKET);
    }

    // Parse a list of statements
    static void statement_list() throws IOException {
        statement();
        while (peek() != null && (peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.RETURN_KEYWORD || peek().token == Common.Token.WHILE_KEYWORD)) {
            statement();
        }
    }

    // Parse a single statement
    static void statement() throws IOException {
        if (peek().token == Common.Token.WHILE_KEYWORD) while_loop();
        else if (peek().token == Common.Token.RETURN_KEYWORD) ret();
        else if (peek().token == Common.Token.IDENTIFIER) assignment();
        else error("Error: In grammar rule statement, expected a valid statement non-terminal");
    }

    // Parse a while-loop statement
    static void while_loop() throws IOException {
        match(Common.Token.WHILE_KEYWORD);
        match(Common.Token.LEFT_PARENTHESIS);
        expression();
        match(Common.Token.RIGHT_PARENTHESIS);
        body();
    }

    // Parse a return statement
    static void ret() throws IOException {
        match(Common.Token.RETURN_KEYWORD);
        expression();
        match(Common.Token.EOL);
    }

    // Parse an assignment statement
    static void assignment() throws IOException {
        match(Common.Token.IDENTIFIER);
        match(Common.Token.EQUAL);
        expression();
        match(Common.Token.EOL);
    }

    // Parse an expression
    static void expression() throws IOException {
        if (peek().token == Common.Token.LEFT_PARENTHESIS) {
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

    // Parse a term (identifier or number)
    static void term() throws IOException {
        if (peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.NUMBER) {
            match(peek().token);
        } else {
            error("Error: In grammar rule term, expected IDENTIFIER or NUMBER");
        }
    }
}
