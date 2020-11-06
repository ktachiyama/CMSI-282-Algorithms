package csp;

/**
 * BinaryDateConstraints are those in which two variables
 * are being compared by some operator, specified by an
 * int L_VAL and R_VAL for the corresponding variable / meeting
 * indexes, such as:
 * 0 == 1
 *   OR
 * 3 <= 5
 */
public class BinaryDateConstraint extends DateConstraint {

    public final int R_VAL;
    
    BinaryDateConstraint (int lVal, String operator, int rVal) {
        super(lVal, operator);
        if (rVal < 0 || lVal == rVal) {
            throw new IllegalArgumentException("Invalid variable index");
        }
        
        R_VAL = rVal;
    }
    
    @Override
    public String toString () {
        return super.toString() + " " + R_VAL;
    }
    
    /**
     * The arity of a constraint determines the number of variables
     * found within
     * @return 1 for UnaryDateConstraints, 2 for Binary
     */
    public int arity () {
        return 2;
    }
    
}
