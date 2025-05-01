
public class Common {
    public enum Token {
        LEFT_PARENTHESIS, RIGHT_PARENTHESIS, LEFT_BRACKET, RIGHT_BRACKET,
        WHILE_KEYWORD, RETURN_KEYWORD, EQUAL, COMMA, EOL, VARTYPE,
        IDENTIFIER, BINOP, NUMBER
    }

    public static class Lex {
        public Token token;
        public String lexeme;

        public Lex(Token token, String lexeme) {
            this.token = token;
            this.lexeme = lexeme;
        }
    }

    public static boolean isNumber(String s) {
        return s.matches("[0-9]+");
    }

    public static boolean isIdentifier(String s) {
        return s.matches("[a-zA-Z][a-zA-Z0-9]*");
    }

    public static boolean isVartype(String s) {
        return s.equals("int") || s.equals("void");
    }

    public static boolean isBinop(String s) {
        return s.equals("+") || s.equals("*") || s.equals("!=") || s.equals("==") || s.equals("%");
    }

    public static Token getTokenForLexeme(String s) {
        switch (s) {
            case "(": return Token.LEFT_PARENTHESIS;
            case ")": return Token.RIGHT_PARENTHESIS;
            case "{": return Token.LEFT_BRACKET;
            case "}": return Token.RIGHT_BRACKET;
            case "while": return Token.WHILE_KEYWORD;
            case "return": return Token.RETURN_KEYWORD;
            case "=": return Token.EQUAL;
            case ",": return Token.COMMA;
            case ";": return Token.EOL;
            case "int": case "void": return Token.VARTYPE;
            case "+": case "*": case "!=": case "==": case "%": return Token.BINOP;
            default:
                if (isNumber(s)) return Token.NUMBER;
                if (isIdentifier(s)) return Token.IDENTIFIER;
                throw new IllegalArgumentException("Unknown lexeme: " + s);
        }
    }
}