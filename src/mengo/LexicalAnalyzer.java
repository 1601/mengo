/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mengo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

/**
 *
 * @author lenovo G40-70
 */
public class LexicalAnalyzer {

    private static PushbackInputStream buffRead;
    private char c;
    private static int EOF = 65535;
    private static int lookahead;

    public LexicalAnalyzer(String inFile) {
        lookahead = 10;
        try {
            buffRead = new PushbackInputStream(new FileInputStream(inFile), lookahead);
        } catch (Exception e) {
            System.out.println(e);
        }
        c = read();
    }

    private char read() {
        try {
            return (char) (buffRead.read());
        } catch (IOException e) {
            e.printStackTrace();
            return (char) -2;
        }
    }

    private void unread(char c) {
        try {
            buffRead.unread(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Token nextToken() {

        int state = 1; // Initial state
        String lexemeBuffer = "";
        while (true) {
//            System.out.println(state + ": " + c);
            switch (state) {
                case 1:                                                      // SPECIAL SYMBOLS
                    switch (c) {
                        case ' ':
                            c = read();
                            System.out.print(" ");
                            continue;
                        case '\r':
                            c = read();
                            System.out.print("\r");
                            continue;
                        case '\b':
                            c = read();
                            System.out.print("\b");
                            continue;
                        case '\f':
                            c = read();
                            System.out.print("\f");
                            continue;
                        case '\t':
                            c = read();
                            System.out.print("\t");
                            continue;
                        case '\n':
                            c = read();
                            System.out.print("\n");
                            continue;
                        case 255:                                           //non braking space
                            c = read();
                            continue;
                        case '+':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("ADD", lexemeBuffer);
                        case '-':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("SUB", lexemeBuffer);
                        case '*':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("MUL", lexemeBuffer);
                        case '/':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("DIV", lexemeBuffer);
                        case '%':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("MOD", lexemeBuffer);
                        case '<':
                            lexemeBuffer += c + "";
                            c = read();
                            state = 2;
                            continue;
                        case '>':
                            lexemeBuffer += c + "";
                            c = read();
                            state = 3;
                            continue;
                        case ',':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("COMMA", lexemeBuffer);
                        case '(':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("LPAREN", lexemeBuffer);
                        case ')':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("RPAREN", lexemeBuffer);
                        case '&':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("AND", lexemeBuffer);
                        case '|':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("OR", lexemeBuffer);
                        case '~':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("NOT", lexemeBuffer);
                        default:
                            state = 4;
                            continue;
                    }
                case 2:                                                         // < or <=

                    switch (c) {
                        case '=':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("LTE", lexemeBuffer);
                        default:
                            return new Token("LT", "<");
                    }

                case 3:                                                         // > or >=
                    switch (c) {
                        case '=':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("GTE", lexemeBuffer);
                        default:
                            return new Token("GT", ">");
                    }

                case 4:                                                         // START OF COMMENTS or PERIOD DELIMITER
                    switch (c) {
                        case '.':
                            lexemeBuffer += c + "";
                            c = read();
                            state = 5;
                            continue;
                        default:
                            state = 13;
                            continue;
                    }
                case 5:                                                         // CAN BE COMMENT OR PERIOD DELIMITER
                    switch (c) {
                        case '.':
                            lexemeBuffer += c + "";
                            c = read();
                            state = 7;
                            continue;
                        default:
                            unread(c);
                            c = read();
                            return new Token("PERIOD", ".");

                    }
                case 7:
                    switch (c) {
                        case '.':
                            lexemeBuffer += c + "";
                            c = read();
                            state = 8;
                            continue;
                        default:
                            return new Token("STXERR" + "", "Syntax Error! Expecting another \".\" in creating a comment");
                    }
                case 8:
                    switch (c) {
                        case '<':
                            lexemeBuffer += c + "";
                            c = read();
                            state = 9;
                            continue;
                        default:
                            return new Token("STXERR" + "", "Syntax Error! Expecting \"<\" in creating a comment");
                    }
                case 9:
                    switch (c) {
                        case '>':
                            lexemeBuffer += c + "";
                            c = read();
                            state = 10;
                            continue;
                        default:
                            lexemeBuffer += c + "";
                            c = read();
                            continue;
                    }
                case 10:
                    switch (c) {
                        case '.':
                            lexemeBuffer += c + "";
                            c = read();
                            state = 11;
                            continue;
                        default:
                            state = 9;
                            continue;
                    }
                case 11:
                    switch (c) {
                        case '.':
                            lexemeBuffer += c + "";
                            c = read();
                            state = 12;
                            continue;
                        default:
                            state = 9;
                    }
                case 12:                                                        // END OF COMMENT
                    switch (c) {
                        case '.':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("COMMENT", lexemeBuffer);
                        default:
                            state = 9;
                    }
                case 13:                                                        // NUMERIC CONSTANT
                    if (isNumber(c)) {
                        lexemeBuffer += c + "";
                        c = read();
                        state = 14;
                        continue;
                    } else {
                        state = 16;
                        continue;
                    }

                case 14:                                                        // NUMERIC CONSTANT
                    if (c == '.') {
                        c = read();
                        state = 15;
                        continue;
                    } else if (isNumber(c)) {
                        lexemeBuffer += c + "";
                        c = read();
                        continue;
                    } else {
                        //c = read();
                        return new Token("NUMCONST", lexemeBuffer);
                    }

                case 15:
                    if (isNumber(c)) {
                        lexemeBuffer += ".";
                        while (true) {
                            if (isNumber(c)) {
                                lexemeBuffer += c + "";
                                c = read();
                            } else {
                                return new Token("NUMCONST", lexemeBuffer);
                            }
                        }
                    } else {
                        unread(c);
                        unread('.');
                        c = read();
                        return new Token("NUMCONST", lexemeBuffer);
                    }

                case 16:                                                        // LITERAL STRING
                    switch (c) {
                        case '\"':
                            lexemeBuffer += c + "";
                            c = read();
                            state = 17;
                            continue;
                        default:
                            state = 18;
                            continue;
                    }
                case 17:
                    switch (c) {
                        case '\"':
                            lexemeBuffer += c + "";
                            c = read();
                            return new Token("LITSTRING", lexemeBuffer);
                        default:
                            lexemeBuffer += c + "";
                            c = read();
                            continue;
                    }

                case 18:                                                        // KEYWORDS
                    if (isLetter(c)) {
                        lexemeBuffer += c + "";
                        c = read();
                        state = 19;
                        continue;
                    } else {
                        state = 20;
                        continue;
                    }
                case 19:
                    if (isNumber(c) || isLetter(c)) {
                        lexemeBuffer += c + "";
                        c = read();
                        continue;
                    } else {
                        unread(c);
                        c = read();
//                        c = read();
                        switch (lexemeBuffer) {
                            case "HELLO":
                                return new Token("HELLO", lexemeBuffer);
                            case "GOODBYE":
                                return new Token("GOODBYE", lexemeBuffer);
                            case "STARTHERE":
                                return new Token("STARTHERE", lexemeBuffer);
                            case "ENDHERE":
                                return new Token("ENDHERE", lexemeBuffer);
                            case "TASK":
                                return new Token("TASK", lexemeBuffer);
                            case "ENDTASK":
                                return new Token("ENDTASK", lexemeBuffer);
                            case "INVOLVES":
                                return new Token("INVOLVES", lexemeBuffer);
                            case "PERMANENT":
                                return new Token("PERMANENT", lexemeBuffer);
                            case "NUMBER":
                                return new Token("NUMBER", lexemeBuffer);
                            case "BOOLEAN":
                                return new Token("BOOLEAN", lexemeBuffer);
                            case "STRING":
                                return new Token("STRING", lexemeBuffer);
                            case "ACCEPT":
                                return new Token("ACCEPT", lexemeBuffer);
                            case "RETURN":
                                return new Token("RETURN", lexemeBuffer);
                            case "WHILE":
                                return new Token("WHILE", lexemeBuffer);
                            case "ENDWHILE":
                                return new Token("ENDWHILE", lexemeBuffer);
                            case "UNTIL":
                                return new Token("UNTIL", lexemeBuffer);
                            case "INCREMENT":
                                return new Token("INCREMENT", lexemeBuffer);
                            case "BY":
                                return new Token("BY", lexemeBuffer);
                            case "SHOW":
                                return new Token("SHOW", lexemeBuffer);
                            case "THE":
                                return new Token("THE", lexemeBuffer);
                            case "NOW":
                                return new Token("NOW", lexemeBuffer);
                            case "IS":
                                return new Token("ASSIGNMENT", lexemeBuffer);
                            case "TRUE":
                                return new Token("TRUE", lexemeBuffer);
                            case "FALSE":
                                return new Token("FALSE", lexemeBuffer);
                            case "FROM":
                                return new Token("FROM", lexemeBuffer);
                            case "ENDFROM":
                                return new Token("ENDFROM", lexemeBuffer);
                            case "WHEN":
                                return new Token("WHEN", lexemeBuffer);
                            case "ENDWHEN":
                                return new Token("ENDWHEN", lexemeBuffer);
                            case "OTHERWISE":
                                return new Token("OTHERWISE", lexemeBuffer);    
                            default:
                                return new Token("ID", lexemeBuffer);
                        }
                    }
                case 20:
//                    System.out.println((int)c + "=>" + c);
                    if (c == EOF) {
                        return new Token("EOF", "End of File");
                    } else {
                        String temp = c + "";
                        c = read();
                        return new Token("STXERROR", "Syntax Error! The character " + temp + " is unknown.");
                    }
                default:
                    String temp = c + "";
                    c = read();
                    return new Token("STXERROR", "Syntax Error! The character " + temp + " is unknown.");
            }
        }
    }

// HELPERS
    public static boolean isLetter(char c) {
        String symbol = "" + c + "";
        if ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".contains(symbol)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNumber(char c) {
        String symbol = "" + c + "";
        if ("0123456789".contains(symbol)) {
            return true;
        } else {
            return false;
        }
    }

}
