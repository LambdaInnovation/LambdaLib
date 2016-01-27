/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.ripple.impl.compiler;

/**
 * Definition of unary operators.
 * @author acaly, WeAthFold
 *
 */
public enum UnaryOperator {
    
    UNKNOWN(""),
    
    MINUS("unMinus"),
    NOT("unNot"),

    U_EQUAL(BinaryOperator.EQUAL),
    U_NOT_EQUAL(BinaryOperator.NOT_EQUAL),
    U_GREATER(BinaryOperator.GREATER),
    U_LESSER(BinaryOperator.LESSER),
    U_GREATER_EQUAL(BinaryOperator.GREATER_EQUAL),
    U_LESSER_EQUAL(BinaryOperator.LESSER_EQUAL);
    
    public final String methodName;
    public final BinaryOperator caseOp;
    
    private UnaryOperator(String methodName) {
        this.methodName = methodName;
        this.caseOp = null;
    }
    
    private UnaryOperator(BinaryOperator caseOp) {
        this.methodName = null;
        this.caseOp = caseOp;
    }
}
