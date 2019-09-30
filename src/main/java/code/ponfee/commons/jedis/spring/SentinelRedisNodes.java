package code.ponfee.commons.jedis.spring;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.RedisNode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Use in spring redis xml configuration
 *
 * @author Ponfee
 */
public class SentinelRedisNodes extends HashSet<RedisNode> {

    private static final long serialVersionUID = 3116034150159346837L;

    public SentinelRedisNodes(String[] sentinelNodes) {
        super.addAll(
            Arrays.stream(
                sentinelNodes
            ).map(
                this::createNode
            ).collect(
                Collectors.toSet()
            )
        );
    }

    private RedisNode createNode(String node) {
        String[] array = StringUtils.split(node, "\\:", 2);
        return new RedisNode(array[0], Integer.parseInt(array[1]));
    }
}
