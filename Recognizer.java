// Govan Henry CMSC 304 Assignment 8
import java.io.*;
import java.util.*;

public class Recognizer {
    static List<Common.Lex> tokens; // list of tokens
    static int index = 0; // current token index
    static BufferedWriter writer; // output writer

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
                tokens.add(new Common.Lex(Common.Token.valueOf(parts[0]), parts[1])); // parse tokens from input
            }

            try {
                function(); // start parsing
                if (index != tokens.size()) {
                    writer.write("Error: Only consumed " + index + " of the " + tokens.size() + " given tokens\n");
                } else {
                    writer.write("PARSED!!!\n"); // successful parse
                }
            } catch (Exception e) {
                writer.write("Error: " + e.getMessage() + "\n"); // handle parsing errors
            }

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage()); // handle file errors
        }
    }

    static void error(String msg) throws IOException {
        writer.write(msg + "\n"); // write error message
        throw new IOException(msg); // throw exception
    }

    static Common.Lex peek() {
        return index < tokens.size() ? tokens.get(index) : null; // get current token
    }

    static Common.Lex match(Common.Token expected) throws IOException {
        Common.Lex tok = peek();
        if (tok == null || tok.token != expected) {
            error("Error: In grammar rule, expected token #" + index + " to be " + expected + " but was " + (tok != null ? tok.token : "null"));
        }
        index++; // move to next token
        return tok;
    }

    static void function() throws IOException {
        header(); // parse function header
        body(); // parse function body
    }

    static void header() throws IOException {
        match(Common.Token.VARTYPE); // match variable type
        match(Common.Token.IDENTIFIER); // match function name
        match(Common.Token.LEFT_PARENTHESIS); // match opening parenthesis
        if (peek() != null && peek().token == Common.Token.VARTYPE) arg_decl(); // parse arguments if present
        match(Common.Token.RIGHT_PARENTHESIS); // match closing parenthesis
    }

    static void arg_decl() throws IOException {
        match(Common.Token.VARTYPE); // match argument type
        match(Common.Token.IDENTIFIER); // match argument name
        while (peek() != null && peek().token == Common.Token.COMMA) {
            match(Common.Token.COMMA); // match comma
            match(Common.Token.VARTYPE); // match next argument type
            match(Common.Token.IDENTIFIER); // match next argument name
        }
    }

    static void body() throws IOException {
        match(Common.Token.LEFT_BRACKET); // match opening bracket
        if (peek() != null && peek().token != Common.Token.RIGHT_BRACKET) statement_list(); // parse statements if present
        match(Common.Token.RIGHT_BRACKET); // match closing bracket
    }

    static void statement_list() throws IOException {
        statement(); // parse first statement
        while (peek() != null && (peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.RETURN_KEYWORD || peek().token == Common.Token.WHILE_KEYWORD)) {
            statement(); // parse additional statements
        }
    }

    static void statement() throws IOException {
        if (peek().token == Common.Token.WHILE_KEYWORD) while_loop(); // parse while loop
        else if (peek().token == Common.Token.RETURN_KEYWORD) ret(); // parse return statement
        else if (peek().token == Common.Token.IDENTIFIER) assignment(); // parse assignment
        else error("Error: In grammar rule statement, expected a valid statement non-terminal");
    }

    static void while_loop() throws IOException {
        match(Common.Token.WHILE_KEYWORD); // match while keyword
        match(Common.Token.LEFT_PARENTHESIS); // match opening parenthesis
        expression(); // parse condition
        match(Common.Token.RIGHT_PARENTHESIS); // match closing parenthesis
        body(); // parse loop body
    }

    static void ret() throws IOException {
        match(Common.Token.RETURN_KEYWORD); // match return keyword
        expression(); // parse return value
        match(Common.Token.EOL); // match end of line
    }

    static void assignment() throws IOException {
        match(Common.Token.IDENTIFIER); // match variable name
        match(Common.Token.EQUAL); // match equals sign
        expression(); // parse assigned value
        match(Common.Token.EOL); // match end of line
    }

    static void expression() throws IOException {
        if (peek().token == Common.Token.LEFT_PARENTHESIS) {
            match(Common.Token.LEFT_PARENTHESIS); // match opening parenthesis
            expression(); // parse nested expression
            match(Common.Token.RIGHT_PARENTHESIS); // match closing parenthesis
        } else {
            term(); // parse term
            while (peek() != null && peek().token == Common.Token.BINOP) {
                match(Common.Token.BINOP); // match binary operator
                term(); // parse next term
            }
        }
    }

    static void term() throws IOException {
        if (peek().token == Common.Token.IDENTIFIER || peek().token == Common.Token.NUMBER) {
            match(peek().token); // match identifier or number
        } else {
            error("Error: In grammar rule term, expected IDENTIFIER or NUMBER");
        }
    }
}