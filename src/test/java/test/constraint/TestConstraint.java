package test.constraint;

import java.util.Date;

import code.ponfee.commons.constrain.Constraint;
import code.ponfee.commons.constrain.Constraint.Tense;
import code.ponfee.commons.constrain.FieldValidator;

public class TestConstraint {

    @Constraint(maxLen=0)
    private String s1;
    @Constraint(minLen=1)
    private String s2;
    
    @Constraint(tense=Tense.FUTURE)
    private Date date = new Date();
    
    @Constraint(datePattern="yyyy-MM-dd", tense=Tense.FUTURE)
    private String d = "2016-05-01";
    
    public static void main(String[] args) {
        TestConstraint t = new TestConstraint();
        t.s1 = "";
        t.s2 = "1";
        t.date = new Date(System.currentTimeMillis()+5000000);

        try {
            FieldValidator.newInstance().constrain(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
