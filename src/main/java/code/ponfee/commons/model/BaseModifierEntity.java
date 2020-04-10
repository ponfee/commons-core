package code.ponfee.commons.model;

/**
 * Base entity with modifier filed
 * 
 * @author Ponfee
 */
public abstract class BaseModifierEntity<I, U> extends BaseEntity<I, U> {

    private static final long serialVersionUID = 5333847915253038118L;

    private U modifier; // last modify user

    public U getModifier() {
        return modifier;
    }

    public void setModifier(U modifier) {
        this.modifier = modifier;
    }

}
