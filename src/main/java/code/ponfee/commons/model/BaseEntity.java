package code.ponfee.commons.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Base class for Persistent Object(PO or DO)
 *
 * @author Ponfee
 */
@Getter
@Setter
public abstract class BaseEntity implements java.io.Serializable {
    private static final long serialVersionUID = -3387171222355207376L;

    private long id;         // database table primary key
    private int version = 1; // data version
    private Date createTime; // create time
    private Date updateTime; // last update time

    /**
     * Base entity with biz-no filed
     *
     * @param <N> biz-no field type
     */
    @Getter
    @Setter
    public static abstract class Number<N> extends BaseEntity {
        private static final long serialVersionUID = -6907471185117485946L;

        private N no; // biz-no
    }

    /**
     * Base entity with creator filed
     *
     * @param <U> creator field type
     */
    @Getter
    @Setter
    public static abstract class Creator<U> extends BaseEntity {
        private static final long serialVersionUID = -812853678840369113L;

        private U creator; // create user
    }

    /**
     * Base entity with creator and updater filed
     *
     * @param <U> updater field type(userid or username)
     */
    @Getter
    @Setter
    public static abstract class Updater<U> extends Creator<U> {
        private static final long serialVersionUID = 5333847915253038118L;

        private U updater; // last update user
    }

    /**
     * Base entity with biz-no, creator and updater filed
     *
     * @param <U> biz-no field type
     * @param <U> creator and updater field type(userid or username)
     */
    @Getter
    @Setter
    public static abstract class Normal<N, U> extends Updater<U> {
        private static final long serialVersionUID = -939331524501110803L;

        private N no; // biz-no
    }

}
