package code.ponfee.commons.loadbalance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 最少活跃数
 *
 * @author fupf
 */
public class LeastActiveLoadBalance extends AbstractLoadBalance {

    private final Map<String, AtomicInteger> serverMap;
    private final List<Map.Entry<String, AtomicInteger>> servers;

    public LeastActiveLoadBalance(Map<String, AtomicInteger> serverMap) {
        this.serverMap = serverMap;
        this.servers = new ArrayList<>(serverMap.entrySet());
        this.servers.sort(Comparator.comparing(e -> e.getValue().get()));

        //this.servers.sort(Comparator.comparing(Entry::getValue));
        //this.servers.sort(Comparator.comparing(e -> e.getValue().get()));
        //this.servers.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        //Collections.sort(servers, Comparator.comparing(Entry<String, Integer>::getValue));
    }

    @Override
    public String select() {
        return servers.get(0).getKey();
    }

    /**
     * 调用前活跃数加1
     *
     * @param server
     */
    public void begin(String server) {
        serverMap.get(server).incrementAndGet();
    }

    /**
     * 调用后活跃数减1
     *
     * @param server
     */
    public void end(String server) {
        serverMap.get(server).decrementAndGet();
    }

}
