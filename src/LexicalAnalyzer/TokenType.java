package LexicalAnalyzer;

public enum TokenType {
    // Program
    SUGOD, KATAPUSAN,

    // Data Types
    NUMERO, LETRA, TINUOD, TIPIK,
    // Keywords
    MUGNA, IPAKITA, PRINT, INPUT,

    // Literals
    NUMBER, CHARACTER, STRING, FLOAT, IDENTIFIER,

    // Arithmetic Operators
    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,
    GREATER_THAN, LESS_THAN, GREATER_EQUAL, LESS_EQUAL, EQUALS, NOT_EQUALS,
    INCREMENT, DECREMENT,
    // Logical Operators
    AND, OR, NOT,

    // Boolean literals
    BOOL_TRUE, // "OO"
    BOOL_FALSE, // "DILI"
    NULL,

    //Control Flow
    IF, ELSE_IF, ELSE , BLOCK, FOR, WHILE,

    // Delimiters and Symbols
    LPAREN, RPAREN,
    ASSIGNMENT, COLON, COMMA,
    LBRACE, RBRACE, CONCAT, NEXT_LINE, ESCAPE_CODE, QUESTION,

    // Location
    EOF,
    START,END
}