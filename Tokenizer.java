// Govan Henry CMSC 304 Assignment 8
import java.io.*;
import java.util.*;

public class Tokenizer {

    public static void main(String[] args) {
        //program is run with two arguments: input and output file paths
        if (args.length < 2) {
            System.err.println("Usage: java Tokenizer <inputFile> <outputFile>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            List<Common.Lex> tokens = new ArrayList<>();
            
            // Read input file line by line and tokenize each line
            while ((line = reader.readLine()) != null) {
                tokens.addAll(tokenizeLine(line));
            }

            // Write the tokens and their lexemes 
            for (Common.Lex l : tokens) {
                writer.write(l.token + " " + l.lexeme + "\n");
            }

        } catch (IOException e) {
            //error handling
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    /
    private static List<Common.Lex> tokenizeLine(String line) {
        List<Common.Lex> result = new ArrayList<>();
        int i = 0;

        // Process each character in the line
        while (i < line.length()) {
            char c = line.charAt(i);

            // Skip whitespace characters
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Handle multi-character symbols (e.g., "==" or "!=")
            if (i + 1 < line.length()) {
                String two = line.substring(i, i + 2);
                if (Common.isBinop(two)) {
                    result.add(new Common.Lex(Common.getTokenForLexeme(two), two));
                    i += 2;
                    continue;
                }
            }

            // Handle single-character symbols (e.g., "(", ")", "{", "}")
            if ("(){}=,;+*%".indexOf(c) != -1) {
                result.add(new Common.Lex(Common.getTokenForLexeme(Character.toString(c)), Character.toString(c)));
                i++;
                continue;
            }

            // Handle identifiers or numbers
            StringBuilder sb = new StringBuilder();
            while (i < line.length() &&
                   (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) {
                sb.append(line.charAt(i++));
            }

            // Add the identifier or number as a token
            String lexeme = sb.toString();
            result.add(new Common.Lex(Common.getTokenForLexeme(lexeme), lexeme));
        }

        return result;
    }
}