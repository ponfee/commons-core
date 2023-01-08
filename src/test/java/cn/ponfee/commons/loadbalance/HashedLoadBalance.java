package cn.ponfee.commons.loadbalance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.ponfee.commons.math.Maths;

/**
 * 源地址哈希法
 * 
 * @author Ponfee
 */
public class HashedLoadBalance extends AbstractLoadBalance {
    private final List<String> servers;

    public HashedLoadBalance(Map<String, Integer> serverMap) {
        this.servers = new ArrayList<>(serverMap.keySet());
    }

    @Override
    public String select() {
        throw new UnsupportedOperationException();
    }

    public String select(String invokeIp) {
        return servers.get(Maths.abs(invokeIp.hashCode()) % servers.size());
    }

}
