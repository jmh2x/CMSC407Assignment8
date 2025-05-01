public class Common {
    public enum TokenType {
        LEFT_PARENTHESIS, RIGHT_PARENTHESIS,
        LEFT_BRACKET, RIGHT_BRACKET,
        WHILE_KEYWORD, RETURN_KEYWORD,
        EQUAL, COMMA, EOL,
        VARTYPE, IDENTIFIER, BINOP, NUMBER
    }

    public static class Lex {
        public TokenType token;
        public String lexeme;

        public Lex(TokenType token, String lexeme) {
            this.token = token;
            this.lexeme = lexeme;
        }

        @Override
        public String toString() {
            return token + " " + lexeme;
        }
    }
}
