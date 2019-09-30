package code.ponfee.commons.loadbalance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机法
 * 
 * @author Ponfee
 */
public class WeightRandomLoadBalance extends AbstractLoadBalance {
    private final List<String> servers;

    public WeightRandomLoadBalance(Map<String, Integer> serverMap) {
        this.servers = new ArrayList<>();
        for (Entry<String, Integer> entry : serverMap.entrySet()) {
            for (int n = entry.getValue(), i = 0; i < n; i++) {
                this.servers.add(entry.getKey());
            }
        }
        Collections.shuffle(this.servers);
    }

    @Override
    public String select() {
        return servers.get(ThreadLocalRandom.current().nextInt(servers.size()));
    }

}
