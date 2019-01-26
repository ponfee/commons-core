package code.ponfee.commons.jedis;

import java.util.List;

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
     * 执行script
     * @param script
     * @return
     */
    public Object eval(String script, List<String> keys, List<String> args) {
        return call(
            sj -> getShard(sj, JEDIS_SCRIPT_OPS_BYTES).eval(script, keys, args),
            null, script, keys, args
        );
    }

    /**
     * 将脚本 script 添加到脚本缓存中，但并不立即执行这个脚本。
     * @param script
     * @return 给定 script 的 SHA1 校验和
     */
    public String scriptLoad(String script) {
        return call(
            sj -> getShard(sj, JEDIS_SCRIPT_OPS_BYTES).scriptLoad(script), null, script
        );
    }

    /**
     * 根据给定的 sha1 校验码，对缓存在服务器中的脚本进行求值
     * @param sha1
     * @param keys
     * @param args
     * @return
     */
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return call(
            sj -> getShard(sj, JEDIS_SCRIPT_OPS_BYTES).evalsha(sha1, keys, args),
            null, sha1, keys, args
        );
    }

}
