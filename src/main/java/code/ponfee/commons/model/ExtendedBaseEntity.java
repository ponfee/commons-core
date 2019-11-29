package code.ponfee.commons.model;

/**
 * Extended BaseEntity
 * 
 * @author Ponfee
 */
public abstract class ExtendedBaseEntity<I, U> extends BaseEntity<I, U> {

    private static final long serialVersionUID = 5333847915253038118L;

    private U modifier; // last modify user

    public U getModifier() {
        return modifier;
    }

    public void setModifier(U modifier) {
        this.modifier = modifier;
    }

}
