import java.io.*;
import java.util.*;

public class Tokenizer {
    private static final Set<String> keywords = new HashSet<>(Arrays.asList("while", "return", "int", "void"));
    private static final Set<String> binops = new HashSet<>(Arrays.asList("+", "*", "!=", "==", "%"));

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java Tokenizer <inputFile> <outputFile>");
            System.exit(0);
        }

        List<Common.Lex> lexemes = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String line;
        while ((line = reader.readLine()) != null) {
            lexemes.addAll(tokenize(line));
        }
        reader.close();

        BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
        for (Common.Lex lex : lexemes) {
            writer.write(lex.toString());
            writer.newLine();
        }
        writer.close();
    }

    private static List<Common.Lex> tokenize(String line) {
        List<Common.Lex> tokens = new ArrayList<>();
        int i = 0;
        while (i < line.length()) {
            char ch = line.charAt(i);

            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }

            if (Character.isLetter(ch)) {
                int start = i;
                while (i < line.length() && Character.isLetterOrDigit(line.charAt(i))) i++;
                String word = line.substring(start, i);
                tokens.add(new Common.Lex(getTokenType(word), word));
                continue;
            }

            if (Character.isDigit(ch)) {
                int start = i;
                while (i < line.length() && Character.isDigit(line.charAt(i))) i++;
                tokens.add(new Common.Lex(Common.TokenType.NUMBER, line.substring(start, i)));
                continue;
            }

            if (i + 1 < line.length()) {
                String twoChar = line.substring(i, i + 2);
                if (binops.contains(twoChar)) {
                    tokens.add(new Common.Lex(Common.TokenType.BINOP, twoChar));
                    i += 2;
                    continue;
                }
            }

            String oneChar = String.valueOf(ch);
            switch (ch) {
                case '(': tokens.add(new Common.Lex(Common.TokenType.LEFT_PARENTHESIS, oneChar)); break;
                case ')': tokens.add(new Common.Lex(Common.TokenType.RIGHT_PARENTHESIS, oneChar)); break;
                case '{': tokens.add(new Common.Lex(Common.TokenType.LEFT_BRACKET, oneChar)); break;
                case '}': tokens.add(new Common.Lex(Common.TokenType.RIGHT_BRACKET, oneChar)); break;
                case '=': tokens.add(new Common.Lex(Common.TokenType.EQUAL, oneChar)); break;
                case ',': tokens.add(new Common.Lex(Common.TokenType.COMMA, oneChar)); break;
                case ';': tokens.add(new Common.Lex(Common.TokenType.EOL, oneChar)); break;
                case '+': case '*': case '%':
                    tokens.add(new Common.Lex(Common.TokenType.BINOP, oneChar)); break;
                default: System.err.println("Unknown character: " + ch); System.exit(0);
            }
            i++;
        }
        return tokens;
    }

    private static Common.TokenType getTokenType(String word) {
        switch (word) {
            case "while": return Common.TokenType.WHILE_KEYWORD;
            case "return": return Common.TokenType.RETURN_KEYWORD;
            case "int": case "void": return Common.TokenType.VARTYPE;
            default: return Common.TokenType.IDENTIFIER;
        }
    }
}
