/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.ripple.impl.compiler;

import java.util.Arrays;
import java.util.HashSet;

public final class Token {

    private static HashSet<String> keywords = new HashSet(Arrays.asList(
            "switch",
            "when",
            "default",
            "true",
            "false"
            ));
    
    public enum TokenType {
        EOS,
        IDENTIFIER,
        INTEGER,
        DOUBLE,
        SINGLE_SYMBOL,
        MULTI_SYMBOL,
        KEY_WORD,
    }
    
    public enum MultiCharSymbol {
        S_GE,
        S_LE,
        S_NE,
        
        //S_AND,
        //S_OR,
    }
    TokenType type;
    char sSymbol;
    MultiCharSymbol mSymbol;
    String str;
    int integerValue;
    double doubleValue;
    
    public boolean isSingleChar(char c) {
        return type == TokenType.SINGLE_SYMBOL && c == sSymbol;
    }
    
    public boolean isMultiChar(MultiCharSymbol m) {
        return type == TokenType.MULTI_SYMBOL && m == mSymbol;
    }
    
    public boolean isIdentifier() {
        return type == TokenType.IDENTIFIER;
    }
    
    public boolean isKeyword(String val) {
        return type == TokenType.KEY_WORD && str.equals(val);
    }
    
    public boolean isInteger() {
        return type == TokenType.INTEGER;
    }
    
    public boolean isDouble() {
        return type == TokenType.DOUBLE;
    }
    
    public boolean isEOS() {
        return type == TokenType.EOS;
    }
    
    public void setSingleChar(char c) {
        type = TokenType.SINGLE_SYMBOL;
        sSymbol = c;
    }
    
    public void setMultiChar(MultiCharSymbol m) {
        type = TokenType.MULTI_SYMBOL;
        mSymbol = m;
    }
    
    public void setInteger(int val) {
        type = TokenType.INTEGER;
        integerValue = val;
    }
    
    public void setDouble(double val) {
        type = TokenType.DOUBLE;
        doubleValue = val;
    }
    
    public void setString(String val) {
        if (keywords.contains(val)) {
            this.type = TokenType.KEY_WORD;
        } else {
            this.type = TokenType.IDENTIFIER;
        }
        this.str = val;
    }
    
    public void setEOS() {
        type = TokenType.EOS;
    }
    
    public BinaryOperator toBinOp() {
        if (this.type == TokenType.SINGLE_SYMBOL) {
            switch (this.sSymbol) {
            case '+': return BinaryOperator.ADD;
            case '-': return BinaryOperator.SUBSTRACT;
            case '*': return BinaryOperator.MULTIPLY;
            case '/': return BinaryOperator.DIVIDE;
            case '=': return BinaryOperator.EQUAL;
            case '>': return BinaryOperator.GREATER;
            case '<': return BinaryOperator.LESSER;
            case '&': return BinaryOperator.AND;
            case '|': return BinaryOperator.OR;
            }
        } else if (this.type == TokenType.MULTI_SYMBOL) {
            switch (this.mSymbol) {
            //case S_AND: return BinaryOperator.AND;
            case S_GE: return BinaryOperator.GREATER_EQUAL;
            case S_LE: return BinaryOperator.LESSER_EQUAL;
            case S_NE: return BinaryOperator.NOT_EQUAL;
            //case S_OR: return BinaryOperator.OR;
            }
        }
        return BinaryOperator.UNKNOWN;
    }
    
    public UnaryOperator toUnOp() {
        if (this.type == TokenType.SINGLE_SYMBOL) {
            switch (this.sSymbol) {
            case '-': return UnaryOperator.MINUS;
            case '!': return UnaryOperator.NOT;
            case '=': return UnaryOperator.U_EQUAL;
            case '>': return UnaryOperator.U_GREATER;
            case '<': return UnaryOperator.U_LESSER;
            }
        } else if (this.type == TokenType.MULTI_SYMBOL){
            switch (this.mSymbol) {
            case S_GE: return UnaryOperator.U_GREATER_EQUAL;
            case S_LE: return UnaryOperator.U_LESSER_EQUAL;
            case S_NE: return UnaryOperator.U_NOT_EQUAL;
            default:
            }
        }
        return UnaryOperator.UNKNOWN;
    }
    
    @Override
    public String toString() {
        return "[" + type + " " + sSymbol + " " + str + "]";
    }
}
