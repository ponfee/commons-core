package code.ponfee.commons.model;

import java.util.Date;

/**
 * Base class for Persistent Object(PO or DO)
 * 
 * @author Ponfee
 * @param <T>
 */
public abstract class BaseEntity<T> implements java.io.Serializable {

    private static final long serialVersionUID = -3387171222355207376L;

    private Long id; // database table primary key id
    private Integer version; // operate version
    private T creator; // create user
    private Date createTm; // create time
    private T Modifier; // last modify user
    private Date modifyTm; // last modify time

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public T getCreator() {
        return creator;
    }

    public void setCreator(T creator) {
        this.creator = creator;
    }

    public Date getCreateTm() {
        return createTm;
    }

    public void setCreateTm(Date createTm) {
        this.createTm = createTm;
    }

    public T getModifier() {
        return Modifier;
    }

    public void setModifier(T modifier) {
        Modifier = modifier;
    }

    public Date getModifyTm() {
        return modifyTm;
    }

    public void setModifyTm(Date modifyTm) {
        this.modifyTm = modifyTm;
    }

}
