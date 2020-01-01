package code.ponfee.commons.model;

import java.util.Date;

/**
 * Base class for Persistent Object(PO or DO)
 * 
 * @author Ponfee
 * @param <I> id field type     (table primary key)
 * @param <U> creator field type(user id or user name)
 */
public abstract class BaseEntity<I, U> implements java.io.Serializable {

    private static final long serialVersionUID = -3387171222355207376L;

    private I    id;          // database table primary key id
    private int  version = 1; // operate version
    private U    creator;     // create user
    private Date createTm;    // create time
    private Date modifyTm;    // last modify time

    public I getId() {
        return id;
    }

    public void setId(I id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public U getCreator() {
        return creator;
    }

    public void setCreator(U creator) {
        this.creator = creator;
    }

    public Date getCreateTm() {
        return createTm;
    }

    public void setCreateTm(Date createTm) {
        this.createTm = createTm;
    }

    public Date getModifyTm() {
        return modifyTm;
    }

    public void setModifyTm(Date modifyTm) {
        this.modifyTm = modifyTm;
    }

}
