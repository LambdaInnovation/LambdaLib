package cn.liutils.ripple.impl.compiler;

/**
 * Definition of unary operators.
 * @author acaly, WeAthFold
 *
 */
public enum BinaryOperator {
    
    UNKNOWN("", 0),
    ADD("binAdd", 3),
    SUBSTRACT("binSubstract", 3),
    MULTIPLY("binMultiply", 4),
    DIVIDE("binDivide", 4),
    EQUAL("binEqual", 2),
    NOT_EQUAL("binNotEqual", 2),
    GREATER("binGreater", 2),
    LESSER("binLesser", 2),
    GREATER_EQUAL("binGreaterEqual", 2),
    LESSER_EQUAL("binLesserEqual", 2), 
    AND("binAnd", 1),
    OR("binOr", 1);

    public final String methodName;
    public final int priority;
    public static final int MAX_PRIORITY = 10;

    private BinaryOperator(String methodName, int priority) {
        this.methodName = methodName;
        this.priority = priority;
    }
}
