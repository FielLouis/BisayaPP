package LexicalAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Iskaner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    public Iskaner(String source) {
        this.source = source;
    }

    static {
        keywords = new HashMap<>();
        keywords.put("SUGOD",TokenType.START);
        keywords.put("KATAPUSAN",TokenType.END);
        keywords.put("MUGNA",TokenType.MUGNA);
        keywords.put("NUMERO",TokenType.NUMERO);
        keywords.put("LETRA",TokenType.LETRA);
        keywords.put("TINUOD",TokenType.TINUOD);
        keywords.put("TIPIK",TokenType.TIPIK);
        keywords.put("IPAKITA",TokenType.PRINT);
        keywords.put("DAWAT",TokenType.INPUT);
        keywords.put("KUNG",TokenType.IF);
        keywords.put("KUNG WALA",TokenType.ELSE);
        keywords.put("KUNG DILI",TokenType.ELSE_IF);
        keywords.put("PUNDOK",TokenType.BLOCK);
        keywords.put("ALANG SA",TokenType.FOR);
        keywords.put("SAMTANG", TokenType.WHILE);
        keywords.put("UG",TokenType.AND);
        keywords.put("O",TokenType.OR);
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken(){
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case '{': addToken(TokenType.LBRACE); break;
            case '}': addToken(TokenType.RBRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '?': addToken(TokenType.QUESTION); break;
            case '[': escapecode(); break;
            case '&': addToken(TokenType.CONCAT); break;
            case '$': addToken(TokenType.NEXT_LINE); break;
            case ':': addToken(TokenType.COLON); break;
            case '*': addToken(TokenType.MULTIPLY); break;
            case '%': addToken(TokenType.MODULO); break;
            case '+':
                if (match('+')) {
                    addToken(TokenType.INCREMENT);
                }
                addToken(TokenType.PLUS);
                break;
            case '/':
                addToken(TokenType.DIVIDE);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUALS : TokenType.ASSIGNMENT);
                break;
            case '<':
                if (match('=')) {
                    addToken(TokenType.LESS_EQUAL);
                } else if (match('>')) {
                    addToken(TokenType.NOT_EQUALS);
                } else {
                    addToken(TokenType.LESS_THAN);
                }
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER_THAN);
                break;
            case '-':
                if (match('-')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.MINUS);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            case '\'':
                character();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                  identifier();
                } else {
                    Bisayapreter.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void character() {
        if (isAtEnd()) {
            Bisayapreter.error(line, "Unclosed character literal.");
            return;
        }

        char c = advance(); // Get the character inside the single quote

        // Check for escape characters like '\n'
        if (c == '\\' && !isAtEnd()) {
            c = advance(); // Advance to get the actual escaped character
        }

        if (peek() != '\'') {
            Bisayapreter.error(line, "Unclosed or invalid character literal.");
            return;
        }

        advance();


        if (c == '\0') {
            Bisayapreter.error(line, "Invalid character literal.");
            return;
        }

        addToken(TokenType.CHARACTER, c);
    }

    private void escapecode() {
        int bracketStart = current; // Position after '['

        // Scan until we hit a closing bracket or the end
        while (peek() != ']' && !isAtEnd()) {
            advance();
        }

        if (isAtEnd()) {
            Bisayapreter.error(line, "Unclosed escape code. Expected ']'.");
            return;
        }

        advance(); // Consume closing ']'

        String value = source.substring(bracketStart, current - 1);

        // Special case: '[]]' should output ']'
        if (value.isEmpty() && peek() == ']') {
            advance(); // consume the second ']'
            addToken(TokenType.ESCAPE_CODE, "]");
            return;
        }

        if (value.isEmpty()) {
            Bisayapreter.error(line, "Empty brackets [] are not allowed.");
            return;
        }

        addToken(TokenType.ESCAPE_CODE, value); // or whatever token type you want
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        if (text.equals("KUNG")) {
            skipWhitespace(); // allow spaces after KUNG

            int saveCurrent = current;
            int saveStart = start;

            if (matchWord("WALA")) {
                addToken(TokenType.ELSE); //  Treat "KUNG WALA" as ELSE
                return;
            }

            else if (matchWord("DILI")) {
                addToken(TokenType.ELSE_IF); //  Treat "KUNG DILI" as ELSE_IF
                return;
            } else {
                current = saveCurrent; // rollback if not matched
                start = saveStart;
            }

            type = TokenType.IF; // fallback: treat as IF
        }

        if(text.equals("ALANG")){
            skipWhitespace();

            int saveCurrent = current;
            int saveStart = start;

            if (matchWord("SA")) {
                addToken(TokenType.FOR);
                return;
            }
        }

        if(text.equals("SAMTANG")) {

        }

        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private void skipWhitespace() {
        while (!isAtEnd() && (peek() == ' ' || peek() == '\t' || peek() == '\r' || peek() == '\n')) {
            if (peek() == '\n') line++;
            advance();
        }
    }

    private boolean matchWord(String expected) {
        int length = expected.length();
        if (current + length > source.length()) return false;

        String next = source.substring(current, current + length);

        if (next.equals(expected) && isBoundary(current + length)) {
            current += length;
            return true;
        }

        return false;
    }

    // Check that the next character is a boundary (end or not alphanumeric)
    private boolean isBoundary(int index) {
        return index >= source.length() || !isAlphaNumeric(source.charAt(index));
    }

    private void number() {
        while(isDigit(peek())) advance();

        // Look for a fractional part.
        if(peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while(isDigit(peek())) advance();
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) {
            Bisayapreter.error(line, "Unterminated string.");
            return;
        }

        // The closing "
        advance();

        // Trim the surrounding quotes
        String value = source.substring(start + 1, current - 1);

        if (value.equals("OO")) {
            addToken(TokenType.BOOL_TRUE, true);
            return;
        } else if(value.equals("DILI")){
            addToken(TokenType.BOOL_FALSE, false);
            return;
        }

        addToken(TokenType.STRING, value);
    }

    private boolean match(char expected) {
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
