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
            // Handle  errors
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    // error message
    static void error(String msg) throws IOException {
        writer.write(msg + "\n");
        throw new IOException(msg);
    }

    // Peekcurrent token
    static Common.Lex peek() {
        return index < tokens.size() ? tokens.get(index) : null;
    }

    //Match current token with expected token and consume it
    static Common.Lex match(Common.Token expected) throws IOException {
        Common.Lex tok = peek();
        if (tok == null || tok.token != expected) {
            error("Error: In grammar rule, expected token #" + index + " to be " + expected + " but was " + (tok != null ? tok.token : "null"));
        }
        index++;
        return tok;
    }

    // Parse function
    static void function() throws IOException {
        header();
        body();
    }

    // Parse header of a function
    static void header() throws IOException {
        match(Common.Token.VARTYPE);
        match(Common.Token.IDENTIFIER);
        match(Common.Token.LEFT_PARENTHESIS);
        if (peek() != null && peek().token == Common.Token.VARTYPE) arg_decl();
        match(Common.Token.RIGHT_PARENTHESIS);
    }

    // Parse argument declarations in function header
    static void arg_decl() throws IOException {
        match(Common.Token.VARTYPE);
        match(Common.Token.IDENTIFIER);
        while (peek() != null && peek().token == Common.Token.COMMA) {
            match(Common.Token.COMMA);
            match(Common.Token.VARTYPE);
            match(Common.Token.IDENTIFIER);
        }
    }

    // Parse the body 
    static void body() throws IOException {
        match(Common.Token.LEFT_BRACKET);
        if (peek() != null && peek().token != Common.Token.RIGHT_BRACKET) statement_list();
        match(Common.Token.RIGHT_BRACKET);
    }

    // Parse list of statements
    static void statement_list() throws IOException {
        statement();
        while (peek() != null && (peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.RETURN_KEYWORD || peek().token == Common.Token.WHILE_KEYWORD)) {
            statement();
        }
    }

    // Parsesingle statement
    static void statement() throws IOException {
        if (peek().token == Common.Token.WHILE_KEYWORD) while_loop();
        else if (peek().token == Common.Token.RETURN_KEYWORD) ret();
        else if (peek().token == Common.Token.IDENTIFIER) assignment();
        else error("Error: In grammar rule statement, expected a valid statement non-terminal");
    }

    // Parse while-loop statement
    static void while_loop() throws IOException {
        match(Common.Token.WHILE_KEYWORD);
        match(Common.Token.LEFT_PARENTHESIS);
        expression();
        match(Common.Token.RIGHT_PARENTHESIS);
        body();
    }

    // Parse return statement
    static void ret() throws IOException {
        match(Common.Token.RETURN_KEYWORD);
        expression();
        match(Common.Token.EOL);
    }

    // Parse assignment statement
    static void assignment() throws IOException {
        match(Common.Token.IDENTIFIER);
        match(Common.Token.EQUAL);
        expression();
        match(Common.Token.EOL);
    }

    // Parse expression
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

    // Parse  term (identifier or number)
    static void term() throws IOException {
        if (peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.NUMBER) {
            match(peek().token);
        } else {
            error("Error: In grammar rule term, expected IDENTIFIER or NUMBER");
        }
    }
}
