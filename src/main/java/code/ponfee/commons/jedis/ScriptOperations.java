package code.ponfee.commons.jedis;

import java.util.List;
import java.util.function.Function;

import redis.clients.jedis.Jedis;

/**
 * redis lua script
 * @author fupf
 */
public class ScriptOperations extends JedisOperations {

     public static final String JEDIS_SCRIPT_OPS = "jedis-script-ops";
     private static final byte[] JEDIS_SCRIPT_OPS_BYTES = JEDIS_SCRIPT_OPS.getBytes();

    ScriptOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * 将脚本 script 添加到脚本缓存中，但并不立即执行这个脚本。
     * 
     * @param script the script
     * @return 给定 script 的 SHA1 校验和
     */
    public String scriptLoad(String script) {
        return call(
            sj -> getShard(sj, JEDIS_SCRIPT_OPS_BYTES).scriptLoad(script), null, script
        );
    }

    /**
     * 执行script
     * 
     * @param script the script
     * @return call redis result
     */
    public Object eval(String script, List<String> keys, List<String> args) {
        return call(
            sj -> getShard(sj, JEDIS_SCRIPT_OPS_BYTES).eval(script, keys, args),
            null, script, keys, args
        );
    }

    /**
     * 根据给定的 sha1 校验码，对缓存在服务器中的脚本进行求值
     * 
     * @param sha1 the script sha1, {@linkplain #scriptLoad(String)}
     * @param keys the ops keys
     * @param args the arguments
     * @return
     */
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return call(
            sj -> getShard(sj, JEDIS_SCRIPT_OPS_BYTES).evalsha(sha1, keys, args),
            null, sha1, keys, args
        );
    }

    /**
     * 执行Jedis命令
     * 
     * @param executor the Consumer
     * @return result
     */
    public <R> void call(Function<Jedis, R> executor) {
        call(sj -> executor.apply(getShard(sj, JEDIS_SCRIPT_OPS_BYTES)), null);
    }

}
