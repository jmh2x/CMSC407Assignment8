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
                tokens.addAll(tokenizeLine(line));
            }

            for (Common.Lex l : tokens) {
                writer.write(l.token + " " + l.lexeme + "\n");
            }

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    private static List<Common.Lex> tokenizeLine(String line) {
        List<Common.Lex> result = new ArrayList<>();
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Multi-char symbols
            if (i + 1 < line.length()) {
                String two = line.substring(i, i + 2);
                if (Common.isBinop(two)) {
                    result.add(new Common.Lex(Common.getTokenForLexeme(two), two));
                    i += 2;
                    continue;
                }
            }

            // Single-char symbols
            if ("(){}=,;+*%".indexOf(c) != -1) {
                result.add(new Common.Lex(Common.getTokenForLexeme(Character.toString(c)), Character.toString(c)));
                i++;
                continue;
            }

            // Identifier or number
            StringBuilder sb = new StringBuilder();
            while (i < line.length() &&
                   (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) {
                sb.append(line.charAt(i++));
            }

            String lexeme = sb.toString();
            result.add(new Common.Lex(Common.getTokenForLexeme(lexeme), lexeme));
        }

        return result;
    }
}
