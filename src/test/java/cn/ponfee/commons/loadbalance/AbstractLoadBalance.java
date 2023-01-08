package cn.ponfee.commons.loadbalance;

/**
 * server load balance algorithm
 * 
 * @author Ponfee
 */
public abstract class AbstractLoadBalance {

    public abstract String select();
}
