package code.ponfee.commons.loadbalance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 轮询法
 * @author fupf
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {
    private final AtomicLong pos = new AtomicLong(0);
    private final List<String> servers;

    public RoundRobinLoadBalance(Map<String, Integer> serverMap) {
        this.servers = new ArrayList<>(serverMap.keySet());
    }

    @Override
    public String select() {
        return servers.get((int) (pos.getAndIncrement() % servers.size()));
    }

}
