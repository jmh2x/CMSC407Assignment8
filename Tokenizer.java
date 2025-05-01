// Govan Henry CMSC 304 Assignment 8
import java.io.*;
import java.util.*;

public class Tokenizer {

    public static void main(String[] args) {
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
            while ((line = reader.readLine()) != null) {
                tokens.addAll(tokenizeLine(line)); // tokenize each line
            }

            for (Common.Lex l : tokens) {
                writer.write(l.token + " " + l.lexeme + "\n"); // write tokens to output file
            }

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage()); // handle file errors
        }
    }

    private static List<Common.Lex> tokenizeLine(String line) {
        List<Common.Lex> result = new ArrayList<>();
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);

            if (Character.isWhitespace(c)) { // skip whitespace
                i++;
                continue;
            }

            // multi-char symbols like binary operators
            if (i + 1 < line.length()) {
                String two = line.substring(i, i + 2);
                if (Common.isBinop(two)) {
                    result.add(new Common.Lex(Common.getTokenForLexeme(two), two)); // add binary operator token
                    i += 2;
                    continue;
                }
            }

            // single-char symbols like parentheses or operators
            if ("(){}=,;+*%".indexOf(c) != -1) {
                result.add(new Common.Lex(Common.getTokenForLexeme(Character.toString(c)), Character.toString(c))); // add single-char token
                i++;
                continue;
            }

            // identifiers or numbers
            StringBuilder sb = new StringBuilder();
            while (i < line.length() &&
                   (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) {
                sb.append(line.charAt(i++)); // build identifier or number
            }

            String lexeme = sb.toString();
            result.add(new Common.Lex(Common.getTokenForLexeme(lexeme), lexeme)); // add identifier or number token
        }

        return result; // return list of tokens
    }
}